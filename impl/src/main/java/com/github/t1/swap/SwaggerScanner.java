package com.github.t1.swap;

import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static javax.tools.Diagnostic.Kind.*;

import java.util.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementScanner7;
import javax.ws.rs.*;
import javax.ws.rs.Path;

import org.slf4j.*;

import io.swagger.annotations.*;
import io.swagger.annotations.Contact;
import io.swagger.annotations.License;
import io.swagger.models.*;
import io.swagger.models.Info;
import io.swagger.models.parameters.*;

public class SwaggerScanner {
    private static final Logger log = LoggerFactory.getLogger(SwaggerScanner.class);

    private final Messager messager;
    private final Swagger swagger = new Swagger();

    public SwaggerScanner(Messager messager) {
        this.messager = messager;
    }

    public void error(CharSequence message, Element element) {
        messager.printMessage(ERROR, message, element);
    }

    public void warning(CharSequence message, Element element) {
        messager.printMessage(WARNING, message, element);
    }

    public void note(CharSequence message, Element element) {
        messager.printMessage(NOTE, message, element);
    }

    public void addSwaggerDefinitions(Set<? extends Element> elements) {
        TypeElement swaggerDefinition = firstSwaggerDefinition(elements);
        if (swaggerDefinition != null)
            addSwaggerDefinition(swaggerDefinition);
    }

    private TypeElement firstSwaggerDefinition(Set<? extends Element> elements) {
        Element result = null;
        for (Element element : elements)
            if (element.getModifiers().contains(PUBLIC))
                if (result == null)
                    result = element;
                else
                    error("conflicting @SwaggerDefinition found besides: " + result, element);
            else
                note("skipping non-public element", element);
        return (TypeElement) result;
    }

    private SwaggerScanner addSwaggerDefinition(TypeElement swaggerDefinitionElement) {
        SwaggerDefinition swaggerDefinition = swaggerDefinitionElement.getAnnotation(SwaggerDefinition.class);

        buildInfo(swaggerDefinition.info());

        swagger.setHost(nonEmpty(swaggerDefinition.host()));
        swagger.setBasePath(nonEmpty(swaggerDefinition.basePath()));

        note("processed", swaggerDefinitionElement);
        return this;
    }

    private void buildInfo(io.swagger.annotations.Info in) {
        Info outInfo = new Info();
        outInfo.title(in.title());
        outInfo.version(in.version());
        outInfo.description(nonEmpty(in.description()));
        outInfo.termsOfService(nonEmpty(in.termsOfService()));

        swagger.setInfo(outInfo);

        if (in.contact() != null)
            buildContact(in.contact());
        if (in.license() != null)
            buildLicense(in.license());
        if (hasExtensions(in))
            buildVendorExtensions(in.extensions());
    }

    private void buildContact(Contact in) {
        io.swagger.models.Contact outContact = new io.swagger.models.Contact();
        outContact.setName(nonEmpty(in.name()));
        outContact.setEmail(nonEmpty(in.email()));
        outContact.setUrl(nonEmpty(in.url()));
        swagger.getInfo().setContact(outContact);
    }

    private void buildLicense(License in) {
        io.swagger.models.License outLicense = new io.swagger.models.License();
        outLicense.setName(nonEmpty(in.name()));
        outLicense.setUrl(nonEmpty(in.url()));
        swagger.getInfo().setLicense(outLicense);
    }

    private boolean hasExtensions(io.swagger.annotations.Info in) {
        return in.extensions().length != 0 && (in.extensions().length != 1 || !in.extensions()[0].name().isEmpty());
    }

    private void buildVendorExtensions(Extension[] inExtensions) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Map<String, Map<String, String>> vendorExtensions = (Map) swagger.getInfo().getVendorExtensions();
        for (Extension inExtension : inExtensions) {
            if (inExtension.name().isEmpty())
                continue;
            String name = name(inExtension.name());
            if (!vendorExtensions.containsKey(name))
                vendorExtensions.put(name, new HashMap<String, String>());
            Map<String, String> extensionProperties = vendorExtensions.get(name);

            for (ExtensionProperty property : inExtension.properties()) {
                String propertyName = name(property.name());
                extensionProperties.put(propertyName, property.value());
            }
        }
    }

    private String name(String propertyName) {
        if (!propertyName.startsWith("x-"))
            propertyName = "x-" + propertyName;
        return propertyName;
    }

    private String nonEmpty(String string) {
        return (string.isEmpty()) ? null : string;
    }

    public SwaggerScanner addPathElements(Set<? extends Element> pathElements) {
        log.debug("addPathElements {}", pathElements);
        for (Element pathElement : pathElements)
            if (pathElement instanceof TypeElement)
                addPathType((TypeElement) pathElement);
        return this;
    }

    private SwaggerScanner addPathType(TypeElement typeElement) {
        // Api api = pathElement.getAnnotation(Api.class);
        final String typePath = typeElement.getAnnotation(javax.ws.rs.Path.class).value();
        log.debug("scan path {} in {}", typePath, typeElement);

        typeElement.accept(new ElementScanner7<Void, Void>() {
            @Override
            public Void visitExecutable(ExecutableElement method, Void p) {
                if (method.getKind() == METHOD) {
                    log.debug("scan method {} of {}", method, method.getEnclosingElement());
                    String methodPath = methodPath(method);
                    io.swagger.models.Path pathModel = pathModel(methodPath);
                    Operation get = scanGET(method);
                    pathModel.get(get);
                }
                return super.visitExecutable(method, p);
            }

            private String methodPath(ExecutableElement method) {
                Path methodPath = method.getAnnotation(javax.ws.rs.Path.class);
                return typePath + ((methodPath == null) ? "" : methodPath.value());
            }

            private io.swagger.models.Path pathModel(String methodPath) {
                io.swagger.models.Path pathModel = swagger.getPath(methodPath);
                if (pathModel == null) {
                    pathModel = new io.swagger.models.Path();
                    swagger.path(methodPath, pathModel);
                }
                return pathModel;
            }

            private Operation scanGET(ExecutableElement method) {
                Operation get = new Operation() //
                        .operationId(method.getSimpleName().toString()) //
                        .deprecated(method.getAnnotation(Deprecated.class) != null);
                scanApiOperation(method, get);
                scanParams(method, get);
                return get;
            }

            private void scanApiOperation(ExecutableElement method, Operation get) {
                ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                if (apiOperation != null) {
                    get.setSummary(apiOperation.value());
                    get.setDescription(nonEmpty(apiOperation.notes()));
                    for (String tag : apiOperation.tags())
                        if (!tag.isEmpty())
                            get.addTag(tag);
                }
            }

            private void scanParams(ExecutableElement method, Operation get) {
                for (VariableElement param : method.getParameters()) {
                    HeaderParam headerParam = param.getAnnotation(HeaderParam.class);
                    MatrixParam matrixParam = param.getAnnotation(MatrixParam.class);
                    QueryParam queryParam = param.getAnnotation(QueryParam.class);
                    PathParam pathParam = param.getAnnotation(PathParam.class);
                    if (headerParam != null) {
                        get.addParameter(new HeaderParameter().name(headerParam.value()));
                    } else if (matrixParam != null) {
                        warning("matrix params are not supported by Swagger; treating like a body param", param);
                        get.addParameter(new BodyParameter());
                    } else if (queryParam != null) {
                        get.addParameter(new QueryParameter().name(queryParam.value()));
                    } else if (pathParam != null) {
                        get.addParameter(new PathParameter().name(pathParam.value()));
                    } else {
                        get.addParameter(new BodyParameter());
                    }
                }
            }
        }, null);

        note("processed", typeElement);
        return this;
    }

    public Swagger getResult() {
        return swagger;
    }

    public boolean isWorthWriting() {
        return hasTitle() || hasPaths();
    }

    private boolean hasTitle() {
        return swagger.getInfo() != null && swagger.getInfo().getTitle() != null;
    }

    private boolean hasPaths() {
        return swagger.getPaths() != null && !swagger.getPaths().isEmpty();
    }
}

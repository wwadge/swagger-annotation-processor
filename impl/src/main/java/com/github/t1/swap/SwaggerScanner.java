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
import javax.ws.rs.core.Context;

import org.slf4j.*;

import io.swagger.annotations.*;
import io.swagger.annotations.Contact;
import io.swagger.annotations.License;
import io.swagger.annotations.Tag;
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

    private String nonEmpty(String string) {
        return (string.isEmpty()) ? null : string;
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

    // visible for testing
    public SwaggerScanner addSwaggerDefinition(TypeElement swaggerDefinitionElement) {
        SwaggerDefinition swaggerDefinition = swaggerDefinitionElement.getAnnotation(SwaggerDefinition.class);

        swagger.setHost(nonEmpty(swaggerDefinition.host()));
        swagger.setBasePath(nonEmpty(swaggerDefinition.basePath()));

        scan(swaggerDefinition.info());
        scan(swaggerDefinition.tags());
        scanConsumes(swaggerDefinition.consumes());
        scanProduces(swaggerDefinition.produces());

        note("processed", swaggerDefinitionElement);
        return this;
    }

    private void scan(io.swagger.annotations.Info in) {
        Info outInfo = new Info();
        outInfo.title(in.title());
        outInfo.version(in.version());
        outInfo.description(nonEmpty(in.description()));
        outInfo.termsOfService(nonEmpty(in.termsOfService()));

        swagger.setInfo(outInfo);

        scanContact(in.contact());
        scanLicense(in.license());
        scanVendorExtensions(in.extensions());
    }

    private void scanContact(Contact in) {
        if (in.name().isEmpty())
            return;
        io.swagger.models.Contact outContact = new io.swagger.models.Contact();
        outContact.setName(nonEmpty(in.name()));
        outContact.setEmail(nonEmpty(in.email()));
        outContact.setUrl(nonEmpty(in.url()));
        swagger.getInfo().setContact(outContact);
    }

    private void scanLicense(License in) {
        if (in.name().isEmpty())
            return;
        io.swagger.models.License outLicense = new io.swagger.models.License();
        outLicense.setName(nonEmpty(in.name()));
        outLicense.setUrl(nonEmpty(in.url()));
        swagger.getInfo().setLicense(outLicense);
    }

    private void scanVendorExtensions(Extension[] inExtensions) {
        if (inExtensions.length == 0 || inExtensions.length == 1 && inExtensions[0].name().isEmpty())
            return;
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

    private void scan(Tag[] tags) {
        for (Tag tag : tags)
            if (!tag.name().isEmpty())
                swagger.tag(new io.swagger.models.Tag() //
                        .name(tag.name()) //
                        .description(tag.description()) //
                );
    }

    private void scanConsumes(String[] mediaTypes) {
        for (String mediaType : mediaTypes)
            if (!mediaType.isEmpty())
                swagger.consumes(mediaType);
    }

    private void scanProduces(String[] mediaTypes) {
        for (String mediaType : mediaTypes)
            if (!mediaType.isEmpty())
                swagger.produces(mediaType);
    }

    public SwaggerScanner addPathElements(Set<? extends Element> pathElements) {
        log.debug("addPathElements {}", pathElements);
        for (Element pathElement : pathElements)
            if (pathElement instanceof TypeElement)
                addPathTypeElement((TypeElement) pathElement);
        return this;
    }

    private SwaggerScanner addPathTypeElement(TypeElement typeElement) {
        Api api = typeElement.getAnnotation(Api.class);
        final List<String> defaultTags = tags(api);
        final String typePath = prefixedPath(typeElement.getAnnotation(javax.ws.rs.Path.class).value());
        log.debug("scan path {} in {}", typePath, typeElement);

        typeElement.accept(new ElementScanner7<Void, Void>() {
            @Override
            public Void visitExecutable(ExecutableElement method, Void p) {
                if (method.getKind() == METHOD) {
                    log.debug("scan method {} of {}", method, method.getEnclosingElement());
                    String methodPath = methodPath(method);
                    io.swagger.models.Path pathModel = pathModel(methodPath);
                    Operation operation = scan(method);
                    // TODO use @GET, @POST, etc. to find get(), post(), etc.
                    pathModel.get(operation);
                }
                return super.visitExecutable(method, p);
            }

            private String methodPath(ExecutableElement method) {
                Path methodPath = method.getAnnotation(javax.ws.rs.Path.class);
                if (methodPath == null)
                    return typePath;
                return typePath + prefixedPath(methodPath.value());
            }

            private io.swagger.models.Path pathModel(String methodPath) {
                io.swagger.models.Path pathModel = swagger.getPath(methodPath);
                if (pathModel == null) {
                    pathModel = new io.swagger.models.Path();
                    swagger.path(methodPath, pathModel);
                }
                return pathModel;
            }

            private Operation scan(ExecutableElement method) {
                Operation operation = new Operation() //
                        .operationId(method.getSimpleName().toString()) //
                        .deprecated(method.getAnnotation(Deprecated.class) != null);
                scanApiOperation(method, operation);
                scanParams(method, operation);
                return operation;
            }

            private void scanApiOperation(ExecutableElement method, Operation operation) {
                ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                if (apiOperation != null) {
                    operation.setSummary(apiOperation.value());
                    operation.setDescription(nonEmpty(apiOperation.notes()));
                    for (String tag : apiOperation.tags())
                        if (!tag.isEmpty())
                            operation.addTag(tag);
                }
                if (operation.getTags() == null)
                    operation.setTags(defaultTags);
            }

            private void scanParams(ExecutableElement method, Operation operation) {
                for (VariableElement param : method.getParameters()) {
                    if (param.getAnnotation(Context.class) != null)
                        continue;
                    HeaderParam headerParam = param.getAnnotation(HeaderParam.class);
                    MatrixParam matrixParam = param.getAnnotation(MatrixParam.class);
                    QueryParam queryParam = param.getAnnotation(QueryParam.class);
                    PathParam pathParam = param.getAnnotation(PathParam.class);

                    Parameter paramModel;

                    if (headerParam != null) {
                        paramModel = new HeaderParameter().name(headerParam.value());
                    } else if (matrixParam != null) {
                        warning("matrix params are not supported by Swagger; treating like a body param", param);
                        paramModel = new BodyParameter();
                    } else if (queryParam != null) {
                        paramModel = new QueryParameter().name(queryParam.value());
                    } else if (pathParam != null) {
                        paramModel = new PathParameter().name(pathParam.value());
                    } else {
                        paramModel = new BodyParameter().name("body");
                    }
                    operation.addParameter(paramModel);
                    scan(param.getAnnotation(ApiParam.class), paramModel);
                }
            }

            private void scan(ApiParam apiParam, Parameter model) {
                if (apiParam == null)
                    return;
                if (!apiParam.name().isEmpty())
                    model.setName(apiParam.name());
                if (!apiParam.value().isEmpty())
                    model.setDescription(apiParam.value());
            }
        }, null);

        note("processed", typeElement);
        return this;
    }

    private List<String> tags(Api api) {
        if (api == null)
            return null;
        List<String> list = new ArrayList<>();
        for (String tag : api.tags())
            if (!tag.isEmpty())
                list.add(tag);
        if (list.isEmpty() && !api.value().isEmpty())
            list.add(api.value());
        return (list.isEmpty()) ? null : list;
    }

    private String prefixedPath(String value) {
        if (!value.startsWith("/"))
            value = "/" + value;
        return value;
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

package com.github.t1.swap;

import com.github.t1.exap.reflection.*;
import io.swagger.annotations.*;
import io.swagger.annotations.Contact;
import io.swagger.annotations.License;
import io.swagger.annotations.Tag;
import io.swagger.models.Info;
import io.swagger.models.*;
import org.slf4j.*;

import java.util.*;

import static com.github.t1.swap.Helpers.*;

public class SwaggerScanner {
    private static final Logger log = LoggerFactory.getLogger(SwaggerScanner.class);

    private final Swagger swagger = new Swagger();

    public void addSwaggerDefinitions(List<Type> elements) {
        Type swaggerDefinition = firstSwaggerDefinition(elements);
        if (swaggerDefinition != null)
            addSwaggerDefinition(swaggerDefinition);
    }

    private Type firstSwaggerDefinition(List<Type> types) {
        Type result = null;
        for (Type type : types)
            if (type.isPublic())
                if (result == null)
                    result = type;
                else
                    type.error("conflicting @SwaggerDefinition found besides: " + result);
            else
                type.note("skipping non-public element");
        return result;
    }

    // visible for testing
    public SwaggerScanner addSwaggerDefinition(Type swaggerDefinitionElement) {
        SwaggerDefinition swaggerDefinition = swaggerDefinitionElement.getAnnotation(SwaggerDefinition.class);

        swagger.setHost(nonEmpty(swaggerDefinition.host()));
        swagger.setBasePath(nonEmpty(swaggerDefinition.basePath()));

        scan(swaggerDefinition.info());
        scan(swaggerDefinition.tags());
        scanConsumes(swaggerDefinition.consumes());
        scanProduces(swaggerDefinition.produces());

        swaggerDefinitionElement.note("processed");
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
                swagger.tag(new io.swagger.models.Tag()
                        .name(tag.name())
                        .description(tag.description())
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

    public SwaggerScanner addJaxRsTypes(List<Type> types) {
        log.debug("addPathElements {}", types);
        for (Type type : types)
            addJaxRsType(type);
        return this;
    }

    public SwaggerScanner addJaxRsType(Type type) {
        log.error("found javadoc in {}: {}", type.getSimpleName(), type.docComment());
        Api api = type.getAnnotation(Api.class);
        final List<String> defaultTags = tags(api);
        final String typePath = prefixedPath(type.getAnnotation(javax.ws.rs.Path.class).value());
        log.debug("scan path {} in {}", typePath, type);

        type.accept(new TypeVisitor() {
            @Override
            public void visit(Method method) {
                new MethodScanner(method, swagger, typePath, defaultTags).scan();
            }
        });

        type.note("processed");
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
            list.add(deprefixed(api.value()));
        return (list.isEmpty()) ? null : list;
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

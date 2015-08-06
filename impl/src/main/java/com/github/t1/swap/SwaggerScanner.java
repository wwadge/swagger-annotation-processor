package com.github.t1.swap;

import static javax.lang.model.element.Modifier.*;
import static javax.tools.Diagnostic.Kind.*;

import java.util.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.*;

import io.swagger.annotations.*;
import io.swagger.annotations.Contact;
import io.swagger.annotations.License;
import io.swagger.models.Info;
import io.swagger.models.Swagger;

public class SwaggerScanner {
    private final Messager messager;
    private final Swagger swagger = new Swagger();

    public SwaggerScanner(Messager messager) {
        this.messager = messager;
    }

    public void error(CharSequence message, Element element) {
        messager.printMessage(ERROR, message, element);
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
        note("processed", swaggerDefinitionElement);

        buildInfo(swaggerDefinition.info());

        swagger.setHost(nonEmpty(swaggerDefinition.host()));
        swagger.setBasePath(nonEmpty(swaggerDefinition.basePath()));

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

    public SwaggerScanner apis(Set<? extends Element> apis) {
        for (Element api : apis)
            api(api);
        return this;
    }

    private SwaggerScanner api(Element apiElement) {
        Api api = apiElement.getAnnotation(Api.class);
        note("processed", apiElement);

        // swagger.path("/", new Path());

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

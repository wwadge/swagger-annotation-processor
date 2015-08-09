package com.github.t1.swap;

import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Map;

import javax.lang.model.element.TypeElement;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.swagger.annotations.*;
import io.swagger.models.Swagger;

@RunWith(MockitoJUnitRunner.class)
public class SwaggerDefinitionScannerTest extends AbstractSwaggerScannerTest {
    protected void scanSwaggerDefinition(Class<?> container) {
        TypeElement swaggerDefinitionElement = mock(TypeElement.class);
        when(swaggerDefinitionElement.getKind()).thenReturn(CLASS);
        when(swaggerDefinitionElement.getModifiers()).thenReturn(asSet(PUBLIC));
        when(swaggerDefinitionElement.getAnnotation(SwaggerDefinition.class))
                .thenReturn(container.getAnnotation(SwaggerDefinition.class));
        swaggerScanner.addSwaggerDefinitions(asSet(swaggerDefinitionElement));
    }

    @Test
    public void shouldParseSwaggerDescriptionInfoTitleAndVersion() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v") )
        class Dummy {}
        scanSwaggerDefinition(Dummy.class);

        io.swagger.models.Info info = swaggerScanner.getResult().getInfo();

        assertNull("description", info.getDescription());
        assertEquals("ti", info.getTitle());
        assertEquals("v", info.getVersion());
    }

    @Test
    public void shouldParseSwaggerDescriptionInfoDescriptionAndTermsOfService() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v", description = "des", termsOfService = "terms") )
        class Dummy {}
        scanSwaggerDefinition(Dummy.class);

        io.swagger.models.Info info = swaggerScanner.getResult().getInfo();

        assertEquals("des", info.getDescription());
        assertEquals("terms", info.getTermsOfService());
    }

    @Test
    public void shouldParseSwaggerDescriptionContact() {
        @SwaggerDefinition(
                info = @Info(title = "ti", version = "v", contact = @Contact(name = "n", email = "e", url = "u") ) )
        class Dummy {}
        scanSwaggerDefinition(Dummy.class);

        io.swagger.models.Contact contact = swaggerScanner.getResult().getInfo().getContact();

        assertEquals("n", contact.getName());
        assertEquals("e", contact.getEmail());
        assertEquals("u", contact.getUrl());
    }

    @Test
    public void shouldParseSwaggerDescriptionLicense() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v", license = @License(name = "n", url = "u") ) )
        class Dummy {}
        scanSwaggerDefinition(Dummy.class);

        io.swagger.models.License license = swaggerScanner.getResult().getInfo().getLicense();

        assertEquals("n", license.getName());
        assertEquals("u", license.getUrl());
    }

    @Test
    public void shouldParseSwaggerDescriptionExtensions() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v",
                extensions = { //
                        @Extension(name = "n0",
                                properties = { //
                                        @ExtensionProperty(name = "n01", value = "v01"), //
                                        @ExtensionProperty(name = "x-n02", value = "v02") }), //
                        @Extension(name = "x-n1",
                                properties = { //
                                        @ExtensionProperty(name = "x-n11", value = "v11"), //
                                        @ExtensionProperty(name = "n12", value = "x-v12") }) //
        }) )
        class Dummy {}

        scanSwaggerDefinition(Dummy.class);

        Map<String, Object> extensions = swaggerScanner.getResult().getInfo().getVendorExtensions();
        System.out.println(extensions);

        assertEquals(2, extensions.size());
        @SuppressWarnings("unchecked")
        Map<String, Object> n0 = (Map<String, Object>) extensions.get("x-n0");
        assertEquals(2, n0.size());
        assertEquals("v01", n0.get("x-n01"));
        assertEquals("v02", n0.get("x-n02"));
        @SuppressWarnings("unchecked")
        Map<String, Object> n1 = (Map<String, Object>) extensions.get("x-n1");
        assertEquals(2, n1.size());
        assertEquals("v11", n1.get("x-n11"));
        assertEquals("x-v12", n1.get("x-n12"));
    }

    @Test
    public void shouldParseSwaggerDescription() {
        @SwaggerDefinition(host = "h", basePath = "b")
        class Dummy {}
        scanSwaggerDefinition(Dummy.class);

        Swagger swagger = swaggerScanner.getResult();

        assertEquals("h", swagger.getHost());
        assertEquals("b", swagger.getBasePath());
    }

    // TODO List<Tag> tags;
    // TODO List<Scheme> schemes;
    // TODO List<String> consumes;
    // TODO List<String> produces;
    // TODO List<SecurityRequirement> securityRequirements;
    // TODO Map<String, SecuritySchemeDefinition> securityDefinitions;
    // TODO Map<String, Model> definitions;
    // TODO Map<String, Parameter> parameters;
    // TODO ExternalDocs externalDocs;
}

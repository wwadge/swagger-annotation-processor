package com.github.t1.swap;

import static javax.ws.rs.core.MediaType.*;

import java.util.*;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.t1.exap.reflection.ReflectionType;

import io.swagger.annotations.*;
import io.swagger.models.Swagger;

@RunWith(MockitoJUnitRunner.class)
public class SwaggerDefinitionScannerTest extends AbstractSwaggerScannerTest {
    protected Swagger scanSwaggerDefinition(Class<?> container) {
        swaggerScanner.addSwaggerDefinition(new ReflectionType(messager, container));
        return swaggerScanner.getResult();
    }

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Test
    public void shouldScanHostAndBasepath() {
        @SwaggerDefinition(host = "h", basePath = "b")
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(swagger.getHost()).isEqualTo("h");
        softly.assertThat(swagger.getBasePath()).isEqualTo("b");
    }

    @Test
    public void shouldScanInfoTitleAndVersion() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v") )
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        io.swagger.models.Info info = swagger.getInfo();
        softly.assertThat(info.getTitle()).isEqualTo("ti");
        softly.assertThat(info.getVersion()).isEqualTo("v");
    }

    @Test
    public void shouldScanInfoDescriptionAndTermsOfService() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v", description = "des", termsOfService = "terms") )
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        io.swagger.models.Info info = swagger.getInfo();
        softly.assertThat(info.getDescription()).isEqualTo("des");
        softly.assertThat(info.getTermsOfService()).isEqualTo("terms");
    }

    @Test
    public void shouldScanNullContactAndLicense() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v") )
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(swagger.getInfo().getContact()).isNull();
        softly.assertThat(swagger.getInfo().getLicense()).isNull();
    }

    @Test
    public void shouldScanContact() {
        @SwaggerDefinition(
                info = @Info(title = "ti", version = "v", contact = @Contact(name = "n", email = "e", url = "u") ) )
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        io.swagger.models.Contact contact = swagger.getInfo().getContact();
        softly.assertThat(contact.getName()).isEqualTo("n");
        softly.assertThat(contact.getEmail()).isEqualTo("e");
        softly.assertThat(contact.getUrl()).isEqualTo("u");
    }

    @Test
    public void shouldScanLicense() {
        @SwaggerDefinition(info = @Info(title = "ti", version = "v", license = @License(name = "n", url = "u") ) )
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        io.swagger.models.License license = swagger.getInfo().getLicense();
        softly.assertThat(license.getName()).isEqualTo("n");
        softly.assertThat(license.getUrl()).isEqualTo("u");
    }

    @Test
    public void shouldScanExtensions() {
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

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        Map<String, Object> extensions = swagger.getInfo().getVendorExtensions();
        System.out.println(extensions);

        softly.assertThat(extensions.size()).isEqualTo(2);
        @SuppressWarnings("unchecked")
        Map<String, Object> n0 = (Map<String, Object>) extensions.get("x-n0");
        softly.assertThat(n0.size()).isEqualTo(2);
        softly.assertThat(n0.get("x-n01")).isEqualTo("v01");
        softly.assertThat(n0.get("x-n02")).isEqualTo("v02");
        @SuppressWarnings("unchecked")
        Map<String, Object> n1 = (Map<String, Object>) extensions.get("x-n1");
        softly.assertThat(n1.size()).isEqualTo(2);
        softly.assertThat(n1.get("x-n11")).isEqualTo("v11");
        softly.assertThat(n1.get("x-n12")).isEqualTo("x-v12");
    }

    @Test
    public void shouldScanTags() {
        @SwaggerDefinition(tags = { //
                @Tag(name = "a", description = "aaa"), //
                @Tag(name = "b", description = "bbb"), //
                @Tag(name = "c"), //
        } //
        )
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        List<io.swagger.models.Tag> tags = swagger.getTags();
        softly.assertThat(tags).extracting("name").containsOnly("a", "b", "c");
        softly.assertThat(tags).extracting("description").containsOnly("aaa", "bbb", "");
    }

    @Test
    public void shouldNotScanEmptyConsumes() {
        @SwaggerDefinition
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(swagger.getConsumes()).isNull();
    }

    @Test
    public void shouldScanConsumes() {
        @SwaggerDefinition(consumes = { APPLICATION_JSON, APPLICATION_XML })
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(swagger.getConsumes()).containsOnly(APPLICATION_JSON, APPLICATION_XML);
    }

    @Test
    public void shouldNotScanEmptyProduces() {
        @SwaggerDefinition
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(swagger.getProduces()).isNull();
    }

    @Test
    public void shouldScanProduces() {
        @SwaggerDefinition(produces = { APPLICATION_JSON, APPLICATION_XML })
        class Dummy {}

        Swagger swagger = scanSwaggerDefinition(Dummy.class);

        softly.assertThat(swagger.getProduces()).containsOnly(APPLICATION_JSON, APPLICATION_XML);
    }

    // TODO List<Scheme> schemes;
    // TODO List<SecurityRequirement> securityRequirements;
    // TODO Map<String, SecuritySchemeDefinition> securityDefinitions;
    // TODO Map<String, Model> definitions;
    // TODO Map<String, Parameter> parameters;
    // TODO ExternalDocs externalDocs;
}

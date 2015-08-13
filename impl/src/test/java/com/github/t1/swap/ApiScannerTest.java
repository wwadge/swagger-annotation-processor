package com.github.t1.swap;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

import java.lang.annotation.*;
import java.time.LocalDate;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.t1.exap.reflection.*;

import io.swagger.annotations.*;
import io.swagger.models.*;
import io.swagger.models.Response;
import io.swagger.models.parameters.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiScannerTest extends AbstractSwaggerScannerTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    private Swagger scanJaxRsType(Class<?> container) {
        Type type = new ReflectionType(messager, container);
        swaggerScanner.addJaxRsTypes(asList(type));
        return swaggerScanner.getResult();
    }

    private Operation getGetOperation(io.swagger.models.Path path) {
        assertThat(path).as("path not found").isNotNull();
        Operation get = path.getGet();
        assertThat(get).as("GET not found").isNotNull();
        return get;
    }

    @Test
    public void shouldConcatenatePathsWithAllSlashes() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/bar")
            public void getMethod() {}
        }

        assertThat(scanJaxRsType(Dummy.class).getPaths().keySet()).containsExactly("/foo/bar");
    }

    @Test
    public void shouldConcatenatePathsNonSlashAndSlash() {
        @Path("foo")
        class Dummy {
            @GET
            @Path("/bar")
            public void getMethod() {}
        }

        assertThat(scanJaxRsType(Dummy.class).getPaths().keySet()).containsExactly("/foo/bar");
    }

    @Test
    public void shouldConcatenatePathsSlashAndNonSlash() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("bar")
            public void getMethod() {}
        }

        assertThat(scanJaxRsType(Dummy.class).getPaths().keySet()).containsExactly("/foo/bar");
    }

    @Test
    public void shouldConcatenatePathsNonSlashAndNonSlash() {
        @Path("foo")
        class Dummy {
            @GET
            @Path("bar")
            public void getMethod() {}
        }

        assertThat(scanJaxRsType(Dummy.class).getPaths().keySet()).containsExactly("/foo/bar");
    }

    @Test
    public void shouldScanFullGET() {
        @Path("/dummy")
        class Dummy {
            @GET
            @Path("/{path-param}")
            @ApiOperation( //
                    value = "get-op", //
                    notes = "get-notes", //
                    tags = { "t0", "t1" } //
            )
            @Deprecated
            @SuppressWarnings("unused")
            public String getMethod( //
                    @Context UriBuilder uriBuilder, //
                    @ApiParam(name = "b-param", value = "b-desc", defaultValue = "b-def",
                            allowableValues = "b-allowable", required = true, access = "b-access", allowMultiple = true,
                            hidden = false) String bodyParam, //
                    @ApiParam(name = "p-param", value = "p-desc", defaultValue = "p-def",
                            allowableValues = "p-allowable", required = true, access = "p-access", allowMultiple = true,
                            hidden = false) @PathParam("path-param") String pathParam, //
                    @ApiParam(name = "h-param", value = "h-desc", defaultValue = "h-def",
                            allowableValues = "h-allowable", required = true, access = "h-access", allowMultiple = true,
                            hidden = false) @HeaderParam("header-param") String headerParam, //
                    @ApiParam(name = "q-param", value = "q-desc", defaultValue = "q-def",
                            allowableValues = "q-allowable", required = true, access = "q-access", allowMultiple = true,
                            hidden = false) @QueryParam("query-param") String queryParam //
            ) {
                return null;
            }
        }

        io.swagger.models.Path path = scanJaxRsType(Dummy.class).getPath("/dummy/{path-param}");

        Operation get = getGetOperation(path);
        softly.assertThat(path.getPut()).as("put").isNull();
        softly.assertThat(path.getPost()).as("post").isNull();
        softly.assertThat(path.getDelete()).as("delete").isNull();
        softly.assertThat(path.getHead()).as("head").isNull();
        softly.assertThat(path.getOptions()).as("options").isNull();
        softly.assertThat(path.getPatch()).as("patch").isNull();

        softly.assertThat(get.getSummary()).isEqualTo("get-op");
        softly.assertThat(get.getDescription()).isEqualTo("get-notes");
        softly.assertThat(get.getTags()).containsExactly("t0", "t1");
        softly.assertThat(get.getOperationId()).isEqualTo("getMethod");
        softly.assertThat(get.getParameters()).containsExactly( //
                new BodyParameter() //
                        .name("b-param") //
                        .description("b-desc"),
                // TODO .defaultValue("b-def")
                // TODO .allowableValues("b-allowable")
                // TODO .required(true) //
                // TODO .access("b-access") //
                // TODO .allowMultiple(true) //
                // TODO .hidden(false) //
                new PathParameter() //
                        .name("p-param") //
                        .description("p-desc") //
                        ,
                new HeaderParameter() //
                        .name("h-param") //
                        .description("h-desc") //
                        ,
                new QueryParameter() //
                        .name("q-param") //
                        .description("q-desc") //
        );
        // TODO CookieParameter
        // TODO FormParameter
        // TODO Map<String, Response> responses
        // TODO List<Scheme> schemes
        // TODO List<String> consumes
        // TODO List<String> produces
        // TODO List<Map<String, List<String>>> security
        // TODO ExternalDocs externalDocs
        softly.assertThat(get.isDeprecated()).as("deprecated").isTrue();
    }

    @Test
    public void shouldScanJaxRsParamNames() {
        @Path("/dummy")
        class Dummy {
            @GET
            @Path("/{path-param}")
            @SuppressWarnings("unused")
            public String getMethod( //
                    String bodyParam, //
                    @PathParam("path-param") String pathParam, //
                    @HeaderParam("header-param") String headerParam, //
                    @QueryParam("query-param") String queryParam //
            ) {
                return null;
            }
        }

        io.swagger.models.Path path = scanJaxRsType(Dummy.class).getPath("/dummy/{path-param}");

        Operation get = getGetOperation(path);
        softly.assertThat(get.getOperationId()).isEqualTo("getMethod");
        softly.assertThat(get.getParameters()).containsExactly( //
                new BodyParameter().name("body"), //
                new PathParameter().name("path-param"), //
                new HeaderParameter().name("header-param"), //
                new QueryParameter().name("query-param") //
        );
        // TODO CookieParameter
        // TODO FormParameter
    }

    @Test
    public void shouldScanNullDescription() {
        @Path("/dummy")
        class Dummy {
            @GET
            @ApiOperation(value = "get-op")
            public void getMethod() {}
        }

        io.swagger.models.Path path = scanJaxRsType(Dummy.class).getPath("/dummy");

        Operation get = getGetOperation(path);
        assertThat(get.getDescription()).as("description").isNull();
    }

    @Test
    public void shouldScanDefaultTags() {
        @Path("/dummy")
        @Api(tags = "api-tag")
        class Dummy {
            @GET
            public void getMethod() {}
        }

        io.swagger.models.Path path = scanJaxRsType(Dummy.class).getPath("/dummy");

        Operation get = getGetOperation(path);
        softly.assertThat(get.getTags()).as("tags").containsExactly("api-tag");
    }

    @Test
    public void shouldNotScanEmptyDefaultTags() {
        @Path("/dummy")
        @Api()
        class Dummy {
            @GET
            public void getMethod() {}
        }

        io.swagger.models.Path path = scanJaxRsType(Dummy.class).getPath("/dummy");

        Operation get = getGetOperation(path);
        softly.assertThat(get.getTags()).as("tags").isNull();
    }

    @Test
    public void shouldScanApiValueAsTag() {
        @Path("/dummy")
        @Api("/t0")
        class Dummy {
            @GET
            public void getMethod() {}
        }

        io.swagger.models.Path path = scanJaxRsType(Dummy.class).getPath("/dummy");

        Operation get = getGetOperation(path);
        softly.assertThat(get.getTags()).as("tags").containsExactly("t0");
    }

    @Test
    public void shouldScanTwoGETs() {
        @Path("/foo")
        class Dummy {
            @GET
            @Path("/bar")
            public void bar() {}

            @GET
            @Path("/baz")
            public void baz() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);

        softly.assertThat(getGetOperation(swagger.getPath("/foo/bar"))).isNotNull();
        softly.assertThat(getGetOperation(swagger.getPath("/foo/baz"))).isNotNull();
    }

    @Test
    public void shouldScanPUT() {
        @Path("/foo")
        class Dummy {
            @PUT
            @Path("/bar")
            public void bar() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);
        io.swagger.models.Path path = swagger.getPath("/foo/bar");
        assertThat(path).as("path not found").isNotNull();
        Operation putOperation = path.getPut();
        assertThat(putOperation).as("PUT not found").isNotNull();

        softly.assertThat(putOperation).isNotNull();
    }

    @Test
    public void shouldScanPOST() {
        @Path("/foo")
        class Dummy {
            @POST
            @Path("/bar")
            public void bar() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);
        io.swagger.models.Path path = swagger.getPath("/foo/bar");
        assertThat(path).as("path not found").isNotNull();
        Operation postOperation = path.getPost();
        assertThat(postOperation).as("POST not found").isNotNull();

        softly.assertThat(postOperation).isNotNull();
    }

    @Test
    public void shouldScanHEAD() {
        @Path("/foo")
        class Dummy {
            @HEAD
            @Path("/bar")
            public void bar() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);
        io.swagger.models.Path path = swagger.getPath("/foo/bar");
        assertThat(path).as("path not found").isNotNull();
        Operation headOperation = path.getHead();
        assertThat(headOperation).as("HEAD not found").isNotNull();

        softly.assertThat(headOperation).isNotNull();
    }

    @Test
    public void shouldScanDELETE() {
        @Path("/foo")
        class Dummy {
            @DELETE
            @Path("/bar")
            public void bar() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);
        io.swagger.models.Path path = swagger.getPath("/foo/bar");
        assertThat(path).as("path not found").isNotNull();
        Operation deleteOperation = path.getDelete();
        assertThat(deleteOperation).as("DELETE not found").isNotNull();

        softly.assertThat(deleteOperation).isNotNull();
    }

    @Test
    public void shouldScanOPTIONS() {
        @Path("/foo")
        class Dummy {
            @OPTIONS
            @Path("/bar")
            public void bar() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);
        io.swagger.models.Path path = swagger.getPath("/foo/bar");
        assertThat(path).as("path not found").isNotNull();
        Operation optionsOperation = path.getOptions();
        assertThat(optionsOperation).as("OPTIONS not found").isNotNull();

        softly.assertThat(optionsOperation).isNotNull();
    }

    /** works even with this custom PATCH annotation! */
    @Target(METHOD)
    @Retention(RUNTIME)
    @HttpMethod("PATCH")
    @Documented
    public @interface PATCH {}

    @Test
    public void shouldScanPATCH() {
        @Path("/foo")
        class Dummy {
            @PATCH
            public void bar() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);
        io.swagger.models.Path path = swagger.getPath("/foo");
        assertThat(path).as("path not found").isNotNull();
        Operation patchOperation = path.getPatch();
        assertThat(patchOperation).as("PATCH not found").isNotNull();

        softly.assertThat(patchOperation).isNotNull();
    }

    @Test
    public void shouldScanResponse() {
        @Path("/foo")
        class Dummy {
            @GET
            @ApiResponses(@ApiResponse(code = 400, message = "error", reference = "ref",
                    responseHeaders = { @ResponseHeader(name = "X-Status", description = "error detail",
                            response = LocalDate.class, responseContainer = "List") },
                    response = Integer.class, responseContainer = "List"))
            public void bar() {}
        }

        Swagger swagger = scanJaxRsType(Dummy.class);

        Operation get = getGetOperation(swagger.getPath("/foo"));
        softly.assertThat(get.getResponses()).hasSize(1);
        softly.assertThat(get.getResponses().get("400")).isEqualTo( //
                new Response() //
                        .description("error") //
        // TODO ref
        // TODO .header("X-Status", "error detail") //
        // TODO response
        // TODO responseContainer
        );
    }
}

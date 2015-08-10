package com.github.t1.swap;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import javax.lang.model.element.TypeElement;
import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.swagger.annotations.*;
import io.swagger.models.*;
import io.swagger.models.parameters.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiScannerTest extends AbstractSwaggerScannerTest {
    private Swagger scanApiClass(Class<?> container) {
        TypeElement typeElement = new ReflectionTypeElement(container);
        swaggerScanner.addPathElements(asSet(typeElement));
        return swaggerScanner.getResult();
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

        io.swagger.models.Path path = scanApiClass(Dummy.class).getPath("/dummy/{path-param}");

        assertNotNull("path not found", path);
        assertNull("put", path.getPut());
        assertNull("post", path.getPost());
        assertNull("delete", path.getDelete());
        assertNull("head", path.getHead());
        assertNull("options", path.getOptions());
        assertNull("patch", path.getPatch());

        Operation get = path.getGet();
        assertEquals("get-op", get.getSummary());
        assertEquals("get-notes", get.getDescription());
        assertEquals(asList("t0", "t1"), get.getTags());
        assertEquals("getMethod", get.getOperationId());
        assertEquals(new BodyParameter() //
                .name("b-param") //
                .description("b-desc") //
        // TODO .defaultValue("b-def")
        // TODO .allowableValues("b-allowable")
        // TODO .required(true) //
        // TODO .access("b-access") //
        // TODO .allowMultiple(true) //
        // TODO .hidden(false) //
        , get.getParameters().get(0));
        assertEquals(new PathParameter() //
                .name("p-param") //
                .description("p-desc") //
                , get.getParameters().get(1));
        assertEquals(new HeaderParameter() //
                .name("h-param") //
                .description("h-desc") //
                , get.getParameters().get(2));
        assertEquals(new QueryParameter() //
                .name("q-param") //
                .description("q-desc") //
                , get.getParameters().get(3));
        // TODO CookieParameter
        // TODO FormParameter
        // TODO Map<String, Response> responses;
        // TODO List<Scheme> schemes;
        // TODO List<String> consumes;
        // TODO List<String> produces;
        // TODO List<Map<String, List<String>>> security;
        // TODO ExternalDocs externalDocs;
        assertTrue("deprecated", get.isDeprecated());
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

        io.swagger.models.Path path = scanApiClass(Dummy.class).getPath("/dummy/{path-param}");

        assertNotNull("path not found", path);
        Operation get = path.getGet();
        assertEquals("getMethod", get.getOperationId());
        assertEquals(new BodyParameter().name("body"), get.getParameters().get(0));
        assertEquals(new PathParameter().name("path-param"), get.getParameters().get(1));
        assertEquals(new HeaderParameter().name("header-param"), get.getParameters().get(2));
        assertEquals(new QueryParameter().name("query-param"), get.getParameters().get(3));
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

        io.swagger.models.Path path = scanApiClass(Dummy.class).getPath("/dummy");

        assertNotNull("path not found", path);
        Operation get = path.getGet();
        assertNull("description", get.getDescription());
    }
}

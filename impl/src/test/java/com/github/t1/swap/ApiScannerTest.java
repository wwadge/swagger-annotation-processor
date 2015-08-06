package com.github.t1.swap;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import javax.ws.rs.Path;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.swagger.annotations.SwaggerDefinition;

@RunWith(MockitoJUnitRunner.class)
public class ApiScannerTest extends AbstractSwaggerScannerTest {
    @SwaggerDefinition
    private static class Index {}

    @Before
    public void before() {
        givenSwaggerDefinition(Index.class);
    }

    @Test
    @Ignore
    public void shouldParseApi() {
        @Path("/dummy")
        class Dummy {}
        givenApi(Dummy.class);

        io.swagger.models.Path path = swaggerScanner.getResult().getPath("/");

        assertEquals(asList("dummy"), path.getGet().getTags());
    }
}

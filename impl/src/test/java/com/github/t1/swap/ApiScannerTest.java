package com.github.t1.swap;

import static java.util.Arrays.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.*;
import javax.lang.model.util.ElementScanner7;
import javax.ws.rs.*;
import javax.ws.rs.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.*;
import io.swagger.models.parameters.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiScannerTest extends AbstractSwaggerScannerTest {
    private Swagger scanApiClass(Class<?> container) {
        TypeElement typeElement = mockType(container);

        List<ExecutableElement> methods = new java.util.ArrayList<>();
        for (Method method : container.getDeclaredMethods())
            methods.add(mockMethod(method));

        mockVisiting(typeElement, methods);

        swaggerScanner.addPathElements(asSet(typeElement));
        return swaggerScanner.getResult();
    }

    private TypeElement mockType(Class<?> container) {
        TypeElement typeElement = mock(TypeElement.class, "container");
        when(typeElement.getAnnotation(Path.class)).thenReturn(container.getAnnotation(Path.class));
        mockName(typeElement, container.getSimpleName());
        return typeElement;
    }

    private ExecutableElement mockMethod(Method method) {
        ExecutableElement executableElement = mock(ExecutableElement.class);

        mockName(executableElement, method.getName());
        when(executableElement.getKind()).thenReturn(ElementKind.METHOD);
        when(executableElement.getAnnotation(Deprecated.class)).thenReturn(method.getAnnotation(Deprecated.class));

        if (method.isAnnotationPresent(Path.class)) {
            when(executableElement.getAnnotation(Path.class)).thenReturn(method.getAnnotation(Path.class));
        }
        if (method.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            when(executableElement.getAnnotation(ApiOperation.class)).thenReturn(apiOperation);
        }
        mockParams(method, executableElement);

        return executableElement;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void mockParams(Method method, ExecutableElement executableElement) {
        List<VariableElement> parameters = new ArrayList<>();
        for (int i = 0; i < method.getParameterTypes().length; i++)
            parameters.add(new MockParameterVariableElement(method, i));
        when(executableElement.getParameters()).thenReturn((List) parameters);
    }

    @SuppressWarnings("unchecked")
    private void mockVisiting(TypeElement typeElement, final List<ExecutableElement> methods) {
        when(typeElement.accept(any(ElementScanner7.class), isNull())).thenAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                ElementScanner7<?, ?> visitor = invocation.getArgumentAt(0, ElementScanner7.class);
                for (ExecutableElement method : methods) {
                    visitor.visitExecutable(method, null);
                }
                return null;
            }
        });
    }

    private void mockName(Element executableElement, String string) {
        Name name = mock(Name.class);
        when(executableElement.getSimpleName()).thenReturn(name);
        when(name.toString()).thenReturn(string);
    }

    @Test
    public void shouldParseFullGET() {
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
        assertEquals(asList( //
                // TODO CookieParameter
                // TODO FormParameter
                new BodyParameter(), //
                new PathParameter().name("path-param"), //
                new HeaderParameter().name("header-param"), //
                new QueryParameter().name("query-param") //
        ), get.getParameters());
        // TODO Map<String, Response> responses;
        // TODO List<Scheme> schemes;
        // TODO List<String> consumes;
        // TODO List<String> produces;
        // TODO List<Map<String, List<String>>> security;
        // TODO ExternalDocs externalDocs;
        assertTrue("deprecated", get.isDeprecated());
    }

    @Test
    public void shouldParseNullDescription() {
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

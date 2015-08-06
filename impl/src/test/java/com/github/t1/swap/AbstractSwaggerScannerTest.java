package com.github.t1.swap;

import static java.util.Arrays.*;
import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static org.mockito.Mockito.*;

import java.util.*;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.ws.rs.Path;

import org.junit.Before;
import org.mockito.Mock;

import io.swagger.annotations.SwaggerDefinition;

public abstract class AbstractSwaggerScannerTest {
    @Mock
    private Messager messager;

    protected SwaggerScanner swaggerScanner;

    @Before
    public void setupSwaggerScanner() {
        this.swaggerScanner = new SwaggerScanner(messager);
    }

    @SafeVarargs
    private static <T> Set<T> asSet(T... elements) {
        return new LinkedHashSet<>(asList(elements));
    }

    protected void givenSwaggerDefinition(Class<?> container) {
        TypeElement swaggerDefinitionElement = mock(TypeElement.class);
        when(swaggerDefinitionElement.getKind()).thenReturn(CLASS);
        when(swaggerDefinitionElement.getModifiers()).thenReturn(asSet(PUBLIC));
        when(swaggerDefinitionElement.getAnnotation(SwaggerDefinition.class))
                .thenReturn(container.getAnnotation(SwaggerDefinition.class));
        swaggerScanner.addSwaggerDefinitions(asSet(swaggerDefinitionElement));
    }

    protected void givenApi(Class<?> container) {
        TypeElement apiElement = mock(TypeElement.class);
        Path path = container.getAnnotation(Path.class);
        when(apiElement.getAnnotation(Path.class)).thenReturn(path);
        swaggerScanner.apis(asSet(apiElement));
    }
}

package com.github.t1.swap;

import static java.util.Arrays.*;

import java.util.*;

import javax.annotation.processing.Messager;

import org.junit.Before;
import org.mockito.Mock;

public abstract class AbstractSwaggerScannerTest {
    @Mock
    private Messager messager;

    protected SwaggerScanner swaggerScanner;

    @Before
    public void setupSwaggerScanner() {
        this.swaggerScanner = new SwaggerScanner(messager);
    }

    @SafeVarargs
    protected static <T> Set<T> asSet(T... elements) {
        return new LinkedHashSet<>(asList(elements));
    }
}

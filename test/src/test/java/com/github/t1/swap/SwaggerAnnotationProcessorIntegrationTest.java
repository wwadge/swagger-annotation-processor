package com.github.t1.swap;

import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.StrictAssertions.*;

public class SwaggerAnnotationProcessorIntegrationTest {
    private static final File EXPECTED_FILE = new File("src/test/resources/expected-swagger.yaml");
    private static final File ACTUAL_FILE = new File("target/classes/swagger.yaml");

    @Test
    public void shouldHaveProducedSwaggerYaml() {
        assertThat(ACTUAL_FILE).hasSameContentAs(EXPECTED_FILE);
    }
}

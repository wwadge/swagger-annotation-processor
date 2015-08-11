package com.github.t1.swap;

import static org.assertj.core.api.StrictAssertions.*;

import java.io.File;

import org.junit.Test;

public class SwaggerAnnotationProcessorIntegrationTest {
    private static final File EXPECTED_FILE = new File("src/test/resources/expected-swagger.yaml");
    private static final File ACTUAL_FILE = new File("target/generated-sources/annotations/swagger.yaml");

    @Test
    public void shouldHaveProducedSwaggerYaml() {
        assertThat(ACTUAL_FILE).hasSameContentAs(EXPECTED_FILE);
    }
}

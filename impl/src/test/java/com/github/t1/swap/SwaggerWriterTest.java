package com.github.t1.swap;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.*;

import org.junit.Test;

import io.swagger.models.*;

public class SwaggerWriterTest {
    /** tests only part of the swagger file format... but we use swagger-core, so that should be okay */
    @Test
    public void shouldWriteSwagger() {
        StringWriter writer = new StringWriter();
        Swagger swagger = new Swagger();
        swagger.info(new Info().version("test-version").title("test-title"));
        swagger.getInfo().setVendorExtension("x-n0", params("n01", "v01", "n02", "v02"));
        swagger.getInfo().setVendorExtension("x-n1", params("n11", "v11", "n12", "v12"));
        swagger.tag(new Tag().name("tag-0").description("tag-0-d"));
        swagger.path("/path-0",
                new Path().get(new Operation() //
                        .tag("tag-0") //
                        .summary("get-op") //
                        .description("descr") //
                        .operationId("op") //
        ));

        new SwaggerWriter(writer).write(swagger);

        assertEquals("---\n" //
                + "swagger: \"2.0\"\n" //
                + "info:\n" //
                + "  version: \"test-version\"\n" //
                + "  title: \"test-title\"\n" //
                + "  x-n0:\n" //
                + "    n01: \"v01\"\n" //
                + "    n02: \"v02\"\n" //
                + "  x-n1:\n" //
                + "    n11: \"v11\"\n" //
                + "    n12: \"v12\"\n" //
                + "tags:\n" //
                + "- name: \"tag-0\"\n" //
                + "  description: \"tag-0-d\"\n" //
                + "paths:\n" //
                + "  /path-0:\n" //
                + "    get:\n" //
                + "      tags:\n" //
                + "      - \"tag-0\"\n" //
                + "      summary: \"get-op\"\n" //
                + "      description: \"descr\"\n" //
                + "      operationId: \"op\"\n" //
                + "      parameters: []\n" //
                , writer.toString());
    }

    private Map<String, String> params(String... params) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < params.length; i += 2)
            map.put(params[i], params[i + 1]);
        return map;
    }
}

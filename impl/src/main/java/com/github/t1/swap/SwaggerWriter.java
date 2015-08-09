package com.github.t1.swap;

import java.io.*;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

public class SwaggerWriter {
    private final PrintWriter writer;

    public SwaggerWriter(Writer writer) {
        this.writer = new PrintWriter(writer);
    }

    public void write(Swagger swagger) {
        try {
            Yaml.mapper().writeValue(writer, swagger);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

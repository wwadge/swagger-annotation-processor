package com.github.t1.swap;

import java.io.*;

import com.esotericsoftware.yamlbeans.YamlWriter;

import io.swagger.models.Swagger;

public class SwaggerWriter {
    private final Writer writer;

    public SwaggerWriter(Writer writer) {
        this.writer = writer;
    }

    public void write(Swagger swagger) throws IOException {
        YamlWriter yaml = new YamlWriter(writer);
        // yaml.getConfig().setClassTag("swagger", Swagger.class);
        yaml.write(swagger);
        yaml.close();
    }
}

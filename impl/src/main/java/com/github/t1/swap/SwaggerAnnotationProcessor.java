package com.github.t1.swap;

import com.github.t1.exap.*;
import com.github.t1.exap.reflection.Resource;
import io.swagger.annotations.SwaggerDefinition;

import javax.annotation.processing.SupportedSourceVersion;
import java.io.*;

import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationClasses({ SwaggerDefinition.class, javax.ws.rs.Path.class })
public class SwaggerAnnotationProcessor extends ExtendedAbstractProcessor {
    private final SwaggerScanner swagger = new SwaggerScanner();

    @Override
    public boolean process(Round round) throws IOException {
        log.debug("process {}", round);

        swagger.addSwaggerDefinitions(round.typesAnnotatedWith(SwaggerDefinition.class))
               .addJaxRsTypes(round.typesAnnotatedWith(javax.ws.rs.Path.class));

        if (round.isLast() && swagger.isWorthWriting())
            writeSwagger(round);
        return false;
    }

    private void writeSwagger(Round round) throws IOException {
        Resource resource = round.createResource("swagger-ui/swagger.yaml");
        log.debug("write {}", resource.getPath());
        try (Writer out = resource.openWriter()) {
            new SwaggerWriter(out).write(swagger.getResult());
        }
        note("created " + resource.getPath());
    }
}

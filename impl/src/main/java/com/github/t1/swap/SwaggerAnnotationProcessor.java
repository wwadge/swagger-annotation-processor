package com.github.t1.swap;

import com.github.t1.exap.*;
import com.github.t1.exap.reflection.Resource;
import io.swagger.annotations.SwaggerDefinition;
import org.slf4j.*;

import javax.annotation.processing.SupportedSourceVersion;
import javax.ws.rs.Path;
import java.io.*;

import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_8)
@SupportedAnnotationClasses({ SwaggerDefinition.class, Path.class })
public class SwaggerAnnotationProcessor extends ExtendedAbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(SwaggerAnnotationProcessor.class);

    private final SwaggerScanner swagger = new SwaggerScanner();

    @Override
    public boolean process(Round round) throws IOException {
        log.debug("process {}", round);

        swagger.addSwaggerDefinitions(round.typesAnnotatedWith(SwaggerDefinition.class));
        swagger.addJaxRsTypes(round.typesAnnotatedWith(Path.class));

        if (round.isLast() && swagger.isWorthWriting())
            writeSwagger(round);
        return false;
    }

    private void writeSwagger(Round round) throws IOException {
        Resource resource = round.createResource("", "swagger.yaml");
        log.debug("write {}", resource.getName());
        try (Writer writer = resource.openWriter()) {
            new SwaggerWriter(writer).write(swagger.getResult());
        }
        note("created " + resource.getName());
    }
}

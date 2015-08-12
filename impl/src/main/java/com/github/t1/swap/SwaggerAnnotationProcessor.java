package com.github.t1.swap;

import static javax.lang.model.SourceVersion.*;
import static javax.tools.StandardLocation.*;

import java.io.*;

import javax.annotation.processing.SupportedSourceVersion;
import javax.tools.FileObject;
import javax.ws.rs.Path;

import org.slf4j.*;

import com.github.t1.exap.*;

import io.swagger.annotations.SwaggerDefinition;

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
            writeSwagger();
        return false;
    }

    private void writeSwagger() throws IOException {
        FileObject fileObject = filer().createResource(SOURCE_OUTPUT, "", "swagger.yaml");
        log.debug("write {}", fileObject.getName());
        try (Writer writer = fileObject.openWriter()) {
            new SwaggerWriter(writer).write(swagger.getResult());
        }
        note("created " + fileObject.getName());
    }
}

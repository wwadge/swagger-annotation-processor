package com.github.t1.swap;

import static javax.tools.StandardLocation.*;

import java.io.*;
import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

import org.slf4j.*;

import com.github.t1.exap.*;

import io.swagger.annotations.*;

@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationClasses(SwaggerDefinition.class)
public class SwaggerAnnotationProcessor extends ExtendedAbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(SwaggerAnnotationProcessor.class);

    private SwaggerScanner swagger;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, int roundNumber)
            throws IOException {
        log.debug("process {}", roundEnv.getRootElements());
        if (swagger == null)
            swagger = new SwaggerScanner(messager());
        swagger.addSwaggerDefinitions(roundEnv.getElementsAnnotatedWith(SwaggerDefinition.class));
        swagger.apis(roundEnv.getElementsAnnotatedWith(Api.class));
        if (roundEnv.processingOver() && swagger.isWorthWriting())
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

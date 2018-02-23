package com.github.wwadge.swaggerapt;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

@Target({TYPE, PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EnableSwagger {

    EnableSwagger.Scheme scheme() default Scheme.APIS;
    String configFile() default "swagger-config.json";
    String specFile() default "swagger.yml";
    String outputDir() default "src/generated/java";
    String lang() default "io.swagger.codegen.languages.SpringCodegen";


    public static enum Scheme {
        APIS, MODELS, NONE ;

        private Scheme() {
        }
    }
}



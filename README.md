Gradle example whereby the generated output is also passed through querydsl (eg for @QueryEntities)


configurations {
    swagger
    querydsl
}

sourceSets {
    swagger {
        java {
            srcDirs = ["src/main/java"] // look into src/main/java to find the @EnableSwagger annotation
        }
    }
    querydsl {
        java {
            srcDirs = ["src/generated/java", "src/main/java"] // look at where swagger generated code (+anything else)
        }
    }

    sourceSets.swagger.compileClasspath += sourceSets.main.compileClasspath
    sourceSets.querydsl.compileClasspath += sourceSets.main.compileClasspath + sourceSets.swagger.compileClasspath
    sourceSets.main.compileClasspath += sourceSets.swagger.compileClasspath + sourceSets.querydsl.compileClasspath
}


compileSwaggerJava {
    options.annotationProcessorPath = configurations.swagger
}

compileQuerydslJava {
    options.annotationProcessorPath = configurations.querydsl
}
compileJava {
    options.annotationProcessorPath = null
}

dependencies {
    compile 'com.github.t1:swagger-annotation-processor-interface:1.0.0'

    swagger 'com.github.t1:swagger-annotation-processor:1.0.0'
    swagger 'io.swagger:swagger-codegen:2.2.7-eft'
    querydsl group: 'com.querydsl', name: 'querydsl-apt', version:'4.1.3'
    querydsl group: 'com.querydsl', name: 'querydsl-apt', version:'4.1.3', classifier: "general"

}


compileJava.dependsOn([swaggerClasses, querydslClasses])
querydslClasses.mustRunAfter(swaggerClasses)
idea {

    module {
        // Marks the already(!) added srcDir as "generated"
        generatedSourceDirs += file('src/generated/java')
    }
}


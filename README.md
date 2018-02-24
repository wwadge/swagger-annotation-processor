Given an annotation somewhere in your code, this will generate all the swagger classes by means of an APT processor.

How to use? 
- Add to gradle dependencies (see below)

- Add @EnableSwagger:

```java
    @EnableSwagger(scheme = EnableSwagger.Scheme.APIS)
    package com.foo.account;

    
    import com.github.wwadge.swaggerapt.EnableSwagger;
```

- Add swagger.yml and swagger-config.json in src/main/resources. For example you can use this for model objects:

```json
    {
      "dateLibrary": "java8",
      "modelPackage" : "com.foo.model",
      "apiPackage" : "com.foo.account.controller",
      "useBeanValidation": "true",
      "delegatePattern": "true",
      "typeMappings":{"Boolean":"boolean"},
      "modelNamePrefix": "",
      "importMappings": {
        "MonetaryAmount": "org.javamoney.moneta.Money",
        "EntityId": "com.foo.common.entities.EntityId",
        "QueryDslBinder": "com.foo.common.util.QueryDslBinder",
        "YearMonth": "java.time.YearMonth",
        "InetAddress": "java.net.InetAddress",
        "CurrencyUnit": "javax.money.CurrencyUnit",
        "Email": "javax.validation.constraints.Email"
      }
    }
```

Gradle example that also configures for use with querydsl apt which means you can use @QueryEntity in your 
swagger template. Every time gradle or Intellij tries to compile, it will generate the swagger files followed by running the Querydsl APT on the resultant files
(obviously querydsl is completely optional here).

    apply plugin: 'java'
    apply plugin: 'idea'

    
    configurations {
        swagger
        querydsl
        testCompile.extendsFrom compile
    }
    
    sourceSets {
        swagger {
            java {
                srcDirs = [sourceSets.main.java.srcDirs, swagger.java.outputDir] // look to find the @EnableSwagger annotation
            }
        }
        querydsl {
            java {
                srcDirs = ["$buildDir/classes/java/swagger"] // look at where swagger generated code (+anything else)
            }
        }
    
    
        main{
            java {
                srcDirs += [sourceSets.querydsl.java.srcDirs, sourceSets.swagger.java.outputDir]
            }
        }
    
        sourceSets.swagger.compileClasspath +=   project.configurations.compile
        sourceSets.querydsl.compileClasspath += project.configurations.compile  + sourceSets.swagger.compileClasspath
        sourceSets.main.compileClasspath += sourceSets.querydsl.compileClasspath + sourceSets.swagger.compileClasspath+ swagger.output + querydsl.output
        sourceSets.test.compileClasspath += sourceSets.main.compileClasspath + sourceSets.querydsl.compileClasspath + sourceSets.swagger.compileClasspath+ swagger.output + querydsl.output
    }
    
    
    
    
    
    compileSwaggerJava {
        options.annotationProcessorPath = configurations.swagger
        options.compilerArgs +=  ["-proc:only"]
    }
    
    compileQuerydslJava {
        options.annotationProcessorPath = configurations.querydsl
        options.compilerArgs +=  ["-proc:only"]
    }
    compileJava {
        options.annotationProcessorPath = null
    }
    
    compileTestJava {
        options.annotationProcessorPath = null
    }
    
    //compileTestJava.dependsOn(compileJava)
    compileJava.dependsOn([swaggerClasses, querydslClasses])
    querydslClasses.mustRunAfter(swaggerClasses)
    
    
    
    idea {
        module {
            // Marks the already(!) added srcDir as "generated"
            generatedSourceDirs += [sourceSets.swagger.java.outputDir, sourceSets.querydsl.java.outputDir]
            sourceDirs += [sourceSets.swagger.java.outputDir, sourceSets.querydsl.java.outputDir]
        }
    }
    
    clean{
        delete sourceSets.swagger.java.outputDir
        delete sourceSets.querydsl.java.outputDir
    
    }
    
    
    dependencies{
        // this little snippet gets the top level folder by asking git to avoid hard-coding paths
        def getTopLevelCode = { ->
            def stdout = new ByteArrayOutputStream()
            exec {
                commandLine 'git', 'rev-parse', '--show-toplevel'
                standardOutput = stdout
            }
            return stdout.toString().trim()
        }
    
    
        apply from: "$getTopLevelCode/gradle-common/versions.gradle"
    
    
        compile "com.github.wwadge:swagger-annotation-processor-interface:${swaggerAnnotationProcessor}"
        swagger "com.github.wwadge:swagger-annotation-processor:${swaggerAnnotationProcessor}"
        compileOnly "com.github.wwadge:swagger-annotation-processor:${swaggerAnnotationProcessor}"
        compileOnly "io.swagger:swagger-codegen:${swaggerCodegenVersion}"
        swagger "io.swagger:swagger-codegen:${swaggerCodegenVersion}"
        querydsl group: 'com.querydsl', name: 'querydsl-apt', version:'4.1.3'
        querydsl group: 'com.querydsl', name: 'querydsl-apt', version:'4.1.3', classifier: "general"
    }
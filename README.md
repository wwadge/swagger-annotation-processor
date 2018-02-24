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
        "QueryDslBinder": "com.akcegroup.common.util.QueryDslBinder",
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
    }
    
    sourceSets {
        swagger {
            java {
                srcDirs = sourceSets.main.java.srcDirs // look into src/main/java to find the @EnableSwagger annotation
            }
        }
        querydsl {
            java {
                srcDirs = ["$buildDir/classes/java/swagger"] // look at where swagger generated code (+anything else)
            }
        }
    
        main.java.srcDirs += querydsl.java.srcDirs
    
        sourceSets.swagger.compileClasspath += project.configurations.compile  // "src/main/java"
        sourceSets.querydsl.compileClasspath += project.configurations.compile  + sourceSets.swagger.compileClasspath
        sourceSets.main.compileClasspath += sourceSets.querydsl.compileClasspath + sourceSets.swagger.compileClasspath+ swagger.output + querydsl.output
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
    
    compileJava.dependsOn([swaggerClasses, querydslClasses])
    querydslClasses.mustRunAfter(swaggerClasses)
    
    idea {
    
        module {
            // Marks the already(!) added srcDir as "generated"
            generatedSourceDirs += [file("$buildDir/java/swagger"), file("$buildDir/java/querydsl")]
        }
    }
    
    
    dependencies{
        compile 'com.github.wwadge:swagger-annotation-processor-interface:1.0.0' // <-- contains @EnableSwagger
        swagger 'com.github.wwadge:swagger-annotation-processor:1.0.0'
        swagger 'io.swagger:swagger-codegen:2.2.7' // <---- replace with the swagger version of your choice here
        querydsl group: 'com.querydsl', name: 'querydsl-apt', version:'4.1.3'
        querydsl group: 'com.querydsl', name: 'querydsl-apt', version:'4.1.3', classifier: "general"
    }
    
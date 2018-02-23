package com.github.wwadge.swaggerapt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.lang.model.element.Element;

import static com.github.wwadge.swaggerapt.EnableSwagger.Scheme;

public class SwaggerScanner {
    private static final Logger log = LoggerFactory.getLogger(SwaggerScanner.class);


    private Scheme scheme = Scheme.APIS;
    private String configFile;
    private String lang;
    private String specFile;
    private String outputDir;



    // visible for testing
    public SwaggerScanner addSwaggerDefinition(Element swaggerDefinitionElement) {
        com.github.wwadge.swaggerapt.EnableSwagger swaggerDefinition = swaggerDefinitionElement.getAnnotation(com.github.wwadge.swaggerapt.EnableSwagger.class);
        this.scheme = swaggerDefinition.scheme();
        this.outputDir = swaggerDefinition.outputDir();
        this.configFile = swaggerDefinition.configFile();
        this.specFile = swaggerDefinition.specFile();
        this.lang = swaggerDefinition.lang();
        return this;
    }

    public Scheme getScheme() {
        return scheme;
    }
    public String getConfigFile() {
        return configFile;
    }
    public String getSpecFile() {
        return specFile;
    }
    public String getOutputDir() {
        return outputDir;
    }
    public String getLang() {
        return lang;
    }


}

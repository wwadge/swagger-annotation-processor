package com.github.wwadge.swaggerapt;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.codegen.config.CodegenConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedSourceVersion(RELEASE_8)
public class SwaggerAnnotationProcessor extends AbstractProcessor {
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String RESOURCES_PREFIX = ""; // src" + File.separator + "main" + File.separator + "resources" + File.separator;

    @Override
    public int hashCode() {
        return Integer.MIN_VALUE;
    }

    public boolean process(Round round)  {


        round.typesAnnotatedWith(com.github.wwadge.swaggerapt.EnableSwagger.class).stream().forEach(e -> {
            SwaggerScanner swagger = new SwaggerScanner();
            swagger.addSwaggerDefinition( e);
            if (!round.isLast()) {
                try {
                    JavaFileObject out = processingEnv.getFiler().createSourceFile("package-info", e);
                    out.openOutputStream().close();
                    out.delete();

                    String outputPath = new File(out.toUri()).getParent();
                    swagger.setOutputDir(outputPath + File.separator+swagger.getOutputDir());


                    File projectRoot = new File(outputPath).getParentFile().getParentFile().getParentFile().getParentFile();

                    if (swagger.getSpecFile().startsWith(CLASSPATH_PREFIX)) {
                        log.info(swagger.getSpecFile().substring(CLASSPATH_PREFIX.length()));
                        FileObject path = processingEnv.getFiler().getResource(StandardLocation.ANNOTATION_PROCESSOR_PATH, "", swagger.getSpecFile().substring(CLASSPATH_PREFIX.length()));
                        if (path == null) {
                            log.error("Unable to find YML file specified in @EnableSwagger. Trying without classpath prefix");
                            swagger.setSpecFile(new File(projectRoot, RESOURCES_PREFIX+swagger.getSpecFile().substring(CLASSPATH_PREFIX.length())).getAbsolutePath());
                        } else {


                            File tmp = File.createTempFile(swagger.getSpecFile().substring(CLASSPATH_PREFIX.length()), "");
                            InputStream is = path.openInputStream();
                            java.nio.file.Files.copy(
                                    is,
                                    tmp.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);

                            is.close();
                            swagger.setSpecFile(tmp.getAbsolutePath());
                            log.info("--->" + swagger.getSpecFile());
                        }
                    } else {
                        swagger.setSpecFile(new File(projectRoot, RESOURCES_PREFIX+swagger.getSpecFile()).getAbsolutePath());

                    }
                    swagger.setConfigFile(new File(projectRoot,RESOURCES_PREFIX+ swagger.getConfigFile()).getAbsolutePath());

                    note("Serializing Swagger types to "+ swagger.getOutputDir()+" via " + swagger.getSpecFile() +" [config: "+swagger.getConfigFile()+" ]");
                    writeSwagger(swagger);


                } catch (IOException e1) {
                    log.error("Error during generation", e1);
                    e1.printStackTrace();
                }
            }
        });
            return false;
    }

    private void writeSwagger(SwaggerScanner swagger)  {
        CodegenConfigurator configurator = CodegenConfigurator.fromFile(swagger.getConfigFile());
        configurator.setLang(swagger.getLang());
        log.info(swagger.getSpecFile());
        configurator.setInputSpec(swagger.getSpecFile());

        configurator.setOutputDir(swagger.getOutputDir());
        final ClientOptInput input = configurator.toClientOptInput();
        final CodegenConfig config = input.getConfig();
        System.setProperty(swagger.getScheme().name().toLowerCase(), "");

        Map<?, ?> configOptions= Maps.newHashMap();

        if (configOptions != null) {
            for (CliOption langCliOption : config.cliOptions()) {
                if (configOptions.containsKey(langCliOption.getOpt())) {
                    input.getConfig().additionalProperties()
                            .put(langCliOption.getOpt(), configOptions.get(langCliOption.getOpt()));
                }
            }
        }
        new DefaultGenerator().opts(input).generate();

    }


    protected final Logger log = LoggerFactory.getLogger(getClass());

    private int roundNumber = -1;

    /** use {@link #process(Round)} */
    @Override
    final public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ++roundNumber;

        log.debug("begin round {} (final = {}) of {}", +roundNumber, roundEnv.processingOver(), name());

        try {
            boolean claimed = process(new Round(log, processingEnv, roundEnv, roundNumber));

            log.debug("end round {} of {}", roundNumber, name());

            return claimed;
        } catch (Exception e) {
            String message = e.getClass().getSimpleName() + ((e.getMessage() == null) ? "" : (": " + e.getMessage()));
            error("annotation processing round " + roundNumber + " failed: " + message);
            log.error("annotation processing round " + roundNumber + " failed", e);
            return true;
        }
    }

    private String name() {
        return getClass().getSimpleName();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Sets.newHashSet(com.github.wwadge.swaggerapt.EnableSwagger.class.getName());
    }

    private Messager messager() {
        return processingEnv.getMessager();
    }

    public void error(CharSequence message) {
        messager().printMessage(ERROR, message);
    }

    public void note(CharSequence message) {
        messager().printMessage(NOTE, message);
    }
}



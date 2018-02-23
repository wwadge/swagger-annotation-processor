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
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static javax.lang.model.SourceVersion.RELEASE_8;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedSourceVersion(RELEASE_8)
public class SwaggerAnnotationProcessor extends AbstractProcessor {
    @Override
    public int hashCode() {
        return Integer.MIN_VALUE;
    }

    private final SwaggerScanner swagger = new SwaggerScanner();

    public boolean process(Round round) throws IOException {


        if (!round.typesAnnotatedWith(com.github.wwadge.swaggerapt.EnableSwagger.class).isEmpty()) {
            swagger.addSwaggerDefinition(round.typesAnnotatedWith(com.github.wwadge.swaggerapt.EnableSwagger.class).stream().findFirst().get());


            if (round.number() == 0) {
                note("Serializing Swagger types");
                writeSwagger(round);

            }
        }
        return false;
    }

    private void writeSwagger(Round round) throws IOException {
        CodegenConfigurator configurator = CodegenConfigurator.fromFile(swagger.getConfigFile());
        configurator.setLang(swagger.getLang());
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

//    public void warning(CharSequence message) {
//        messager().printMessage(WARNING, message);
//    }
//
//    public void mandatoryWarning(CharSequence message) {
//        messager().printMessage(MANDATORY_WARNING, message);
//    }

    public void note(CharSequence message) {
        messager().printMessage(NOTE, message);
    }

//    public void otherMessage(CharSequence message) {
//        messager().printMessage(OTHER, message);
//    }
//
//    public List<? extends Element> getAllMembers(TypeElement type) {
//        return processingEnv.getElementUtils().getAllMembers(type);
//    }
//
//    public void printElements(Writer writer, Element... elements) {
//        processingEnv.getElementUtils().printElements(writer, elements);
//    }
}



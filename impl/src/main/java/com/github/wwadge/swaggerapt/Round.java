package com.github.wwadge.swaggerapt;


import org.slf4j.Logger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import java.lang.annotation.Annotation;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class Round {

    private final Logger log;
    private final ProcessingEnvironment processingEnv;
    private final RoundEnvironment roundEnv;
    private final int roundNumber;

    public Round(Logger log, ProcessingEnvironment processingEnv, RoundEnvironment roundEnv, int roundNumber) {
        this.log = log;
        this.processingEnv = processingEnv;
        this.roundEnv = roundEnv;
        this.roundNumber = roundNumber;
    }

    public List<Element> typesAnnotatedWith(Class<? extends Annotation> type) {
        return roundEnv.getElementsAnnotatedWith(type).stream()
                .filter(element -> element.getKind().equals(ElementKind.PACKAGE))
                .collect(toList());
    }

    public Logger log() {
        return log;
    }


    public int number() {
        return roundNumber;
    }

    public boolean isLast() {
        return roundEnv.processingOver();
    }

    @Override
    public String toString() {
        return "Round#" + roundNumber + "-"
                + ((roundEnv == null) ? "mock" : (roundEnv.getRootElements() + (isLast() ? " [last]" : "")));
    }


}

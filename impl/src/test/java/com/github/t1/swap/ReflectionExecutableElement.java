package com.github.t1.swap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

public class ReflectionExecutableElement implements ExecutableElement {
    private final Method method;

    public ReflectionExecutableElement(Method method) {
        this.method = method;
    }

    @Override
    public TypeMirror asType() {
        throw new UnsupportedOperationException("asType");
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

    @Override
    public Set<Modifier> getModifiers() {
        throw new UnsupportedOperationException("getModifiers");
    }

    @Override
    public Element getEnclosingElement() {
        return new ReflectionTypeElement(method.getDeclaringClass());
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        throw new UnsupportedOperationException("getEnclosedElements");
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        throw new UnsupportedOperationException("getAnnotationMirrors");
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return method.getAnnotation(annotationType);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
        throw new UnsupportedOperationException("accept");
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        throw new UnsupportedOperationException("getAnnotationsByType");
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        throw new UnsupportedOperationException("getTypeParameters");
    }

    @Override
    public TypeMirror getReturnType() {
        throw new UnsupportedOperationException("getReturnType");
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        List<VariableElement> parameters = new ArrayList<>();
        for (int i = 0; i < method.getParameterTypes().length; i++)
            parameters.add(new ReflectionParameterVariableElement(method, i));
        return parameters;
    }

    @Override
    public TypeMirror getReceiverType() {
        throw new UnsupportedOperationException("getReceiverType");
    }

    @Override
    public boolean isVarArgs() {
        return method.isVarArgs();
    }

    @Override
    public boolean isDefault() {
        return method.isDefault();
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        throw new UnsupportedOperationException("geThrownTypes");
    }

    @Override
    public AnnotationValue getDefaultValue() {
        throw new UnsupportedOperationException("getDefaultValue");
    }

    @Override
    public Name getSimpleName() {
        return new ReflectionName(method.getName());
    }
}

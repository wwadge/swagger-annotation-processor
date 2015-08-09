package com.github.t1.swap;

import static java.util.Collections.*;
import static javax.lang.model.element.ElementKind.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

public class MockParameterVariableElement implements VariableElement {
    private final Method method;
    private final int paramIndex;

    public MockParameterVariableElement(Method method, int paramIndex) {
        this.method = method;
        this.paramIndex = paramIndex;
    }

    @Override
    public TypeMirror asType() {
        throw new UnsupportedOperationException("asType");
    }

    @Override
    public ElementKind getKind() {
        return PARAMETER;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        throw new UnsupportedOperationException("getAnnotationMirrors");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        for (Annotation annotation : method.getParameterAnnotations()[paramIndex])
            if (annotationType.isInstance(annotation))
                return (A) annotation;
        return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        throw new UnsupportedOperationException("getAnnotationsByType");
    }

    @Override
    public Set<Modifier> getModifiers() {
        throw new UnsupportedOperationException("getModifiers");
    }

    @Override
    public Name getSimpleName() {
        return new MockName("arg" + paramIndex);
    }

    @Override
    public Element getEnclosingElement() {
        throw new UnsupportedOperationException("getEnclosingElement");
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return emptyList();
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> visitor, P param) {
        return visitor.visitVariable(this, param);
    }

    @Override
    public Object getConstantValue() {
        throw new UnsupportedOperationException("getConstantValue");
    }
}

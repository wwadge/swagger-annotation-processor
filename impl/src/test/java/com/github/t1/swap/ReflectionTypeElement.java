package com.github.t1.swap;

import static javax.lang.model.element.Modifier.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;

public class ReflectionTypeElement implements TypeElement {
    private final Class<?> type;

    public ReflectionTypeElement(Class<?> type) {
        this.type = type;
    }

    @Override
    public TypeMirror asType() {
        throw new UnsupportedOperationException("asType");
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.CLASS;
    }

    @Override
    public Set<Modifier> getModifiers() {
        EnumSet<Modifier> result = EnumSet.noneOf(Modifier.class);
        if (java.lang.reflect.Modifier.isPublic(type.getModifiers()))
            result.add(PUBLIC);
        if (java.lang.reflect.Modifier.isProtected(type.getModifiers()))
            result.add(PROTECTED);
        if (java.lang.reflect.Modifier.isPrivate(type.getModifiers()))
            result.add(PRIVATE);
        if (java.lang.reflect.Modifier.isAbstract(type.getModifiers()))
            result.add(ABSTRACT);
        // DEFAULT is not applicable to types
        if (java.lang.reflect.Modifier.isStatic(type.getModifiers()))
            result.add(STATIC);
        if (java.lang.reflect.Modifier.isFinal(type.getModifiers()))
            result.add(FINAL);
        if (java.lang.reflect.Modifier.isTransient(type.getModifiers()))
            result.add(TRANSIENT);
        if (java.lang.reflect.Modifier.isVolatile(type.getModifiers()))
            result.add(VOLATILE);
        if (java.lang.reflect.Modifier.isSynchronized(type.getModifiers()))
            result.add(SYNCHRONIZED);
        if (java.lang.reflect.Modifier.isNative(type.getModifiers()))
            result.add(NATIVE);
        if (java.lang.reflect.Modifier.isStrict(type.getModifiers()))
            result.add(STRICTFP);
        return result;
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
        throw new UnsupportedOperationException("getAnnotationMirrors");
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return type.getAnnotation(annotationType);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> visitor, P param) {
        R response = null;
        for (Method method : type.getDeclaredMethods()) {
            response = visitor.visitExecutable(new ReflectionExecutableElement(method), param);
        }
        return response;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
        throw new UnsupportedOperationException("getAnnotationsByType");
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        List<ExecutableElement> methods = new java.util.ArrayList<>();
        for (Method method : type.getDeclaredMethods())
            methods.add(new ReflectionExecutableElement(method));
        return methods;
    }

    @Override
    public NestingKind getNestingKind() {
        throw new UnsupportedOperationException("getNestingType");
    }

    @Override
    public Name getQualifiedName() {
        return new ReflectionName(type.getName());
    }

    @Override
    public Name getSimpleName() {
        return new ReflectionName(type.getSimpleName());
    }

    @Override
    public TypeMirror getSuperclass() {
        throw new UnsupportedOperationException("getSuperclass");
    }

    @Override
    public List<? extends TypeMirror> getInterfaces() {
        throw new UnsupportedOperationException("getInterfaces");
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        throw new UnsupportedOperationException("getTypeParameters");
    }

    @Override
    public Element getEnclosingElement() {
        throw new UnsupportedOperationException("getEnclosingElement");
    }
}

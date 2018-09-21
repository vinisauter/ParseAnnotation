package com.parse.annotation.processor;

import com.squareup.javapoet.ClassName;

import java.text.MessageFormat;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by user on 20/09/2018.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class Utils {
    public static PackageElement getPackage(Element element) {
        while (element.getKind() != ElementKind.PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element;
    }

    public static TypeElement findEnclosingTypeElement(Element e) {
        while (e != null && !(e instanceof TypeElement)) {
            e = e.getEnclosingElement();
        }
        return TypeElement.class.cast(e);
    }

    public static String getCanonicalName(TypeMirror typeMirror) {
        if (!(typeMirror instanceof DeclaredType)) {
            return null;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return null;
        }
//        TypeElement typeElement = (TypeElement) element;
//        List<? extends TypeParameterElement> typeParameterElements = typeElement.getTypeParameters();
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (!typeArguments.isEmpty()) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');

            return typeString.toString();
        } else {
            return declaredType.toString();
        }
    }

    public static TypeElement getSuperClass(TypeElement typeElement) {
        if (!(typeElement.getSuperclass() instanceof DeclaredType)) return null;
        DeclaredType declaredAncestor = (DeclaredType) typeElement.getSuperclass();
        return (TypeElement) declaredAncestor.asElement();
    }

    public static TypeElement instanceOf(TypeElement typeElement, ClassName className) {
        TypeElement superclass = getSuperClass(typeElement);
        System.out.printf(MessageFormat.format(" -> {0}", superclass));
        if (superclass != null) {
            if (superclass.toString().equals(className.toString())) {
                return superclass;
            } else {
                return instanceOf(superclass, className);
            }
        }
        return null;
    }
}

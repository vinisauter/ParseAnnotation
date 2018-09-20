package com.parse.annotation.processor;

import com.parse.annotation.BindParseObject;
import com.parse.annotation.Ignore;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by user on 20/09/2018.
 */

public class ParseObjectGenerator {
    static void generateClass(Filer filer, Element elementBase, ClassName className) throws IOException {
        String pack = Utils.getPackage(elementBase).toString();
        String name = elementBase.getSimpleName().toString();
        boolean abstractClass = elementBase.getModifiers().contains(ABSTRACT);
        BindParseObject annotation = elementBase.getAnnotation(BindParseObject.class);
        String value = annotation.value();
        if ("".equals(value)) {
            value = name;
        }
        System.out.printf(MessageFormat.format(
                "\nElement{3} class: {0}.{1} value: {2} {4} ",
                pack,
                name,
                value,
                abstractClass ? " abstract" : "",
                elementBase.getKind()
        ));

        TypeElement typeElement = (TypeElement) elementBase;
        if (elementBase.getKind() != CLASS) {
            throw new IOException("Can only be applied to class.");
        }

        if (elementBase.getModifiers().contains(PRIVATE)) {
            throw new IOException(MessageFormat.format("{0} {1} may not be applied to private classes. ({2})", BindParseObject.class.getSimpleName(), typeElement.getQualifiedName(), elementBase.getSimpleName()));
        }

        // 2- Generate a class
        TypeMirror type = elementBase.asType();

        TypeSpec.Builder navigatorClass = TypeSpec
                .classBuilder(className)
                .addAnnotation(
                        AnnotationSpec.builder(ClassName.get("com.parse", "ParseClassName"))
                                .addMember("value", "$S", value)
                                .build())
                .superclass(TypeName.get(type))
                .addModifiers(PUBLIC);
        if (abstractClass) {
            navigatorClass.addModifiers(ABSTRACT);
        }

        FieldSpec staticClassName = FieldSpec.builder(String.class, "CLASS_NAME")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", value)
                .build();
        navigatorClass.addField(staticClassName);

        MethodSpec create = MethodSpec
                .methodBuilder("create")
                .addModifiers(PUBLIC, STATIC)
                .returns(className)
                .addStatement("return ($N) create(CLASS_NAME)", className.simpleName())
                .build();
        navigatorClass.addMethod(create);

        MethodSpec instance = MethodSpec
                .methodBuilder("createWithoutData")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(String.class, "objectId")
                .returns(className)
                .addStatement("return ($N) createWithoutData(CLASS_NAME, objectId)", className.simpleName())
                .build();
        navigatorClass.addMethod(instance);
//        public static SampleObject_ fetch(String objectId, com.parse.GetCallback<SampleObject_> callback) {
//            SampleObject_ object = createWithoutData(objectId);
//            object.fetchIfNeededInBackground(callback);
//            return object;
//        }

        ClassName classGetCallback = ClassName.get("com.parse", "GetCallback");
        TypeName callbackOfClass = ParameterizedTypeName.get(classGetCallback, className);
        MethodSpec fetch = MethodSpec
                .methodBuilder("fetch")
                .addModifiers(PUBLIC, STATIC)
                .addParameter(String.class, "objectId")
                .addParameter(callbackOfClass, "callback")
                .returns(className)
                .addStatement("$N object = createWithoutData(objectId)", className.simpleName())
                .addStatement("object.fetchIfNeededInBackground(callback)")
                .addStatement("return object")
                .build();
        navigatorClass.addMethod(fetch);

        ClassName classQuery = ClassName.get("com.parse", "ParseQuery");
        TypeName queryOfClass = ParameterizedTypeName.get(classQuery, className);
        MethodSpec query = MethodSpec
                .methodBuilder("query")
                .addModifiers(PUBLIC, STATIC)
                .returns(queryOfClass)
                .addStatement("return ParseQuery.getQuery(CLASS_NAME)")
                .build();
        navigatorClass.addMethod(query);

        for (Element elementEnclosed : elementBase.getEnclosedElements()) {
            ElementKind fieldKind = elementEnclosed.getKind();
            Set<Modifier> fieldModifiers = elementEnclosed.getModifiers();
            Ignore ignore = elementEnclosed.getAnnotation(Ignore.class);
            System.out.printf(MessageFormat.format(
                    "\n    EnclosedElement {0} {1} {2} {3} {4}",
                    Arrays.toString(fieldModifiers.toArray()),
                    ignore != null ? "ignore" : "",
                    fieldKind,
                    elementEnclosed.getSimpleName().toString(),
                    elementEnclosed.asType()
            ));

            if (elementEnclosed.getKind() == ElementKind.FIELD && ignore == null) {
                generateField(elementEnclosed, className, navigatorClass);
            }
        }
        MethodSpec.Builder toString = MethodSpec.methodBuilder("toString")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("String oId = $N", "getObjectId()")
                .addStatement("return super.getClassName() + \"{ objectId=\" + oId + \" }\" ")
                .returns(String.class);
        navigatorClass.addMethod(toString.build());

        // 3- Write generated class to a file
        JavaFile.builder(pack, navigatorClass.build()).build().writeTo(filer);
        System.out.printf("\n\n");
    }

    private static void generateField(Element elementEnclosed, ClassName className, TypeSpec.Builder navigatorClass) throws IOException {
        String fieldName = elementEnclosed.getSimpleName().toString();
        TypeMirror typeEnclosed = elementEnclosed.asType();
        String typeName = typeEnclosed.toString();

        String staticFieldName = fieldName.replaceAll("(.)(\\p{Upper})", "$1_$2").toUpperCase();
        FieldSpec staticField = FieldSpec.builder(String.class, staticFieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", fieldName)
                .build();
        navigatorClass.addField(staticField);

        MethodSpec.Builder getMethod = MethodSpec
                .methodBuilder("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1))
                .addModifiers(PUBLIC)
                .returns(TypeName.get(typeEnclosed));
        String cName = Utils.getCanonicalName(typeEnclosed);
        if ("com.parse.ParseRelation<?>".equals(cName)) {
            getMethod.addStatement("super.$N = getRelation($N)", fieldName, staticFieldName);
        } else if ("java.util.List<?>".equals(cName)) {
            getMethod.addStatement("super.$N = getList($N)", fieldName, staticFieldName);
        } else if ("java.util.Map<?,?>".equals(cName)) {
            getMethod.addStatement("super.$N = getMap($N)", fieldName, staticFieldName);
        } else if ("org.json.JSONObject".equals(cName)) {
            getMethod.addStatement("super.$N = getJSONObject($N)", fieldName, staticFieldName);
        } else if ("org.json.JSONArray".equals(cName)) {
            getMethod.addStatement("super.$N = getJSONArray($N)", fieldName, staticFieldName);
        } else {
            getMethod.addStatement("super.$N = ($N) get($N)", fieldName, typeName, staticFieldName);
        }
        getMethod.addStatement("return super.$N", fieldName);
        navigatorClass.addMethod(getMethod.build());

        MethodSpec.Builder setMethod = MethodSpec
                .methodBuilder("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1))
                .addModifiers(PUBLIC)
                .returns(className)
                .addParameter(TypeName.get(typeEnclosed), fieldName)
                .addStatement("super.$N = $N", fieldName, fieldName)
                .addStatement("put($N, $N)", staticFieldName, fieldName)
                .addStatement("return this");
        navigatorClass.addMethod(setMethod.build());

    }
}

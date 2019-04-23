package com.parse.annotation.processor;

import com.google.common.base.CaseFormat;
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
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import static com.parse.annotation.processor.Utils.logInfo;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

/**
 * Created by user on 20/09/2018.
 */

public class ParseObjectGenerator {
    static void generateClass(ProcessingEnvironment processingEnv, Filer filer, Element elementBase, ClassName className) throws IOException {
        String pack = Utils.getPackage(elementBase).toString();
        String name = elementBase.getSimpleName().toString();
        boolean abstractClass = elementBase.getModifiers().contains(ABSTRACT);
        BindParseObject annotation = elementBase.getAnnotation(BindParseObject.class);
        String value = annotation.value();
        com.parse.annotation.CaseFormat caseFormat = annotation.columnCaseFormat();
        if ("".equals(value)) {
            value = name;
        }
        TypeElement typeElement = (TypeElement) elementBase;
        logInfo(MessageFormat.format(
                "{0}.{1}",
                pack,
                name
        ));

        Set<Modifier> baseModifiers = elementBase.getModifiers();
        ClassName parseObjectClassName = ClassName.get("com.parse", "ParseObject");
        logInfo(MessageFormat.format(
                "Element {0} {1} {2} ({3}, {4}) {5}.{6} {7}",
                elementBase.getKind(),
                abstractClass ? "abstract" : "-",
                Arrays.toString(baseModifiers.toArray()),
                value,
                caseFormat,
                pack,
                name,
                Arrays.toString(typeElement.getInterfaces().toArray())
        ));
        boolean isParseObject = Utils.instanceOf(typeElement, "com.parse.ParseObject");
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
                                .build());
        if (isParseObject)
            navigatorClass.superclass(TypeName.get(type));
        else
            navigatorClass.superclass(parseObjectClassName);
        navigatorClass.addModifiers(PUBLIC);
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
            Ignore ignoreAnnotation = elementEnclosed.getAnnotation(Ignore.class);
            boolean canGenerate = ignoreAnnotation == null && !elementEnclosed.getModifiers().contains(PRIVATE);
            logInfo(MessageFormat.format(
                    "    EnclosedElement {0} {1} {2} {3} {4} {5}",
                    canGenerate ? "generate" : "ignore",
                    fieldKind,
                    Arrays.toString(fieldModifiers.toArray()),
                    elementEnclosed.getSimpleName().toString(),
                    elementEnclosed.asType(),
                    elementEnclosed.asType().getKind().isPrimitive() ? "primitive" : ""
            ));

            if (elementEnclosed.getKind() == ElementKind.FIELD && canGenerate) {
                generateField(elementEnclosed, navigatorClass, className, caseFormat, isParseObject);
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
    }

    /**
     * public Object get(String key)
     * public String getString(String key)
     * public byte[] getBytes(String key)
     * public Number getNumber(String key)
     * public JSONArray getJSONArray(String key)
     * public <T> List<T> getList(String key)
     * public <V> Map<String, V> getMap(String key)
     * public JSONObject getJSONObject(String key)
     * public int getInt(String key)
     * public double getDouble(String key)
     * public long getLong(String key)
     * public boolean getBoolean(String key)
     * public Date getDate(String key)
     * public ParseObject getParseObject(String key)
     * public ParseUser getParseUser(String key)
     * public ParseFile getParseFile(String key)
     * public ParseGeoPoint getParseGeoPoint(String key)
     * public ParsePolygon getParsePolygon(String key)
     * public <T extends ParseObject> ParseRelation<T> getRelation(String key)
     */
    private static void generateField(Element elementEnclosed, TypeSpec.Builder navigatorClass, ClassName className, com.parse.annotation.CaseFormat caseFormat, boolean isParseObject) throws IOException {
        String fieldName = elementEnclosed.getSimpleName().toString();
        TypeMirror typeMirror = elementEnclosed.asType();
        String typeName = typeMirror.toString();

        String staticFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);

        String formattedFieldName;
        switch (caseFormat) {
            case NONE:
                formattedFieldName = fieldName;
                break;
            case LOWER_UNDERSCORE:
                formattedFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
                break;
            case LOWER_CAMEL:
                formattedFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_CAMEL, fieldName);
                break;
            case UPPER_CAMEL:
                formattedFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldName);
                break;
            case UPPER_UNDERSCORE:
                formattedFieldName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
                break;
            default:
                formattedFieldName = fieldName;
                break;
        }

        FieldSpec staticField = FieldSpec.builder(String.class, staticFieldName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", formattedFieldName)
                .build();
        navigatorClass.addField(staticField);

        MethodSpec.Builder getMethod = MethodSpec
                .methodBuilder("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1))
                .addModifiers(PUBLIC)
                .returns(TypeName.get(typeMirror));
        String cName = Utils.getCanonicalName(typeMirror);
        if ("com.parse.ParseRelation<?>".equals(cName)) {
            getMethod.addStatement("$N value = getRelation($N)", typeName, staticFieldName);
        } else if ("java.util.List<?>".equals(cName)) {
            getMethod.addStatement("$N value = getList($N)", typeName, staticFieldName);
        } else if ("java.util.Map<?,?>".equals(cName)) {
            getMethod.addStatement("$N value = getMap($N)", typeName, staticFieldName);
        } else if ("org.json.JSONObject".equals(cName)) {
            getMethod.addStatement("$N value = getJSONObject($N)", typeName, staticFieldName);
        } else if ("org.json.JSONArray".equals(cName)) {
            getMethod.addStatement("$N value = getJSONArray($N)", typeName, staticFieldName);
        } else {
            if (typeMirror.getKind().isPrimitive()) {
                getMethod.addStatement("Object value = get($N)", staticFieldName);
                getMethod.beginControlFlow("if (value == null)", typeName);
                switch (typeName) {
                    case "byte":
//                        Class<Byte> clazz = Primitives.wrap(byte.class);
                    case "short":
//                        Class<Short> clazz = Primitives.wrap(short.class);
                    case "int":
//                        Class<Integer> clazz = Primitives.wrap(int.class);
                        getMethod.addStatement("return 0");
                        break;
                    case "long":
//                        Class<Long> clazz = Primitives.wrap(long.class);
                        getMethod.addStatement("return 0l");
                        break;
                    case "float":
//                        Class<Float> clazz = Primitives.wrap(float.class);
                        getMethod.addStatement("return 0.0f");
                        break;
                    case "double":
//                        Class<Double> clazz = Primitives.wrap(double.class);
                        getMethod.addStatement("return 0.0d");
                        break;
                    case "char":
//                        Class<Character> clazz = Primitives.wrap(char.class);
                        getMethod.addStatement("return \t'\\u0000'");
                        break;
                    case "boolean":
//                        Class<Boolean> clazz = Primitives.wrap(boolean.class);
                        getMethod.addStatement("return false");
                        break;
                    default:
                        getMethod.addStatement("return null");
                }
                getMethod.endControlFlow();
            } else {
                getMethod.addStatement("Object value = get($N)", staticFieldName);
                getMethod.beginControlFlow("if (!(value instanceof $N))", typeName);
                getMethod.addStatement("return null");
                getMethod.endControlFlow();
            }
        }
        if (isParseObject) {
            getMethod.addStatement("super.$N = ($N) value", fieldName, typeName);
        }
        getMethod.addStatement("return ($N) value", typeName);

        navigatorClass.addMethod(getMethod.build());

        MethodSpec.Builder setMethod = MethodSpec
                .methodBuilder("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1))
                .addModifiers(PUBLIC)
                .returns(className)
                .addParameter(TypeName.get(typeMirror), fieldName);

        if (isParseObject)
            setMethod.addStatement("super.$N = $N", fieldName, fieldName);

        setMethod.addStatement("put($N, $N)", staticFieldName, fieldName)
                .addStatement("return this");
        navigatorClass.addMethod(setMethod.build());

    }
}

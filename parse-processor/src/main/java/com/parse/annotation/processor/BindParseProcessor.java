package com.parse.annotation.processor;

import com.parse.annotation.BindParseObject;
import com.parse.annotation.Ignore;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

public class BindParseProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elements;
    private SourceVersion sourceVersion;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
        sourceVersion = processingEnvironment.getSourceVersion();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean claimed = (annotations.size() > 0);
        if (claimed) {
            process(roundEnv);
            return true;
        } else {
            return false;
        }
    }

    private void process(RoundEnvironment roundEnvironment) {
        System.out.printf("\n-----------BindParseObject_START-----------");
        Set<ClassName> classNames = new HashSet<>();
        // 1- Find all annotated element
        for (Element elementBase : roundEnvironment.getElementsAnnotatedWith(BindParseObject.class)) {
            String pack = Utils.getPackage(elementBase).toString();
            String name = elementBase.getSimpleName().toString();
            try {
                String generatedClassName = name + "_";
                ClassName className = ClassName.get(pack, generatedClassName);
                classNames.add(className);
                // 2- Generate a class
                ParseObjectGenerator.generateClass(processingEnv, filer, elementBase, className);
            } catch (FilerException ignore) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.printf("\n------------BindParseObject_END------------");

        try {
            TypeSpec.Builder appClass = TypeSpec
                    .classBuilder("ParseInit")
                    .addModifiers(PUBLIC);
            MethodSpec.Builder register = MethodSpec
                    .methodBuilder("registerSubclasses")
                    .addModifiers(PUBLIC, STATIC, FINAL);
            for (ClassName className : classNames) {
                register.addStatement("com.parse.ParseObject.registerSubclass($T.class)", className);
            }
            appClass.addMethod(register.build());

            appClass.addMethod(MethodSpec
                    .methodBuilder("initialize")
                    .addModifiers(PUBLIC, STATIC)
                    .addParameter(ClassName.get("com.parse.Parse", "Configuration"), "configuration")
                    .addStatement("registerSubclasses()")
                    .addStatement("com.parse.Parse.initialize(configuration)")
                    .build());
            JavaFile.builder("com.parse.annotation", appClass.build()).build().writeTo(filer);
        } catch (FilerException ignore) {
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.printf("\n");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        Collections.addAll(types,
                BindParseObject.class.getCanonicalName(),
                Ignore.class.getCanonicalName()
        );
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}

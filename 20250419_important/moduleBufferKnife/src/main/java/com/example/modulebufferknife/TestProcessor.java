package com.example.modulebufferknife;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.annotation.processing.Processor;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8) // 添加源版本支持
public class TestProcessor extends AbstractProcessor {

    private boolean fileGenerated = false; // 防止重复生成

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        env.getMessager().printMessage(Diagnostic.Kind.NOTE, "TestProcessor PROCESSOR INIT");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "TEST PROCESSOR RUNNING");

        // 如果已经生成过文件，跳过
        if (fileGenerated) {
            return true;
        }

        try {
            TypeSpec testClass = TypeSpec.classBuilder("TestProcessorTest")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(MethodSpec.methodBuilder("test")
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(String.class)
                            .addStatement("return $S", "Processor is working!")
                            .build())
                    .build();

            JavaFile.builder("com.example.generated", testClass)
                    .build()
                    .writeTo(processingEnv.getFiler());

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Generated test file");
            fileGenerated = true; // 标记为已生成

        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Failed to generate file: " + e);
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {// 这个错了
        return Set.of("start");// 添加其他需要处理的注解"); // 处理所有注解
    }
}
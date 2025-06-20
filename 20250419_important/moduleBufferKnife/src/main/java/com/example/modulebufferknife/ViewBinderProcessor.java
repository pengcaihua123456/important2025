package com.example.modulebufferknife;

import com.example.modulebufferknifeantotations.BindView;
import com.example.modulebufferknifeantotations.OnClick;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ViewBinderProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Messager messager;
    private Filer filer;
    private boolean processed = false;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        messager = env.getMessager();
        filer = env.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        // 防止重复处理
        if (processed || annotations.isEmpty()) {
            return false;
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "ViewBinderProcessor 开始处理");

        try {
            // 收集所有使用@BindView的元素
            Map<TypeElement, List<Element>> bindViewMap = new HashMap<>();
            for (Element element : env.getElementsAnnotatedWith(BindView.class)) {
                if (element.getKind() != ElementKind.FIELD) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "@BindView 只能用于字段", element);
                    continue;
                }

                TypeElement enclosingClass = (TypeElement) element.getEnclosingElement();
                bindViewMap.computeIfAbsent(enclosingClass, k -> new ArrayList<>()).add(element);

                messager.printMessage(Diagnostic.Kind.NOTE, "找到 @BindView: " +
                        enclosingClass.getSimpleName() + "." + element.getSimpleName());
            }

            // 收集所有使用@OnClick的元素
            Map<TypeElement, List<Element>> onClickMap = new HashMap<>();
            for (Element element : env.getElementsAnnotatedWith(OnClick.class)) {
                if (element.getKind() != ElementKind.METHOD) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "@OnClick 只能用于方法", element);
                    continue;
                }

                TypeElement enclosingClass = (TypeElement) element.getEnclosingElement();
                onClickMap.computeIfAbsent(enclosingClass, k -> new ArrayList<>()).add(element);

                messager.printMessage(Diagnostic.Kind.NOTE, "找到 @OnClick: " +
                        enclosingClass.getSimpleName() + "." + element.getSimpleName());
            }

            // 为每个类生成绑定类
            for (TypeElement enclosingClass : bindViewMap.keySet()) {
                generateBinderClass(
                        enclosingClass,
                        bindViewMap.get(enclosingClass),
                        onClickMap.getOrDefault(enclosingClass, Collections.emptyList())
                );
            }

            processed = true;
            messager.printMessage(Diagnostic.Kind.NOTE, "ViewBinderProcessor 完成处理");
            return true;

        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "处理失败: " + e);
            e.printStackTrace();
            return false;
        }
    }

    private void generateBinderClass(TypeElement enclosingClass,
                                     List<Element> bindViewElements,
                                     List<Element> onClickElements) throws IOException {
        // 获取包名和类名
        String packageName = elementUtils.getPackageOf(enclosingClass).getQualifiedName().toString();
        String className = enclosingClass.getSimpleName().toString();
        String binderClassName = className + "_ViewBinder";

        messager.printMessage(Diagnostic.Kind.NOTE, "生成: " + packageName + "." + binderClassName);

        // 创建绑定方法
        MethodSpec.Builder bindMethodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ClassName.get(enclosingClass), "target");

        // 处理@BindView
        for (Element element : bindViewElements) {
            String fieldName = element.getSimpleName().toString();
            int viewId = element.getAnnotation(BindView.class).value();
            String viewType = element.asType().toString(); // 使用字符串形式

            bindMethodBuilder.addStatement("$T view_$L = target.findViewById($L)",
                            ClassName.bestGuess(viewType), fieldName, viewId)
                    .addStatement("target.$L = view_$L", fieldName, fieldName);
        }

        // 处理@OnClick
        for (Element element : onClickElements) {
            ExecutableElement method = (ExecutableElement) element;
            int viewId = element.getAnnotation(OnClick.class).value();
            String methodName = method.getSimpleName().toString();

            // 创建监听器
            bindMethodBuilder.addStatement("target.findViewById($L).setOnClickListener(v -> target.$L(v))",
                    viewId, methodName);
        }

        // 构建类
        TypeSpec binderClass = TypeSpec.classBuilder(binderClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(bindMethodBuilder.build())
                .build();

        // 写入文件
        JavaFile.builder(packageName, binderClass)
                .build()
                .writeTo(filer);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                "com.example.modulebufferknifeantotations.BindView",
                "com.example.modulebufferknifeantotations.OnClick"
        );
    }
}

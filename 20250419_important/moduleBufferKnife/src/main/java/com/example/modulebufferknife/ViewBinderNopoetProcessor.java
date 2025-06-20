package com.example.modulebufferknife;

import com.example.modulebufferknifeantotations.BindView;
import com.example.modulebufferknifeantotations.OnClick;
import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.Writer;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;



/**
 * @Author pengcaihua
 * @Date 11:14
 * @describe  没有使用javapoet的处理器
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ViewBinderNopoetProcessor extends AbstractProcessor {

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
        if (processed || annotations.isEmpty()) return false;

        messager.printMessage(Diagnostic.Kind.NOTE, "ViewBinderNopoetProcessor 开始处理");

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
            messager.printMessage(Diagnostic.Kind.NOTE, "ViewBinderNopoetProcessor 完成处理");
            return true;
        } catch (Exception e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "处理失败: " + e);
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

        // 创建Java源文件
        JavaFileObject file = filer.createSourceFile(packageName + "." + binderClassName);
        messager.printMessage(Diagnostic.Kind.NOTE, "生成: " + file.getName());

        try (Writer writer = file.openWriter()) {
            // ============== 开始拼接Java文件内容 ==============
            // 1. 包声明
            writer.write("package " + packageName + ";\n\n");

            // 2. 导入必要的类
            writer.write("import android.view.View;\n");
            writer.write("import " + enclosingClass.getQualifiedName() + ";\n\n");

            // 3. 类声明
            writer.write("public final class " + binderClassName + " {\n");
            writer.write("    public static void bind(" + className + " target) {\n");

            // 4. 处理@BindView字段绑定
            for (Element element : bindViewElements) {
                String fieldName = element.getSimpleName().toString();
                int viewId = element.getAnnotation(BindView.class).value();
                String fieldType = element.asType().toString();

                writer.write("        target." + fieldName + " = (" + fieldType + ") target.findViewById(" + viewId + ");\n");
            }

            // 5. 处理@OnClick点击事件
            for (Element element : onClickElements) {
                String methodName = element.getSimpleName().toString();
                int viewId = element.getAnnotation(OnClick.class).value();

                writer.write("        target.findViewById(" + viewId + ").setOnClickListener(new View.OnClickListener() {\n");
                writer.write("            @Override\n");
                writer.write("            public void onClick(View v) {\n");
                writer.write("                target." + methodName + "(v);\n");
                writer.write("            }\n");
                writer.write("        });\n");
            }

            // 6. 关闭方法体和类体
            writer.write("    }\n");
            writer.write("}\n");
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                "com.example.modulebufferknifeantotations.BindView",
                "com.example.modulebufferknifeantotations.OnClick"
        );
    }
}

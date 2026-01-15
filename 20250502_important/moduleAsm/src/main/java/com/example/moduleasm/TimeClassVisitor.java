package com.example.moduleasm;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TimeClassVisitor extends ClassVisitor {
    private String className;
    private boolean isApplicationClass = false;
    private boolean isSkippedClass = false;

    private final Logger logger = Logging.getLogger(TimePlugin.class);

    // 需要完全跳过的类名单
    private static final Set<String> FULLY_EXCLUDED_CLASSES = new HashSet<>(Arrays.asList(
            "android/",
            "androidx/",
            "java/",
            "javax/",
            "kotlin/",
            "org/",
            "com/google/",
            "com/evenbus/myapplication/MatrixApplication",
            "com/evenbus/myapplication/BuildConfig"
    ));

    // 需要跳过插桩但允许通过的其他类
    private static final Set<String> SKIP_INSTRUMENTATION_CLASSES = new HashSet<>(Arrays.asList(
            "com/example/thirdparty/"
    ));

    public TimeClassVisitor(ClassVisitor classVisitor) {
        super(Opcodes.ASM7, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        this.className = name;
        this.isApplicationClass = isApplicationClass(superName);
        this.isSkippedClass = shouldFullySkipClass(name);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (isSkippedClass || shouldSkipMethod(access, name, descriptor)) {
            return mv;
        }

        logger.lifecycle("visitMethod");


        return new TimeMethodVisitor(
                mv,
                className.replace('/', '.'),
                name,
                descriptor,
                access
        );
    }

    // ========== 安全判断方法 ========== //

    private boolean isApplicationClass(String superName) {
        return "android/app/Application".equals(superName) ||
                "androidx/multidex/MultiDexApplication".equals(superName);
    }

    private boolean shouldFullySkipClass(String className) {
        for (String excluded : FULLY_EXCLUDED_CLASSES) {
            if (className.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }

    private boolean shouldSkipMethod(int access, String methodName, String descriptor) {
        // 跳过构造方法和静态初始化块
        if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
            return true;
        }

        // 跳过Application类及其子类的方法
        if (isApplicationClass) {
            return true;
        }

        // 跳过native/abstract/synthetic方法
        if ((access & Opcodes.ACC_NATIVE) != 0 ||
                (access & Opcodes.ACC_ABSTRACT) != 0 ||
                (access & Opcodes.ACC_SYNTHETIC) != 0) {
            return true;
        }

        // 跳过特定包的方法
        for (String skipClass : SKIP_INSTRUMENTATION_CLASSES) {
            if (className.startsWith(skipClass)) {
                return true;
            }
        }
        logger.lifecycle("shouldSkipMethod");

        return false;
    }

    @Override
    public void visitEnd() {
        logger.lifecycle("visitEnd");
        super.visitEnd();
    }
}
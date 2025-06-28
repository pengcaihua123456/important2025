package com.example.moduleasm;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeMethodVisitor extends AdviceAdapter {
    private final String methodName;
    private final String className;
    private int startTimeVarIndex;
    private final List<LocalVariableInfo> pendingLocals = new ArrayList<>();
    private Label methodStartLabel;
    private Label methodEndLabel;
    private final AtomicInteger varCounter = new AtomicInteger(0);

    private final Logger logger = Logging.getLogger(TimePlugin.class);

    private static class LocalVariableInfo {
        String name;
        String desc;
        int index;
        Label start;
        Label end;
    }

    public TimeMethodVisitor(MethodVisitor mv,
                             String className,
                             String methodName,
                             String descriptor,
                             int access) {
        super(Opcodes.ASM7, mv, access, methodName, descriptor); // 升级到ASM9
        this.className = className;
        this.methodName = methodName;
    }

    @Override
    protected void onMethodEnter() {
        methodStartLabel = new Label();
        visitLabel(methodStartLabel);

        // 生成唯一变量名
        startTimeVarIndex = newUniqueLocal(Type.LONG_TYPE, "startTime");

        // 插入: long startTime_<uid> = System.currentTimeMillis();
        visitMethodInsn(
                INVOKESTATIC,
                "java/lang/System",
                "currentTimeMillis",
                "()J",
                false
        );
        storeLocal(startTimeVarIndex, Type.LONG_TYPE);
    }

    @Override
    protected void onMethodExit(int opcode) {
        methodEndLabel = new Label();
        visitLabel(methodEndLabel);

        if (opcode != ATHROW) {
            // 生成唯一变量名
            int endTimeVarIndex = newUniqueLocal(Type.LONG_TYPE, "endTime");

            // 插入: long endTime_<uid> = System.currentTimeMillis();
            visitMethodInsn(
                    INVOKESTATIC,
                    "java/lang/System",
                    "currentTimeMillis",
                    "()J",
                    false
            );
            storeLocal(endTimeVarIndex, Type.LONG_TYPE);

            // 计算耗时并输出日志
            printDuration(endTimeVarIndex, startTimeVarIndex);
        }
    }

    /**
     * 创建唯一命名的局部变量
     */
    private int newUniqueLocal(Type type, String prefix) {
        int index = super.newLocal(type);
        String uniqueName = prefix + "_" + varCounter.getAndIncrement();

        LocalVariableInfo local = new LocalVariableInfo();
        local.name = uniqueName;
        local.desc = type.getDescriptor();
        local.index = index;
        local.start = methodStartLabel;
        local.end = methodEndLabel;
        pendingLocals.add(local);

        return index;
    }

    /**
     * 优化的日志输出方法
     */
    private void printDuration(int endVarIndex, int startVarIndex) {
        // 计算耗时 duration = endTime - startTime
        int durationVarIndex = newUniqueLocal(Type.LONG_TYPE, "duration");
        loadLocal(endVarIndex, Type.LONG_TYPE);
        loadLocal(startVarIndex, Type.LONG_TYPE);
        visitInsn(LSUB);
        storeLocal(durationVarIndex, Type.LONG_TYPE);

        // 构建日志信息
        visitLdcInsn("TimeTrace"); // Log tag

        // 使用StringBuilder优化字符串拼接
        visitTypeInsn(NEW, "java/lang/StringBuilder");
        visitInsn(DUP);
        visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);

        // 拼接方法信息
        visitLdcInsn("Method [" + className.replace('/', '.') + "." + methodName + "] took ");
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        // 拼接耗时
        loadLocal(durationVarIndex, Type.LONG_TYPE);
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                "(J)Ljava/lang/StringBuilder;", false);

        // 拼接单位
        visitLdcInsn("ms");
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);

        // 生成最终字符串
        visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
                "()Ljava/lang/String;", false);

        // 调用 Log.d(tag, message)
        visitMethodInsn(INVOKESTATIC, "android/util/Log", "d",
                "(Ljava/lang/String;Ljava/lang/String;)I", false);
        visitInsn(POP); // 丢弃返回值
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        // 确保栈深度足够（StringBuilder操作需要更多栈空间）
        super.visitMaxs(Math.max(maxStack, 6), maxLocals);
    }

    @Override
    public void visitEnd() {
        if (methodEndLabel == null) {
            methodEndLabel = new Label();
            visitLabel(methodEndLabel);
        }

        // 注册所有局部变量
        for (LocalVariableInfo local : pendingLocals) {
            mv.visitLocalVariable(
                    local.name,
                    local.desc,
                    null,
                    local.start != null ? local.start : methodStartLabel,
                    local.end != null ? local.end : methodEndLabel,
                    local.index
            );
        }
        pendingLocals.clear();
        super.visitEnd();
    }
}
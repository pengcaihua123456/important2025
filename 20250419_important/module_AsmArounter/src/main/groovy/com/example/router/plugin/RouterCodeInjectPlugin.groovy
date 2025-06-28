package com.example.router.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import org.objectweb.asm.*
import org.apache.commons.io.FileUtils


import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class RouterCodeInjectPlugin implements Plugin<Project> {

    static final String TARGET_CLASS = "com/alibaba/android/arouter/core/LogisticsCenter"
    static final String TARGET_METHOD = "loadRouterMap"
    static final String TARGET_METHOD_DESC = "()V"
    static final String REGISTER_METHOD = "register"
    static final String REGISTER_METHOD_DESC = "(Ljava/lang/String;)V"

    @Override
    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        android.registerTransform(new RouterCodeTransform())
    }

    class RouterCodeTransform extends Transform {

        @Override
        String getName() {
            return "RouterCodeInject"
        }

        @Override
        Set<QualifiedContent.ContentType> getInputTypes() {
            return [QualifiedContent.DefaultContentType.CLASSES]
        }

        @Override
        Set<QualifiedContent.Scope> getScopes() {
            return [QualifiedContent.Scope.PROJECT,
                    QualifiedContent.Scope.SUB_PROJECTS,
                    QualifiedContent.Scope.EXTERNAL_LIBRARIES]
        }

        @Override
        boolean isIncremental() {
            return false
        }

        @Override
        void transform(Context context, Collection<TransformInput> inputs,
                       Collection<TransformInput> referencedInputs,
                       TransformOutputProvider outputProvider,
                       boolean isIncremental) throws IOException, TransformException, InterruptedException {

            inputs.each { TransformInput input ->
                // 处理目录中的class文件
                input.directoryInputs.each { DirectoryInput dirInput ->
                    File dest = outputProvider.getContentLocation(
                            dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)

                    // 遍历目录中的所有文件
                    dirInput.file.eachFileRecurse { File file ->
                        if (file.isFile() && file.name.endsWith(".class")) {
                            handleClassFile(file)
                        }
                    }

                    // 复制整个目录到输出位置
                    FileUtils.copyDirectory(dirInput.file, dest)
                }

                // 处理JAR文件
                input.jarInputs.each { JarInput jarInput ->
                    String destName = jarInput.name
                    File src = jarInput.file
                    File dest = outputProvider.getContentLocation(
                            destName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

                    // 处理JAR中的类文件
                    if (src.exists()) {
                        handleJarFile(src, dest)
                    }
                }
            }
        }

        private void handleClassFile(File classFile) {
            def className = classFile.name
            if (className == 'LogisticsCenter.class') {
                Logger.info("处理LogisticsCenter类文件: ${classFile.absolutePath}")

                def bytes = FileUtils.readFileToByteArray(classFile)
                def reader = new ClassReader(bytes)
                def writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
                def visitor = new RouterClassVisitor(Opcodes.ASM5, writer)

                reader.accept(visitor, ClassReader.EXPAND_FRAMES)

                def modifiedBytes = writer.toByteArray()
                FileUtils.writeByteArrayToFile(classFile, modifiedBytes)
            }
        }

        private void handleJarFile(File srcJar, File destJar) {
            def jarFile = new JarFile(srcJar)
            def outputJar = new JarOutputStream(new FileOutputStream(destJar))

            jarFile.entries().each { jarEntry ->
                def inputStream = jarFile.getInputStream(jarEntry)
                outputJar.putNextEntry(new JarEntry(jarEntry.name))

                if (jarEntry.name.endsWith(".class") &&
                        jarEntry.name.contains("LogisticsCenter")) {

                    Logger.info("处理JAR中的LogisticsCenter类: ${jarEntry.name}")

                    def reader = new ClassReader(inputStream)
                    def writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS)
                    def visitor = new RouterClassVisitor(Opcodes.ASM5, writer)

                    reader.accept(visitor, ClassReader.EXPAND_FRAMES)
                    outputJar.write(writer.toByteArray())
                } else {
                    // 使用缓冲区复制流
                    def buffer = new byte[1024]
                    def length
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputJar.write(buffer, 0, length)
                    }
                }

                outputJar.closeEntry()
                inputStream.close()
            }

            outputJar.close()
            jarFile.close()
        }
    }

    static class RouterClassVisitor extends ClassVisitor {

        RouterClassVisitor(int api, ClassVisitor cv) {
            super(api, cv)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc,
                                  String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)

            if (name == TARGET_METHOD && desc == TARGET_METHOD_DESC) {
                Logger.info("找到目标方法: ${TARGET_METHOD}")
                return new RouterMethodVisitor(api, mv)
            }
            return mv
        }
    }

    static class RouterMethodVisitor extends MethodVisitor {

        RouterMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        void visitCode() {
            super.visitCode()
            // 在方法开头插入代码
            Logger.info("在方法开头插入注册代码")
            mv.visitLdcInsn("pengchengshibashao_start")
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    TARGET_CLASS, REGISTER_METHOD, REGISTER_METHOD_DESC, false)
        }

        @Override
        void visitInsn(int opcode) {
            // 在RETURN指令前插入代码
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                Logger.info("在方法结尾插入注册代码")
                mv.visitLdcInsn("pengchengshibashao_end")
                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        TARGET_CLASS, REGISTER_METHOD, REGISTER_METHOD_DESC, false)
            }
            super.visitInsn(opcode)
        }

        @Override
        void visitMaxs(int maxStack, int maxLocals) {
            // 增加栈空间以确保有足够空间
            super.visitMaxs(Math.max(maxStack, 2), maxLocals)
        }
    }
}

// 简单日志工具
class Logger {
    static void info(String msg) {
        println "[RouterPlugin] $msg"
    }
}
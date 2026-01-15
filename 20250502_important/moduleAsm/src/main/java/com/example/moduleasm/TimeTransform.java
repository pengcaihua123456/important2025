package com.example.moduleasm;


import com.android.build.api.transform.*;
import com.android.build.gradle.internal.pipeline.TransformManager;
import org.apache.commons.io.FileUtils;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class TimeTransform extends Transform {
    private final Set<String> excludeList;
    private final Logger logger = Logging.getLogger(TimePlugin.class);

    public TimeTransform(Set<String> excludeList) {
        this.excludeList = excludeList;
    }

    @Override
    public String getName() {
        return "TimeTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation invocation) throws IOException {
        logger.lifecycle("🛠️ [TimeTransform] 开始处理，排除列表: {}", excludeList);

        for (TransformInput input : invocation.getInputs()) {
            // 处理目录输入
            for (DirectoryInput dirInput : input.getDirectoryInputs()) {
                processDirectory(dirInput.getFile(), invocation.getOutputProvider());
            }

            // 处理Jar输入（可选）
            for (JarInput jarInput : input.getJarInputs()) {
                processJar(jarInput.getFile(), invocation.getOutputProvider());
            }
        }

        logger.lifecycle("✅ [TimeTransform] 处理完成");
    }

    private void processDirectory(File inputDir, TransformOutputProvider outputProvider) throws IOException {
        File outputDir = outputProvider.getContentLocation(
                inputDir.getName(),
                getInputTypes(),
                getScopes(),
                Format.DIRECTORY
        );

        FileUtils.copyDirectory(inputDir, outputDir);

        Collection<File> classFiles = FileUtils.listFiles(outputDir, new String[]{"class"}, true);
        logger.lifecycle("处理目录: {}，找到 {} 个class文件", inputDir, classFiles.size());

        for (File file : classFiles) {
            if (shouldSkip(file)) {
                logger.debug("跳过排除文件: {}", file.getAbsolutePath());
                continue;
            }

            try {
                byte[] bytes = FileUtils.readFileToByteArray(file);
                byte[] modified = modifyClass(bytes);
                FileUtils.writeByteArrayToFile(file, modified);
                logger.debug("已处理: {}", file.getName());
            } catch (Exception e) {
                logger.error("处理文件失败: " + file.getAbsolutePath(), e);
            }
        }
    }

    private void processJar(File inputJar, TransformOutputProvider outputProvider) throws IOException {
        // 示例：简单复制Jar文件（可根据需要修改）
        File outputJar = outputProvider.getContentLocation(
                inputJar.getName(),
                getInputTypes(),
                getScopes(),
                Format.JAR
        );
        FileUtils.copyFile(inputJar, outputJar);
        logger.debug("处理Jar文件: {}", inputJar.getName());
    }

    private boolean shouldSkip(File classFile) {
        String path = classFile.getAbsolutePath().replace(File.separatorChar, '/');

        // 检查是否在排除列表中
        for (String exclude : excludeList) {
            if (path.contains(exclude)) {
                return true;
            }
        }

        // 额外排除规则（可选）
        return path.endsWith("/R.class") ||
                path.endsWith("/BuildConfig.class") ||
                path.contains("androidx/multidex/");
    }

    private byte[] modifyClass(byte[] original) {
        try {
            ClassReader reader = new ClassReader(original);
            ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = new TimeClassVisitor(writer);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);
            return writer.toByteArray();
        } catch (Exception e) {
            logger.error("修改类失败", e);
            return original; // 返回原始字节码避免构建失败
        }
    }
}
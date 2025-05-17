package com.example.moduleasm;


import com.android.build.gradle.AppExtension;
import com.android.build.gradle.LibraryExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TimePlugin implements Plugin<Project> {
    private final Logger logger = Logging.getLogger(TimePlugin.class);

    @Override
    public void apply(Project project) {
        logger.lifecycle("⏳ [TimePlugin] 初始化开始");

        // 1. 确保是Android项目
        if (!project.getPlugins().hasPlugin("com.android.application") &&
                !project.getPlugins().hasPlugin("com.android.library")) {
            throw new IllegalStateException("必须应用 'com.android.application' 或 'com.android.library' 插件");
        }

        // 2. 创建排除列表
        Set<String> excludeList = createExcludeList();
        logger.lifecycle("排除列表: " + excludeList);

//        // 3. 注册Transform（兼容Application和Library）
//        project.afterEvaluate(p -> {
//            try {
//                if (project.getPlugins().hasPlugin("com.android.application")) {
//                    AppExtension android = project.getExtensions().getByType(AppExtension.class);
//                    android.registerTransform(new TimeTransform(excludeList)); // 修复：传递excludeList
//                    logger.lifecycle("✅ 成功注册Application Transform");
//                } else {
//                    LibraryExtension android = project.getExtensions().getByType(LibraryExtension.class);
//                    android.registerTransform(new TimeTransform(excludeList)); // 修复：传递excludeList
//                    logger.lifecycle("✅ 成功注册Library Transform");
//                }
//            } catch (Exception e) {
//                logger.error("注册Transform失败", e);
//            }
//        });

        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        android.registerTransform(new TimeTransform(excludeList)); // 修复：传递excludeList

        // 4. 配置MultiDex
        enableMultiDex(project);

        logger.lifecycle("✅ [TimePlugin] 初始化完成");

        logger.lifecycle("✅ [TimePlugin] 2115");
    }

    private void enableMultiDex(Project project) {
        project.afterEvaluate(p -> {
            try {
                if (project.getExtensions().findByName("android") != null) {
                    Object android = project.getExtensions().getByName("android");
                    Object defaultConfig = android.getClass().getMethod("getDefaultConfig").invoke(android);

                    // 尝试三种设置方式确保兼容性
                    try {
                        defaultConfig.getClass().getMethod("setMultiDexEnabled", boolean.class)
                                .invoke(defaultConfig, true);
                    } catch (Exception e1) {
                        try {
                            defaultConfig.getClass().getField("multiDexEnabled").set(defaultConfig, true);
                        } catch (Exception e2) {
                            logger.warn("无法直接设置multiDexEnabled，尝试manifestPlaceholders");
                            defaultConfig.getClass()
                                    .getMethod("manifestPlaceholders", Map.class)
                                    .invoke(defaultConfig,
                                            Collections.singletonMap("android:name", "androidx.multidex.MultiDexApplication"));
                        }
                    }

                    // 添加依赖
                    project.getDependencies().add("implementation", "androidx.multidex:multidex:2.0.1");
                    logger.lifecycle("✅ MultiDex配置完成");
                }
            } catch (Exception e) {
                logger.error("配置MultiDex失败", e);
            }
        });
    }

    private Set<String> createExcludeList() {
        return new HashSet<String>() {{
            // 系统类
            add("android/");
            add("androidx/");
            add("java/");
            add("javax/");
            add("kotlin/");

            // 项目特殊类
            add("com/example/BuildConfig");
            add("com/example/R");

            // 第三方库
            add("com/google/");
            add("org/");

            // 排除MultiDex类避免循环
            add("androidx/multidex/");
        }};
    }
}
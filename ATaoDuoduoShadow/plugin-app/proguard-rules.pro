# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保持 AIDL 接口不被混淆或移除
-keep class com.tencent.shadow.sample.plugin.IMyAidlInterface { *; }
-keep interface com.tencent.shadow.sample.plugin.IMyAidlInterface { *; }

# 如果 Shadow 框架有特定的保持规则，确保也加上
# 通常 Shadow 样本项目中会有专门的 proguard 文件，检查是否遗漏了 include

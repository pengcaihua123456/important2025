package com.evenbus.myapplication.trace;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.R;

import java.util.Random;

/**
 * 布局性能追踪Activity - 演示复杂布局导致的性能问题
 * 该Activity展示嵌套布局和动态添加视图对性能的影响
 */
public class TraceLayoutActivity extends AppCompatActivity {

    // 视图组件引用
    private LinearLayout containerLayout;    // 主容器布局
    private View nestedLayout;               // 嵌套布局视图
    private View flatLayout;                 // 扁平布局视图
    private TextView tvPerformance;          // 性能状态显示
    private TextView tvViewCount;            // 视图数量显示
    private Button btnShowNested;            // 显示嵌套布局按钮
    private Button btnShowFlat;              // 显示扁平布局按钮
    private Button btnAddDynamicViews;       // 添加动态视图按钮
    private Button btnClearViews;            // 清除视图按钮

    // 状态和工具变量
    private int viewCount = 0;               // 当前视图数量计数
    private Random random = new Random();    // 随机数生成器
    private Handler handler = new Handler(Looper.getMainLooper()); // 主线程Handler

    /**
     * Activity创建生命周期回调
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("嵌套层级太大卡顿");
        // 设置布局文件
        setContentView(R.layout.activity_trace_layout_layout);

        // 初始化视图组件
        initViews();

        // 设置按钮点击监听器
        setupClickListeners();
    }

    /**
     * 初始化所有视图组件
     */
    private void initViews() {
        containerLayout = findViewById(R.id.container_layout);
        nestedLayout = findViewById(R.id.nested_layout);
        flatLayout = findViewById(R.id.flat_layout);
        tvPerformance = findViewById(R.id.tv_performance);
        tvViewCount = findViewById(R.id.tv_view_count);
        btnShowNested = findViewById(R.id.btn_show_nested);
        btnShowFlat = findViewById(R.id.btn_show_flat);
        btnAddDynamicViews = findViewById(R.id.btn_add_dynamic_views);
        btnClearViews = findViewById(R.id.btn_clear_views);
    }

    /**
     * 设置按钮点击事件监听器
     */
    private void setupClickListeners() {
        // 显示嵌套布局按钮点击事件
        btnShowNested.setOnClickListener(v -> showNestedLayout());

        // 显示扁平布局按钮点击事件
        btnShowFlat.setOnClickListener(v -> showFlatLayout());

        // 添加动态视图按钮点击事件
        btnAddDynamicViews.setOnClickListener(v -> addDynamicViews());

        // 清除视图按钮点击事件
        btnClearViews.setOnClickListener(v -> clearAllViews());
    }

    /**
     * 显示嵌套布局 - 演示复杂布局导致的性能问题
     */
    private void showNestedLayout() {
        // 先清除现有视图
        clearAllViews();

        // 模拟测量和布局的耗时操作
        simulateHeavyLayoutWork();

        // 将嵌套布局添加到容器中
        nestedLayout.setVisibility(View.VISIBLE);
        ViewGroup parent = (ViewGroup) nestedLayout.getParent();

        // 如果嵌套布局已有父布局，先从其父布局中移除
        if (parent != null) {
            parent.removeView(nestedLayout);
        }

        // 添加到主容器
        containerLayout.addView(nestedLayout);

        // 更新视图数量显示（估计嵌套布局中的视图数量）
        updateViewCount(50);

        // 更新性能状态提示
        updatePerformanceStatus("显示嵌套布局 - 可能会卡顿");
    }

    /**
     * 显示扁平布局 - 演示优化后的布局性能
     */
    private void showFlatLayout() {
        // 先清除现有视图
        clearAllViews();

        // 将扁平布局添加到容器中
        flatLayout.setVisibility(View.VISIBLE);
        ViewGroup parent = (ViewGroup) flatLayout.getParent();

        // 如果扁平布局已有父布局，先从其父布局中移除
        if (parent != null) {
            parent.removeView(flatLayout);
        }

        // 添加到主容器
        containerLayout.addView(flatLayout);

        // 更新视图数量显示（估计扁平布局中的视图数量）
        updateViewCount(25);

        // 更新性能状态提示
        updatePerformanceStatus("显示扁平布局 - 流畅");
    }

    /**
     * 动态添加大量视图 - 演示主线程阻塞导致的卡顿
     */
    private void addDynamicViews() {
        // 更新性能状态提示
        updatePerformanceStatus("正在添加视图，主线程阻塞中...");

        // 在主线程执行耗时操作，故意造成卡顿
        // 使用Handler.postDelayed模拟异步操作，但实际上仍在主线程执行
        handler.postDelayed(() -> {
            // 添加100个复杂视图
            for (int i = 0; i < 800; i++) {
                createAndAddComplexView();
            }

            // 更新性能状态提示
            updatePerformanceStatus("已添加100个复杂视图 - 严重卡顿风险");
        }, 100); // 延迟100ms执行，避免立即阻塞UI
    }

    /**
     * 创建并添加复杂嵌套视图 - 演示视图创建和添加的性能开销
     */
    private void createAndAddComplexView() {
        // 创建一个复杂的嵌套视图结构

        // 外层横向布局
        LinearLayout outerLayout = new LinearLayout(this);
        outerLayout.setOrientation(LinearLayout.HORIZONTAL);
        outerLayout.setBackgroundColor(0x33FF0000); // 半透明红色背景
        LinearLayout.LayoutParams outerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        outerParams.setMargins(4, 4, 4, 4);
        outerLayout.setLayoutParams(outerParams);

        // 左侧嵌套纵向布局
        LinearLayout leftLayout = new LinearLayout(this);
        leftLayout.setOrientation(LinearLayout.VERTICAL);
        leftLayout.setBackgroundColor(0x3300FF00); // 半透明绿色背景
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1); // 权重为1
        leftParams.setMargins(2, 2, 2, 2);
        leftLayout.setLayoutParams(leftParams);

        // 在左侧布局中添加3个TextView
        for (int j = 0; j < 3; j++) {
            TextView textView = new TextView(this);
            textView.setText("Item " + viewCount + "-" + j);
            textView.setBackgroundColor(0x330000FF); // 半透明蓝色背景
            textView.setPadding(8, 8, 8, 8);
            leftLayout.addView(textView);
            viewCount++; // 增加视图计数
        }

        // 右侧嵌套纵向布局
        LinearLayout rightLayout = new LinearLayout(this);
        rightLayout.setOrientation(LinearLayout.VERTICAL);
        rightLayout.setBackgroundColor(0x33FFFF00); // 半透明黄色背景
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1); // 权重为1
        rightParams.setMargins(2, 2, 2, 2);
        rightLayout.setLayoutParams(rightParams);

        // 在右侧布局中添加2个Button
        for (int j = 0; j < 2; j++) {
            Button button = new Button(this);
            button.setText("Btn " + viewCount + "-" + j);
            button.setPadding(8, 8, 8, 8);
            rightLayout.addView(button);
            viewCount++; // 增加视图计数
        }

        // 将左右布局添加到外层布局
        outerLayout.addView(leftLayout);
        outerLayout.addView(rightLayout);

        // 将外层布局添加到主容器
        containerLayout.addView(outerLayout);

        // 计数外层布局（1个外层 + 2个内层 = 3个布局视图）
        viewCount += 3;

        // 更新视图数量显示
        updateViewCount(viewCount);
    }

    /**
     * 清除所有动态添加的视图
     */
    private void clearAllViews() {
        // 移除容器中的所有子视图
        containerLayout.removeAllViews();

        // 重置视图计数器
        viewCount = 0;

        // 更新视图数量显示
        updateViewCount(viewCount);

        // 更新性能状态提示
        updatePerformanceStatus("已清除所有视图");
    }

    /**
     * 更新视图数量显示
     * @param count 当前视图数量
     */
    private void updateViewCount(int count) {
        tvViewCount.setText("视图数量: " + count);
    }

    /**
     * 更新性能状态显示
     * @param status 性能状态文本
     */
    private void updatePerformanceStatus(String status) {
        tvPerformance.setText("性能状态: " + status);
    }

    /**
     * 模拟繁重的布局计算工作 - 演示测量和布局过程的性能开销
     */
    private void simulateHeavyLayoutWork() {
//        // 记录开始时间
//        long startTime = System.currentTimeMillis();
//
//        // 执行复杂的数学计算来模拟布局计算耗时
//        double result = 0;
//        for (int i = 0; i < 1000000; i++) {
//            result += Math.sin(i) * Math.cos(i);
//        }
//
//        // 计算耗时
//        long duration = System.currentTimeMillis() - startTime;

        // 更新性能状态显示
        updatePerformanceStatus("布局计算耗时: " + 200 + "ms");
    }

    /**
     * Activity销毁生命周期回调
     * 进行资源清理工作
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 移除所有Handler回调，避免内存泄漏
        handler.removeCallbacksAndMessages(null);
    }
}
package com.evenbus.myapplication.trace;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.evenbus.myapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡顿追踪测试Activity - 演示频繁invalidate()导致的性能问题
 * 该Activity通过BadAnimationView展示错误的动画实现方式导致的卡顿现象
 */
public class TraceInvalideActivity extends AppCompatActivity implements BadAnimationView.OnAnimationUpdateListener {

    // UI组件引用
    private BadAnimationView badAnimationView;  // 展示错误动画的自定义View
    private TextView tvPerformance;             // 性能状态显示TextView
    private TextView tvInvalidateCount;         // invalidate调用次数显示TextView
    private TextView tvFPS;                     // 帧率显示TextView
    private ListView listView;                  // 用于测试卡顿的列表视图

    /**
     * Activity创建生命周期回调
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置布局文件
        setContentView(R.layout.activity_trace_invalide_layout);
        setTitle("invalide过渡绘制导致的卡顿");

        // 初始化视图组件
        initViews();

        // 设置按钮点击监听器
        setupClickListeners();

        // 设置列表视图数据
        setupListView();
    }

    /**
     * 初始化所有视图组件
     */
    private void initViews() {
        // 获取自定义动画View引用
        badAnimationView = findViewById(R.id.bad_animation_view);

        // 获取性能显示TextView引用
        tvPerformance = findViewById(R.id.tv_performance);
        tvInvalidateCount = findViewById(R.id.tv_invalidate_count);
        tvFPS = findViewById(R.id.tv_fps);

        // 获取测试用列表视图引用
        listView = findViewById(R.id.list_view);

        // 设置动画更新监听器，用于接收性能数据回调
        badAnimationView.setUpdateListener(this);
    }

    /**
     * 设置按钮点击事件监听器
     */
    private void setupClickListeners() {
        // 启动错误动画按钮
        findViewById(R.id.btn_start_bad_animation).setOnClickListener(v -> startBadAnimation());

        // 启动正确动画按钮（预留功能）
        findViewById(R.id.btn_start_good_animation).setOnClickListener(v -> startGoodAnimation());

        // 停止动画按钮
        findViewById(R.id.btn_stop_animation).setOnClickListener(v -> stopAnimation());

        // 清空性能日志按钮
        findViewById(R.id.btn_clear_log).setOnClickListener(v -> clearPerformanceLog());
    }

    /**
     * 设置列表视图数据和适配器
     * 该列表用于在动画运行时测试滚动卡顿情况
     */
    private void setupListView() {
        // 创建测试数据
        List<String> items = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            items.add("列表项 " + i + " - 尝试滚动我感受卡顿");
        }

        // 创建并设置数组适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1, // 使用系统简单列表项布局
                items);
        listView.setAdapter(adapter);
    }

    /**
     * 启动错误动画演示
     * 该方法会启动BadAnimationView中的错误动画实现
     */
    private void startBadAnimation() {
        // 先停止现有动画
        stopAnimation();

        // 更新性能状态显示
        updatePerformanceStatus("启动错误动画 - Handler频繁invalidate");

        // 启动错误动画
        badAnimationView.startExtremeBadAnimation();
    }

    /**
     * 启动正确动画（预留方法）
     * 该方法用于展示正确的动画实现方式
     */
    private void startGoodAnimation() {
        // 先停止现有动画
        stopAnimation();

        // 更新性能状态显示
        updatePerformanceStatus("启动正确动画 - 使用ValueAnimator");

        // 预留：这里可以添加ValueAnimator的正确实现
        // 用于对比错误动画和正确动画的性能差异
    }

    /**
     * 停止所有动画
     */
    private void stopAnimation() {
        // 调用自定义View的停止动画方法
        badAnimationView.stopAnimation();

        // 更新性能状态显示
        updatePerformanceStatus("动画已停止");
    }

    /**
     * 清空性能监控日志
     */
    private void clearPerformanceLog() {
        // 重置invalidate调用次数显示
        tvInvalidateCount.setText("invalidate()调用次数: 0");

        // 重置FPS显示
        tvFPS.setText("当前FPS: 0");
    }

    /**
     * 更新性能状态显示
     * @param status 要显示的性能状态文本
     */
    private void updatePerformanceStatus(String status) {
        tvPerformance.setText("性能状态: " + status);
    }

    /**
     * invalidate调用次数回调方法
     * 实现OnAnimationUpdateListener接口
     * @param count 当前invalidate调用次数
     * @param fps 当前帧率
     */
    @Override
    public void onInvalidateCalled(int count, int fps) {
        // 在主线程更新UI
        runOnUiThread(() -> {
            // 更新invalidate调用次数显示
            tvInvalidateCount.setText("invalidate()调用次数: " + count);

            // 更新FPS显示
            tvFPS.setText("当前FPS: " + fps);

            // 根据FPS更新性能状态
            updatePerformanceStatusBasedOnFPS(fps);
        });
    }

    /**
     * FPS更新回调方法
     * 实现OnAnimationUpdateListener接口
     * @param fps 新的FPS值
     */
    @Override
    public void onFPSUpdated(int fps) {
        // 在主线程更新UI
        runOnUiThread(() -> {
            // 更新FPS显示
            tvFPS.setText("当前FPS: " + fps);

            // 根据FPS更新性能状态
            updatePerformanceStatusBasedOnFPS(fps);
        });
    }

    /**
     * 根据FPS值更新性能状态显示和颜色
     * @param fps 当前帧率值
     */
    private void updatePerformanceStatusBasedOnFPS(int fps) {
        // 根据FPS范围设置不同的颜色和状态文本
        if (fps < 10) {
            // 严重卡顿：红色显示
            tvPerformance.setTextColor(0xFFFF0000); // ARGB: 红色
            updatePerformanceStatus("严重卡顿 - FPS: " + fps);
        } else if (fps < 30) {
            // 明显卡顿：橙色显示
            tvPerformance.setTextColor(0xFFFFA500); // ARGB: 橙色
            updatePerformanceStatus("明显卡顿 - FPS: " + fps);
        } else if (fps < 50) {
            // 轻微卡顿：黄色显示
            tvPerformance.setTextColor(0xFFFFFF00); // ARGB: 黄色
            updatePerformanceStatus("轻微卡顿 - FPS: " + fps);
        } else {
            // 流畅：绿色显示
            tvPerformance.setTextColor(0xFF00FF00); // ARGB: 绿色
            updatePerformanceStatus("流畅 - FPS: " + fps);
        }
    }

    /**
     * Activity销毁生命周期回调
     * 进行资源清理工作
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 确保动画停止，避免内存泄漏
        badAnimationView.stopAnimation();
    }
}
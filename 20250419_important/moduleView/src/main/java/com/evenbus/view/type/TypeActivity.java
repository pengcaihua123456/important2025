package com.evenbus.view.type;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.module_view.R;


public class TypeActivity extends AppCompatActivity {


    private TypewriterTextView typewriterView;
    private final StringBuilder pendingText = new StringBuilder();
    private static final String DEMO_TEXT = "# 登长城的故事\n\n" +
            "在历史的长河中，人类的潜能无限。它是刻在骨子里的文化认同，是 “我是中国人” 的具象表达。\n\n" +
            "## 勇攀高峰人生\n\n" +
            "李勇是一个普通的上班族，**但他的梦想不凡**。\n" +
            "他决定挑战自我，攀登万里长城。更在于它始终活着。它不是冰冷的遗迹，而是一代代人守护家园的象征";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_view);
        setTitle("大模型打字机效果");

        typewriterView = findViewById(R.id.typewriter_view);


        Button startBtn = findViewById(R.id.start_btn);
        startBtn.setOnClickListener(v -> startTypingAnimation());

        Button clearBtn = findViewById(R.id.clear_btn);

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typewriterView.clearText();;
            }
        });


        // 添加样式测试按钮
        Button testBtn = findViewById(R.id.test_btn);
        testBtn.setOnClickListener(v -> testStyles());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 确保在正确的时机请求焦点
        typewriterView.postDelayed(() -> {
            if (typewriterView.isAttachedToWindow()) {
                typewriterView.requestFocus();
            }
        }, 3000);
    }

    private void startTypingAnimation() {

        typewriterView.appendStreamText(DEMO_TEXT.toString());

//            new Thread(() -> {
//                for (char c : DEMO_TEXT.toCharArray()) {
//                    try {
//                        Thread.sleep(30);
//                    } catch (InterruptedException e) {
//                        Thread.currentThread().interrupt();
//                    }
//
//                    synchronized (pendingText) {
//                        pendingText.append(c);
//                    }
//
//                    runOnUiThread(() -> {
//                        synchronized (pendingText) {
//                            typewriterView.appendStreamText(pendingText.toString());
//                            pendingText.setLength(0);
//                        }
//                    });
//                }
//            }).start();
    }

    private void testStyles() {
        typewriterView.appendStreamText("# 标题测试\n");
        typewriterView.appendStreamText("## 二级标题\n");
        typewriterView.appendStreamText("普通文本\n");
        typewriterView.appendStreamText("**加粗文本**\n");
        typewriterView.appendStreamText("混合**加粗**样式\n");
        typewriterView.appendStreamText("跨行**加粗\n文本**测试");
    }
}
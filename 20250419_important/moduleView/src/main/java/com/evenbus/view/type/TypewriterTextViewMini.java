package com.evenbus.view.type;

/**
 * @Author pengcaihua
 * @Date 13:06
 * @describe
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayDeque;
import java.util.Deque;


public class TypewriterTextViewMini extends AppCompatTextView {

    private static final String TAG = "TypewriterTextView";
    private static final int CURSOR_BLINK_DELAY = 500;
    private static final int CHAR_DELAY = 100;
    private static final int DEFAULT_TEXT_COLOR = 0x00ffff; // 深灰色

    private final Deque<CharItem> charQueue = new ArrayDeque<>();
    private final SpannableStringBuilder ssb = new SpannableStringBuilder();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Paint cursorPaint = new Paint();

    private boolean showCursor = true;
    private boolean isTyping = false;
    private int currentTitleLevel = 0;
    private boolean isBold = false;
    private int currentPosition = 0;
    private int cursorColor = DEFAULT_TEXT_COLOR;
    private float cursorWidth = 3f;

    public TypewriterTextViewMini(Context context) {
        super(context);
        init();
    }

    public TypewriterTextViewMini(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TypewriterTextViewMini(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 设置光标样式
        cursorPaint.setColor(cursorColor);
        cursorPaint.setStrokeWidth(dpToPx(cursorWidth));

        // 确保视图准备好后再开始光标闪烁
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                startCursorBlink();
            }
        });

        // 设置焦点相关属性
        setFocusable(true);
        setFocusableInTouchMode(true);

        // 初始文本位置
        currentPosition = 0;
    }

    public void appendStreamText(String text) {
        if (text == null || text.isEmpty()) return;

        Log.d(TAG, "Appending text: " + text);
        parseMarkdown(text);

        // 如果没有在打字，启动打字动画
        if (!isTyping) {
            Log.d(TAG, "Starting typing animation");
            startTyping();
        }
    }

    private void startTyping() {
        isTyping = true;
        handler.post(typeRunnable);
    }

    private final Runnable typeRunnable = new Runnable() {
        @Override
        public void run() {
            typeNextCharacter();
        }
    };

    private void parseMarkdown(String text) {
        int pos = 0;
        int length = text.length();

        while (pos < length) {
            char c = text.charAt(pos);

            // 处理标题
            if (c == '#' && (pos == 0 || text.charAt(pos - 1) == '\n')) {
                int headerLevel = 0;
                while (pos < length && text.charAt(pos) == '#') {
                    headerLevel++;
                    pos++;
                }

                if (pos < length && Character.isWhitespace(text.charAt(pos))) {
                    currentTitleLevel = Math.min(headerLevel, 2); // 只支持1-2级标题
                    pos++; // 跳过空格
                    continue;
                }
            }

            // 处理加粗
            if (c == '*' && pos + 1 < length && text.charAt(pos + 1) == '*') {
                charQueue.add(new CharItem("", 0, false, true)); // 标记样式变化
                isBold = !isBold;
                pos += 2;
                continue;
            }

            // 处理换行
            if (c == '\n') {
                charQueue.add(new CharItem("\n", currentTitleLevel, isBold, false));
                currentTitleLevel = 0; // 重置标题
                isBold = false; // 重置加粗
                pos++;
                continue;
            }

            // 普通字符
            charQueue.add(new CharItem(String.valueOf(c), currentTitleLevel, isBold, false));
            pos++;
        }
    }

    private void typeNextCharacter() {
        if (charQueue.isEmpty()) {
            Log.d(TAG, "Typing completed");
            isTyping = false;
            return;
        }

        CharItem item = charQueue.poll();
        int start = ssb.length();

        // 只处理实际内容字符（跳过纯样式标记）
        if (!item.isStyleChange) {
            ssb.append(item.text);
            currentPosition = ssb.length();

            // 应用标题样式
            if (item.titleLevel > 0) {
                float textSize = getTextSize();
                float headerSize = textSize * (item.titleLevel == 1 ? 1.8f : 1.4f);
                ssb.setSpan(new AbsoluteSizeSpan((int) headerSize, true),
                        start, currentPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // 一级标题加粗
                if (item.titleLevel == 1) {
                    ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            start, currentPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // 应用加粗样式
            if (item.isBold) {
                ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                        start, currentPosition, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            setText(ssb);
        }

        // 继续下一个字符
        if (!charQueue.isEmpty()) {
            handler.postDelayed(typeRunnable, CHAR_DELAY);
        } else {
            isTyping = false;
        }
    }

    // 光标绘制
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (showCursor && hasFocus()) {
            float x = getCursorX();
            float baseline = getBaseline();
            float bottom = baseline + getLineHeight();
            canvas.drawLine(x, baseline, x, bottom, cursorPaint);
        }
    }

    private float getCursorX() {
        if (getLayout() == null || currentPosition > getLayout().getText().length()) {
            return getPaddingStart();
        }

        int line = getLayout().getLineForOffset(currentPosition);
        return getLayout().getPrimaryHorizontal(currentPosition);
    }

    private void startCursorBlink() {
        handler.postDelayed(cursorRunnable, CURSOR_BLINK_DELAY);
    }

    private final Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            invalidateCursor();
        }
    };

    private void invalidateCursor() {
        if (hasFocus()) {
            showCursor = !showCursor;
            invalidate();
        }
        handler.postDelayed(cursorRunnable, CURSOR_BLINK_DELAY);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(typeRunnable);
        handler.removeCallbacks(cursorRunnable);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  android.graphics.Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            showCursor = true;
            invalidate();
        }
    }

    // 设置光标颜色
    public void setCursorColor(int color) {
        cursorColor = color;
        cursorPaint.setColor(color);
        invalidate();
    }

    // 设置光标宽度 (dp)
    public void setCursorWidth(float widthDp) {
        cursorWidth = widthDp;
        cursorPaint.setStrokeWidth(dpToPx(widthDp));
        invalidate();
    }

    // 清除所有内容并重置状态
    public void clear() {
        ssb.clear();
        charQueue.clear();
        setText("");
        currentPosition = 0;
        currentTitleLevel = 0;
        isBold = false;
        isTyping = false;
        handler.removeCallbacks(typeRunnable);
        handler.removeCallbacks(cursorRunnable);
    }

    // 字符项封装类
    private static class CharItem {
        final String text;
        final int titleLevel; // 0=普通, 1=H1, 2=H2
        final boolean isBold;
        final boolean isStyleChange; // 仅样式变化无内容

        CharItem(String text, int titleLevel, boolean isBold, boolean isStyleChange) {
            this.text = text;
            this.titleLevel = titleLevel;
            this.isBold = isBold;
            this.isStyleChange = isStyleChange;
        }
    }
}
package com.evenbus.view.type;

/**
 * @Author pengcaihua
 * @Date 13:40
 * @describe
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayDeque;
import java.util.Deque;

public class TypewriterTextViewPlus extends AppCompatTextView {

    private static final String TAG = "TypewriterTextView";
    private static final int CURSOR_BLINK_DELAY = 500;
    private static final int CHAR_DELAY = 30;
    private static final int DEFAULT_CURSOR_COLOR = 0xFF00FF00; // 亮绿色

    private final Deque<CharItem> charQueue = new ArrayDeque<>();
    private final SpannableStringBuilder ssb = new SpannableStringBuilder();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Paint cursorPaint = new Paint();

    private boolean showCursor = true;
    private boolean isTyping = false;
    private int currentTitleLevel = 0;
    private boolean isBold = false;
    private int cursorPosition = 0;
    private int cursorColor = DEFAULT_CURSOR_COLOR;
    private float cursorWidth = 3f;
    private boolean isCursorActive = true;
    private boolean layoutFinished = false;

    // 新增：文本测量辅助变量
    private boolean pendingLayout = false;
    private int lastMeasureWidth = 0;
    private int lastMeasureHeight = 0;

    public TypewriterTextViewPlus(Context context) {
        super(context);
        init();
    }

    public TypewriterTextViewPlus(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TypewriterTextViewPlus(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        cursorPaint.setColor(cursorColor);
        cursorPaint.setStrokeWidth(dpToPx(cursorWidth));

        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (layoutFinished) return;
            layoutFinished = true;
            startCursorBlink();
            Log.d(TAG, "Global layout finished");
        });

        setFocusable(true);
        setFocusableInTouchMode(true);
        setCursorPosition(0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(() -> {
            if (isAttachedToWindow()) {
                requestFocus();
                Log.d(TAG, "Requested focus");
            }
        });
    }

    public void appendStreamText(String text) {
        if (TextUtils.isEmpty(text)) return;

        Log.d(TAG, "Appending text: " + text);
        parseMarkdown(text);

        if (!isTyping) {
            Log.d(TAG, "Starting typing animation");
            startTyping();
        }

        showCursor = true;
        invalidate();
    }

    private void startTyping() {
        isTyping = true;
        handler.post(typeRunnable);
    }

    private final Runnable typeRunnable = this::typeNextCharacter;

    private void parseMarkdown(String text) {
        int pos = 0;
        int length = text.length();

        while (pos < length) {
            char c = text.charAt(pos);

            // 处理标题
            if (c == '#' && (pos == 0 || (pos > 0 && text.charAt(pos - 1) == '\n'))){
                int headerLevel = 0;
                int startPos = pos;
                while (pos < length && text.charAt(pos) == '#') {
                    headerLevel++;
                    pos++;
                }

                if (pos < length && Character.isWhitespace(text.charAt(pos))) {
                    currentTitleLevel = Math.min(headerLevel, 2);
                    pos++;
                    continue;
                } else {
                    pos = startPos;
                }
            }

            // 处理加粗
            if (c == '*' && pos + 1 < length && text.charAt(pos + 1) == '*') {
                charQueue.add(new CharItem("", 0, false, true));
                isBold = !isBold;
                pos += 2;
                continue;
            }

            // 处理换行
            if (c == '\n') {
                charQueue.add(new CharItem("\n", currentTitleLevel, isBold, false));
                currentTitleLevel = 0;
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

        if (!item.isStyleChange) {
            ssb.append(item.text);

            // 应用标题样式
            if (item.titleLevel > 0) {
                float textSize = getTextSize();
                float headerSize = textSize * (item.titleLevel == 1 ? 0.8f : 0.8f);
                int spanStart = start;
                int spanEnd = ssb.length();

                if (spanStart < spanEnd) {
                    ssb.setSpan(new AbsoluteSizeSpan((int) headerSize, true),
                            spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    if (item.titleLevel == 1) {
                        ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                                spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }

            // 应用加粗样式
            if (item.isBold) {
                int spanStart = start;
                int spanEnd = ssb.length();

                if (spanStart < spanEnd) {
                    ssb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                            spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            // 更新文本并标记需要重新布局
            setTextInternal(ssb);
        }

        // 更新光标位置
        setCursorPosition(ssb.length());

        showCursor = true;
        invalidate();

        if (!charQueue.isEmpty()) {
            handler.postDelayed(typeRunnable, CHAR_DELAY);
        } else {
            isTyping = false;
        }
    }

    private void setTextInternal(CharSequence text) {
        super.setText(text, BufferType.SPANNABLE);

        // 强制测量和布局
        pendingLayout = true;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 保存之前的测量结果用于比较
        int oldWidth = lastMeasureWidth;
        int oldHeight = lastMeasureHeight;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        lastMeasureWidth = getMeasuredWidth();
        lastMeasureHeight = getMeasuredHeight();

        // 如果尺寸变化，需要重新布局
        if (oldWidth != lastMeasureWidth || oldHeight != lastMeasureHeight) {
            pendingLayout = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (pendingLayout) {
            pendingLayout = false;
            // 布局完成后立即更新光标位置
            updateCursorPosition();
        }
    }

    private void setCursorPosition(int position) {
        cursorPosition = Math.max(0, Math.min(position, ssb.length()));
        Log.d(TAG, "Cursor position set to: " + cursorPosition);

        // 立即更新光标位置
        updateCursorPosition();
    }

    private void updateCursorPosition() {
        // 确保在UI线程执行
        post(() -> {
            // 请求重绘光标
            invalidate();
            // 滚动到光标位置
            if (getLayout() != null && cursorPosition <= getLayout().getText().length()) {
                int line = getLayout().getLineForOffset(cursorPosition);
                int y = getLayout().getLineTop(line);
                scrollTo(0, y);
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isCursorActive && showCursor && layoutFinished) {
            float x = getCursorX();
            if (x >= 0) {
                float baseline = getBaseline();
                float bottom = baseline + getLineHeight();
                canvas.drawLine(x, baseline, x, bottom, cursorPaint);
                Log.v(TAG, "Drawing cursor at x: " + x);
            }
        }
    }

    private float getCursorX() {
        if (getLayout() == null) {
            Log.w(TAG, "Layout is null, returning padding start");
            return getPaddingStart();
        }

        int layoutLength = getLayout().getText().length();
        if (cursorPosition > layoutLength) {
            Log.w(TAG, "Cursor position " + cursorPosition + " exceeds layout length " + layoutLength);
            cursorPosition = layoutLength;
        }

        try {
            int line = getLayout().getLineForOffset(cursorPosition);
            float x = getLayout().getPrimaryHorizontal(cursorPosition);

            // 考虑滚动偏移
            int scrollX = getScrollX();
            if (x > getWidth() - getPaddingRight()) {
                scrollTo((int) x - getWidth() + getPaddingRight(), getScrollY());
            }

            Log.v(TAG, "Cursor X calculated: " + x + " for position: " + cursorPosition + " on line: " + line);
            return x;
        } catch (Exception e) {
            Log.e(TAG, "Error getting cursor position: " + e.getMessage());
            return getPaddingStart();
        }
    }

    private void startCursorBlink() {
        handler.removeCallbacks(cursorRunnable);
        handler.post(cursorRunnable);
    }

    private final Runnable cursorRunnable = new Runnable() {
        @Override
        public void run() {
            if (isCursorActive && layoutFinished) {
                showCursor = !showCursor;
                invalidate();
            }
            handler.postDelayed(cursorRunnable, CURSOR_BLINK_DELAY);
        }
    };


    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(typeRunnable);
        handler.removeCallbacks(cursorRunnable);
    }

    // ... 其他方法保持不变 ...

    @Override
    public void scrollTo(int x, int y) {
        // 防止过度滚动
        int maxY = getLayout().getHeight() - getHeight();
        if (y > maxY) y = maxY;
        if (y < 0) y = 0;

        super.scrollTo(x, y);
    }

    // 字符项封装类
    private static class CharItem {
        final String text;
        final int titleLevel;
        final boolean isBold;
        final boolean isStyleChange;

        CharItem(String text, int titleLevel, boolean isBold, boolean isStyleChange) {
            this.text = text;
            this.titleLevel = titleLevel;
            this.isBold = isBold;
            this.isStyleChange = isStyleChange;
        }
    }
}
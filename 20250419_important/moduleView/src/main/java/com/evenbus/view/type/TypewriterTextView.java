package com.evenbus.view.type;

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
import android.view.ViewTreeObserver;

import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class TypewriterTextView extends AppCompatTextView {

    private static final String TAG = "TypewriterTextView";
    private static final int CURSOR_BLINK_DELAY = 500;
    private static final int CHAR_DELAY = 2000;
    private static final int DEFAULT_CURSOR_COLOR = 0xFF00FF00; // 亮绿色
    private static final long SCROLL_THROTTLE = 50; // 滚动节流时间(ms)

    private final Deque<CharItem> charQueue = new ArrayDeque<>();
    private final List<CharItem> pendingItems = new ArrayList<>(); // 批处理队列
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
    private boolean isLayoutPending = false;
    private int pendingCursorPosition = -1;
    private long lastScrollTime = 0;

    public TypewriterTextView(Context context) {
        super(context);
        init();
    }

    public TypewriterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TypewriterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        cursorPaint.setColor(cursorColor);
        cursorPaint.setStrokeWidth(dpToPx(cursorWidth));

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (layoutFinished) return;
                layoutFinished = true;
                startCursorBlink();
                Log.d(TAG, "Global layout finished");
            }
        });

        setFocusable(true);
        setFocusableInTouchMode(true);
        setCursorPosition(0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        post(new Runnable() {
            @Override
            public void run() {
                if (isAttachedToWindow()) {
                    requestFocus();
                    Log.d(TAG, "Requested focus");
                }
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
            if (c == '#' && (pos == 0 || (pos > 0 && text.charAt(pos - 1) == '\n'))) {
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
        if (charQueue.isEmpty() && pendingItems.isEmpty()) {
            Log.d(TAG, "Typing completed");
            isTyping = false;
            return;
        }

        // 批处理：每次处理最多10个字符
        int batchCount = 0;
        while (batchCount < 10 && !charQueue.isEmpty()) {
            CharItem item = charQueue.poll();
            pendingItems.add(item);
            batchCount++;
        }

        if (!pendingItems.isEmpty()) {
            batchAppend();
        }

        // 更新光标位置
        setCursorPosition(ssb.length());

        showCursor = true;
        invalidate();

        if (!charQueue.isEmpty() || !pendingItems.isEmpty()) {
            handler.postDelayed(typeRunnable, CHAR_DELAY);
        } else {
            isTyping = false;
        }
    }

    private void batchAppend() {
        if (pendingItems.isEmpty()) return;

        for (CharItem item : pendingItems) {
            int start = ssb.length();

            if (!item.isStyleChange) {
                // 智能换行检测
                if (shouldAutoWrap() && !item.text.equals("\n")) {
                    handleNewlineCharacter();
                }

                if ("\n".equals(item.text)) {
                    // 显式换行
                    ssb.append("\n");
                } else {
                    ssb.append(item.text);
                }

                // 应用标题样式
                if (item.titleLevel > 0) {
                    float textSize = getTextSize();
                    float headerSize = textSize * (item.titleLevel == 1 ? 1.5f : 1.2f); // 标题1放大1.5倍，标题2放大1.2倍
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
            }
        }

        // 更新文本
        setTextInternal(ssb);
        pendingItems.clear();
    }

    private void setTextInternal(CharSequence text) {
        super.setText(text, BufferType.SPANNABLE);
        // 标记需要重新布局
        isLayoutPending = true;
        requestLayout();
    }

    // 智能换行判断逻辑
    private boolean shouldAutoWrap() {
        if (getLayout() == null) return false;

        int currentLine = getLayout().getLineCount() - 1;
        if (currentLine < 0) return false;

        // 获取当前行宽度和视图可用宽度
        float lineWidth = getLayout().getLineWidth(currentLine);
        float availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();

        // 当当前行宽度超过可用宽度的95%时触发换行
        return (lineWidth > availableWidth * 0.95);
    }

    // 智能换行处理器
    private void handleNewlineCharacter() {
        ssb.append("\n");
        setTextInternal(ssb);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (isLayoutPending) {
            isLayoutPending = false;
            // 布局完成后同步光标位置
            if (pendingCursorPosition != -1) {
                realignCursor(pendingCursorPosition);
                pendingCursorPosition = -1;
            }
        }
    }

    private void realignCursor(int position) {
        cursorPosition = position;
        // 精确计算光标位置并滚动
        scrollToCursor();
        invalidate();
    }

    private void setCursorPosition(int position) {
        // 延迟到布局完成后更新
        pendingCursorPosition = position;
        isLayoutPending = true;
        requestLayout();
    }

    private void scrollToCursor() {
        if (getLayout() == null) return;

        try {
            int line = getLayout().getLineForOffset(cursorPosition);
            int lineTop = getLayout().getLineTop(line);
            int lineBottom = getLayout().getLineBottom(line);

            int visibleTop = getScrollY();
            int visibleBottom = visibleTop + getHeight();

            // 垂直滚动逻辑
            if (lineTop < visibleTop) {
                // 向上滚动使新行可见
                safeScrollTo(0, lineTop);
            } else if (lineBottom > visibleBottom) {
                // 向下滚动使新行完整可见
                int targetY = lineBottom - getHeight() + getPaddingBottom();
                safeScrollTo(0, targetY);
            }

            // 水平滚动逻辑
            float cursorX = calculatePreciseCursorX(cursorPosition);
            float visibleRight = getScrollX() + getWidth() - getPaddingRight();
            if (cursorX > visibleRight) {
                // 向右滚动使光标可见
                safeScrollTo((int) (cursorX - getWidth() + getPaddingRight()), getScrollY());
            } else if (cursorX < getScrollX() + getPaddingLeft()) {
                // 向左滚动使光标可见
                safeScrollTo((int) (cursorX - getPaddingLeft()), getScrollY());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error scrolling to cursor: " + e.getMessage());
        }
    }

    // 精确计算光标位置（考虑滚动偏移）
    private float calculatePreciseCursorX(int position) {
        if (getLayout() == null) return getPaddingStart();

        try {
            int line = getLayout().getLineForOffset(position);
            float x = getLayout().getPrimaryHorizontal(position);
            // 添加内边距
            return x + getPaddingLeft();
        } catch (Exception e) {
            return getPaddingStart();
        }
    }

    // 滚动节流
    private void safeScrollTo(int x, int y) {
        long now = System.currentTimeMillis();
        if (now - lastScrollTime > SCROLL_THROTTLE) {
            scrollTo(x, y);
            lastScrollTime = now;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (shouldDrawCursor()) {
            float x = calculatePreciseCursorX(cursorPosition);
            int line = getLayout().getLineForOffset(cursorPosition);
            float baseline = getLayout().getLineBaseline(line);
            canvas.drawLine(x, baseline - getLayout().getLineDescent(line),
                    x, baseline + getLayout().getLineAscent(line), cursorPaint);
        }
    }

    private boolean shouldDrawCursor() {
        return isCursorActive &&
                showCursor &&
                layoutFinished &&
                getLayout() != null &&
                cursorPosition <= getLayout().getText().length();
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
            handler.postDelayed(this, CURSOR_BLINK_DELAY);
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

    @Override
    public void scrollTo(int x, int y) {
        // 防止过度滚动
        int maxX = getLayout().getWidth() - getWidth();
        int maxY = getLayout().getHeight() - getHeight();

        x = Math.max(0, Math.min(x, maxX));
        y = Math.max(0, Math.min(y, maxY));

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

    // ========== 公共API ==========

    public void setCursorColor(int color) {
        cursorColor = color;
        cursorPaint.setColor(color);
        invalidate();
    }

    public void setCursorWidth(float widthDp) {
        cursorWidth = widthDp;
        cursorPaint.setStrokeWidth(dpToPx(widthDp));
        invalidate();
    }

    public void pauseTyping() {
        handler.removeCallbacks(typeRunnable);
        isTyping = false;
    }

    public void resumeTyping() {
        if (!charQueue.isEmpty() && !isTyping) {
            isTyping = true;
            handler.post(typeRunnable);
        }
    }

    public void clearText() {
        handler.removeCallbacks(typeRunnable);
        charQueue.clear();
        pendingItems.clear();
        ssb.clear();
        setText("");
        setCursorPosition(0);
        isTyping = false;
    }
}
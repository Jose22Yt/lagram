package org.telegram.ui.lagram.player;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

/**
 * exteraless-style Material 3 Expressive WavySeekBar, optimized with a 128-entry
 * precomputed sine LUT (zero Math.sin calls per frame) and VSYNC-locked animation.
 */
public class WavySeekBar extends View {

    public interface Delegate {
        void onSeekStart();
        void onSeekMove(float progress, float thumbCenterX);
        void onSeekEnd(float progress, boolean commit);
    }

    private static final float TAU = 6.2831853f;
    private static final int SIN_LUT_SIZE = 128;
    private static final int SIN_LUT_MASK = SIN_LUT_SIZE - 1;
    private static final float LUT_PER_RAD = SIN_LUT_SIZE / TAU;
    private static final float[] SIN_LUT = new float[SIN_LUT_SIZE];
    static {
        for (int i = 0; i < SIN_LUT_SIZE; i++) {
            SIN_LUT[i] = (float) Math.sin((i * 6.283185307179586) / SIN_LUT_SIZE);
        }
    }

    private final Paint wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private Delegate delegate;
    private float progress;
    private float dragProgress;
    private boolean dragging;
    private boolean playing;
    private float amplitude;
    private float phase;
    private long lastFrame;
    private final float maxAmplitude = dp(3.5f);

    public WavySeekBar(Context context) {
        super(context);
        wavePaint.setStyle(Paint.Style.STROKE);
        wavePaint.setStrokeWidth(dp(4));
        wavePaint.setStrokeCap(Paint.Cap.ROUND);
        wavePaint.setStrokeJoin(Paint.Join.ROUND);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeWidth(dp(4));
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
    }

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public void setColors(int active, int inactive) {
        wavePaint.setColor(active);
        fillPaint.setColor(active);
        trackPaint.setColor(inactive);
        invalidate();
    }

    public void setProgress(float value) {
        if (!dragging && progress != value) {
            progress = Math.max(0f, Math.min(1f, value));
            invalidate();
        }
    }

    public float getProgress() {
        return dragging ? dragProgress : progress;
    }

    public void setPlaying(boolean value) {
        if (playing != value) {
            playing = value;
            lastFrame = 0;
            postInvalidateOnAnimation();
        }
    }

    public boolean isDragging() {
        return dragging;
    }

    private float left() {
        return dp(3);
    }

    private float right() {
        return getWidth() - dp(3);
    }

    private float thumbXFor(float p) {
        return left() + p * Math.max(1f, right() - left());
    }

    private float progressAt(float x) {
        float w = right() - left();
        if (w <= 0f) return 0f;
        return Math.max(0f, Math.min(1f, (x - left()) / w));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dragging = true;
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                dragProgress = progressAt(event.getX());
                performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                if (delegate != null) {
                    delegate.onSeekStart();
                    delegate.onSeekMove(dragProgress, thumbXFor(dragProgress));
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    dragProgress = progressAt(event.getX());
                    if (delegate != null) {
                        delegate.onSeekMove(dragProgress, thumbXFor(dragProgress));
                    }
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (dragging) {
                    dragging = false;
                    progress = dragProgress;
                    if (delegate != null) {
                        delegate.onSeekEnd(progress, true);
                    }
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    dragging = false;
                    if (delegate != null) {
                        delegate.onSeekEnd(progress, false);
                    }
                    invalidate();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        long now = SystemClock.elapsedRealtime();
        float dt = lastFrame == 0 ? 0.016f : Math.min(0.05f, Math.max(0f, (now - lastFrame) / 1000f));
        lastFrame = now;
        float targetAmp = (playing && !dragging) ? maxAmplitude : 0f;
        amplitude += (targetAmp - amplitude) * Math.min(1f, dt * 8f);
        if (Math.abs(amplitude - targetAmp) < dpf2(0.06f)) {
            amplitude = targetAmp;
        }
        if (amplitude > 0.01f) {
            phase = (phase + 5.6f * dt) % TAU;
        }

        float cy = getHeight() / 2f;
        float left = left();
        float right = right();
        float p = dragging ? dragProgress : progress;
        float thumbW = dragging ? dp(3) : dp(5);
        float x = left + p * Math.max(0f, right - left);
        float gap = dp(6);
        float waveEnd = x - gap - thumbW / 2f;
        if (waveEnd > left) {
            if (amplitude > 0.05f) {
                float lambda = dp(32);
                float lutStep = (TAU / lambda) * LUT_PER_RAD;
                float phaseLut = phase * LUT_PER_RAD;
                float step = dp(3.5f);
                path.rewind();
                boolean first = true;
                for (float px = left; ; px += step) {
                    boolean last = px >= waveEnd;
                    float cx = last ? waveEnd : px;
                    int idx = ((int) (cx * lutStep - phaseLut)) & SIN_LUT_MASK;
                    float y = cy + amplitude * SIN_LUT[idx];
                    if (first) {
                        path.moveTo(cx, y);
                        first = false;
                    } else {
                        path.lineTo(cx, y);
                    }
                    if (last) break;
                }
                canvas.drawPath(path, wavePaint);
            } else {
                canvas.drawLine(left, cy, waveEnd, cy, wavePaint);
            }
        }
        float stopX = right - dp(1);
        float restX = Math.min(stopX, x + thumbW / 2f + gap);
        if (restX < stopX) {
            canvas.drawLine(restX, cy, stopX, cy, trackPaint);
        }
        canvas.drawCircle(stopX, cy, dp(2), fillPaint);
        rect.set(x - thumbW / 2f, cy - dp(13), x + thumbW / 2f, cy + dp(13));
        canvas.drawRoundRect(rect, thumbW * 0.5f, thumbW * 0.5f, fillPaint);

        if (amplitude > 0.01f || targetAmp != amplitude) {
            postInvalidateOnAnimation();
        } else {
            lastFrame = 0;
        }
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName("android.widget.SeekBar");
        info.setRangeInfo(AccessibilityNodeInfo.RangeInfo.obtain(AccessibilityNodeInfo.RangeInfo.RANGE_TYPE_PERCENT, 0, 100, getProgress() * 100f));
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
        info.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD || action == AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) {
            float value = Math.max(0f, Math.min(1f, progress + (action == AccessibilityNodeInfo.ACTION_SCROLL_FORWARD ? 0.05f : -0.05f)));
            progress = value;
            if (delegate != null) {
                delegate.onSeekEnd(value, true);
            }
            invalidate();
            return true;
        }
        return super.performAccessibilityAction(action, arguments);
    }
}

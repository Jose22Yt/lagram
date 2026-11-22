package org.telegram.ui.lagram;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.telegram.messenger.LagramConfig;
import org.telegram.ui.Components.CubicBezierInterpolator;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Authentic Opexgram-style per-character typing animation engine.
 *
 * Attaches cleanly to any EditText (ChatActivityEnterView.messageEditText and the real interactive
 * preview in LagramChatsActivity). Newly typed characters smoothly animate in with user-selected
 * Blur/Fade, Slide, Spring Scale, and Rotate effects at 60/120fps with 0% CPU when idle.
 */
public final class LagramTypingAnimEngine implements TextWatcher, Runnable {

    private static final CubicBezierInterpolator EASE_OUT = CubicBezierInterpolator.EASE_OUT_QUINT;

    private final WeakReference<EditText> editTextRef;
    private final ArrayList<CharAnimSpan> activeSpans = new ArrayList<>();
    private int pendingStart = -1;
    private int pendingCount = 0;
    private boolean frameScheduled = false;
    private boolean internalEdit = false;

    private LagramTypingAnimEngine(EditText editText) {
        this.editTextRef = new WeakReference<>(editText);
    }

    public static LagramTypingAnimEngine attach(@NonNull EditText editText) {
        LagramTypingAnimEngine engine = new LagramTypingAnimEngine(editText);
        editText.addTextChangedListener(engine);
        return engine;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // No-op
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (internalEdit) {
            return;
        }
        LagramConfig.ensureLoaded();
        if (!LagramConfig.textAppearAnimation) {
            pendingStart = -1;
            pendingCount = 0;
            return;
        }
        if (!LagramConfig.textAnimBlur && !LagramConfig.textAnimSlide
                && !LagramConfig.textAnimScale && !LagramConfig.textAnimRotate) {
            pendingStart = -1;
            pendingCount = 0;
            return;
        }
        // Only animate interactive typing (1..4 chars at a time), never bulk pastes
        if (count > 0 && count <= 4 && count >= before) {
            pendingStart = start + Math.max(0, before);
            pendingCount = count - Math.max(0, before);
            if (pendingCount <= 0) {
                pendingStart = start;
                pendingCount = count;
            }
        } else {
            pendingStart = -1;
            pendingCount = 0;
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (internalEdit || editable == null) {
            return;
        }
        if (pendingStart < 0 || pendingCount <= 0) {
            return;
        }
        int start = pendingStart;
        int end = Math.min(editable.length(), start + pendingCount);
        pendingStart = -1;
        pendingCount = 0;
        if (start >= end) {
            return;
        }

        // Skip characters covered by custom emoji / image spans or whitespace-only
        long now = SystemClock.uptimeMillis();
        int durationMs = LagramConfig.getTextAnimDurationMs();
        boolean blur = LagramConfig.textAnimBlur;
        boolean slide = LagramConfig.textAnimSlide;
        boolean scale = LagramConfig.textAnimScale;
        boolean rotate = LagramConfig.textAnimRotate;

        int i = start;
        while (i < end) {
            char c = editable.charAt(i);
            if (Character.isHighSurrogate(c) || Character.isLowSurrogate(c) || Character.isWhitespace(c)) {
                i++;
                continue;
            }
            // Don't interfere with existing ReplacementSpans (like EmojiSpan / AnimatedEmojiSpan)
            ReplacementSpan[] existing = editable.getSpans(i, i + 1, ReplacementSpan.class);
            if (existing != null && existing.length > 0) {
                i++;
                continue;
            }
            CharAnimSpan span = new CharAnimSpan(now, durationMs, blur, slide, scale, rotate);
            editable.setSpan(span, i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            activeSpans.add(span);
            i++;
        }

        if (!activeSpans.isEmpty()) {
            scheduleNextFrame();
        }
    }

    private void scheduleNextFrame() {
        if (frameScheduled) {
            return;
        }
        EditText et = editTextRef.get();
        if (et == null) {
            activeSpans.clear();
            return;
        }
        frameScheduled = true;
        et.postOnAnimation(this);
    }

    @Override
    public void run() {
        frameScheduled = false;
        EditText et = editTextRef.get();
        if (et == null || !et.isAttachedToWindow()) {
            activeSpans.clear();
            return;
        }
        long now = SystemClock.uptimeMillis();
        Editable editable = et.getText();
        boolean anyRunning = false;

        for (int i = activeSpans.size() - 1; i >= 0; i--) {
            CharAnimSpan span = activeSpans.get(i);
            if (span.isFinished(now)) {
                activeSpans.remove(i);
                if (editable != null) {
                    int s = editable.getSpanStart(span);
                    if (s >= 0) {
                        internalEdit = true;
                        try {
                            editable.removeSpan(span);
                        } finally {
                            internalEdit = false;
                        }
                    }
                }
            } else {
                anyRunning = true;
            }
        }

        et.invalidate();
        if (anyRunning) {
            scheduleNextFrame();
        }
    }

    private static final class CharAnimSpan extends ReplacementSpan {
        private final long startTime;
        private final int durationMs;
        private final boolean blur;
        private final boolean slide;
        private final boolean scale;
        private final boolean rotate;

        CharAnimSpan(long startTime, int durationMs, boolean blur, boolean slide, boolean scale, boolean rotate) {
            this.startTime = startTime;
            this.durationMs = Math.max(80, durationMs);
            this.blur = blur;
            this.slide = slide;
            this.scale = scale;
            this.rotate = rotate;
        }

        boolean isFinished(long now) {
            return (now - startTime) >= durationMs;
        }

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
            return Math.round(paint.measureText(text, start, end));
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end,
                         float x, int top, int y, int bottom, @NonNull Paint paint) {
            long elapsed = SystemClock.uptimeMillis() - startTime;
            float raw = Math.max(0f, Math.min(1f, elapsed / (float) durationMs));
            float p = EASE_OUT.getInterpolation(raw);

            float charW = paint.measureText(text, start, end);
            float centerX = x + charW * 0.5f;
            float centerY = y - paint.getTextSize() * 0.35f;

            float dy = slide ? (1f - p) * dp(9f) : 0f;
            // Subtle spring overshoot on scale (0.35 -> 1.12 -> 1.0)
            float sc = 1f;
            if (scale) {
                float overshoot = (float) (Math.sin(raw * Math.PI) * 0.14f * (1f - raw * 0.35f));
                sc = 0.35f + 0.65f * p + overshoot;
            }
            float rot = rotate ? (1f - p) * -18f : 0f;

            int savedAlpha = paint.getAlpha();
            canvas.save();
            canvas.translate(centerX, centerY + dy);
            if (scale) {
                canvas.scale(sc, sc);
            }
            if (rotate) {
                canvas.rotate(rot);
            }

            if (blur && p < 0.92f) {
                // Soft blur halo pass (zero GPU allocation, works on every API)
                float spread = (1f - p) * dp(2.2f);
                int haloAlpha = Math.round(savedAlpha * (0.28f * (1f - p) + 0.12f * p));
                paint.setAlpha(haloAlpha);
                canvas.drawText(text, start, end, -charW * 0.5f - spread, y - centerY, paint);
                canvas.drawText(text, start, end, -charW * 0.5f + spread, y - centerY, paint);
                paint.setAlpha(Math.round(savedAlpha * (0.18f + 0.82f * p)));
                canvas.drawText(text, start, end, -charW * 0.5f, y - centerY, paint);
            } else {
                paint.setAlpha(blur ? Math.round(savedAlpha * p) : savedAlpha);
                canvas.drawText(text, start, end, -charW * 0.5f, y - centerY, paint);
            }

            paint.setAlpha(savedAlpha);
            canvas.restore();
        }
    }
}

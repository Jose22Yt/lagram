package org.telegram.ui.lagram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.palette.graphics.Palette;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.LayoutHelper;

/**
 * Modern Material 3 / exteraless-style music player built from scratch for Lagram.
 * Supports animated waveform seekbar, real cover dynamic Palette tinting,
 * speed/repeat/shuffle/prev/play-pause/next controls, and one-tap fallback to classic player.
 */
public class LagramAudioPlayerAlert extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    private final LinearLayout rootCard;
    private final ImageView coverView;
    private final TextView coverPlaceholder;
    private final TextView titleView;
    private final TextView artistView;
    private final WaveSeekBar waveSeekBar;
    private final TextView elapsedView;
    private final TextView durationView;
    private final TextView playPauseBtn;
    private final TextView speedBtn;
    private final TextView repeatBtn;
    private final TextView shuffleBtn;
    private final TextView classicBtn;

    private final int currentAccount;
    private int dynamicBgColor;
    private int dynamicAccentColor;
    private Bitmap lastCoverBitmap;
    private boolean isDraggingSeek;

    public static BottomSheet create(Context context, Theme.ResourcesProvider resourcesProvider) {
        LagramConfig.ensureLoaded();
        if (LagramConfig.playerClassicMode) {
            return new AudioPlayerAlert(context, resourcesProvider);
        }
        return new LagramAudioPlayerAlert(context, resourcesProvider);
    }

    public LagramAudioPlayerAlert(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context, false, resourcesProvider);
        LagramConfig.ensureLoaded();
        currentAccount = UserConfig.selectedAccount;
        LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColors();
        dynamicBgColor = LagramThemeEngine.getGraphiteCardColor(Theme.isCurrentThemeDark());
        dynamicAccentColor = accent.primary;

        rootCard = new LinearLayout(context);
        rootCard.setOrientation(LinearLayout.VERTICAL);
        rootCard.setPadding(AndroidUtilities.dp(20), AndroidUtilities.dp(14), AndroidUtilities.dp(20), AndroidUtilities.dp(22));
        applyCardBackground();

        // Top drag handle + Classic switch button
        LinearLayout topRow = new LinearLayout(context);
        topRow.setOrientation(LinearLayout.HORIZONTAL);
        topRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView brandLabel = new TextView(context);
        brandLabel.setText("Reproductor Lagram");
        brandLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12.5f);
        brandLabel.setTypeface(AndroidUtilities.bold());
        brandLabel.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_SECONDARY);
        topRow.addView(brandLabel, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        classicBtn = new TextView(context);
        classicBtn.setText("Clásico / Cola");
        classicBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        classicBtn.setTypeface(AndroidUtilities.bold());
        classicBtn.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_PRIMARY);
        classicBtn.setPadding(AndroidUtilities.dp(12), AndroidUtilities.dp(6), AndroidUtilities.dp(12), AndroidUtilities.dp(6));
        GradientDrawable classicBg = new GradientDrawable();
        classicBg.setCornerRadius(AndroidUtilities.dp(12));
        classicBg.setColor(LagramThemeEngine.withAlpha(Color.WHITE, 22));
        classicBtn.setBackground(classicBg);
        classicBtn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            dismiss();
            new AudioPlayerAlert(context, resourcesProvider).show();
        });
        topRow.addView(classicBtn, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        rootCard.addView(topRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Album Cover container
        FrameLayout coverContainer = new FrameLayout(context);
        GradientDrawable coverBg = new GradientDrawable();
        coverBg.setCornerRadius(AndroidUtilities.dp(22));
        coverBg.setColor(LagramThemeEngine.withAlpha(dynamicAccentColor, 50));
        coverContainer.setBackground(coverBg);
        coverContainer.setClipToOutline(true);

        coverPlaceholder = new TextView(context);
        coverPlaceholder.setText("♪");
        coverPlaceholder.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 56);
        coverPlaceholder.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_PRIMARY);
        coverPlaceholder.setGravity(Gravity.CENTER);
        coverContainer.addView(coverPlaceholder, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        coverView = new ImageView(context);
        coverView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverContainer.addView(coverView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        rootCard.addView(coverContainer, LayoutHelper.createLinear(210, 210, Gravity.CENTER_HORIZONTAL, 0, 0, 0, 18));

        // Track title & artist
        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_PRIMARY);
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setGravity(Gravity.CENTER_HORIZONTAL);
        rootCard.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        artistView = new TextView(context);
        artistView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13.5f);
        artistView.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_SECONDARY);
        artistView.setSingleLine(true);
        artistView.setEllipsize(TextUtils.TruncateAt.END);
        artistView.setGravity(Gravity.CENTER_HORIZONTAL);
        rootCard.addView(artistView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 16));

        // Animated Waveform SeekBar
        waveSeekBar = new WaveSeekBar(context);
        waveSeekBar.setListener(new WaveSeekBar.SeekListener() {
            @Override
            public void onSeekStart() {
                isDraggingSeek = true;
            }

            @Override
            public void onSeekChanged(float progress) {
                MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
                if (msg != null) {
                    int dur = (int) Math.max(0, msg.getDuration());
                    elapsedView.setText(formatTime(Math.round(dur * progress)));
                }
            }

            @Override
            public void onSeekCommit(float progress) {
                isDraggingSeek = false;
                MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
                if (msg != null) {
                    MediaController.getInstance().seekToProgress(msg, progress);
                }
            }
        });
        rootCard.addView(waveSeekBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 38));

        // Time row
        LinearLayout timeRow = new LinearLayout(context);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        elapsedView = new TextView(context);
        elapsedView.setText("0:00");
        elapsedView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
        elapsedView.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_SECONDARY);
        timeRow.addView(elapsedView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        durationView = new TextView(context);
        durationView.setText("0:00");
        durationView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
        durationView.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_SECONDARY);
        durationView.setGravity(Gravity.END);
        timeRow.addView(durationView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));
        rootCard.addView(timeRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 2, 0, 14));

        // Main transport controls (Prev / Play-Pause / Next)
        LinearLayout controlsRow = new LinearLayout(context);
        controlsRow.setOrientation(LinearLayout.HORIZONTAL);
        controlsRow.setGravity(Gravity.CENTER);

        TextView prevBtn = createCircleControl(context, "⏮", 48, false);
        prevBtn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            MediaController.getInstance().playPreviousMessage();
            syncWithMediaController();
        });
        controlsRow.addView(prevBtn, LayoutHelper.createLinear(48, 48, 0, 0, 18, 0));

        playPauseBtn = createCircleControl(context, "▶", 64, true);
        playPauseBtn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
            if (msg != null) {
                if (MediaController.getInstance().isMessagePaused()) {
                    MediaController.getInstance().playMessage(msg);
                } else {
                    MediaController.getInstance().pauseMessage(msg);
                }
                syncWithMediaController();
            }
        });
        controlsRow.addView(playPauseBtn, LayoutHelper.createLinear(64, 64));

        TextView nextBtn = createCircleControl(context, "⏭", 48, false);
        nextBtn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            MediaController.getInstance().playNextMessage();
            syncWithMediaController();
        });
        controlsRow.addView(nextBtn, LayoutHelper.createLinear(48, 48, 18, 0, 0, 0));
        rootCard.addView(controlsRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 16));

        // Secondary utility pills (Shuffle / Speed / Repeat)
        LinearLayout extraRow = new LinearLayout(context);
        extraRow.setOrientation(LinearLayout.HORIZONTAL);
        extraRow.setGravity(Gravity.CENTER);

        shuffleBtn = createPillControl(context, "Aleatorio");
        shuffleBtn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            SharedConfig.setPlaybackOrderType(SharedConfig.shuffleMusic ? 0 : 2);
            updateSecondaryControls();
        });
        extraRow.addView(shuffleBtn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 0, 0, 6, 0));

        speedBtn = createPillControl(context, "1.0x");
        speedBtn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            float cur = MediaController.getInstance().getPlaybackSpeed(true);
            float next = cur < 1.25f ? 1.5f : (cur < 1.75f ? 2.0f : 1.0f);
            MediaController.getInstance().setPlaybackSpeed(true, next);
            updateSecondaryControls();
        });
        extraRow.addView(speedBtn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 3, 0, 3, 0));

        repeatBtn = createPillControl(context, "Repetir");
        repeatBtn.setOnClickListener(v -> {
            LagramThemeEngine.performHaptic(v, HapticFeedbackConstants.KEYBOARD_TAP);
            SharedConfig.setRepeatMode((SharedConfig.repeatMode + 1) % 3);
            updateSecondaryControls();
        });
        extraRow.addView(repeatBtn, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 6, 0, 0, 0));

        rootCard.addView(extraRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        setCustomView(rootCard);

        NotificationCenter nc = NotificationCenter.getInstance(currentAccount);
        nc.addObserver(this, NotificationCenter.messagePlayingDidStart);
        nc.addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        nc.addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
        nc.addObserver(this, NotificationCenter.messagePlayingDidReset);

        syncWithMediaController();
    }

    private TextView createCircleControl(Context context, String symbol, int sizeDp, boolean primary) {
        TextView tv = new TextView(context);
        tv.setText(symbol);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, primary ? 24 : 18);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(primary ? Color.WHITE : LagramThemeEngine.GRAPHITE_TEXT_PRIMARY);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(primary ? dynamicAccentColor : LagramThemeEngine.withAlpha(Color.WHITE, 22));
        tv.setBackground(bg);
        return tv;
    }

    private TextView createPillControl(Context context, String label) {
        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(9), AndroidUtilities.dp(8), AndroidUtilities.dp(9));
        return tv;
    }

    private void applyCardBackground() {
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadii(new float[]{
                AndroidUtilities.dp(24), AndroidUtilities.dp(24),
                AndroidUtilities.dp(24), AndroidUtilities.dp(24),
                0, 0, 0, 0
        });
        bg.setColor(dynamicBgColor);
        rootCard.setBackground(bg);
    }

    private void applyAccentToViews() {
        GradientDrawable playBg = new GradientDrawable();
        playBg.setShape(GradientDrawable.OVAL);
        playBg.setColor(dynamicAccentColor);
        playPauseBtn.setBackground(playBg);
        waveSeekBar.setAccentColor(dynamicAccentColor);
        updateSecondaryControls();
    }

    private void syncWithMediaController() {
        MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
        if (msg == null) {
            titleView.setText("Sin reproducción activa");
            artistView.setText("Lagram Audio");
            playPauseBtn.setText("▶");
            waveSeekBar.setPlaying(false);
            return;
        }
        String title = msg.getMusicTitle();
        String author = msg.getMusicAuthor();
        titleView.setText(TextUtils.isEmpty(title) ? "Audio" : title);
        artistView.setText(TextUtils.isEmpty(author) ? "Artista desconocido" : author);

        boolean paused = MediaController.getInstance().isMessagePaused();
        playPauseBtn.setText(paused ? "▶" : "❚❚");
        waveSeekBar.setPlaying(!paused);

        int durationSec = (int) Math.max(0, msg.getDuration());
        durationView.setText(formatTime(durationSec));
        if (!isDraggingSeek) {
            waveSeekBar.setProgress(msg.audioProgress);
            elapsedView.setText(formatTime(msg.audioProgressSec));
        }

        AudioInfo info = MediaController.getInstance().getAudioInfo();
        Bitmap cover = info != null ? info.getCover() : null;
        if (cover != null && !cover.isRecycled()) {
            coverView.setImageBitmap(cover);
            coverView.setVisibility(View.VISIBLE);
            coverPlaceholder.setVisibility(View.GONE);
            if (LagramConfig.playerDynamicCoverPalette && cover != lastCoverBitmap) {
                lastCoverBitmap = cover;
                Palette.from(cover).generate(palette -> {
                    if (palette == null || !isShowing()) {
                        return;
                    }
                    int vibrant = palette.getVibrantColor(LagramThemeEngine.getAccentColors().primary);
                    int darkMuted = palette.getDarkMutedColor(LagramThemeEngine.getGraphiteCardColor(true));
                    dynamicAccentColor = vibrant;
                    dynamicBgColor = LagramThemeEngine.blendColor(darkMuted, 0xFF16181E, 0.45f);
                    applyCardBackground();
                    applyAccentToViews();
                });
            }
        } else {
            coverView.setVisibility(View.GONE);
            coverPlaceholder.setVisibility(View.VISIBLE);
        }

        updateSecondaryControls();
    }

    private void updateSecondaryControls() {
        stylePill(shuffleBtn, SharedConfig.shuffleMusic, SharedConfig.shuffleMusic ? "Aleatorio ON" : "Aleatorio");
        float speed = MediaController.getInstance().getPlaybackSpeed(true);
        String speedText = speed >= 1.9f ? "2.0x" : (speed >= 1.4f ? "1.5x" : "1.0x");
        stylePill(speedBtn, speed > 1.05f, speedText);
        String repeatLabel = SharedConfig.repeatMode == 2 ? "Repetir 1" : (SharedConfig.repeatMode == 1 ? "Repetir todo" : "Repetir");
        stylePill(repeatBtn, SharedConfig.repeatMode > 0, repeatLabel);
    }

    private void stylePill(TextView tv, boolean active, String text) {
        tv.setText(text);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(AndroidUtilities.dp(14));
        if (active) {
            bg.setColor(LagramThemeEngine.withAlpha(dynamicAccentColor, 55));
            tv.setTextColor(dynamicAccentColor);
        } else {
            bg.setColor(LagramThemeEngine.withAlpha(Color.WHITE, 18));
            tv.setTextColor(LagramThemeEngine.GRAPHITE_TEXT_SECONDARY);
        }
        tv.setBackground(bg);
    }

    private static String formatTime(int seconds) {
        int s = Math.max(0, seconds);
        int mins = s / 60;
        int rem = s % 60;
        return mins + ":" + (rem < 10 ? "0" : "") + rem;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.messagePlayingProgressDidChanged) {
            MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
            if (msg != null && !isDraggingSeek) {
                waveSeekBar.setProgress(msg.audioProgress);
                elapsedView.setText(formatTime(msg.audioProgressSec));
            }
        } else if (id == NotificationCenter.messagePlayingDidReset) {
            if (MediaController.getInstance().getPlayingMessageObject() == null) {
                dismiss();
            } else {
                syncWithMediaController();
            }
        } else {
            syncWithMediaController();
        }
    }

    @Override
    public void dismissInternal() {
        super.dismissInternal();
        lastCoverBitmap = null;
        NotificationCenter nc = NotificationCenter.getInstance(currentAccount);
        nc.removeObserver(this, NotificationCenter.messagePlayingDidStart);
        nc.removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        nc.removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
        nc.removeObserver(this, NotificationCenter.messagePlayingDidReset);
    }

    /**
     * Smooth 120 Hz exteraless-style animated sine-wave seekbar with touch seeking.
     */
    private static final class WaveSeekBar extends View {
        interface SeekListener {
            void onSeekStart();
            void onSeekChanged(float progress);
            void onSeekCommit(float progress);
        }

        private final Paint activePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint inactivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path wavePath = new Path();
        private final RectF thumbRect = new RectF();

        private float progress = 0f;
        private boolean playing = false;
        private float waveAmplitude = 0f;
        private float phase = 0f;
        private long lastFrameTime = 0L;
        private SeekListener listener;

        WaveSeekBar(Context context) {
            super(context);
            activePaint.setStyle(Paint.Style.STROKE);
            activePaint.setStrokeCap(Paint.Cap.ROUND);
            activePaint.setStrokeWidth(AndroidUtilities.dp(3.2f));
            activePaint.setColor(LagramThemeEngine.getAccentColors().primary);

            inactivePaint.setStyle(Paint.Style.STROKE);
            inactivePaint.setStrokeCap(Paint.Cap.ROUND);
            inactivePaint.setStrokeWidth(AndroidUtilities.dp(3f));
            inactivePaint.setColor(0x44FFFFFF);

            thumbPaint.setStyle(Paint.Style.FILL);
            thumbPaint.setColor(LagramThemeEngine.getAccentColors().primary);
        }

        void setListener(SeekListener listener) {
            this.listener = listener;
        }

        void setAccentColor(int color) {
            activePaint.setColor(color);
            thumbPaint.setColor(color);
            invalidate();
        }

        void setProgress(float p) {
            progress = Math.max(0f, Math.min(1f, p));
            invalidate();
        }

        void setPlaying(boolean playing) {
            this.playing = playing;
            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int w = getWidth();
            if (w <= 0) {
                return false;
            }
            float pad = AndroidUtilities.dp(8);
            float usable = Math.max(1f, w - pad * 2f);
            float p = Math.max(0f, Math.min(1f, (event.getX() - pad) / usable));
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    getParent().requestDisallowInterceptTouchEvent(true);
                    if (listener != null) listener.onSeekStart();
                    setProgress(p);
                    if (listener != null) listener.onSeekChanged(p);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    setProgress(p);
                    if (listener != null) listener.onSeekChanged(p);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    setProgress(p);
                    if (listener != null) listener.onSeekCommit(p);
                    return true;
            }
            return super.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            float pad = AndroidUtilities.dp(8);
            float left = pad;
            float right = w - pad;
            float cy = h / 2f;
            float splitX = left + (right - left) * progress;

            // Inactive remaining track
            if (splitX < right) {
                canvas.drawLine(splitX + AndroidUtilities.dp(4), cy, right, cy, inactivePaint);
            }

            boolean animateWave = playing && LagramConfig.playerWaveAnimation && LagramConfig.animationPreset != LagramConfig.ANIM_NONE;
            long now = SystemClock.uptimeMillis();
            float dt = lastFrameTime == 0 ? 0.016f : Math.min(0.05f, (now - lastFrameTime) / 1000f);
            lastFrameTime = now;

            float targetAmp = animateWave ? AndroidUtilities.dp(4.2f) : 0f;
            waveAmplitude += (targetAmp - waveAmplitude) * Math.min(1f, dt * 10f);
            if (animateWave) {
                phase += dt * 5.5f;
            }

            // Active wavy or flat track
            if (splitX > left) {
                if (waveAmplitude > 0.25f) {
                    wavePath.reset();
                    float step = AndroidUtilities.dp(3);
                    float freq = 0.085f / Math.max(0.5f, AndroidUtilities.density / 2f);
                    wavePath.moveTo(left, cy + (float) Math.sin(phase) * waveAmplitude);
                    for (float x = left + step; x <= splitX; x += step) {
                        float y = cy + (float) Math.sin((x - left) * freq + phase) * waveAmplitude;
                        wavePath.lineTo(x, y);
                    }
                    canvas.drawPath(wavePath, activePaint);
                } else {
                    canvas.drawLine(left, cy, splitX, cy, activePaint);
                }
            }

            // M3 pill thumb
            float thumbW = AndroidUtilities.dp(4.5f);
            float thumbH = AndroidUtilities.dp(18);
            thumbRect.set(splitX - thumbW / 2f, cy - thumbH / 2f, splitX + thumbW / 2f, cy + thumbH / 2f);
            canvas.drawRoundRect(thumbRect, thumbW / 2f, thumbW / 2f, thumbPaint);

            if (isAttachedToWindow() && (animateWave || Math.abs(targetAmp - waveAmplitude) > 0.2f)) {
                postInvalidateOnAnimation();
            }
        }
    }
}

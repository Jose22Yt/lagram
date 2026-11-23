package org.telegram.ui.lagram.player;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AudioPlayerAlert;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Full-screen exteraless-style Material 3 Expressive Music Player for Lagram.
 * Features:
 * - Dynamic Material 3 tonal palette extracted asynchronously from album art (smooth crossfade)
 * - Swipeable expressive album cover card with breathing pause/play scale spring
 * - LUT-accelerated WavySeekBar with floating scrub time bubble & tabular timestamps
 * - Connected 5-button spring ControlsRow (Shuffle/Reverse, Prev, Morphing Play/Pause, Next, Repeat/Repeat-1)
 * - Bottom segmented action island (Playback Speed cycler + inline Playlist Queue sheet)
 * - Zero Liquid Glass by default (solid tonal Material 3 surfaces)
 */
public class LagramPlayerSheet extends BottomSheet implements NotificationCenter.NotificationCenterDelegate {

    public static LagramPlayerSheet instance;

    private static final float[] SPEEDS = {1f, 1.2f, 1.5f, 1.7f, 2f, 0.5f};

    private final int account;
    private final boolean dark;
    private final int fallbackSeed;
    private PlayerColors colors;
    private ValueAnimator colorAnimator;

    private final FrameLayout root;
    private final LinearLayout mainColumn;
    private final ImageView collapseBtn;
    private final ImageView classicBtn;
    private final PlayerIcon collapseIcon = PlayerIcon.stroke(PlayerIcon.CHEVRON_DOWN, 24);
    private final PlayerIcon moreIcon = PlayerIcon.fill(PlayerIcon.MORE, 22);
    private final TextView headerSubtitle;

    private final FrameLayout coverHost;
    private final CoverImage cover;
    private final TextView titleView;
    private final TextView artistView;

    private final FrameLayout seekHost;
    private final WavySeekBar seekBar;
    private final TextView scrubBubble;
    private final GradientDrawable scrubBubbleBg = new GradientDrawable();
    private final TextView timeNow;
    private final TextView timeLeft;

    private final ControlsRow controls;
    private final LinearLayout actionIsland;
    private final MorphButton speedButton;
    private final MorphButton queueButton;

    // Inline M3 Queue overlay
    private final FrameLayout queueOverlay;
    private final TextView queueTitle;
    private final ImageView queueCloseBtn;
    private final PlayerIcon closeIcon = PlayerIcon.stroke(PlayerIcon.CLOSE, 22);
    private final RecyclerListView queueList;
    private final QueueAdapter queueAdapter;
    private boolean queueOpen;

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Matrix bgMatrix = new Matrix();
    private LinearGradient bgGradient;
    private int lastGradTop = Integer.MIN_VALUE;
    private int lastGradBot = Integer.MIN_VALUE;

    private int currentDurationSec;
    private String currentTrackKey;
    private float coverDragX;
    private float coverDownX;
    private float coverDownY;
    private boolean coverDragging;
    private final int touchSlop;

    @SuppressLint("ClickableViewAccessibility")
    public LagramPlayerSheet(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context, false, resourcesProvider);
        instance = this;
        this.account = UserConfig.selectedAccount;
        this.dark = PlayerColors.isDark(resourcesProvider);
        this.fallbackSeed = PlayerColors.fallbackSeed(resourcesProvider);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        MessageObject initial = MediaController.getInstance().getPlayingMessageObject();
        Integer cached = CoverImage.cachedSeed(initial);
        this.colors = PlayerColors.fromSeed(cached != null ? cached : fallbackSeed, dark);

        setApplyTopPadding(false);
        setApplyBottomPadding(false);
        setBackgroundColor(Color.TRANSPARENT);
        fixNavigationBar(colors.surface);

        root = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int w = MeasureSpec.getSize(widthMeasureSpec);
                int hMode = MeasureSpec.getMode(heightMeasureSpec);
                int hSize = MeasureSpec.getSize(heightMeasureSpec);
                int fullH = AndroidUtilities.displaySize.y + AndroidUtilities.statusBarHeight + AndroidUtilities.navigationBarHeight;
                int targetH = (hMode == MeasureSpec.EXACTLY || (hMode == MeasureSpec.AT_MOST && hSize > 0)) ? hSize : fullH;
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(targetH, MeasureSpec.EXACTLY)
                );
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;
                int topCol = ColorUtils.blendARGB(colors.surface, colors.primaryContainer, dark ? 0.32f : 0.24f);
                int botCol = colors.surface;
                if (bgGradient == null || lastGradTop != topCol || lastGradBot != botCol) {
                    lastGradTop = topCol;
                    lastGradBot = botCol;
                    bgGradient = new LinearGradient(0f, 0f, 0f, 1f, topCol, botCol, Shader.TileMode.CLAMP);
                    bgPaint.setShader(bgGradient);
                }
                bgMatrix.setScale(1f, h);
                bgGradient.setLocalMatrix(bgMatrix);
                canvas.drawRect(0, 0, w, h, bgPaint);
            }
        };
        root.setWillNotDraw(false);
        root.setFitsSystemWindows(false);

        mainColumn = new LinearLayout(context);
        mainColumn.setOrientation(LinearLayout.VERTICAL);
        mainColumn.setGravity(Gravity.CENTER_HORIZONTAL);
        int topPad = AndroidUtilities.statusBarHeight + dp(10);
        int botPad = AndroidUtilities.navigationBarHeight + dp(18);
        mainColumn.setPadding(dp(24), topPad, dp(24), botPad);
        root.addView(mainColumn, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        // 1. Top Header Bar
        FrameLayout header = new FrameLayout(context);
        collapseBtn = new ImageView(context);
        collapseBtn.setScaleType(ImageView.ScaleType.CENTER);
        collapseBtn.setImageDrawable(collapseIcon);
        collapseBtn.setOnClickListener(v -> dismiss());
        header.addView(collapseBtn, LayoutHelper.createFrame(44, 44, Gravity.LEFT | Gravity.CENTER_VERTICAL));

        headerSubtitle = new TextView(context);
        headerSubtitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        headerSubtitle.setTypeface(AndroidUtilities.bold());
        headerSubtitle.setSingleLine(true);
        headerSubtitle.setEllipsize(TextUtils.TruncateAt.END);
        headerSubtitle.setGravity(Gravity.CENTER);
        headerSubtitle.setText("Lagram Player");
        header.addView(headerSubtitle, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 52, 0, 52, 0));

        classicBtn = new ImageView(context);
        classicBtn.setScaleType(ImageView.ScaleType.CENTER);
        classicBtn.setImageDrawable(moreIcon);
        classicBtn.setOnClickListener(v -> {
            dismiss();
            try {
                new AudioPlayerAlert(context, resourcesProvider).show();
            } catch (Throwable ignored) {}
        });
        header.addView(classicBtn, LayoutHelper.createFrame(44, 44, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
        mainColumn.addView(header, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48));

        // 2. Expressive Swipeable Album Artwork Card
        coverHost = new FrameLayout(context);
        coverHost.setClipChildren(false);
        coverHost.setClipToPadding(false);
        cover = new CoverImage(context, 28, 72);
        cover.setElevation(dp(12));
        cover.setSeedListener(fallbackSeed, (msg, seed) -> animateColorsTo(PlayerColors.fromSeed(seed, dark)));
        coverHost.addView(cover, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER));
        cover.setOnTouchListener((v, ev) -> {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    coverDownX = ev.getRawX();
                    coverDownY = ev.getRawY();
                    coverDragX = 0f;
                    coverDragging = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = ev.getRawX() - coverDownX;
                    float dy = ev.getRawY() - coverDownY;
                    if (!coverDragging && Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                        coverDragging = true;
                        if (coverHost.getParent() != null) {
                            coverHost.getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                    if (coverDragging) {
                        coverDragX = dx * 0.65f;
                        cover.setTranslationX(coverDragX);
                        cover.setRotation(coverDragX / dp(28));
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (coverDragging) {
                        float threshold = dp(64);
                        if (coverDragX < -threshold) {
                            cover.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                            MediaController.getInstance().playNextMessage();
                        } else if (coverDragX > threshold) {
                            cover.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                            MediaController.getInstance().playPreviousMessage();
                        }
                        cover.animate().translationX(0f).rotation(0f).setDuration(260)
                                .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
                        coverDragging = false;
                    }
                    return true;
            }
            return false;
        });
        int coverSizeDp = Math.min(308, Math.max(210, Math.round(AndroidUtilities.displaySize.x / AndroidUtilities.density) - 64));
        mainColumn.addView(coverHost, LayoutHelper.createLinear(coverSizeDp, coverSizeDp, Gravity.CENTER_HORIZONTAL, 0, 14, 0, 22));

        // 3. Title & Artist
        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        titleView.setTypeface(AndroidUtilities.bold());
        titleView.setSingleLine(true);
        titleView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        titleView.setMarqueeRepeatLimit(-1);
        titleView.setSelected(true);
        titleView.setGravity(Gravity.LEFT);
        mainColumn.addView(titleView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 4));

        artistView = new TextView(context);
        artistView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        artistView.setSingleLine(true);
        artistView.setEllipsize(TextUtils.TruncateAt.END);
        artistView.setGravity(Gravity.LEFT);
        mainColumn.addView(artistView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 18));

        // 4. WavySeekBar + Floating Scrub Bubble + Tabular Time Row
        seekHost = new FrameLayout(context);
        seekHost.setClipChildren(false);
        seekBar = new WavySeekBar(context);
        seekHost.addView(seekBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.BOTTOM));

        scrubBubble = new TextView(context);
        scrubBubble.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        scrubBubble.setTypeface(AndroidUtilities.bold());
        scrubBubble.setFontFeatureSettings("tnum");
        scrubBubble.setPadding(dp(10), dp(4), dp(10), dp(4));
        scrubBubbleBg.setCornerRadius(dp(12));
        scrubBubble.setBackground(scrubBubbleBg);
        scrubBubble.setAlpha(0f);
        scrubBubble.setVisibility(View.INVISIBLE);
        seekHost.addView(scrubBubble, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP | Gravity.LEFT));
        mainColumn.addView(seekHost, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 58, 0, 0, 0, 2));

        seekBar.setDelegate(new WavySeekBar.Delegate() {
            @Override
            public void onSeekStart() {
                scrubBubble.setVisibility(View.VISIBLE);
                scrubBubble.animate().cancel();
                scrubBubble.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(140)
                        .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
            }

            @Override
            public void onSeekMove(float progress, float thumbCenterX) {
                int sec = Math.round(currentDurationSec * progress);
                String t = formatTime(sec);
                timeNow.setText(t);
                timeLeft.setText("-" + formatTime(Math.max(0, currentDurationSec - sec)));
                scrubBubble.setText(t);
                int bw = scrubBubble.getMeasuredWidth();
                if (bw <= 0) {
                    scrubBubble.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    bw = scrubBubble.getMeasuredWidth();
                }
                float maxTx = Math.max(0f, seekHost.getWidth() - bw);
                scrubBubble.setTranslationX(Math.max(0f, Math.min(maxTx, thumbCenterX - bw * 0.5f)));
            }

            @Override
            public void onSeekEnd(float progress, boolean commit) {
                scrubBubble.animate().cancel();
                scrubBubble.animate().alpha(0f).scaleX(0.9f).scaleY(0.9f).setDuration(140)
                        .withEndAction(() -> scrubBubble.setVisibility(View.INVISIBLE)).start();
                if (commit) {
                    MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
                    if (msg != null) {
                        MediaController.getInstance().seekToProgress(msg, progress);
                    }
                }
            }
        });

        LinearLayout timeRow = new LinearLayout(context);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeNow = new TextView(context);
        timeNow.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        timeNow.setTypeface(AndroidUtilities.bold());
        timeNow.setFontFeatureSettings("tnum");
        timeNow.setText("0:00");
        timeRow.addView(timeNow, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

        timeLeft = new TextView(context);
        timeLeft.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        timeLeft.setTypeface(AndroidUtilities.bold());
        timeLeft.setFontFeatureSettings("tnum");
        timeLeft.setGravity(Gravity.RIGHT);
        timeLeft.setText("-0:00");
        timeRow.addView(timeLeft, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));
        mainColumn.addView(timeRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 18));

        // 5. Connected Spring Morphing ControlsRow
        controls = new ControlsRow(context);
        controls.play.setOnClickListener(v -> {
            MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
            if (msg == null) return;
            if (MediaController.getInstance().isMessagePaused()) {
                MediaController.getInstance().playMessage(msg);
            } else {
                MediaController.getInstance().pauseMessage(msg);
            }
        });
        controls.prev.setOnClickListener(v -> MediaController.getInstance().playPreviousMessage());
        controls.next.setOnClickListener(v -> MediaController.getInstance().playNextMessage());
        controls.shuffle.setOnClickListener(v -> {
            if (SharedConfig.shuffleMusic) {
                SharedConfig.setPlaybackOrderType(1); // Reversed
            } else if (SharedConfig.playOrderReversed) {
                SharedConfig.setPlaybackOrderType(0); // Normal
            } else {
                SharedConfig.setPlaybackOrderType(2); // Shuffle
            }
            syncModes(true);
        });
        controls.repeat.setOnClickListener(v -> {
            int nextMode = (SharedConfig.repeatMode + 1) % 3;
            SharedConfig.setRepeatMode(nextMode);
            syncModes(true);
        });
        mainColumn.addView(controls, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 22));

        // 6. Bottom Material 3 Expressive Segmented Action Island (Speed + Queue)
        actionIsland = new LinearLayout(context);
        actionIsland.setOrientation(LinearLayout.HORIZONTAL);
        actionIsland.setGravity(Gravity.CENTER);

        speedButton = new MorphButton(context, PlayerIcon.stroke(PlayerIcon.SPEED, 20));
        speedButton.setRadius(dp(22), dp(10), false);
        speedButton.setOnClickListener(v -> cycleSpeed());
        actionIsland.addView(speedButton, LayoutHelper.createLinear(0, 44, 1f, 0, 0, 4, 0));

        queueButton = new MorphButton(context, PlayerIcon.stroke(PlayerIcon.QUEUE, 20));
        queueButton.setRadius(dp(10), dp(22), false);
        queueButton.setText("Playlist");
        queueButton.setOnClickListener(v -> toggleQueue(true));
        actionIsland.addView(queueButton, LayoutHelper.createLinear(0, 44, 1f, 4, 0, 0, 0));

        mainColumn.addView(new View(context), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));
        mainColumn.addView(actionIsland, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));

        // 7. Inline M3 Playlist Queue Overlay
        queueOverlay = new FrameLayout(context);
        queueOverlay.setVisibility(View.GONE);
        queueOverlay.setAlpha(0f);
        LinearLayout queueCol = new LinearLayout(context);
        queueCol.setOrientation(LinearLayout.VERTICAL);
        queueCol.setPadding(dp(16), topPad, dp(16), botPad);

        FrameLayout queueHeader = new FrameLayout(context);
        queueTitle = new TextView(context);
        queueTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 19);
        queueTitle.setTypeface(AndroidUtilities.bold());
        queueTitle.setText("Playlist");
        queueHeader.addView(queueTitle, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 8, 0, 48, 0));

        queueCloseBtn = new ImageView(context);
        queueCloseBtn.setScaleType(ImageView.ScaleType.CENTER);
        queueCloseBtn.setImageDrawable(closeIcon);
        queueCloseBtn.setOnClickListener(v -> toggleQueue(false));
        queueHeader.addView(queueCloseBtn, LayoutHelper.createFrame(44, 44, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
        queueCol.addView(queueHeader, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, 0, 0, 0, 8));

        queueList = new RecyclerListView(context);
        queueList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        queueAdapter = new QueueAdapter(context);
        queueList.setAdapter(queueAdapter);
        queueList.setOnItemClickListener((view, position) -> {
            ArrayList<MessageObject> playlist = MediaController.getInstance().getPlaylist();
            if (playlist != null && position >= 0 && position < playlist.size()) {
                MediaController.getInstance().playMessage(playlist.get(position));
                toggleQueue(false);
            }
        });
        queueCol.addView(queueList, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));
        queueOverlay.addView(queueCol, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        root.addView(queueOverlay, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        setCustomView(root);
        applyColors(colors);
        syncTrack(false);
        syncPlayState(false);
        syncModes(false);
        syncSpeed();
    }

    private void toggleQueue(boolean show) {
        if (queueOpen == show) return;
        queueOpen = show;
        queueOverlay.animate().cancel();
        if (show) {
            queueAdapter.refresh();
            queueOverlay.setVisibility(View.VISIBLE);
            queueOverlay.setTranslationY(dp(28));
            queueOverlay.animate().alpha(1f).translationY(0f).setDuration(240)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
        } else {
            queueOverlay.animate().alpha(0f).translationY(dp(28)).setDuration(180)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                    .withEndAction(() -> queueOverlay.setVisibility(View.GONE)).start();
        }
    }

    private void cycleSpeed() {
        float cur = MediaController.getInstance().getPlaybackSpeed(true);
        int idx = 0;
        for (int i = 0; i < SPEEDS.length; i++) {
            if (Math.abs(cur - SPEEDS[i]) < 0.08f) {
                idx = (i + 1) % SPEEDS.length;
                break;
            }
        }
        MediaController.getInstance().setPlaybackSpeed(true, SPEEDS[idx]);
        syncSpeed();
    }

    private void syncSpeed() {
        float spd = MediaController.getInstance().getPlaybackSpeed(true);
        DecimalFormat df = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
        speedButton.setText(df.format(spd) + "x");
        speedButton.setActive(Math.abs(spd - 1f) > 0.05f, true);
    }

    private void syncModes(boolean animated) {
        boolean shuf = SharedConfig.shuffleMusic;
        boolean rev = !shuf && SharedConfig.playOrderReversed;
        boolean rep = SharedConfig.repeatMode != 0;
        boolean repOne = SharedConfig.repeatMode == 2;
        controls.setModes(shuf, rev, rep, repOne, animated);
    }

    private void syncPlayState(boolean animated) {
        boolean playing = !MediaController.getInstance().isMessagePaused();
        controls.setPlaying(playing, animated);
        seekBar.setPlaying(playing);
        float targetScale = playing ? 1f : 0.91f;
        if (animated) {
            cover.animate().scaleX(targetScale).scaleY(targetScale).setDuration(300)
                    .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
        } else {
            cover.setScaleX(targetScale);
            cover.setScaleY(targetScale);
        }
    }

    private void syncTrack(boolean animated) {
        MessageObject msg = MediaController.getInstance().getPlayingMessageObject();
        if (msg == null) {
            dismiss();
            return;
        }
        String key = CoverImage.keyOf(msg);
        boolean changed = !TextUtils.equals(key, currentTrackKey);
        currentTrackKey = key;
        if (changed) {
            String title = msg.getMusicTitle();
            String author = msg.getMusicAuthor();
            titleView.setText(TextUtils.isEmpty(title) ? getString(R.string.AttachMusic) : title);
            artistView.setText(TextUtils.isEmpty(author) ? getString(R.string.AudioUnknownArtist) : author);
            cover.setMessage(msg);
            Integer cached = CoverImage.cachedSeed(msg);
            if (cached != null) {
                animateColorsTo(PlayerColors.fromSeed(cached, dark));
            }
            if (queueOpen) {
                queueAdapter.refresh();
            }
        }
        syncProgress(msg);
    }

    private void syncProgress(MessageObject msg) {
        if (msg == null || seekBar.isDragging()) return;
        int dur = Math.max(0, (int) Math.round(msg.getDuration()));
        currentDurationSec = dur;
        float progress = msg.audioProgress;
        int curSec = Math.max(0, Math.min(dur, msg.audioProgressSec));
        seekBar.setProgress(progress);
        timeNow.setText(formatTime(curSec));
        timeLeft.setText("-" + formatTime(Math.max(0, dur - curSec)));
    }

    private void animateColorsTo(PlayerColors target) {
        if (colorAnimator != null) {
            colorAnimator.cancel();
        }
        final PlayerColors from = this.colors;
        colorAnimator = ValueAnimator.ofFloat(0f, 1f);
        colorAnimator.setDuration(380);
        colorAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        colorAnimator.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            this.colors = PlayerColors.lerp(from, target, t);
            applyColors(this.colors);
        });
        colorAnimator.start();
    }

    private void applyColors(PlayerColors c) {
        root.invalidate();
        fixNavigationBar(c.surface);
        collapseIcon.setColor(c.onSurface);
        moreIcon.setColor(c.onSurface);
        closeIcon.setColor(c.onSurface);
        headerSubtitle.setTextColor(c.onSurfaceVariant);
        cover.setPlaceholderColors(c.surfaceContainer, c.onSurfaceVariant);
        titleView.setTextColor(c.onSurface);
        artistView.setTextColor(c.onSurfaceVariant);
        seekBar.setColors(c.primary, c.outlineVariant);
        scrubBubbleBg.setColor(c.primary);
        scrubBubble.setTextColor(c.onPrimary);
        timeNow.setTextColor(c.onSurfaceVariant);
        timeLeft.setTextColor(c.onSurfaceVariant);

        controls.play.setColors(c.primary, c.onPrimary, c.primary, c.onPrimary);
        controls.prev.setColors(c.secondaryContainer, c.onSecondaryContainer, c.secondaryContainer, c.onSecondaryContainer);
        controls.next.setColors(c.secondaryContainer, c.onSecondaryContainer, c.secondaryContainer, c.onSecondaryContainer);
        controls.shuffle.setColors(c.surfaceContainer, c.onSurfaceVariant, c.primaryContainer, c.onPrimaryContainer);
        controls.repeat.setColors(c.surfaceContainer, c.onSurfaceVariant, c.primaryContainer, c.onPrimaryContainer);
        controls.repeat.setBadgeColors(c.primary, c.onPrimary);

        speedButton.setColors(c.surfaceContainer, c.onSurface, c.primaryContainer, c.onPrimaryContainer);
        queueButton.setColors(c.surfaceContainer, c.onSurface, c.primaryContainer, c.onPrimaryContainer);

        queueOverlay.setBackgroundColor(c.surface);
        queueTitle.setTextColor(c.onSurface);
        if (queueOpen) {
            queueAdapter.notifyDataSetChanged();
        }
    }

    private static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        if (mins >= 60) {
            return String.format(Locale.US, "%d:%02d:%02d", mins / 60, mins % 60, secs);
        }
        return String.format(Locale.US, "%d:%02d", mins, secs);
    }

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationCenter.getInstance(account).addObserver(this, NotificationCenter.messagePlayingDidStart);
        NotificationCenter.getInstance(account).addObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        NotificationCenter.getInstance(account).addObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
        NotificationCenter.getInstance(account).addObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(account).addObserver(this, NotificationCenter.messagePlayingSpeedChanged);
    }

    @Override
    public void dismiss() {
        if (queueOpen) {
            toggleQueue(false);
            return;
        }
        super.dismiss();
    }

    @Override
    public void dismissInternal() {
        if (colorAnimator != null) {
            colorAnimator.cancel();
            colorAnimator = null;
        }
        NotificationCenter.getInstance(account).removeObserver(this, NotificationCenter.messagePlayingDidStart);
        NotificationCenter.getInstance(account).removeObserver(this, NotificationCenter.messagePlayingPlayStateChanged);
        NotificationCenter.getInstance(account).removeObserver(this, NotificationCenter.messagePlayingProgressDidChanged);
        NotificationCenter.getInstance(account).removeObserver(this, NotificationCenter.messagePlayingDidReset);
        NotificationCenter.getInstance(account).removeObserver(this, NotificationCenter.messagePlayingSpeedChanged);
        if (instance == this) {
            instance = null;
        }
        super.dismissInternal();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.messagePlayingDidStart) {
            syncTrack(true);
            syncPlayState(true);
        } else if (id == NotificationCenter.messagePlayingPlayStateChanged) {
            syncPlayState(true);
        } else if (id == NotificationCenter.messagePlayingProgressDidChanged) {
            syncProgress(MediaController.getInstance().getPlayingMessageObject());
        } else if (id == NotificationCenter.messagePlayingSpeedChanged) {
            syncSpeed();
        } else if (id == NotificationCenter.messagePlayingDidReset) {
            if (MediaController.getInstance().getPlayingMessageObject() == null) {
                super.dismiss();
            }
        }
    }

    private final class QueueAdapter extends RecyclerListView.SelectionAdapter {
        private final Context context;
        private final ArrayList<MessageObject> items = new ArrayList<>();

        QueueAdapter(Context context) {
            this.context = context;
        }

        void refresh() {
            items.clear();
            ArrayList<MessageObject> pl = MediaController.getInstance().getPlaylist();
            if (pl != null) {
                items.addAll(pl);
            }
            notifyDataSetChanged();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return true;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(8), dp(12), dp(8));
            row.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            CoverImage thumb = new CoverImage(context, 10, 22);
            row.addView(thumb, LayoutHelper.createLinear(44, 44, 0, 0, 12, 0));

            LinearLayout col = new LinearLayout(context);
            col.setOrientation(LinearLayout.VERTICAL);
            TextView t = new TextView(context);
            t.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            t.setTypeface(AndroidUtilities.bold());
            t.setSingleLine(true);
            t.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(t);

            TextView s = new TextView(context);
            s.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
            s.setSingleLine(true);
            s.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(s);
            row.addView(col, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

            row.setTag(new Object[]{thumb, t, s});
            return new RecyclerListView.Holder(row);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            MessageObject msg = items.get(position);
            Object[] refs = (Object[]) holder.itemView.getTag();
            CoverImage thumb = (CoverImage) refs[0];
            TextView t = (TextView) refs[1];
            TextView s = (TextView) refs[2];
            boolean isCurrent = TextUtils.equals(CoverImage.keyOf(msg), currentTrackKey);

            thumb.setPlaceholderColors(colors.surfaceContainer, colors.onSurfaceVariant);
            thumb.setMessage(msg);
            t.setText(TextUtils.isEmpty(msg.getMusicTitle()) ? getString(R.string.AttachMusic) : msg.getMusicTitle());
            s.setText(TextUtils.isEmpty(msg.getMusicAuthor()) ? getString(R.string.AudioUnknownArtist) : msg.getMusicAuthor());
            t.setTextColor(isCurrent ? colors.primary : colors.onSurface);
            s.setTextColor(colors.onSurfaceVariant);
            holder.itemView.setBackgroundColor(isCurrent ? colors.surfaceContainer : Color.TRANSPARENT);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
}

package org.telegram.ui.lagram;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.AndroidUtilities.dpf2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.LagramMessageHistoryDb;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackgroundGradientDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.FilterTabsView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.MotionBackgroundDrawable;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalRecyclerView;
import org.telegram.ui.Stories.recorder.StoryEntry;

import java.util.ArrayList;
import java.util.Random;

/**
 * 100% REAL Interactive Lagram Previews.
 *
 * Universal design rule:
 * - Zero fake/abstract mockups.
 * - Every preview renders over the user's REAL active Telegram chat wallpaper and REAL Theme colors.
 * - Every preview embeds REAL Telegram components (ChatMessageCell, FilterTabsView, AvatarDrawable,
 *   EditText + LagramTypingAnimEngine, RecyclerListView + LagramScrollEdgeController) and is
 *   directly interactive in real time!
 */
public final class LagramRealPreviews {

    private LagramRealPreviews() {}

    // =============================================================================================
    // Shared real chat wallpaper + ChatMessageCell background rendering helpers
    // =============================================================================================
    private static final class WallpaperHelper {
        private BackgroundGradientDrawable.Disposable gradientDisposable;
        private ColorDrawable fallbackBg;
        private Drawable registeredWallpaper;

        void draw(Canvas canvas, View host, Theme.ResourcesProvider resourcesProvider) {
            Drawable wallpaper = Theme.getCachedWallpaperNonBlocking();
            if (Theme.wallpaperLoadTask != null) {
                host.invalidate();
            }
            if (SharedConfig.compactMode || wallpaper == null) {
                int color = Theme.getColor(SharedConfig.compactMode ? Theme.key_chat_compact_background : Theme.key_windowBackgroundGray, resourcesProvider);
                if (fallbackBg == null || fallbackBg.getColor() != color) {
                    fallbackBg = new ColorDrawable(color);
                }
                wallpaper = fallbackBg;
            }
            int w = host.getMeasuredWidth();
            int h = host.getMeasuredHeight();
            if (w <= 0 || h <= 0) {
                return;
            }
            LagramGlassBubbleEngine.setViewportSize(w, h);
            if (registeredWallpaper != wallpaper) {
                registeredWallpaper = wallpaper;
                LagramGlassBubbleEngine.setSource(host, null, wallpaper);
            }
            wallpaper.setAlpha(255);
            if (wallpaper instanceof ColorDrawable || wallpaper instanceof GradientDrawable || wallpaper instanceof MotionBackgroundDrawable) {
                wallpaper.setBounds(0, 0, w, h);
                if (wallpaper instanceof BackgroundGradientDrawable) {
                    if (gradientDisposable != null) {
                        gradientDisposable.dispose();
                    }
                    gradientDisposable = ((BackgroundGradientDrawable) wallpaper).drawExactBoundsSize(canvas, host);
                } else {
                    wallpaper.draw(canvas);
                }
            } else {
                StoryEntry.drawBackgroundDrawable(canvas, wallpaper, w, h);
            }
        }

        void detach(View host) {
            LagramGlassBubbleEngine.clearSource(host, null, registeredWallpaper);
            registeredWallpaper = null;
            if (gradientDisposable != null) {
                gradientDisposable.dispose();
                gradientDisposable = null;
            }
        }
    }

    private static void applyRoundedCardOutline(View view) {
        view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline outline) {
                outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), dp(LagramConfig.getEffectiveCardRadiusDp()));
            }
        });
        view.setClipToOutline(true);
    }

    private static MessageObject makeTextMessage(int account, int id, boolean out, String text, int dateOffsetSec, boolean withForward, MessageObject replyTo) {
        int now = (int) (System.currentTimeMillis() / 1000L);
        long selfId = UserConfig.getInstance(account).getClientUserId();
        TLRPC.Message msg = new TLRPC.TL_message();
        msg.message = text;
        msg.date = now - dateOffsetSec;
        msg.dialog_id = 1;
        msg.id = id;
        msg.out = out;
        msg.media = new TLRPC.TL_messageMediaEmpty();
        msg.from_id = new TLRPC.TL_peerUser();
        msg.peer_id = new TLRPC.TL_peerUser();
        if (out) {
            msg.flags = 259;
            msg.from_id.user_id = selfId;
            msg.peer_id.user_id = 0;
        } else {
            msg.flags = 257;
            msg.peer_id.user_id = selfId;
        }
        if (replyTo != null) {
            msg.flags |= 8;
            msg.reply_to = new TLRPC.TL_messageReplyHeader();
            msg.reply_to.flags |= 16;
            msg.reply_to.reply_to_msg_id = replyTo.getId();
        }
        if (withForward) {
            msg.flags |= 4 | 16384;
            msg.fwd_from = new TLRPC.TL_messageFwdHeader();
            msg.fwd_from.from_name = "Lagram";
            msg.fwd_from.date = now - 3600;
            msg.forwards = 18;
        }
        MessageObject obj = new MessageObject(account, msg, true, false);
        if (replyTo != null) {
            obj.customReplyName = "Lagram";
            obj.replyMessageObject = replyTo;
        }
        obj.resetLayout();
        return obj;
    }

    private static void drawCellParentBackground(Canvas canvas, ChatMessageCell cell, int parentW, int parentH) {
        if (cell != null && cell.getVisibility() == View.VISIBLE && cell.getAlpha() > 0.01f && cell.drawBackgroundInParent() && cell.currentBackgroundDrawable != null) {
            canvas.save();
            canvas.translate(cell.getX(), cell.getY());
            cell.setBackgroundTopY(true);
            cell.currentBackgroundDrawable.setGlassPosition(cell.getX(), cell.getY(), parentW, parentH);
            cell.drawBackgroundInternal(canvas, true);
            canvas.restore();
        }
    }

    private static ChatMessageCell createRealMessageCell(Context context, int account, Theme.ResourcesProvider resourcesProvider) {
        ChatMessageCell cell = new ChatMessageCell(context, account, false, null, resourcesProvider);
        cell.setDelegate(new ChatMessageCell.ChatMessageCellDelegate() {
            @Override
            public boolean canPerformActions() {
                return false;
            }
        });
        cell.isChat = false;
        cell.setFullyDraw(true);
        return cell;
    }

    // =============================================================================================
    // 1. RealChatHeaderPreviewCell
    // Real wallpaper + REAL styled Chat Top Bar (Default / iOS Centered / Compact / Adaptive Pill /
    // Expressive) + real ChatMessageCell below. Tap the header to toggle short/long contact name!
    // =============================================================================================
    public static class RealChatHeaderPreviewCell extends FrameLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final WallpaperHelper wallpaperHelper = new WallpaperHelper();
        private final ChatMessageCell sampleCell;
        private final HeaderCanvasView headerView;
        private final TextView hintView;
        private long lastFingerprint = Long.MIN_VALUE;

        public RealChatHeaderPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            LinearLayout col = new LinearLayout(context);
            col.setOrientation(LinearLayout.VERTICAL);
            addView(col, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            headerView = new HeaderCanvasView(context, resourcesProvider);
            col.addView(headerView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 62));

            sampleCell = createRealMessageCell(context, currentAccount, resourcesProvider);
            sampleCell.setMessageObject(makeTextMessage(currentAccount, 501, false,
                    "¡Mira cómo cambia la barra superior real de tus chats!", 30, false, null), null, false, false, false);
            col.addView(sampleCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 2));

            hintView = new TextView(context);
            hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
            hintView.setTypeface(AndroidUtilities.bold());
            hintView.setGravity(Gravity.CENTER);
            hintView.setText(LocaleController.getString(R.string.LagramTapHeaderPreviewHint));
            hintView.setPadding(dp(12), dp(4), dp(12), dp(8));
            col.addView(hintView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + LagramConfig.chatHeaderStyle;
            h = h * 31L + LagramConfig.avatarShape;
            h = h * 31L + LagramConfig.roundingPreset;
            h = h * 31L + (LagramConfig.glassMessages ? 1 : 0);
            return h;
        }

        public void bind(long fp) {
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                hintView.setTextColor(ColorUtils.setAlphaComponent(
                        Theme.isCurrentThemeDark() ? Color.WHITE : 0xFF1C2430, 195));
                headerView.invalidate();
                sampleCell.requestLayout();
                sampleCell.invalidate();
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            wallpaperHelper.draw(canvas, this, resourcesProvider);
            drawCellParentBackground(canvas, sampleCell, getWidth(), getHeight());
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            wallpaperHelper.detach(this);
        }

        private static class HeaderCanvasView extends View {
            private final Theme.ResourcesProvider resourcesProvider;
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint titlePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint subPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rect = new RectF();
            private final AvatarDrawable avatarDrawable = new AvatarDrawable();
            private final Drawable backIcon;
            private final Drawable callIcon;
            private final Drawable moreIcon;
            private boolean longName = false;
            private float nameAnim = 0f;
            private ValueAnimator animator;

            HeaderCanvasView(Context context, Theme.ResourcesProvider resourcesProvider) {
                super(context);
                this.resourcesProvider = resourcesProvider;
                strokePaint.setStyle(Paint.Style.STROKE);
                strokePaint.setStrokeWidth(dpf2(0.9f));
                titlePaint.setTypeface(AndroidUtilities.bold());
                titlePaint.setTextSize(dp(16));
                subPaint.setTextSize(dp(12));
                avatarDrawable.setInfo(42L, "Alex", "Lagram");
                backIcon = ContextCompat.getDrawable(context, R.drawable.ic_ab_back);
                callIcon = ContextCompat.getDrawable(context, R.drawable.msg_calls);
                moreIcon = ContextCompat.getDrawable(context, R.drawable.ic_ab_other);
                setClickable(true);
                setOnClickListener(v -> {
                    longName = !longName;
                    try {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    } catch (Exception ignored) {}
                    if (animator != null) animator.cancel();
                    animator = ValueAnimator.ofFloat(nameAnim, longName ? 1f : 0f);
                    animator.setDuration(260);
                    animator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                    animator.addUpdateListener(a -> {
                        nameAnim = (float) a.getAnimatedValue();
                        invalidate();
                    });
                    animator.start();
                });
            }

            private void drawIcon(Canvas c, Drawable d, int cx, int cy, int size, int tint) {
                if (d == null) return;
                Drawable m = d.mutate();
                m.setColorFilter(new PorterDuffColorFilter(tint, PorterDuff.Mode.SRC_IN));
                m.setBounds(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
                m.draw(c);
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;

                LagramConfig.ensureLoaded();
                int style = LagramConfig.chatHeaderStyle;
                boolean isDark = Theme.isCurrentThemeDark();
                int barBg = Theme.getColor(Theme.key_actionBarDefault, resourcesProvider);
                int titleColor = Theme.getColor(Theme.key_actionBarDefaultTitle, resourcesProvider);
                int subColor = Theme.getColor(Theme.key_actionBarDefaultSubtitle, resourcesProvider);
                int iconColor = Theme.getColor(Theme.key_actionBarDefaultIcon, resourcesProvider);
                int glassFill = ColorUtils.setAlphaComponent(isDark ? 0xFF171A21 : 0xFFFFFFFF, isDark ? 188 : 210);
                int glassStroke = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 34 : 20);
                int cy = h / 2;

                String name = longName ? "Alejandro Morales • Lagram" : "Alex";
                String sub = "en línea";

                if (style == LagramConfig.CHAT_HEADER_DEFAULT) {
                    paint.setColor(barBg);
                    canvas.drawRect(0, 0, w, h, paint);
                    drawIcon(canvas, backIcon, dp(24), cy, dp(22), iconColor);
                    int av = dp(38);
                    avatarDrawable.setBounds(dp(48), cy - av / 2, dp(48) + av, cy + av / 2);
                    avatarDrawable.draw(canvas);
                    titlePaint.setColor(titleColor);
                    subPaint.setColor(subColor);
                    float maxTw = w - dp(164);
                    CharSequence ellTitle = TextUtils.ellipsize(name, titlePaint, maxTw, TextUtils.TruncateAt.END);
                    canvas.drawText(ellTitle, 0, ellTitle.length(), dp(96), cy - dp(2), titlePaint);
                    canvas.drawText(sub, dp(96), cy + dp(14), subPaint);
                    drawIcon(canvas, callIcon, w - dp(58), cy, dp(22), iconColor);
                    drawIcon(canvas, moreIcon, w - dp(24), cy, dp(22), iconColor);
                    return;
                }

                if (style == LagramConfig.CHAT_HEADER_IOS) {
                    // iOS Centered: glass bar + back pill left + centered name/status + avatar right
                    paint.setColor(glassFill);
                    canvas.drawRect(0, 0, w, h, paint);
                    strokePaint.setColor(glassStroke);
                    canvas.drawLine(0, h - 1, w, h - 1, strokePaint);
                    int pillColor = isDark ? Color.WHITE : 0xFF11141A;
                    paint.setColor(ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 24 : 14));
                    canvas.drawCircle(dp(26), cy, dp(17), paint);
                    drawIcon(canvas, backIcon, dp(26), cy, dp(20), pillColor);

                    int av = dp(36);
                    avatarDrawable.setBounds(w - dp(14) - av, cy - av / 2, w - dp(14), cy + av / 2);
                    avatarDrawable.draw(canvas);

                    titlePaint.setColor(pillColor);
                    subPaint.setColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider));
                    float maxTw = w - dp(126);
                    CharSequence ellTitle = TextUtils.ellipsize(name, titlePaint, maxTw, TextUtils.TruncateAt.END);
                    float tw = titlePaint.measureText(ellTitle, 0, ellTitle.length());
                    float sw = subPaint.measureText(sub);
                    canvas.drawText(ellTitle, 0, ellTitle.length(), (w - tw) * 0.5f, cy - dp(2), titlePaint);
                    canvas.drawText(sub, (w - sw) * 0.5f, cy + dp(14), subPaint);
                    return;
                }

                if (style == LagramConfig.CHAT_HEADER_COMPACT) {
                    // Compact slim floating top strip
                    paint.setColor(ColorUtils.setAlphaComponent(barBg, 232));
                    rect.set(dp(10), dp(9), w - dp(10), h - dp(9));
                    canvas.drawRoundRect(rect, dp(22), dp(22), paint);
                    strokePaint.setColor(glassStroke);
                    canvas.drawRoundRect(rect, dp(22), dp(22), strokePaint);
                    drawIcon(canvas, backIcon, dp(30), cy, dp(20), iconColor);
                    int av = dp(30);
                    avatarDrawable.setBounds(dp(48), cy - av / 2, dp(48) + av, cy + av / 2);
                    avatarDrawable.draw(canvas);
                    titlePaint.setColor(titleColor);
                    subPaint.setColor(subColor);
                    float maxTw = w - dp(148);
                    CharSequence ellTitle = TextUtils.ellipsize(name, titlePaint, maxTw, TextUtils.TruncateAt.END);
                    canvas.drawText(ellTitle, 0, ellTitle.length(), dp(86), cy - dp(1), titlePaint);
                    canvas.drawText(sub, dp(86), cy + dp(12), subPaint);
                    drawIcon(canvas, moreIcon, w - dp(28), cy, dp(20), iconColor);
                    return;
                }

                if (style == LagramConfig.CHAT_HEADER_ADAPTIVE) {
                    // Adaptive Name-Width Pill: pill shrinks/grows dynamically with contact name length!
                    int fg = isDark ? Color.WHITE : 0xFF11141A;
                    paint.setColor(glassFill);
                    strokePaint.setColor(glassStroke);
                    // Back circle pill
                    canvas.drawCircle(dp(26), cy, dp(18), paint);
                    canvas.drawCircle(dp(26), cy, dp(18), strokePaint);
                    drawIcon(canvas, backIcon, dp(26), cy, dp(20), fg);
                    // Right menu pill
                    canvas.drawCircle(w - dp(26), cy, dp(18), paint);
                    canvas.drawCircle(w - dp(26), cy, dp(18), strokePaint);
                    drawIcon(canvas, moreIcon, w - dp(26), cy, dp(20), fg);

                    // Compute smooth adaptive width between short & long name
                    float shortW = dp(34 + 26) + Math.max(titlePaint.measureText("Alex"), subPaint.measureText(sub));
                    float longRawW = dp(34 + 26) + titlePaint.measureText("Alejandro Morales • Lagram");
                    float maxPillW = w - dp(104);
                    float targetW = Math.min(maxPillW, shortW + (longRawW - shortW) * nameAnim);
                    float pillL = dp(52);
                    float pillR = pillL + targetW;
                    rect.set(pillL, cy - dp(21), pillR, cy + dp(21));
                    canvas.drawRoundRect(rect, dp(21), dp(21), paint);
                    canvas.drawRoundRect(rect, dp(21), dp(21), strokePaint);

                    int av = dp(32);
                    avatarDrawable.setBounds((int) pillL + dp(5), cy - av / 2, (int) pillL + dp(5) + av, cy + av / 2);
                    avatarDrawable.draw(canvas);

                    titlePaint.setColor(fg);
                    subPaint.setColor(ColorUtils.setAlphaComponent(fg, 165));
                    float textAvail = Math.max(dp(24), targetW - dp(50));
                    CharSequence ellTitle = TextUtils.ellipsize(name, titlePaint, textAvail, TextUtils.TruncateAt.END);
                    canvas.drawText(ellTitle, 0, ellTitle.length(), pillL + dp(43), cy - dp(2), titlePaint);
                    canvas.drawText(sub, pillL + dp(43), cy + dp(13), subPaint);
                    return;
                }

                // CHAT_HEADER_EXPRESSIVE: Liquid Glass full pill + floating circular actions
                int fg = isDark ? Color.WHITE : 0xFF11141A;
                paint.setColor(glassFill);
                strokePaint.setColor(glassStroke);
                canvas.drawCircle(dp(26), cy, dp(18), paint);
                canvas.drawCircle(dp(26), cy, dp(18), strokePaint);
                drawIcon(canvas, backIcon, dp(26), cy, dp(20), fg);

                rect.set(dp(50), cy - dp(21), w - dp(86), cy + dp(21));
                canvas.drawRoundRect(rect, dp(21), dp(21), paint);
                canvas.drawRoundRect(rect, dp(21), dp(21), strokePaint);

                int av = dp(34);
                avatarDrawable.setBounds(dp(55), cy - av / 2, dp(55) + av, cy + av / 2);
                avatarDrawable.draw(canvas);
                titlePaint.setColor(fg);
                subPaint.setColor(ColorUtils.setAlphaComponent(fg, 170));
                float textAvail = Math.max(dp(40), rect.width() - dp(54));
                CharSequence ellTitle = TextUtils.ellipsize(name, titlePaint, textAvail, TextUtils.TruncateAt.END);
                canvas.drawText(ellTitle, 0, ellTitle.length(), dp(96), cy - dp(2), titlePaint);
                canvas.drawText(sub, dp(96), cy + dp(13), subPaint);

                // Floating dual action capsule right
                rect.set(w - dp(80), cy - dp(18), w - dp(10), cy + dp(18));
                canvas.drawRoundRect(rect, dp(18), dp(18), paint);
                canvas.drawRoundRect(rect, dp(18), dp(18), strokePaint);
                drawIcon(canvas, callIcon, w - dp(60), cy, dp(19), fg);
                drawIcon(canvas, moreIcon, w - dp(28), cy, dp(19), fg);
            }
        }

        public static class Factory extends UItem.UItemFactory<RealChatHeaderPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealChatHeaderPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealChatHeaderPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealChatHeaderPreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 2. RealBubblesGlassPreviewCell
    // Real wallpaper + 2 REAL ChatMessageCells with live Liquid Glass, Rounding, Tails & Refraction!
    // =============================================================================================
    public static class RealBubblesGlassPreviewCell extends LinearLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final WallpaperHelper wallpaperHelper = new WallpaperHelper();
        private final ChatMessageCell[] cells = new ChatMessageCell[2];
        private long lastFingerprint = Long.MIN_VALUE;

        public RealBubblesGlassPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setWillNotDraw(false);
            setPadding(0, dp(10), 0, dp(10));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            for (int i = 0; i < 2; i++) {
                ChatMessageCell cell = createRealMessageCell(context, currentAccount, resourcesProvider);
                cell.setOnClickListener(v -> {
                    try {
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    } catch (Exception ignored) {}
                    v.animate().cancel();
                    v.setScaleX(0.97f);
                    v.setScaleY(0.97f);
                    v.animate().scaleX(1f).scaleY(1f).setDuration(220).setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT).start();
                });
                cells[i] = cell;
                addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            }
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + (LagramConfig.glassMessages ? 1 : 0);
            h = h * 31L + LagramConfig.glassMessagesAlpha;
            h = h * 31L + (LagramConfig.glassMessagesBorder ? 1 : 0);
            h = h * 31L + (LagramConfig.glassHighlights ? 1 : 0);
            h = h * 31L + (LagramConfig.showBubbleTails ? 1 : 0);
            h = h * 31L + LagramConfig.roundingPreset;
            h = h * 31L + LagramConfig.avatarShape;
            h = h * 31L + (LagramConfig.showSecondsInTime ? 1 : 0);
            h = h * 31L + (LagramConfig.showForwardTime ? 1 : 0);
            h = h * 31L + (LagramConfig.showForwardsCounter ? 1 : 0);
            return h;
        }

        public void bind(long fp) {
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                MessageObject reply = makeTextMessage(currentAccount, 10, true, "Lagram Turbo", 120, false, null);
                MessageObject inMsg = makeTextMessage(currentAccount, 11, false,
                        "¡Así se ven tus burbujas reales sobre tu fondo actual de Lagram!", 60, true, reply);
                MessageObject outMsg = makeTextMessage(currentAccount, 12, true,
                        "Liquid Glass: " + (LagramConfig.glassMessages ? (LagramConfig.glassMessagesAlpha + "%") : "Off")
                                + " • Radio " + LagramConfig.getEffectiveBubbleRadiusDp() + "dp", 10, false, null);
                cells[0].setMessageObject(inMsg, null, false, false, false);
                cells[1].setMessageObject(outMsg, null, false, false, false);
                for (ChatMessageCell c : cells) {
                    if (c.currentBackgroundDrawable != null) {
                        c.currentBackgroundDrawable.forceInvalidatePath = true;
                    }
                    c.requestLayout();
                    c.invalidate();
                }
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            wallpaperHelper.draw(canvas, this, resourcesProvider);
            for (ChatMessageCell cell : cells) {
                drawCellParentBackground(canvas, cell, getWidth(), getHeight());
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            wallpaperHelper.detach(this);
        }

        public static class Factory extends UItem.UItemFactory<RealBubblesGlassPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealBubblesGlassPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealBubblesGlassPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealBubblesGlassPreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 3. RealMessageMenuPreviewCell
    // Real wallpaper + REAL ChatMessageCell + REAL interactive Focus dim/blur + Reactions pill +
    // REAL Telegram styled Message Menu (List / Tiles / Capsule) with real Telegram icons!
    // Tap the message to toggle open/close with spring animation!
    // =============================================================================================
    public static class RealMessageMenuPreviewCell extends FrameLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final WallpaperHelper wallpaperHelper = new WallpaperHelper();
        private final ChatMessageCell cell;
        private final LinearLayout contentCol;
        private final LinearLayout reactionsBar;
        private final FrameLayout menuHost;
        private final TextView hintView;
        private boolean menuOpen = true;
        private float openProgress = 1f;
        private ValueAnimator openAnim;
        private long lastFingerprint = Long.MIN_VALUE;

        public RealMessageMenuPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            contentCol = new LinearLayout(context);
            contentCol.setOrientation(LinearLayout.VERTICAL);
            contentCol.setPadding(dp(10), dp(10), dp(10), dp(10));
            addView(contentCol, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            // Real Reaction Pill Bar
            reactionsBar = new LinearLayout(context);
            reactionsBar.setOrientation(LinearLayout.HORIZONTAL);
            reactionsBar.setGravity(Gravity.CENTER_VERTICAL);
            reactionsBar.setPadding(dp(10), dp(5), dp(10), dp(5));
            String[] emojis = {"❤️", "🔥", "👍", "😂", "⚡", "🎉"};
            for (String em : emojis) {
                TextView tv = new TextView(context);
                tv.setText(em);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                tv.setPadding(dp(6), dp(2), dp(6), dp(2));
                tv.setOnClickListener(v -> {
                    try { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignored) {}
                    v.animate().scaleX(1.32f).scaleY(1.32f).setDuration(110).withEndAction(() ->
                            v.animate().scaleX(1f).scaleY(1f).setDuration(140).start()).start();
                });
                reactionsBar.addView(tv);
            }
            contentCol.addView(reactionsBar, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.END, 0, 0, 4, 6));

            // Real ChatMessageCell
            cell = createRealMessageCell(context, currentAccount, resourcesProvider);
            cell.setMessageObject(makeTextMessage(currentAccount, 601, true,
                    "Toca este mensaje real para probar el menú y el enfoque ✨", 15, false, null), null, false, false, false);
            cell.setOnClickListener(v -> toggleMenu());
            contentCol.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            // Real Menu Host (rebuilt dynamically for List / Tiles / Capsule)
            menuHost = new FrameLayout(context);
            contentCol.addView(menuHost, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 6, 0, 0));

            hintView = new TextView(context);
            hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
            hintView.setTypeface(AndroidUtilities.bold());
            hintView.setGravity(Gravity.CENTER);
            hintView.setText(LocaleController.getString(R.string.LagramTapMenuPreviewHint));
            hintView.setPadding(dp(8), dp(8), dp(8), dp(2));
            contentCol.addView(hintView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        private void toggleMenu() {
            menuOpen = !menuOpen;
            try { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignored) {}
            if (openAnim != null) openAnim.cancel();
            openAnim = ValueAnimator.ofFloat(openProgress, menuOpen ? 1f : 0f);
            openAnim.setDuration(260);
            openAnim.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
            openAnim.addUpdateListener(a -> {
                openProgress = (float) a.getAnimatedValue();
                applyAnimProgress();
            });
            openAnim.start();
        }

        private void applyAnimProgress() {
            float s = 0.86f + 0.14f * openProgress;
            reactionsBar.setAlpha(openProgress);
            reactionsBar.setScaleX(s);
            reactionsBar.setScaleY(s);
            menuHost.setAlpha(openProgress);
            menuHost.setScaleX(s);
            menuHost.setScaleY(s);
            invalidate();
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + (LagramConfig.messageFocusEnabled ? 1 : 0);
            h = h * 31L + (LagramConfig.messageFocusBlur ? 1 : 0);
            h = h * 31L + LagramConfig.messageFocusStyle;
            h = h * 31L + (LagramConfig.popupGlass ? 1 : 0);
            h = h * 31L + (LagramConfig.glassMessages ? 1 : 0);
            h = h * 31L + LagramConfig.roundingPreset;
            return h;
        }

        public void bind(long fp) {
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                rebuildMenuUI();
                cell.requestLayout();
                cell.invalidate();
                invalidate();
            }
        }

        private void rebuildMenuUI() {
            Context ctx = getContext();
            boolean isDark = Theme.isCurrentThemeDark();
            int popupBgBase = Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground, resourcesProvider);
            int popupBg = LagramConfig.popupGlass
                    ? ColorUtils.setAlphaComponent(isDark ? 0xFF1A1E26 : Color.WHITE, isDark ? 212 : 226)
                    : popupBgBase;
            int fg = Theme.getColor(Theme.key_actionBarDefaultSubmenuItem, resourcesProvider);
            int iconFg = Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon, resourcesProvider);
            int red = Theme.getColor(Theme.key_text_RedRegular, resourcesProvider);
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            float cardR = dp(Math.min(20, LagramConfig.getEffectiveCardRadiusDp()));

            reactionsBar.setBackground(Theme.createRoundRectDrawable(dp(22), popupBg));
            hintView.setTextColor(ColorUtils.setAlphaComponent(isDark ? Color.WHITE : 0xFF1C2430, 200));

            menuHost.removeAllViews();
            int[] icons = {R.drawable.menu_reply, R.drawable.msg_copy, R.drawable.msg_forward, R.drawable.msg_pin, R.drawable.msg_delete};
            String[] labels = {
                    LocaleController.getString(R.string.Reply),
                    LocaleController.getString(R.string.Copy),
                    LocaleController.getString(R.string.Forward),
                    LocaleController.getString(R.string.PinMessage),
                    LocaleController.getString(R.string.Delete)
            };

            int style = LagramConfig.messageFocusStyle;
            if (style == LagramConfig.FOCUS_STYLE_CAPSULE) {
                // Sleek floating horizontal capsule bar + compact secondary card
                LinearLayout wrap = new LinearLayout(ctx);
                wrap.setOrientation(LinearLayout.VERTICAL);
                wrap.setGravity(Gravity.END);

                LinearLayout capsule = new LinearLayout(ctx);
                capsule.setOrientation(LinearLayout.HORIZONTAL);
                capsule.setGravity(Gravity.CENTER_VERTICAL);
                capsule.setPadding(dp(8), dp(6), dp(8), dp(6));
                capsule.setBackground(Theme.createRoundRectDrawable(dp(24), popupBg));

                for (int i = 0; i < icons.length; i++) {
                    boolean isDel = i == icons.length - 1;
                    ImageView iv = new ImageView(ctx);
                    iv.setImageResource(icons[i]);
                    iv.setScaleType(ImageView.ScaleType.CENTER);
                    iv.setColorFilter(new PorterDuffColorFilter(isDel ? red : accent, PorterDuff.Mode.SRC_IN));
                    iv.setBackground(Theme.createSelectorDrawable(ColorUtils.setAlphaComponent(isDel ? red : accent, 32), Theme.RIPPLE_MASK_CIRCLE_AUTO));
                    iv.setOnClickListener(v -> {
                        try { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignored) {}
                    });
                    capsule.addView(iv, LayoutHelper.createLinear(42, 38, 2, 0, 2, 0));
                }
                wrap.addView(capsule, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
                menuHost.addView(wrap, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            } else if (style == LagramConfig.FOCUS_STYLE_TILES) {
                // Opexgram Tiles: top 4 tactile square tiles + bottom compact action row
                LinearLayout box = new LinearLayout(ctx);
                box.setOrientation(LinearLayout.VERTICAL);
                box.setPadding(dp(8), dp(8), dp(8), dp(8));
                box.setBackground(Theme.createRoundRectDrawable((int) cardR, popupBg));

                LinearLayout row = new LinearLayout(ctx);
                row.setOrientation(LinearLayout.HORIZONTAL);
                int tileFill = ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 18 : 10);
                for (int i = 0; i < 4; i++) {
                    LinearLayout tile = new LinearLayout(ctx);
                    tile.setOrientation(LinearLayout.VERTICAL);
                    tile.setGravity(Gravity.CENTER);
                    tile.setPadding(dp(4), dp(8), dp(4), dp(8));
                    tile.setBackground(Theme.createRoundRectDrawable(dp(12), tileFill));
                    tile.setOnClickListener(v -> {
                        try { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignored) {}
                    });
                    ImageView iv = new ImageView(ctx);
                    iv.setImageResource(icons[i]);
                    iv.setColorFilter(new PorterDuffColorFilter(iconFg, PorterDuff.Mode.SRC_IN));
                    tile.addView(iv, LayoutHelper.createLinear(20, 20));
                    TextView tv = new TextView(ctx);
                    tv.setText(labels[i]);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
                    tv.setTextColor(fg);
                    tv.setMaxLines(1);
                    tv.setEllipsize(TextUtils.TruncateAt.END);
                    tile.addView(tv, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 0));
                    row.addView(tile, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, i == 0 ? 0 : 4, 0, 0, 0));
                }
                box.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

                // Delete action row below tiles
                LinearLayout delRow = new LinearLayout(ctx);
                delRow.setOrientation(LinearLayout.HORIZONTAL);
                delRow.setGravity(Gravity.CENTER_VERTICAL);
                delRow.setPadding(dp(10), dp(9), dp(10), dp(5));
                ImageView delIc = new ImageView(ctx);
                delIc.setImageResource(icons[4]);
                delIc.setColorFilter(new PorterDuffColorFilter(red, PorterDuff.Mode.SRC_IN));
                delRow.addView(delIc, LayoutHelper.createLinear(20, 20, 0, 0, 12, 0));
                TextView delTv = new TextView(ctx);
                delTv.setText(labels[4]);
                delTv.setTextColor(red);
                delTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                delRow.addView(delTv);
                box.addView(delRow, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 4, 0, 0));

                menuHost.addView(box, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.END, 18, 0, 0, 0));
            } else {
                // Classic Telegram List menu card
                LinearLayout list = new LinearLayout(ctx);
                list.setOrientation(LinearLayout.VERTICAL);
                list.setPadding(dp(4), dp(6), dp(4), dp(6));
                list.setBackground(Theme.createRoundRectDrawable((int) cardR, popupBg));
                for (int i = 0; i < icons.length; i++) {
                    boolean isDel = i == icons.length - 1;
                    LinearLayout row = new LinearLayout(ctx);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(dp(12), dp(8), dp(16), dp(8));
                    row.setOnClickListener(v -> {
                        try { v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignored) {}
                    });
                    ImageView iv = new ImageView(ctx);
                    iv.setImageResource(icons[i]);
                    iv.setColorFilter(new PorterDuffColorFilter(isDel ? red : iconFg, PorterDuff.Mode.SRC_IN));
                    row.addView(iv, LayoutHelper.createLinear(20, 20, 0, 0, 14, 0));
                    TextView tv = new TextView(ctx);
                    tv.setText(labels[i]);
                    tv.setTextColor(isDel ? red : fg);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14.5f);
                    row.addView(tv);
                    list.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                }
                menuHost.addView(list, LayoutHelper.createFrame(210, LayoutHelper.WRAP_CONTENT, Gravity.END));
            }
            applyAnimProgress();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            wallpaperHelper.draw(canvas, this, resourcesProvider);
            // Focus dim / backdrop overlay over real wallpaper
            if (LagramConfig.messageFocusEnabled && openProgress > 0.01f) {
                int dimAlpha = Math.round((LagramConfig.messageFocusBlur ? 108 : 58) * openProgress);
                canvas.drawColor(Color.argb(dimAlpha, 0, 0, 0));
            }
            if (cell != null && cell.drawBackgroundInParent() && cell.currentBackgroundDrawable != null) {
                float cx = contentCol.getX() + cell.getX();
                float cy = contentCol.getY() + cell.getY();
                canvas.save();
                canvas.translate(cx, cy);
                cell.setBackgroundTopY(true);
                cell.currentBackgroundDrawable.setGlassPosition(cx, cy, getWidth(), getHeight());
                cell.drawBackgroundInternal(canvas, true);
                canvas.restore();
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            wallpaperHelper.detach(this);
        }

        public static class Factory extends UItem.UItemFactory<RealMessageMenuPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealMessageMenuPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealMessageMenuPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealMessageMenuPreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 4. RealDeleteAnimPreviewCell
    // Real wallpaper + REAL ChatMessageCell + interactive particle/physics delete animator!
    // Tap the message OR switch delete effect to immediately watch the real animation & respawn!
    // =============================================================================================
    public static class RealDeleteAnimPreviewCell extends FrameLayout {
        private static final class Particle {
            float x, y, vx, vy, size, rot, vrot;
            int color;
        }

        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final WallpaperHelper wallpaperHelper = new WallpaperHelper();
        private final ChatMessageCell cell;
        private final TextView hintView;
        private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF particleRect = new RectF();
        private final ArrayList<Particle> particles = new ArrayList<>();
        private final Random random = new Random();
        private float particleProgress = 0f;
        private boolean particlesActive = false;
        private boolean isShardMode = false;
        private ValueAnimator deleteAnimator;
        private int lastEffect = -1;
        private long lastFingerprint = Long.MIN_VALUE;

        public RealDeleteAnimPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            LinearLayout col = new LinearLayout(context);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(0, dp(12), 0, dp(8));
            addView(col, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            cell = createRealMessageCell(context, currentAccount, resourcesProvider);
            cell.setMessageObject(makeTextMessage(currentAccount, 701, true,
                    "Tócame para probar la animación real de borrado 💥", 5, false, null), null, false, false, false);
            cell.setOnClickListener(v -> playDeleteDemo());
            col.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            hintView = new TextView(context);
            hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11.5f);
            hintView.setTypeface(AndroidUtilities.bold());
            hintView.setGravity(Gravity.CENTER);
            hintView.setText(LocaleController.getString(R.string.LagramTapDeletePreviewHint));
            hintView.setPadding(dp(12), dp(6), dp(12), dp(4));
            col.addView(hintView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + LagramConfig.deleteEffect;
            h = h * 31L + (LagramConfig.glassMessages ? 1 : 0);
            h = h * 31L + LagramConfig.roundingPreset;
            return h;
        }

        public void bind(long fp) {
            int curEffect = LagramConfig.deleteEffect;
            boolean effectChanged = lastEffect != -1 && lastEffect != curEffect;
            lastEffect = curEffect;
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                hintView.setTextColor(ColorUtils.setAlphaComponent(
                        Theme.isCurrentThemeDark() ? Color.WHITE : 0xFF1C2430, 200));
                cell.requestLayout();
                cell.invalidate();
                invalidate();
                if (effectChanged) {
                    post(this::playDeleteDemo);
                }
            }
        }

        private void spawnParticlesFromCell(boolean shards) {
            particles.clear();
            isShardMode = shards;
            int w = cell.getWidth();
            int h = cell.getHeight();
            if (w <= 0 || h <= 0) return;

            int left = Math.max(dp(48), w - dp(250));
            int right = w - dp(12);
            int top = (int) cell.getY() + dp(14);
            int bottom = top + Math.max(dp(36), h - dp(8));

            int baseColor = Theme.getColor(Theme.key_chat_outBubble, resourcesProvider);
            int accentColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int count = shards ? 42 : 120;

            for (int i = 0; i < count; i++) {
                Particle p = new Particle();
                p.x = left + random.nextFloat() * Math.max(10, right - left);
                p.y = top + random.nextFloat() * Math.max(10, bottom - top);
                if (shards) {
                    float cx = (left + right) * 0.5f;
                    float cy = (top + bottom) * 0.5f;
                    float dx = p.x - cx;
                    float dy = p.y - cy;
                    float len = Math.max(1f, (float) Math.hypot(dx, dy));
                    float spd = dp(40) + random.nextFloat() * dp(65);
                    p.vx = (dx / len) * spd;
                    p.vy = (dy / len) * spd - dp(15);
                    p.size = dp(4.5f) + random.nextFloat() * dp(5f);
                    p.rot = random.nextFloat() * 360f;
                    p.vrot = (random.nextFloat() - 0.5f) * 240f;
                } else {
                    // Thanos dust: fine luminous particles drifting up-right
                    p.vx = dp(18) + random.nextFloat() * dp(58);
                    p.vy = -dp(26) + (random.nextFloat() - 0.5f) * dp(32);
                    p.size = dp(1.8f) + random.nextFloat() * dp(2.4f);
                }
                p.color = (i % 4 == 0) ? accentColor : ColorUtils.blendARGB(baseColor, Color.WHITE, random.nextFloat() * 0.35f);
                particles.add(p);
            }
        }

        public void playDeleteDemo() {
            if (deleteAnimator != null) {
                deleteAnimator.cancel();
            }
            try { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignored) {}

            cell.animate().cancel();
            cell.setAlpha(1f);
            cell.setScaleX(1f);
            cell.setScaleY(1f);
            cell.setRotation(0f);
            cell.setTranslationX(0f);
            particlesActive = false;

            int effect = LagramConfig.deleteEffect;
            if (effect == LagramConfig.DELETE_EFFECT_THANOS || effect == LagramConfig.DELETE_EFFECT_SHARDS) {
                boolean shards = effect == LagramConfig.DELETE_EFFECT_SHARDS;
                spawnParticlesFromCell(shards);
                particlesActive = true;
                deleteAnimator = ValueAnimator.ofFloat(0f, 1f);
                deleteAnimator.setDuration(shards ? 520 : 680);
                deleteAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                deleteAnimator.addUpdateListener(a -> {
                    particleProgress = (float) a.getAnimatedValue();
                    float cellAlpha = Math.max(0f, 1f - particleProgress * (shards ? 2.6f : 1.7f));
                    cell.setAlpha(cellAlpha);
                    if (shards) {
                        float sc = particleProgress < 0.25f ? (1f + particleProgress * 0.4f) : Math.max(0.2f, 1.1f - particleProgress);
                        cell.setScaleX(sc);
                        cell.setScaleY(sc);
                    }
                    invalidate();
                });
                deleteAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        particlesActive = false;
                        respawnCell();
                    }
                });
                deleteAnimator.start();
            } else if (effect == LagramConfig.DELETE_EFFECT_PORTAL) {
                cell.setPivotX(cell.getWidth() * 0.65f);
                cell.setPivotY(cell.getHeight() * 0.5f);
                deleteAnimator = ValueAnimator.ofFloat(0f, 1f);
                deleteAnimator.setDuration(420);
                deleteAnimator.setInterpolator(CubicBezierInterpolator.EASE_IN);
                deleteAnimator.addUpdateListener(a -> {
                    float t = (float) a.getAnimatedValue();
                    cell.setScaleX(1f - t);
                    cell.setScaleY(1f - t);
                    cell.setRotation(t * 28f);
                    cell.setAlpha(1f - t);
                    invalidate();
                });
                deleteAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) { respawnCell(); }
                });
                deleteAnimator.start();
            } else if (effect == LagramConfig.DELETE_EFFECT_SLIDE) {
                float shift = Math.max(dp(120), getWidth() * 0.5f);
                deleteAnimator = ValueAnimator.ofFloat(0f, 1f);
                deleteAnimator.setDuration(360);
                deleteAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
                deleteAnimator.addUpdateListener(a -> {
                    float t = (float) a.getAnimatedValue();
                    cell.setTranslationX(shift * t);
                    cell.setScaleY(1f - 0.12f * t);
                    cell.setAlpha(1f - t);
                    invalidate();
                });
                deleteAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) { respawnCell(); }
                });
                deleteAnimator.start();
            } else {
                // Classic fade
                deleteAnimator = ValueAnimator.ofFloat(0f, 1f);
                deleteAnimator.setDuration(320);
                deleteAnimator.addUpdateListener(a -> {
                    cell.setAlpha(1f - (float) a.getAnimatedValue());
                    invalidate();
                });
                deleteAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override public void onAnimationEnd(Animator animation) { respawnCell(); }
                });
                deleteAnimator.start();
            }
        }

        private void respawnCell() {
            postDelayed(() -> {
                if (!isAttachedToWindow()) return;
                cell.setTranslationX(0f);
                cell.setRotation(0f);
                cell.setScaleX(0.92f);
                cell.setScaleY(0.92f);
                cell.setAlpha(0f);
                cell.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(260)
                        .setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT)
                        .setUpdateListener(a -> invalidate())
                        .start();
            }, 340);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            wallpaperHelper.draw(canvas, this, resourcesProvider);
            if (cell != null && cell.getAlpha() > 0.01f && cell.drawBackgroundInParent() && cell.currentBackgroundDrawable != null) {
                View parentCol = (View) cell.getParent();
                float cx = (parentCol != null ? parentCol.getX() : 0) + cell.getX() + cell.getTranslationX();
                float cy = (parentCol != null ? parentCol.getY() : 0) + cell.getY();
                canvas.save();
                canvas.translate(cx, cy);
                if (cell.getScaleX() != 1f || cell.getScaleY() != 1f) {
                    canvas.scale(cell.getScaleX(), cell.getScaleY(), cell.getPivotX(), cell.getPivotY());
                }
                if (cell.getRotation() != 0f) {
                    canvas.rotate(cell.getRotation(), cell.getPivotX(), cell.getPivotY());
                }
                cell.setBackgroundTopY(true);
                cell.currentBackgroundDrawable.setGlassPosition(cx, cy, getWidth(), getHeight());
                cell.drawBackgroundInternal(canvas, true);
                canvas.restore();
            }
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            super.dispatchDraw(canvas);
            if (particlesActive && !particles.isEmpty()) {
                float p = particleProgress;
                int a = Math.round(255 * Math.max(0f, 1f - p * 1.12f));
                for (int i = 0; i < particles.size(); i++) {
                    Particle pt = particles.get(i);
                    float px = pt.x + pt.vx * p;
                    float py = pt.y + pt.vy * p + (isShardMode ? dp(24) * p * p : 0f);
                    float sz = pt.size * (1f - p * 0.45f);
                    particlePaint.setColor(ColorUtils.setAlphaComponent(pt.color, a));
                    if (isShardMode) {
                        canvas.save();
                        canvas.translate(px, py);
                        canvas.rotate(pt.rot + pt.vrot * p);
                        particleRect.set(-sz * 0.5f, -sz * 0.35f, sz * 0.5f, sz * 0.35f);
                        canvas.drawRoundRect(particleRect, dp(1.5f), dp(1.5f), particlePaint);
                        canvas.restore();
                    } else {
                        canvas.drawCircle(px, py, sz, particlePaint);
                    }
                }
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (deleteAnimator != null) deleteAnimator.cancel();
            wallpaperHelper.detach(this);
        }

        public static class Factory extends UItem.UItemFactory<RealDeleteAnimPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealDeleteAnimPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealDeleteAnimPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealDeleteAnimPreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 5. RealInteractiveChatBoxPreviewCell
    // Real wallpaper + REAL styled Chat Bottom Bar (Telegram / Expressive / Buttons / iOS Glass +
    // live contrast) with a REAL focusable EditText wired to LagramTypingAnimEngine!
    // Tap the input field -> keyboard opens -> type real characters & watch live effects!
    // Tap the send/demo button -> runs a 1-tap character-by-character demo!
    // =============================================================================================
    public static class RealInteractiveChatBoxPreviewCell extends FrameLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final WallpaperHelper wallpaperHelper = new WallpaperHelper();
        private final ChatMessageCell topCell;
        private final LinearLayout barContainer;
        private final FrameLayout inputCapsule;
        private final ImageView emojiBtn;
        private final EditText liveInput;
        private final ImageView attachBtn;
        private final ImageView actionBtn;
        private long lastFingerprint = Long.MIN_VALUE;
        private boolean runningDemo = false;

        public RealInteractiveChatBoxPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            LinearLayout col = new LinearLayout(context);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(0, dp(8), 0, dp(8));
            addView(col, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            topCell = createRealMessageCell(context, currentAccount, resourcesProvider);
            topCell.setMessageObject(makeTextMessage(currentAccount, 801, false,
                    "Toca la barra inferior y escribe con tu teclado para probar los efectos ⌨️", 20, false, null), null, false, false, false);
            col.addView(topCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            barContainer = new LinearLayout(context);
            barContainer.setOrientation(LinearLayout.HORIZONTAL);
            barContainer.setGravity(Gravity.CENTER_VERTICAL);
            barContainer.setPadding(dp(8), dp(6), dp(8), dp(2));
            col.addView(barContainer, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            inputCapsule = new FrameLayout(context);
            LinearLayout inputRow = new LinearLayout(context);
            inputRow.setOrientation(LinearLayout.HORIZONTAL);
            inputRow.setGravity(Gravity.CENTER_VERTICAL);
            inputRow.setPadding(dp(8), dp(2), dp(8), dp(2));
            inputCapsule.addView(inputRow, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

            emojiBtn = new ImageView(context);
            emojiBtn.setImageResource(R.drawable.smiles_tab_smiles);
            emojiBtn.setScaleType(ImageView.ScaleType.CENTER);
            inputRow.addView(emojiBtn, LayoutHelper.createLinear(34, 34));

            liveInput = new EditText(context) {
                @Override
                public boolean onTouchEvent(MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return super.onTouchEvent(event);
                }
            };
            liveInput.setBackground(null);
            liveInput.setSingleLine(true);
            liveInput.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            liveInput.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15.5f);
            liveInput.setHint(LocaleController.getString(R.string.LagramTypeHereHint));
            liveInput.setPadding(dp(6), dp(8), dp(6), dp(8));
            LagramTypingAnimEngine.attach(liveInput);
            liveInput.setOnClickListener(v -> {
                v.requestFocus();
                AndroidUtilities.showKeyboard(v);
            });
            inputRow.addView(liveInput, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));

            attachBtn = new ImageView(context);
            attachBtn.setImageResource(R.drawable.input_attach);
            attachBtn.setScaleType(ImageView.ScaleType.CENTER);
            inputRow.addView(attachBtn, LayoutHelper.createLinear(34, 34));

            barContainer.addView(inputCapsule, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f, 0, 0, 8, 0));

            actionBtn = new ImageView(context);
            actionBtn.setImageResource(R.drawable.msg_send);
            actionBtn.setScaleType(ImageView.ScaleType.CENTER);
            actionBtn.setOnClickListener(v -> runTypingDemo());
            barContainer.addView(actionBtn, LayoutHelper.createLinear(44, 44));
        }

        private void runTypingDemo() {
            if (runningDemo) return;
            runningDemo = true;
            try { performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); } catch (Exception ignored) {}
            liveInput.setText("");
            final String phrase = "Lagram Turbo ✨";
            for (int i = 0; i < phrase.length(); i++) {
                final int idx = i;
                postDelayed(() -> {
                    if (!isAttachedToWindow()) {
                        runningDemo = false;
                        return;
                    }
                    if (liveInput.getText() != null) {
                        liveInput.getText().append(phrase.charAt(idx));
                        liveInput.setSelection(liveInput.getText().length());
                    }
                    if (idx == phrase.length() - 1) {
                        runningDemo = false;
                    }
                }, (i + 1) * 75L);
            }
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + LagramConfig.chatBoxStyle;
            h = h * 31L + LagramConfig.chatBoxContrast;
            h = h * 31L + (LagramConfig.textAppearAnimation ? 1 : 0);
            h = h * 31L + (LagramConfig.textAnimBlur ? 1 : 0);
            h = h * 31L + (LagramConfig.textAnimSlide ? 1 : 0);
            h = h * 31L + (LagramConfig.textAnimScale ? 1 : 0);
            h = h * 31L + (LagramConfig.textAnimRotate ? 1 : 0);
            h = h * 31L + LagramConfig.textAnimSpeed;
            h = h * 31L + LagramConfig.roundingPreset;
            return h;
        }

        public void bind(long fp) {
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                applyBoxStyle();
                topCell.requestLayout();
                topCell.invalidate();
                invalidate();
            }
        }

        private void applyBoxStyle() {
            boolean isDark = Theme.isCurrentThemeDark();
            int accent = Theme.getColor(Theme.key_windowBackgroundWhiteBlueText4, resourcesProvider);
            int panelBg = Theme.getColor(Theme.key_chat_messagePanelBackground, resourcesProvider);
            int textColor = Theme.getColor(Theme.key_chat_messagePanelText, resourcesProvider);
            int hintColor = Theme.getColor(Theme.key_chat_messagePanelHint, resourcesProvider);
            int iconColor = Theme.getColor(Theme.key_chat_messagePanelIcons, resourcesProvider);
            int contrast = Math.max(10, Math.min(100, LagramConfig.chatBoxContrast));

            liveInput.setTextColor(textColor);
            liveInput.setHintTextColor(hintColor);
            emojiBtn.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));
            attachBtn.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.SRC_IN));

            int style = LagramConfig.chatBoxStyle;
            if (style == LagramConfig.CHAT_BOX_TELEGRAM) {
                barContainer.setBackgroundColor(panelBg);
                barContainer.setPadding(dp(6), dp(2), dp(6), dp(2));
                inputCapsule.setBackground(null);
                actionBtn.setBackground(null);
                actionBtn.setColorFilter(new PorterDuffColorFilter(accent, PorterDuff.Mode.SRC_IN));
            } else if (style == LagramConfig.CHAT_BOX_BUTTONS) {
                barContainer.setBackground(null);
                barContainer.setPadding(dp(8), dp(6), dp(8), dp(2));
                int pillAlpha = Math.round(255 * (Math.max(15, contrast) / 140f));
                int pillColor = ColorUtils.setAlphaComponent(isDark ? 0xFF181C24 : Color.WHITE, pillAlpha);
                inputCapsule.setBackground(Theme.createRoundRectDrawable(dp(22), pillColor));
                actionBtn.setBackground(Theme.createCircleDrawable(dp(44), pillColor));
                actionBtn.setColorFilter(new PorterDuffColorFilter(accent, PorterDuff.Mode.SRC_IN));
            } else if (style == LagramConfig.CHAT_BOX_IOS_GLASS) {
                barContainer.setBackground(null);
                barContainer.setPadding(dp(8), dp(6), dp(8), dp(2));
                int glassAlpha = Math.round(255 * (Math.max(25, contrast) / 115f));
                GradientDrawable gd = new GradientDrawable();
                gd.setCornerRadius(dp(24));
                gd.setColor(ColorUtils.setAlphaComponent(isDark ? 0xFF1C2029 : Color.WHITE, glassAlpha));
                gd.setStroke(dp(1), ColorUtils.setAlphaComponent(isDark ? Color.WHITE : Color.BLACK, isDark ? 45 : 24));
                inputCapsule.setBackground(gd);
                actionBtn.setBackground(Theme.createCircleDrawable(dp(44), accent));
                actionBtn.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            } else {
                // CHAT_BOX_EXPRESSIVE
                barContainer.setBackground(null);
                barContainer.setPadding(dp(8), dp(6), dp(8), dp(2));
                int expAlpha = Math.round(255 * (Math.max(30, contrast) / 100f));
                int fill = ColorUtils.setAlphaComponent(panelBg, expAlpha);
                inputCapsule.setBackground(Theme.createRoundRectDrawable(dp(24), fill));
                actionBtn.setBackground(Theme.createCircleDrawable(dp(44), accent));
                actionBtn.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            wallpaperHelper.draw(canvas, this, resourcesProvider);
            if (topCell != null && topCell.drawBackgroundInParent() && topCell.currentBackgroundDrawable != null) {
                View parentCol = (View) topCell.getParent();
                float cx = (parentCol != null ? parentCol.getX() : 0) + topCell.getX();
                float cy = (parentCol != null ? parentCol.getY() : 0) + topCell.getY();
                canvas.save();
                canvas.translate(cx, cy);
                topCell.setBackgroundTopY(true);
                topCell.currentBackgroundDrawable.setGlassPosition(cx, cy, getWidth(), getHeight());
                topCell.drawBackgroundInternal(canvas, true);
                canvas.restore();
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            wallpaperHelper.detach(this);
        }

        public static class Factory extends UItem.UItemFactory<RealInteractiveChatBoxPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealInteractiveChatBoxPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealInteractiveChatBoxPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealInteractiveChatBoxPreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 6. RealScrollEffectPreviewCell
    // Real wallpaper + interactive mini RecyclerListView of REAL ChatMessageCells attached directly
    // to LagramScrollEdgeController! Drag up & down right inside the preview to feel 3D Drum / Scale!
    // =============================================================================================
    public static class RealScrollEffectPreviewCell extends FrameLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final WallpaperHelper wallpaperHelper = new WallpaperHelper();
        private final RecyclerListView miniList;
        private final TextView hintView;
        private final ArrayList<MessageObject> sampleMessages = new ArrayList<>();
        private long lastFingerprint = Long.MIN_VALUE;

        public RealScrollEffectPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setWillNotDraw(false);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            String[] texts = {
                    "Desliza esta vista real arriba y abajo ↕️",
                    "Motor Opexgram 3D Scroll Drum AGSL activo",
                    "Cada burbuja se curva suavemente en los bordes",
                    "Optimizado para 120Hz con cero saltos",
                    "Compatible con Liquid Glass en tiempo real ✨",
                    "Prueba cambiar entre Escala y Tambor 3D abajo"
            };
            for (int i = 0; i < texts.length; i++) {
                sampleMessages.add(makeTextMessage(currentAccount, 900 + i, (i % 2) == 1, texts[i], (texts.length - i) * 60, false, null));
            }

            miniList = new RecyclerListView(context, resourcesProvider) {
                @Override
                public boolean onInterceptTouchEvent(MotionEvent e) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return super.onInterceptTouchEvent(e);
                }

                @Override
                public boolean onTouchEvent(MotionEvent e) {
                    if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return super.onTouchEvent(e);
                }
            };
            miniList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            miniList.setPadding(0, dp(8), 0, dp(28));
            miniList.setClipToPadding(false);
            miniList.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @NonNull
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    ChatMessageCell c = createRealMessageCell(context, currentAccount, resourcesProvider);
                    c.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    return new RecyclerListView.Holder(c);
                }

                @Override
                public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                    ChatMessageCell c = (ChatMessageCell) holder.itemView;
                    c.setMessageObject(sampleMessages.get(position), null, false, false, false);
                }

                @Override
                public int getItemCount() {
                    return sampleMessages.size();
                }
            });
            LagramScrollEdgeController.attach(miniList);
            addView(miniList, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 196));

            hintView = new TextView(context);
            hintView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11f);
            hintView.setTypeface(AndroidUtilities.bold());
            hintView.setGravity(Gravity.CENTER);
            hintView.setText(LocaleController.getString(R.string.LagramDragScrollPreviewHint));
            hintView.setPadding(dp(10), dp(4), dp(10), dp(4));
            addView(hintView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, 0, 6));
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + (LagramConfig.scrollEdgeScale ? 1 : 0);
            h = h * 31L + LagramConfig.scrollEdgeMode;
            h = h * 31L + (LagramConfig.glassMessages ? 1 : 0);
            h = h * 31L + LagramConfig.roundingPreset;
            return h;
        }

        public void bind(long fp) {
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                boolean isDark = Theme.isCurrentThemeDark();
                hintView.setTextColor(isDark ? Color.WHITE : 0xFF141820);
                hintView.setBackground(Theme.createRoundRectDrawable(dp(12), ColorUtils.setAlphaComponent(isDark ? 0xFF101319 : Color.WHITE, 195)));
                miniList.invalidateViews();
                miniList.invalidate();
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            wallpaperHelper.draw(canvas, this, resourcesProvider);
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            wallpaperHelper.detach(this);
        }

        public static class Factory extends UItem.UItemFactory<RealScrollEffectPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealScrollEffectPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealScrollEffectPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealScrollEffectPreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 7. RealChatListFoldersPreviewCell
    // Real Theme surface + REAL interactive FilterTabsView (tap tabs!) + 2 real-styled Lagram chat
    // cards using real AvatarDrawable (live avatarShape, materialChatList, roundingPreset, spacing)!
    // =============================================================================================
    public static class RealChatListFoldersPreviewCell extends LinearLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final FilterTabsView filterTabsView;
        private final ChatRowsView chatRowsView;
        private long lastFingerprint = Long.MIN_VALUE;
        private static final int[] SAMPLE_COUNTS = {14, 3, 28, 7};

        public RealChatListFoldersPreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            filterTabsView = new FilterTabsView(context, resourcesProvider);
            filterTabsView.setDelegate(new FilterTabsView.FilterTabsViewDelegate() {
                @Override public void onPageSelected(FilterTabsView.Tab tab, boolean forward) {
                    chatRowsView.setFolderFilter(tab != null ? tab.id : 0);
                }
                @Override public void onPageScrolled(float progress) {}
                @Override public void onSamePageSelected() {}
                @Override public int getTabCounter(int tabId) {
                    if (!LagramConfig.showFolderCounters) return 0;
                    return (tabId >= 0 && tabId < SAMPLE_COUNTS.length) ? SAMPLE_COUNTS[tabId] : 0;
                }
                @Override public boolean didSelectTab(FilterTabsView.TabView tabView, boolean selected) { return false; }
                @Override public boolean isTabMenuVisible() { return false; }
                @Override public void onDeletePressed(int id) {}
                @Override public void onPageReorder(int fromId, int toId) {}
                @Override public boolean canPerformActions() { return true; }
            });
            addView(filterTabsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 46, 4, 4, 4, 2));

            chatRowsView = new ChatRowsView(context, resourcesProvider);
            addView(chatRowsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + LagramConfig.folderBarStyle;
            h = h * 31L + LagramConfig.folderTabsStyle;
            h = h * 31L + (LagramConfig.showFolderCounters ? 1 : 0);
            h = h * 31L + (LagramConfig.hideAllChatsFolder ? 1 : 0);
            h = h * 31L + (LagramConfig.materialChatList ? 1 : 0);
            h = h * 31L + (LagramConfig.materialContainers ? 1 : 0);
            h = h * 31L + LagramConfig.sectionSpacingDp;
            h = h * 31L + LagramConfig.roundingPreset;
            h = h * 31L + LagramConfig.avatarShape;
            return h;
        }

        public void bind(long fp) {
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                int bg = Theme.isCurrentThemeDark()
                        ? (LagramConfig.amoledSettingsBackdrop ? 0xFF000000 : Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider))
                        : Theme.getColor(Theme.key_windowBackgroundGray, resourcesProvider);
                setBackgroundColor(bg);
                rebuildTabs();
                chatRowsView.invalidate();
            }
        }

        private void rebuildTabs() {
            filterTabsView.removeTabs();
            int style = LagramConfig.folderTabsStyle;
            String[] icons = {"💬", "👤", "👥", "📢"};
            String[] titles = {
                    LocaleController.getString(R.string.FilterAllChats),
                    LocaleController.getString(R.string.FilterContacts),
                    LocaleController.getString(R.string.FilterGroups),
                    LocaleController.getString(R.string.FilterChannels)
            };
            int sortId = 0;
            for (int i = 0; i < titles.length; i++) {
                if (i == 0 && LagramConfig.hideAllChatsFolder) continue;
                String label = style == LagramConfig.FOLDER_TABS_ICON_ONLY ? icons[i]
                        : (style == LagramConfig.FOLDER_TABS_TEXT_ONLY ? titles[i] : icons[i] + " " + titles[i]);
                filterTabsView.addTab(sortId++, i, label, null, false, i == 0, false);
            }
            filterTabsView.finishAddingTabs(false);
            filterTabsView.requestLayout();
            filterTabsView.invalidate();
            if (filterTabsView.getListView() != null) {
                filterTabsView.getListView().invalidateViews();
            }
        }

        private static class ChatRowsView extends View {
            private final Theme.ResourcesProvider resourcesProvider;
            private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint namePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint msgPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint timePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final TextPaint badgePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rect = new RectF();
            private final AvatarDrawable av1 = new AvatarDrawable();
            private final AvatarDrawable av2 = new AvatarDrawable();
            private int folderId = 0;

            ChatRowsView(Context context, Theme.ResourcesProvider resourcesProvider) {
                super(context);
                this.resourcesProvider = resourcesProvider;
                namePaint.setTypeface(AndroidUtilities.bold());
                namePaint.setTextSize(dp(15));
                msgPaint.setTextSize(dp(13.5f));
                timePaint.setTextSize(dp(11.5f));
                badgePaint.setTypeface(AndroidUtilities.bold());
                badgePaint.setTextSize(dp(11.5f));
                av1.setInfo(101L, "Lagram", "News");
                av2.setInfo(205L, "Sofía", "Vega");
            }

            void setFolderFilter(int id) {
                this.folderId = id;
                invalidate();
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), dp(138));
            }

            @Override
            protected void onDraw(@NonNull Canvas canvas) {
                int w = getWidth();
                if (w <= 0) return;

                boolean cards = LagramConfig.materialChatList;
                float cardRad = dp(LagramConfig.getEffectiveCardRadiusDp());
                int cardColor = Theme.isCurrentThemeDark()
                        ? (LagramConfig.amoledSettingsBackdrop ? 0xFF151515 : Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider))
                        : Theme.getColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                int nameCol = Theme.getColor(Theme.key_chats_name, resourcesProvider);
                int msgCol = Theme.getColor(Theme.key_chats_message, resourcesProvider);
                int timeCol = Theme.getColor(Theme.key_chats_date, resourcesProvider);
                int badgeBg = Theme.getColor(Theme.key_chats_unreadCounter, resourcesProvider);
                int dividerCol = Theme.getColor(Theme.key_divider, resourcesProvider);

                String[] names = folderId == 2 ? new String[]{"Lagram Dev Group", "Diseño Android"} : new String[]{"Lagram Channel", "Sofía Vega"};
                String[] msgs = folderId == 2 ? new String[]{"Nuevo diseño M3 + vistas reales listo", "¡Súper fluido a 120Hz!"} : new String[]{"Actualización Turbo lista para descargar", "¿Ya probaste los nuevos estilos?"};
                String[] times = {"14:28", "13:55"};
                String[] badges = {"3", "1"};
                AvatarDrawable[] avs = {av1, av2};

                int rowH = dp(64);
                for (int i = 0; i < 2; i++) {
                    float top = dp(3) + i * (rowH + dp(cards ? 4 : 0));
                    if (cards) {
                        paint.setColor(cardColor);
                        rect.set(dp(8), top, w - dp(8), top + rowH);
                        canvas.drawRoundRect(rect, cardRad, cardRad, paint);
                    } else {
                        paint.setColor(cardColor);
                        canvas.drawRect(0, top, w, top + rowH, paint);
                        if (i == 0) {
                            paint.setColor(dividerCol);
                            canvas.drawLine(dp(72), top + rowH - 1, w - dp(12), top + rowH - 1, paint);
                        }
                    }

                    int avSz = dp(46);
                    int avL = dp(cards ? 18 : 14);
                    int avT = (int) (top + (rowH - avSz) * 0.5f);
                    avs[i].setBounds(avL, avT, avL + avSz, avT + avSz);
                    avs[i].draw(canvas);

                    float textL = avL + avSz + dp(12);
                    namePaint.setColor(nameCol);
                    msgPaint.setColor(msgCol);
                    timePaint.setColor(timeCol);

                    canvas.drawText(names[i], textL, top + dp(27), namePaint);
                    CharSequence ellMsg = TextUtils.ellipsize(msgs[i], msgPaint, w - textL - dp(56), TextUtils.TruncateAt.END);
                    canvas.drawText(ellMsg, 0, ellMsg.length(), textL, top + dp(47), msgPaint);

                    float tw = timePaint.measureText(times[i]);
                    canvas.drawText(times[i], w - dp(cards ? 20 : 14) - tw, top + dp(25), timePaint);

                    paint.setColor(badgeBg);
                    float bx = w - dp(cards ? 20 : 14) - dp(11);
                    float by = top + dp(43);
                    canvas.drawCircle(bx, by, dp(10), paint);
                    badgePaint.setColor(Color.WHITE);
                    float bw = badgePaint.measureText(badges[i]);
                    canvas.drawText(badges[i], bx - bw * 0.5f, by + dp(4), badgePaint);
                }
            }
        }

        public static class Factory extends UItem.UItemFactory<RealChatListFoldersPreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealChatListFoldersPreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealChatListFoldersPreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealChatListFoldersPreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }

    // =============================================================================================
    // 8. RealDeletedMessagePreviewCell
    // Real wallpaper + REAL ChatMessageCell marked with LagramMessageHistoryDb.PREVIEW_DELETED_MSG_ID!
    // Renders the EXACT live customized Deleted Bubble Tint, Text/Badge Color, Icon, Dim & Strike!
    // =============================================================================================
    public static class RealDeletedMessagePreviewCell extends LinearLayout {
        private final Theme.ResourcesProvider resourcesProvider;
        private final int currentAccount = UserConfig.selectedAccount;
        private final WallpaperHelper wallpaperHelper = new WallpaperHelper();
        private final ChatMessageCell deletedCell;
        private final ChatMessageCell normalCell;
        private long lastFingerprint = Long.MIN_VALUE;

        public RealDeletedMessagePreviewCell(Context context, Theme.ResourcesProvider resourcesProvider) {
            super(context);
            this.resourcesProvider = resourcesProvider;
            setOrientation(VERTICAL);
            setWillNotDraw(false);
            setPadding(0, dp(10), 0, dp(10));
            setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            applyRoundedCardOutline(this);

            deletedCell = createRealMessageCell(context, currentAccount, resourcesProvider);
            normalCell = createRealMessageCell(context, currentAccount, resourcesProvider);
            addView(deletedCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            addView(normalCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        public static long fingerprint() {
            LagramConfig.ensureLoaded();
            long h = 17L;
            h = h * 31L + LagramConfig.deletedBubbleTintColor;
            h = h * 31L + LagramConfig.deletedTextColor;
            h = h * 31L + LagramConfig.deletedIconStyle;
            h = h * 31L + (LagramConfig.deletedMessageDim ? 1 : 0);
            h = h * 31L + (LagramConfig.deletedMessageStrike ? 1 : 0);
            h = h * 31L + (LagramConfig.showDeletedTime ? 1 : 0);
            h = h * 31L + (LagramConfig.ghostModeEnabled ? 1 : 0);
            h = h * 31L + (LagramConfig.glassMessages ? 1 : 0);
            h = h * 31L + LagramConfig.roundingPreset;
            return h;
        }

        public void bind(long fp) {
            if (lastFingerprint != fp) {
                lastFingerprint = fp;
                applyRoundedCardOutline(this);
                MessageObject delObj = makeTextMessage(
                        currentAccount,
                        LagramMessageHistoryDb.PREVIEW_DELETED_MSG_ID,
                        false,
                        LocaleController.getString(R.string.LagramDeletedPreviewText),
                        90,
                        false,
                        null
                );
                MessageObject okObj = makeTextMessage(
                        currentAccount,
                        750,
                        true,
                        LagramConfig.ghostModeEnabled ? "👻 Modo Fantasma activo • Invisible" : "Modo Fantasma desactivado",
                        10,
                        false,
                        null
                );
                deletedCell.setMessageObject(delObj, null, false, false, false);
                normalCell.setMessageObject(okObj, null, false, false, false);
                deletedCell.requestLayout();
                deletedCell.invalidate();
                normalCell.requestLayout();
                normalCell.invalidate();
                invalidate();
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            wallpaperHelper.draw(canvas, this, resourcesProvider);
            drawCellParentBackground(canvas, deletedCell, getWidth(), getHeight());
            drawCellParentBackground(canvas, normalCell, getWidth(), getHeight());
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            wallpaperHelper.detach(this);
        }

        public static class Factory extends UItem.UItemFactory<RealDeletedMessagePreviewCell> {
            static { setup(new Factory()); }

            @Override
            public RealDeletedMessagePreviewCell createView(Context context, RecyclerListView listView, int currentAccount, int classGuid, Theme.ResourcesProvider resourcesProvider) {
                return new RealDeletedMessagePreviewCell(context, resourcesProvider);
            }

            @Override
            public void bindView(View view, UItem item, boolean divider, UniversalAdapter adapter, UniversalRecyclerView listView) {
                ((RealDeletedMessagePreviewCell) view).bind(item.longValue);
            }

            @Override public boolean isClickable() { return false; }
            @Override public boolean equals(UItem a, UItem b) { return a.id == b.id; }
            @Override public boolean contentsEquals(UItem a, UItem b) { return a.longValue == b.longValue; }

            public static UItem of(int id) {
                UItem item = UItem.ofFactory(Factory.class);
                item.id = id;
                item.longValue = fingerprint();
                return item;
            }
        }
    }
}

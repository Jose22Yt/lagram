package org.telegram.ui.lagram;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;

/**
 * Live interactive Canvas previews embedded inside Graphite cards at the top
 * of each Lagram Settings subcategory fragment.
 */
public final class LagramPreviewViews {

    private LagramPreviewViews() {
    }

    /**
     * Live preview for Apariencia: home title, folder tabs, chat list rows,
     * avatars, unread badge, online dot, M3 accent, AMOLED black, font & bottom nav.
     */
    public static final class AppearanceLivePreviewView extends View {
        private static final String[] TABS_DEFAULT = new String[]{"Todos", "Personal", "Canales"};
        private static final String[] TABS_NO_ALL = new String[]{"Personal", "Trabajo", "Canales"};
        private static final String[] NAV_ITEMS = new String[]{"Chats", "Contactos", "Llamadas", "Ajustes"};

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public AppearanceLivePreviewView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = MeasureSpec.getSize(widthMeasureSpec);
            int extraNav = LagramConfig.bottomNavigationEnabled ? AndroidUtilities.dp(44) : 0;
            int h = AndroidUtilities.dp(226) + extraNav;
            setMeasuredDimension(w, h);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.accentPalette);
            float scale = LagramThemeEngine.getTextScaleFactor();
            Typeface tf = LagramThemeEngine.resolveCustomTypeface(null);
            textPaint.setTypeface(tf != null ? tf : Typeface.DEFAULT);

            // Outer phone canvas inside the graphite card
            int bg = LagramConfig.amoledPureBlack ? 0xFF000000 : 0xFF1A1D24;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(bg);
            rect.set(AndroidUtilities.dp(6), AndroidUtilities.dp(4), w - AndroidUtilities.dp(6), h - AndroidUtilities.dp(4));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(16), AndroidUtilities.dp(16), paint);

            float left = AndroidUtilities.dp(18);
            float right = w - AndroidUtilities.dp(18);

            // Top title bar
            String title = LagramConfig.homeHeaderTitleMode == LagramConfig.TITLE_USERNAME
                    ? "@lagram_user"
                    : (LagramConfig.homeHeaderTitleMode == LagramConfig.TITLE_ACTIVE_FOLDER ? "Personal" : "Lagram");
            textPaint.setColor(0xFFF3F5FA);
            textPaint.setTextSize(AndroidUtilities.dp(16) * scale);
            textPaint.setFakeBoldText(true);
            canvas.drawText(title, left, AndroidUtilities.dp(30), textPaint);
            textPaint.setFakeBoldText(false);

            // Folder tabs bar
            float tabY = AndroidUtilities.dp(42);
            String[] tabs = LagramConfig.hideAllChatsTab ? TABS_NO_ALL : TABS_DEFAULT;
            float tabX = left;
            for (int i = 0; i < tabs.length; i++) {
                boolean active = i == 0;
                String label = LagramConfig.folderTabsStyle == LagramConfig.FOLDER_TABS_COMPACT_ICONS
                        ? (i == 0 ? "★" : (i == 1 ? "●" : "◆"))
                        : tabs[i];
                textPaint.setTextSize(AndroidUtilities.dp(11.5f) * scale);
                float tw = textPaint.measureText(label);
                float pillW = tw + AndroidUtilities.dp(18);
                if (LagramConfig.folderTabsStyle == LagramConfig.FOLDER_TABS_PILL_M3) {
                    paint.setColor(active ? accent.primary : 0xFF2B2F3A);
                    rect.set(tabX, tabY, tabX + pillW, tabY + AndroidUtilities.dp(24));
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(12), AndroidUtilities.dp(12), paint);
                    textPaint.setColor(active ? accent.onPrimary : 0xFFB0B7C5);
                    canvas.drawText(label, tabX + AndroidUtilities.dp(9), tabY + AndroidUtilities.dp(16), textPaint);
                } else if (LagramConfig.folderTabsStyle == LagramConfig.FOLDER_TABS_UNDERLINE) {
                    textPaint.setColor(active ? accent.primary : 0xFF9EA6B8);
                    canvas.drawText(label, tabX + AndroidUtilities.dp(4), tabY + AndroidUtilities.dp(15), textPaint);
                    if (active) {
                        paint.setColor(accent.primary);
                        rect.set(tabX + AndroidUtilities.dp(2), tabY + AndroidUtilities.dp(21), tabX + tw + AndroidUtilities.dp(6), tabY + AndroidUtilities.dp(23.5f));
                        canvas.drawRoundRect(rect, AndroidUtilities.dp(2), AndroidUtilities.dp(2), paint);
                    }
                } else {
                    paint.setColor(active ? accent.containerDark : 0xFF262A34);
                    rect.set(tabX, tabY, tabX + AndroidUtilities.dp(32), tabY + AndroidUtilities.dp(24));
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(12), AndroidUtilities.dp(12), paint);
                    textPaint.setColor(active ? accent.primary : 0xFF9EA6B8);
                    canvas.drawText(label, tabX + AndroidUtilities.dp(11), tabY + AndroidUtilities.dp(16), textPaint);
                    pillW = AndroidUtilities.dp(32);
                }
                tabX += pillW + AndroidUtilities.dp(8);
            }

            // Two sample chat list rows
            float rowTop = AndroidUtilities.dp(76);
            drawChatRow(canvas, left, right, rowTop, "Elena Morales", "E", "Nuevo diseño Material 3 listo ✨", "10:42", "3", true, accent, scale);
            float rowStep = LagramConfig.chatListStyle == LagramConfig.CHAT_LIST_MINIMAL_SPACIOUS ? AndroidUtilities.dp(70)
                    : (LagramConfig.chatListStyle == LagramConfig.CHAT_LIST_M3_CARDS ? AndroidUtilities.dp(66) : AndroidUtilities.dp(60));
            drawChatRow(canvas, left, right, rowTop + rowStep, "Canal Lagram", "C", "Rendimiento 120 Hz activado", "09:15", "", false, accent, scale);

            // Optional M3 Expressive Bottom Navigation Bar
            if (LagramConfig.bottomNavigationEnabled) {
                float navTop = h - AndroidUtilities.dp(46);
                paint.setColor(LagramConfig.amoledPureBlack ? 0xFF111216 : 0xFF222630);
                rect.set(AndroidUtilities.dp(14), navTop, w - AndroidUtilities.dp(14), h - AndroidUtilities.dp(10));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(18), AndroidUtilities.dp(18), paint);

                float segW = (w - AndroidUtilities.dp(28)) / 4f;
                for (int i = 0; i < NAV_ITEMS.length; i++) {
                    float cx = AndroidUtilities.dp(14) + segW * i + segW * 0.5f;
                    boolean selected = i == 0;
                    if (selected) {
                        paint.setColor(accent.containerDark);
                        rect.set(cx - AndroidUtilities.dp(26), navTop + AndroidUtilities.dp(5), cx + AndroidUtilities.dp(26), navTop + AndroidUtilities.dp(31));
                        canvas.drawRoundRect(rect, AndroidUtilities.dp(13), AndroidUtilities.dp(13), paint);
                    }
                    textPaint.setTextSize(AndroidUtilities.dp(10.5f));
                    textPaint.setColor(selected ? accent.primary : 0xFF9EA6B8);
                    float lw = textPaint.measureText(NAV_ITEMS[i]);
                    canvas.drawText(NAV_ITEMS[i], cx - lw / 2f, navTop + AndroidUtilities.dp(22), textPaint);
                }
            }
        }

        private void drawChatRow(Canvas canvas, float left, float right, float top, String name, String initial, String msg, String time, String unread, boolean online, LagramThemeEngine.AccentColors accent, float scale) {
            float rowH = LagramConfig.chatListStyle == LagramConfig.CHAT_LIST_MINIMAL_SPACIOUS ? AndroidUtilities.dp(62) : AndroidUtilities.dp(56);
            if (LagramConfig.chatListStyle == LagramConfig.CHAT_LIST_M3_CARDS) {
                paint.setColor(LagramConfig.amoledPureBlack ? 0xFF15171D : 0xFF262A34);
                rect.set(left - AndroidUtilities.dp(4), top, right + AndroidUtilities.dp(4), top + rowH);
                canvas.drawRoundRect(rect, AndroidUtilities.dp(16), AndroidUtilities.dp(16), paint);
            } else if (LagramConfig.chatListStyle == LagramConfig.CHAT_LIST_CLASSIC) {
                paint.setColor(0xFF2D313B);
                canvas.drawLine(left + AndroidUtilities.dp(54), top + rowH, right, top + rowH, paint);
            }

            float avatarPx = Math.min(AndroidUtilities.dp(44), Math.max(AndroidUtilities.dp(28), AndroidUtilities.dp(LagramConfig.avatarSizeDp * 0.72f)));
            float avatarRadius = Math.min(avatarPx / 2f, AndroidUtilities.dp(LagramThemeEngine.computeAvatarRadiusDp() * 0.72f));
            float ax = left + AndroidUtilities.dp(4);
            float ay = top + (rowH - avatarPx) / 2f;

            paint.setColor(online ? accent.primary : 0xFF475064);
            rect.set(ax, ay, ax + avatarPx, ay + avatarPx);
            canvas.drawRoundRect(rect, avatarRadius, avatarRadius, paint);

            // Avatar initial
            textPaint.setColor(online ? accent.onPrimary : 0xFFEAEFF8);
            textPaint.setTextSize(avatarPx * 0.42f);
            textPaint.setFakeBoldText(true);
            float iw = textPaint.measureText(initial);
            canvas.drawText(initial, ax + (avatarPx - iw) / 2f, ay + avatarPx * 0.65f, textPaint);
            textPaint.setFakeBoldText(false);

            // Online dot
            if (online && LagramConfig.onlineStatusPosition != LagramConfig.ONLINE_POS_HIDDEN) {
                float dotR = AndroidUtilities.dp(4.5f);
                float dotCx = ax + avatarPx - dotR * 0.6f;
                float dotCy = LagramConfig.onlineStatusPosition == LagramConfig.ONLINE_POS_TOP_RIGHT
                        ? ay + dotR * 0.8f
                        : ay + avatarPx - dotR * 0.6f;
                paint.setColor(0xFF1A1D24);
                canvas.drawCircle(dotCx, dotCy, dotR + AndroidUtilities.dp(1.5f), paint);
                paint.setColor(0xFF2EE07A);
                canvas.drawCircle(dotCx, dotCy, dotR, paint);
            }

            float textX = ax + avatarPx + AndroidUtilities.dp(12);
            textPaint.setColor(0xFFF3F5FA);
            textPaint.setTextSize(AndroidUtilities.dp(13.5f) * scale);
            textPaint.setFakeBoldText(true);
            canvas.drawText(name, textX, top + rowH * 0.42f, textPaint);
            textPaint.setFakeBoldText(false);

            textPaint.setColor(0xFFA2ABB9);
            textPaint.setTextSize(AndroidUtilities.dp(11.5f) * scale);
            canvas.drawText(msg, textX, top + rowH * 0.74f, textPaint);

            // Time
            textPaint.setColor(0xFF8C95A6);
            textPaint.setTextSize(AndroidUtilities.dp(10.5f));
            float timeW = textPaint.measureText(time);
            canvas.drawText(time, right - timeW - AndroidUtilities.dp(4), top + rowH * 0.40f, textPaint);

            // Unread badge
            if (!unread.isEmpty()) {
                if (LagramConfig.unreadBadgeStyle == LagramConfig.UNREAD_BADGE_MINIMAL_DOT) {
                    paint.setColor(accent.primary);
                    canvas.drawCircle(right - AndroidUtilities.dp(10), top + rowH * 0.70f, AndroidUtilities.dp(4.5f), paint);
                } else {
                    float bw = LagramConfig.unreadBadgeStyle == LagramConfig.UNREAD_BADGE_TONAL_PILL ? AndroidUtilities.dp(24) : AndroidUtilities.dp(18);
                    float bh = AndroidUtilities.dp(18);
                    paint.setColor(LagramConfig.unreadBadgeStyle == LagramConfig.UNREAD_BADGE_TONAL_PILL ? accent.containerDark : accent.primary);
                    rect.set(right - bw - AndroidUtilities.dp(4), top + rowH * 0.56f, right - AndroidUtilities.dp(4), top + rowH * 0.56f + bh);
                    canvas.drawRoundRect(rect, bh / 2f, bh / 2f, paint);
                    textPaint.setColor(LagramConfig.unreadBadgeStyle == LagramConfig.UNREAD_BADGE_TONAL_PILL ? accent.primary : accent.onPrimary);
                    textPaint.setTextSize(AndroidUtilities.dp(10.5f));
                    textPaint.setFakeBoldText(true);
                    float uw = textPaint.measureText(unread);
                    canvas.drawText(unread, rect.centerX() - uw / 2f, rect.centerY() + AndroidUtilities.dp(3.8f), textPaint);
                    textPaint.setFakeBoldText(false);
                }
            }
        }
    }

    /**
     * Live preview for Chats: top header, bubbles (corner radius, tail, vertical spacing),
     * sticker scale, and customizable bottom input bar (iOS 3 pills / M3 unified / Classic).
     */
    public static final class ChatsLivePreviewView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Path tailPath = new Path();

        public ChatsLivePreviewView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(256));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            LagramConfig.ensureLoaded();
            int w = getWidth();
            int h = getHeight();
            LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColors();

            // Chat background
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(LagramConfig.amoledPureBlack ? 0xFF000000 : 0xFF181B22);
            rect.set(AndroidUtilities.dp(6), AndroidUtilities.dp(4), w - AndroidUtilities.dp(6), h - AndroidUtilities.dp(4));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(16), AndroidUtilities.dp(16), paint);

            // Top chat header
            paint.setColor(LagramConfig.amoledPureBlack ? 0xFF101216 : 0xFF232731);
            rect.set(AndroidUtilities.dp(12), AndroidUtilities.dp(10), w - AndroidUtilities.dp(12), AndroidUtilities.dp(54));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(14), AndroidUtilities.dp(14), paint);

            float hdrAvatar = AndroidUtilities.dp(LagramConfig.chatHeaderAvatarSizeDp * 0.72f);
            float hdrAx = AndroidUtilities.dp(22);
            float hdrAy = AndroidUtilities.dp(32) - hdrAvatar / 2f;
            paint.setColor(accent.primary);
            rect.set(hdrAx, hdrAy, hdrAx + hdrAvatar, hdrAy + hdrAvatar);
            canvas.drawRoundRect(rect, hdrAvatar / 2f, hdrAvatar / 2f, paint);

            textPaint.setColor(0xFFF3F5FA);
            textPaint.setTextSize(AndroidUtilities.dp(13));
            textPaint.setFakeBoldText(true);
            float nameY = LagramConfig.chatHeaderShowSubtitle ? AndroidUtilities.dp(29) : AndroidUtilities.dp(36);
            canvas.drawText("Sofía Vega", hdrAx + hdrAvatar + AndroidUtilities.dp(10), nameY, textPaint);
            textPaint.setFakeBoldText(false);

            if (LagramConfig.chatHeaderShowSubtitle) {
                textPaint.setColor(accent.primary);
                textPaint.setTextSize(AndroidUtilities.dp(10.5f));
                canvas.drawText("en línea", hdrAx + hdrAvatar + AndroidUtilities.dp(10), AndroidUtilities.dp(43), textPaint);
            }

            if (LagramConfig.chatHeaderCleanQuickButtons) {
                paint.setColor(0xFF323746);
                canvas.drawCircle(w - AndroidUtilities.dp(52), AndroidUtilities.dp(32), AndroidUtilities.dp(11), paint);
                canvas.drawCircle(w - AndroidUtilities.dp(26), AndroidUtilities.dp(32), AndroidUtilities.dp(11), paint);
            }

            // Bubbles
            float rad = AndroidUtilities.dp(Math.min(22, LagramConfig.bubbleCornerRadius));
            int extraGap = LagramThemeEngine.getMessageVerticalSpacingExtraDp();
            float bubbleY1 = AndroidUtilities.dp(66);
            float bubbleH = AndroidUtilities.dp(36);

            // Incoming bubble
            paint.setColor(0xFF282C36);
            rect.set(AndroidUtilities.dp(18), bubbleY1, w * 0.68f, bubbleY1 + bubbleH);
            canvas.drawRoundRect(rect, rad, rad, paint);
            drawTail(canvas, rect, false, 0xFF282C36);
            textPaint.setColor(0xFFF2F4F8);
            textPaint.setTextSize(AndroidUtilities.dp(12.5f));
            canvas.drawText("¿Probaste las nuevas burbujas?", rect.left + AndroidUtilities.dp(12), rect.centerY() + AndroidUtilities.dp(4), textPaint);

            // Outgoing bubble
            float bubbleY2 = bubbleY1 + bubbleH + AndroidUtilities.dp(10 + extraGap);
            paint.setColor(accent.outgoingBubbleDark);
            rect.set(w * 0.30f, bubbleY2, w - AndroidUtilities.dp(18), bubbleY2 + bubbleH);
            canvas.drawRoundRect(rect, rad, rad, paint);
            drawTail(canvas, rect, true, accent.outgoingBubbleDark);
            textPaint.setColor(0xFFFFFFFF);
            canvas.drawText("¡Sí! Ultra fluido a 120 Hz 🚀", rect.left + AndroidUtilities.dp(12), rect.centerY() + AndroidUtilities.dp(4), textPaint);

            // Mini sticker size indicator
            float stickerBox = AndroidUtilities.dp(32) * LagramThemeEngine.getStickerScaleFactor();
            stickerBox = Math.min(AndroidUtilities.dp(42), Math.max(AndroidUtilities.dp(20), stickerBox));
            float stTop = bubbleY2 + bubbleH + AndroidUtilities.dp(8 + extraGap);
            paint.setColor(LagramThemeEngine.withAlpha(accent.primary, 48));
            rect.set(AndroidUtilities.dp(20), stTop, AndroidUtilities.dp(20) + stickerBox, stTop + stickerBox);
            canvas.drawRoundRect(rect, AndroidUtilities.dp(10), AndroidUtilities.dp(10), paint);
            textPaint.setTextSize(stickerBox * 0.52f);
            canvas.drawText("★", rect.centerX() - stickerBox * 0.24f, rect.centerY() + stickerBox * 0.2f, textPaint);

            // Bottom input bar preview
            float barTop = h - AndroidUtilities.dp(48);
            float barBottom = h - AndroidUtilities.dp(12);
            float barLeft = AndroidUtilities.dp(14);
            float barRight = w - AndroidUtilities.dp(14);

            if (LagramConfig.inputBarStyle == LagramConfig.INPUT_BAR_IOS_3_PILLS) {
                float pillH = barBottom - barTop;
                // Left pill: attach
                paint.setColor(0xFF2B2F3A);
                canvas.drawCircle(barLeft + pillH / 2f, barTop + pillH / 2f, pillH / 2f, paint);
                // Right pill: send/mic
                paint.setColor(accent.primary);
                canvas.drawCircle(barRight - pillH / 2f, barTop + pillH / 2f, pillH / 2f, paint);
                // Center pill: text field
                paint.setColor(0xFF2B2F3A);
                rect.set(barLeft + pillH + AndroidUtilities.dp(6), barTop, barRight - pillH - AndroidUtilities.dp(6), barBottom);
                canvas.drawRoundRect(rect, pillH / 2f, pillH / 2f, paint);
                drawInputHintAndButtons(canvas, rect, accent);
            } else if (LagramConfig.inputBarStyle == LagramConfig.INPUT_BAR_M3_UNIFIED) {
                float pillH = barBottom - barTop;
                paint.setColor(0xFF2B2F3A);
                rect.set(barLeft, barTop, barRight, barBottom);
                canvas.drawRoundRect(rect, pillH / 2f, pillH / 2f, paint);
                paint.setColor(accent.primary);
                canvas.drawCircle(barRight - pillH / 2f, barTop + pillH / 2f, pillH * 0.38f, paint);
                drawInputHintAndButtons(canvas, rect, accent);
            } else {
                paint.setColor(0xFF232731);
                rect.set(AndroidUtilities.dp(10), barTop, w - AndroidUtilities.dp(10), barBottom);
                canvas.drawRoundRect(rect, AndroidUtilities.dp(6), AndroidUtilities.dp(6), paint);
                drawInputHintAndButtons(canvas, rect, accent);
            }
        }

        private void drawInputHintAndButtons(Canvas canvas, RectF barRect, LagramThemeEngine.AccentColors accent) {
            textPaint.setColor(0xFF8E96A8);
            textPaint.setTextSize(AndroidUtilities.dp(11.5f));
            canvas.drawText("Mensaje...", barRect.left + AndroidUtilities.dp(14), barRect.centerY() + AndroidUtilities.dp(4), textPaint);

            float bx = barRect.right - AndroidUtilities.dp(30);
            if (!LagramConfig.hideInputQuickAudioVideoButton) {
                paint.setColor(accent.primary);
                canvas.drawCircle(bx, barRect.centerY(), AndroidUtilities.dp(4), paint);
                bx -= AndroidUtilities.dp(14);
            }
            if (!LagramConfig.hideInputBotCommandButton) {
                paint.setColor(0xFF8E96A8);
                canvas.drawCircle(bx, barRect.centerY(), AndroidUtilities.dp(4), paint);
                bx -= AndroidUtilities.dp(14);
            }
            if (!LagramConfig.hideInputGiftButton) {
                paint.setColor(0xFFF5A623);
                canvas.drawCircle(bx, barRect.centerY(), AndroidUtilities.dp(4), paint);
            }
        }

        private void drawTail(Canvas canvas, RectF bubble, boolean outgoing, int color) {
            if (LagramConfig.bubbleTailStyle == LagramConfig.TAIL_MODERN_NONE) {
                return;
            }
            float size = LagramConfig.bubbleTailStyle == LagramConfig.TAIL_MINIMAL_SHORT
                    ? AndroidUtilities.dp(5)
                    : AndroidUtilities.dp(9);
            paint.setColor(color);
            tailPath.reset();
            if (outgoing) {
                tailPath.moveTo(bubble.right - size, bubble.bottom - size);
                tailPath.lineTo(bubble.right + size * 0.55f, bubble.bottom);
                tailPath.lineTo(bubble.right - size * 1.4f, bubble.bottom);
            } else {
                tailPath.moveTo(bubble.left + size, bubble.bottom - size);
                tailPath.lineTo(bubble.left - size * 0.55f, bubble.bottom);
                tailPath.lineTo(bubble.left + size * 1.4f, bubble.bottom);
            }
            tailPath.close();
            canvas.drawPath(tailPath, paint);
        }
    }

    /**
     * Live preview for Reproductor y Multimedia: exteraless-style audio player card
     * with dynamic cover palette and waveform animation indicator.
     */
    public static final class MediaLivePreviewView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public MediaLivePreviewView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(136));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.accentPalette);

            int cardBg = LagramConfig.playerDynamicCoverPalette
                    ? LagramThemeEngine.blendColor(accent.containerDark, 0xFF1C2029, 0.35f)
                    : 0xFF1F222B;
            paint.setColor(cardBg);
            rect.set(AndroidUtilities.dp(6), AndroidUtilities.dp(4), w - AndroidUtilities.dp(6), h - AndroidUtilities.dp(4));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(16), AndroidUtilities.dp(16), paint);

            // Album cover
            float coverSize = AndroidUtilities.dp(56);
            paint.setColor(accent.primary);
            rect.set(AndroidUtilities.dp(18), AndroidUtilities.dp(18), AndroidUtilities.dp(18) + coverSize, AndroidUtilities.dp(18) + coverSize);
            canvas.drawRoundRect(rect, AndroidUtilities.dp(LagramConfig.playerClassicMode ? 8 : 14), AndroidUtilities.dp(LagramConfig.playerClassicMode ? 8 : 14), paint);

            textPaint.setColor(accent.onPrimary);
            textPaint.setTextSize(AndroidUtilities.dp(22));
            canvas.drawText("♪", rect.centerX() - AndroidUtilities.dp(7), rect.centerY() + AndroidUtilities.dp(8), textPaint);

            float tx = rect.right + AndroidUtilities.dp(14);
            textPaint.setColor(0xFFF4F6FA);
            textPaint.setTextSize(AndroidUtilities.dp(14.5f));
            textPaint.setFakeBoldText(true);
            canvas.drawText(LagramConfig.playerClassicMode ? "Reproductor clásico" : "Reproductor Lagram M3", tx, AndroidUtilities.dp(38), textPaint);
            textPaint.setFakeBoldText(false);

            textPaint.setColor(0xFFB0B8C7);
            textPaint.setTextSize(AndroidUtilities.dp(11.5f));
            String photoTag = LagramConfig.photoUploadQuality == LagramConfig.PHOTO_QUALITY_ULTRA ? "Ultra"
                    : (LagramConfig.photoUploadQuality == LagramConfig.PHOTO_QUALITY_HIGH ? "Alta" : "Std");
            String videoTag = LagramConfig.videoUploadQuality == LagramConfig.VIDEO_QUALITY_FHD ? "FHD"
                    : (LagramConfig.videoUploadQuality == LagramConfig.VIDEO_QUALITY_HD ? "HD" : "Std");
            String meta = (LagramConfig.playerWaveAnimation ? "Onda ON" : "Barra fija")
                    + (LagramConfig.videoGesturesEnabled ? " · Gestos ON" : " · Gestos OFF")
                    + " · " + photoTag + "/" + videoTag;
            canvas.drawText(meta, tx, AndroidUtilities.dp(56), textPaint);

            // Waveform or straight progress bar
            float barY = AndroidUtilities.dp(98);
            float barLeft = AndroidUtilities.dp(18);
            float barRight = w - AndroidUtilities.dp(18);
            if (LagramConfig.playerWaveAnimation) {
                int bars = 26;
                float step = (barRight - barLeft) / bars;
                for (int i = 0; i < bars; i++) {
                    float bh = AndroidUtilities.dp(6 + ((i * 7) % 14));
                    paint.setColor(i < 16 ? accent.primary : 0xFF474E60);
                    float bx = barLeft + i * step;
                    rect.set(bx, barY - bh / 2f, bx + step * 0.55f, barY + bh / 2f);
                    canvas.drawRoundRect(rect, AndroidUtilities.dp(3), AndroidUtilities.dp(3), paint);
                }
            } else {
                paint.setColor(0xFF474E60);
                rect.set(barLeft, barY - AndroidUtilities.dp(2.5f), barRight, barY + AndroidUtilities.dp(2.5f));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(3), AndroidUtilities.dp(3), paint);
                paint.setColor(accent.primary);
                rect.set(barLeft, barY - AndroidUtilities.dp(2.5f), barLeft + (barRight - barLeft) * 0.62f, barY + AndroidUtilities.dp(2.5f));
                canvas.drawRoundRect(rect, AndroidUtilities.dp(3), AndroidUtilities.dp(3), paint);
            }
        }
    }

    /**
     * Live preview for Privacidad y Utilidades: masked phone, parallel translator bubble,
     * and confirmation guard badges.
     */
    public static final class PrivacyLivePreviewView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public PrivacyLivePreviewView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), AndroidUtilities.dp(128));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            LagramThemeEngine.AccentColors accent = LagramThemeEngine.getAccentColorsForPalette(LagramConfig.accentPalette);

            paint.setColor(LagramConfig.amoledPureBlack ? 0xFF000000 : 0xFF1A1D24);
            rect.set(AndroidUtilities.dp(6), AndroidUtilities.dp(4), w - AndroidUtilities.dp(6), h - AndroidUtilities.dp(4));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(16), AndroidUtilities.dp(16), paint);

            // Phone visibility line
            textPaint.setColor(0xFFA7B0C0);
            textPaint.setTextSize(AndroidUtilities.dp(11.5f));
            canvas.drawText("Teléfono en perfil:", AndroidUtilities.dp(18), AndroidUtilities.dp(28), textPaint);

            textPaint.setColor(0xFFF3F5FA);
            textPaint.setTextSize(AndroidUtilities.dp(13.5f));
            textPaint.setFakeBoldText(true);
            canvas.drawText(LagramThemeEngine.maskPhoneNumber("+34 612 345 678"), AndroidUtilities.dp(126), AndroidUtilities.dp(28), textPaint);
            textPaint.setFakeBoldText(false);

            // Parallel translator bubble preview
            paint.setColor(0xFF282C36);
            rect.set(AndroidUtilities.dp(18), AndroidUtilities.dp(42), w - AndroidUtilities.dp(18), AndroidUtilities.dp(86));
            canvas.drawRoundRect(rect, AndroidUtilities.dp(12), AndroidUtilities.dp(12), paint);

            textPaint.setColor(0xFFF3F5FA);
            textPaint.setTextSize(AndroidUtilities.dp(12));
            canvas.drawText("Hello! Ready for Lagram?", AndroidUtilities.dp(28), AndroidUtilities.dp(60), textPaint);

            textPaint.setColor(LagramConfig.parallelTranslatorEnabled ? accent.primary : 0xFF778094);
            textPaint.setTextSize(AndroidUtilities.dp(11.5f));
            String secondLine = LagramConfig.parallelTranslatorEnabled
                    ? "Traducción: ¡Hola! ¿Listo para Lagram?"
                    : "Traductor paralelo desactivado";
            canvas.drawText(secondLine, AndroidUtilities.dp(28), AndroidUtilities.dp(77), textPaint);

            // Guard chips (zero allocations)
            String guards;
            if (LagramConfig.confirmBeforeCall) {
                guards = LagramConfig.confirmBeforeSendSticker
                        ? "Llamadas: Confirmar   ·   Stickers: Confirmar"
                        : "Llamadas: Confirmar   ·   Stickers: Directo";
            } else {
                guards = LagramConfig.confirmBeforeSendSticker
                        ? "Llamadas: Directo   ·   Stickers: Confirmar"
                        : "Llamadas: Directo   ·   Stickers: Directo";
            }
            textPaint.setColor(0xFFA7B0C0);
            textPaint.setTextSize(AndroidUtilities.dp(11));
            canvas.drawText(guards, AndroidUtilities.dp(18), AndroidUtilities.dp(108), textPaint);
        }
    }
}

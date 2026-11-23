package org.telegram.ui.lagram.player;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.LruCache;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.audioinfo.AudioInfo;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BackupImageView;

/**
 * exteraless-style high-resolution album artwork view with LRU-cached async Palette extraction.
 */
public class CoverImage extends BackupImageView {

    public interface SeedListener {
        void onSeed(MessageObject message, int seed);
    }

    private static final LruCache<String, Integer> seedCache = new LruCache<>(64);

    private final Paint placeholderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final PlayerIcon note;
    private int radius;
    private int fallbackSeed;
    private String currentKey;
    private MessageObject message;
    private SeedListener listener;

    public CoverImage(Context context, int radiusDp, int noteSizeDp) {
        super(context);
        radius = dp(radiusDp);
        note = PlayerIcon.stroke(PlayerIcon.NOTE, noteSizeDp);
        setRoundRadius(radius);
        getImageReceiver().setDelegate((imageReceiver, set, thumb, memCache) -> {
            if (set) {
                Bitmap bmp = imageReceiver.getBitmap();
                if (bmp != null) {
                    extractSeed(bmp);
                }
            }
        });
    }

    public void setPlaceholderColors(int bg, int fg) {
        placeholderPaint.setColor(bg);
        note.setColor(fg);
        invalidate();
    }

    public void setSeedListener(int fallback, SeedListener listener) {
        this.fallbackSeed = fallback;
        this.listener = listener;
    }

    public static String keyOf(MessageObject msg) {
        if (msg == null) return null;
        TLRPC.Document doc = msg.getDocument();
        if (doc != null && doc.id != 0) {
            return "d" + doc.id;
        }
        return "m" + msg.getDialogId() + "_" + msg.getId();
    }

    public static Integer cachedSeed(MessageObject msg) {
        String k = keyOf(msg);
        return k == null ? null : seedCache.get(k);
    }

    private static Bitmap resolveEmbeddedCover(MessageObject msg) {
        if (msg == null) return null;
        MessageObject playing = MediaController.getInstance().getPlayingMessageObject();
        if (playing != null && TextUtils.equals(keyOf(playing), keyOf(msg))) {
            AudioInfo info = MediaController.getInstance().getAudioInfo();
            if (info != null && info.getCover() != null) {
                return info.getCover();
            }
        }
        return null;
    }

    private static TLRPC.PhotoSize docThumb(MessageObject msg, int size) {
        TLRPC.Document doc = msg.getDocument();
        TLRPC.PhotoSize thumb = doc != null ? FileLoader.getClosestPhotoSizeWithSize(doc.thumbs, size) : null;
        return (thumb instanceof TLRPC.TL_photoSize || thumb instanceof TLRPC.TL_photoSizeProgressive) ? thumb : null;
    }

    public void setMessage(MessageObject msg) {
        if (msg == null) {
            currentKey = null;
            message = null;
            setImageDrawable(null);
            return;
        }
        Bitmap embedded = resolveEmbeddedCover(msg);
        String key = keyOf(msg) + (embedded != null ? ":e" : "");
        if (key.equals(currentKey)) {
            return;
        }
        currentKey = key;
        message = msg;
        if (embedded != null && !embedded.isRecycled()) {
            setImageBitmap(embedded);
            extractSeed(embedded);
            return;
        }
        TLRPC.PhotoSize fullThumb = docThumb(msg, 1280);
        TLRPC.PhotoSize smallThumb = docThumb(msg, 360);
        ImageLocation fullLoc = fullThumb != null ? ImageLocation.getForDocument(fullThumb, msg.getDocument()) : null;
        ImageLocation smallLoc = smallThumb != null ? ImageLocation.getForDocument(smallThumb, msg.getDocument()) : null;
        if (fullLoc == null) {
            String url = msg.getArtworkUrl(false);
            if (!TextUtils.isEmpty(url)) fullLoc = ImageLocation.getForPath(url);
        }
        if (smallLoc == null) {
            String url = msg.getArtworkUrl(true);
            if (!TextUtils.isEmpty(url)) smallLoc = ImageLocation.getForPath(url);
        }
        if (fullLoc != null) {
            setImage(fullLoc, null, smallLoc, null, null, 0, 1, msg);
        } else if (smallLoc != null) {
            setImage(null, null, smallLoc, null, null, 0, 1, msg);
        } else {
            setImageDrawable(null);
        }
    }

    private void extractSeed(Bitmap bitmap) {
        final MessageObject target = message;
        if (bitmap == null || target == null || listener == null) return;
        final String key = keyOf(target);
        if (key == null) return;
        Integer cached = seedCache.get(key);
        if (cached != null) {
            listener.onSeed(target, cached);
            return;
        }
        final Bitmap sample = PlayerColors.sample(bitmap);
        if (sample == null) return;
        final int fb = fallbackSeed;
        Utilities.globalQueue.postRunnable(() -> {
            int seed = PlayerColors.seedFromSample(sample, fb);
            AndroidUtilities.runOnUIThread(() -> {
                seedCache.put(key, seed);
                if (target == message && listener != null) {
                    listener.onSeed(target, seed);
                }
            });
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!getImageReceiver().hasBitmapImage()) {
            rect.set(0, 0, getWidth(), getHeight());
            canvas.drawRoundRect(rect, radius, radius, placeholderPaint);
            note.setBounds(0, 0, getWidth(), getHeight());
            note.draw(canvas);
        }
        super.onDraw(canvas);
    }
}

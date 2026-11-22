package org.telegram.ui.lagram;

import static org.telegram.messenger.AndroidUtilities.dp;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.Cells.ChatMessageCell;

/**
 * Opexgram-inspired Scroll Edge Scale animation controller (port of gc/j.java).
 * Smoothly scales and compresses list items near the top and bottom visible scroll edges.
 */
public final class LagramScrollEdgeController {

    private static final float MIN_EDGE_SCALE = 0.90f;
    private static final int EDGE_ZONE_DP = 92;

    private LagramScrollEdgeController() {}

    public static void attach(RecyclerView recyclerView) {
        if (recyclerView == null) {
            return;
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (!LagramConfig.scrollEdgeScale) {
                    return;
                }
                if (dy != 0) {
                    applyEdgeScale(rv);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                if (!LagramConfig.scrollEdgeScale) {
                    resetAllChildren(rv);
                    return;
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    animateResetAllChildren(rv);
                }
            }
        });

        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                if (LagramConfig.scrollEdgeScale && recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
                    applyChildScale(recyclerView, view);
                } else {
                    resetChild(view);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                resetChild(view);
            }
        });
    }

    private static void applyEdgeScale(RecyclerView rv) {
        int count = rv.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = rv.getChildAt(i);
            if (child != null) {
                applyChildScale(rv, child);
            }
        }
    }

    private static void applyChildScale(RecyclerView rv, View child) {
        int rvHeight = rv.getHeight();
        if (rvHeight <= 0 || child.getHeight() <= 0) {
            return;
        }
        // Avoid interfering with pinned headers or tall messages larger than half the screen
        if (child.getHeight() > rvHeight * 0.65f) {
            resetChild(child);
            return;
        }
        int topEdge = rv.getPaddingTop();
        int bottomEdge = rvHeight - rv.getPaddingBottom();
        float edgeZone = dp(EDGE_ZONE_DP);
        if (edgeZone <= 0) {
            return;
        }

        float childCenterY = (child.getTop() + child.getBottom()) * 0.5f;
        float distFromTop = childCenterY - topEdge;
        float distFromBottom = bottomEdge - childCenterY;
        float minDist = Math.min(distFromTop, distFromBottom);

        if (minDist >= edgeZone) {
            if (child.getScaleX() != 1f || child.getScaleY() != 1f) {
                child.setScaleX(1f);
                child.setScaleY(1f);
            }
            return;
        }

        float t = Utilities.clamp(minDist / edgeZone, 1f, 0f);
        // Smooth cubic step t*t*(3 - 2*t)
        float smooth = t * t * (3f - 2f * t);
        float scale = MIN_EDGE_SCALE + (1f - MIN_EDGE_SCALE) * smooth;

        if (child instanceof ChatMessageCell) {
            ChatMessageCell cell = (ChatMessageCell) child;
            boolean isOut = cell.getMessageObject() != null && cell.getMessageObject().isOutOwner();
            child.setPivotX(isOut ? child.getWidth() * 0.82f : child.getWidth() * 0.18f);
        } else {
            child.setPivotX(child.getWidth() * 0.5f);
        }
        child.setPivotY(distFromTop < distFromBottom ? child.getHeight() : 0f);
        child.setScaleX(scale);
        child.setScaleY(scale);
    }

    private static void animateResetAllChildren(RecyclerView rv) {
        int count = rv.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = rv.getChildAt(i);
            if (child != null && (Math.abs(child.getScaleX() - 1f) > 0.002f || Math.abs(child.getScaleY() - 1f) > 0.002f)) {
                child.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            }
        }
    }

    private static void resetAllChildren(RecyclerView rv) {
        int count = rv.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = rv.getChildAt(i);
            if (child != null) {
                resetChild(child);
            }
        }
    }

    private static void resetChild(View child) {
        if (child.getScaleX() != 1f) {
            child.setScaleX(1f);
        }
        if (child.getScaleY() != 1f) {
            child.setScaleY(1f);
        }
    }
}

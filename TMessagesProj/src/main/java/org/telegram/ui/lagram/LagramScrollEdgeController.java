package org.telegram.ui.lagram;

import android.graphics.RenderEffect;
import android.graphics.RuntimeShader;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LagramConfig;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.ui.Cells.ChatActionCell;
import org.telegram.ui.Cells.ChatMessageCell;
import org.telegram.ui.Cells.DialogCell;

/**
 * Authentic Opexgram Scroll Edge Animation engine (port of Opexgram gc.j + gc.h + gc.m).
 * Supports both:
 *  - Mode 0 (SCROLL_EDGE_SCALE): PreDraw-driven elastic edge scaling, smooth alpha falloff,
 *    cumulative stacking translationY (so scaled items pack cleanly without gaps), and bubble-edge
 *    pivotX with critically-damped spring transitions.
 *  - Mode 1 (SCROLL_EDGE_DRUM, API 33+): Authentic 3D cylindrical AGSL RuntimeShader RenderEffect
 *    from lagram_scroll_drum.agsl curving the top and bottom viewport edges.
 */
public final class LagramScrollEdgeController implements
        ViewTreeObserver.OnPreDrawListener,
        View.OnAttachStateChangeListener,
        RecyclerView.OnChildAttachStateChangeListener,
        View.OnLayoutChangeListener {

    private static final int TAG_KEY = 0x5A4C4752;

    private final RecyclerView recyclerView;
    private final float scaleZonePx;
    private final float maxScaleRefHeightPx;
    private final float drumZonePx;

    private ViewTreeObserver observedTree;
    private DrumEffect drumEffect;

    private long lastFrameTimeMs;
    private int generation;

    // Critically damped spring states: [value, velocity]
    private final float[] scaleSpring = new float[2];
    private final float[] drumSpring = new float[2];
    private final View[] boundaryViews = new View[2];

    // Tracked transformed child state (10 floats per tracked view:
    // [0..3] = base scaleX, scaleY, alpha, translationY
    // [4..7] = last applied scaleX, scaleY, alpha, translationY
    // [8..9] = external translationY tracking ratio)
    private int trackedCount;
    private View[] trackedViews = new View[16];
    private float[] trackedState = new float[160];
    private int[] trackedGen = new int[16];
    private boolean[] trackedPivotSet = new boolean[16];
    private boolean[] trackedIdentity = new boolean[16];

    // Per-frame sorted visible children buffers (zero-allocation)
    private int visibleCount;
    private View[] visibleViews = new View[16];
    private float[] itemScale = new float[16];
    private float[] itemAlpha = new float[16];
    private float[] itemShiftY = new float[16];
    private float[] itemShrinkH = new float[16];
    private boolean[] itemIsTopEdge = new boolean[16];

    private LagramScrollEdgeController(@NonNull RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        float d = AndroidUtilities.density;
        this.scaleZonePx = 84.0f * d;
        this.maxScaleRefHeightPx = 130.0f * d;
        this.drumZonePx = 96.0f * d;
        recyclerView.addOnChildAttachStateChangeListener(this);
        recyclerView.addOnAttachStateChangeListener(this);
        if (recyclerView.isAttachedToWindow()) {
            onViewAttachedToWindow(recyclerView);
        }
    }

    public static void attach(RecyclerView listView) {
        if (listView == null) {
            return;
        }
        if (listView.getTag(TAG_KEY) instanceof LagramScrollEdgeController) {
            return;
        }
        LagramScrollEdgeController controller = new LagramScrollEdgeController(listView);
        listView.setTag(TAG_KEY, controller);
    }

    private static float clamp01(float v) {
        if (v < 0.0f) return 0.0f;
        if (v > 1.0f) return 1.0f;
        return v;
    }

    /**
     * Critically damped harmonic spring step (exact match to Opexgram gc.m.f).
     * @return true when settled at target.
     */
    private static boolean stepSpring(float target, float stiffness, float dt, float[] state) {
        if (dt > 0.0f) {
            float clampedDt = Math.min(dt, 0.25f);
            float w = (float) Math.sqrt(Math.max(1.0E-4f, stiffness));
            float x0 = state[0] - target;
            float v0 = state[1];
            float exp = (float) Math.exp(-w * clampedDt);
            float c2 = w * x0 + v0;
            float xTerm = x0 + c2 * clampedDt;
            state[0] = target + xTerm * exp;
            state[1] = (c2 - w * xTerm) * exp;
        }
        if (Math.abs(state[0] - target) < 0.002f && Math.abs(state[1]) < 0.125f) {
            state[0] = target;
            state[1] = 0.0f;
            return true;
        }
        return false;
    }

    private static float resolveExternalTranslationY(float[] arr, int base, float currentTy) {
        int prevExtIdx = base + 8;
        float prevExt = arr[prevExtIdx];
        if (prevExt == 0.0f || currentTy * prevExt <= 0.0f || Math.abs(currentTy) > Math.abs(prevExt)) {
            arr[prevExtIdx] = 0.0f;
            float appliedTy = arr[base + 7];
            float baseTy = arr[base + 3];
            if (appliedTy == baseTy || currentTy * appliedTy <= 0.0f || Math.abs(currentTy) >= Math.abs(appliedTy)) {
                return currentTy;
            }
            float ratio = baseTy / appliedTy;
            arr[base + 9] = ratio;
            arr[prevExtIdx] = currentTy;
            return currentTy * ratio;
        } else {
            arr[prevExtIdx] = currentTy;
            return currentTy * arr[base + 9];
        }
    }

    private float computeScale(float distFromEdge, float zone, float itemHeight, float strength) {
        float t1 = clamp01(1.0f - (distFromEdge / (Math.min(itemHeight, zone) + zone))) * strength;
        float t2 = clamp01((-distFromEdge) / zone) * strength;
        return 1.0f - (((t2 * 0.06f) + (0.14f * t1 * t1)) * Math.min(1.0f, maxScaleRefHeightPx / itemHeight));
    }

    private static float computeAlpha(float distFromEdge, float zone, float strength) {
        return 1.0f - (clamp01(1.0f - (distFromEdge / zone)) * strength * 0.5f)
                - (clamp01((-distFromEdge) / zone) * strength * 0.32f);
    }

    private static boolean isEligibleChild(View child) {
        if (child instanceof ChatMessageCell) {
            return ((ChatMessageCell) child).getCurrentMessagesGroup() == null;
        }
        return (child instanceof ChatActionCell) || (child instanceof DialogCell);
    }

    private static float computePivotX(View child) {
        if (child instanceof ChatMessageCell) {
            ChatMessageCell cell = (ChatMessageCell) child;
            MessageObject msg = cell.getMessageObject();
            if (msg != null && msg.isOutOwner()) {
                return cell.getBackgroundDrawableRight();
            }
            return cell.getBackgroundDrawableLeft();
        }
        return child.getWidth() * 0.5f;
    }

    private void collectSortedVisibleChildren(boolean includeTranslationY) {
        visibleCount = 0;
        int childCount = recyclerView.getChildCount();
        if (visibleViews.length < childCount) {
            int cap = Math.max(childCount, visibleViews.length * 2);
            visibleViews = new View[cap];
            itemScale = new float[cap];
            itemAlpha = new float[cap];
            itemShiftY = new float[cap];
            itemShrinkH = new float[cap];
            itemIsTopEdge = new boolean[cap];
        }
        for (int i = 0; i < childCount; i++) {
            View child = recyclerView.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                float y = includeTranslationY ? child.getY() : child.getTop();
                int pos = visibleCount++;
                while (pos > 0) {
                    View prev = visibleViews[pos - 1];
                    float prevY = includeTranslationY ? prev.getY() : prev.getTop();
                    if (prevY <= y) {
                        break;
                    }
                    visibleViews[pos] = prev;
                    pos--;
                }
                visibleViews[pos] = child;
            }
        }
    }

    private boolean isBoundaryItem(View view, boolean topEdge, int edgeOffset, boolean cacheBoundary) {
        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        if (lm == null) {
            return true;
        }
        int slot = topEdge ? 0 : 1;
        int adapterPos = recyclerView.getChildAdapterPosition(view);
        if (adapterPos != RecyclerView.NO_POSITION) {
            boolean reverse = (lm instanceof LinearLayoutManager) && ((LinearLayoutManager) lm).getReverseLayout();
            int targetPos = edgeOffset;
            if (topEdge == reverse) {
                targetPos = (lm.getItemCount() - 1) - edgeOffset;
            }
            boolean match = adapterPos == targetPos;
            if (cacheBoundary) {
                boundaryViews[slot] = match ? view : null;
            }
            return match;
        }
        return cacheBoundary && boundaryViews[slot] == view;
    }

    private float computeTopReach(float topEdgeY, float zone, boolean includeTy) {
        if (visibleCount == 0) {
            return 0.0f;
        }
        View first = visibleViews[0];
        if (isBoundaryItem(first, true, 0, includeTy)) {
            float top = first.getTop() + (includeTy ? first.getTranslationY() : 0.0f);
            return clamp01((topEdgeY - top) / zone);
        }
        return 1.0f;
    }

    private float computeBottomReach(float bottomEdgeY, float zone, boolean includeTy) {
        if (visibleCount == 0) {
            return 0.0f;
        }
        View last = visibleViews[visibleCount - 1];
        if (isBoundaryItem(last, false, 0, includeTy)) {
            float bottom = last.getBottom() + (includeTy ? last.getTranslationY() : 0.0f);
            return clamp01((bottom - bottomEdgeY) / zone);
        }
        return 1.0f;
    }

    private int findTrackedIndex(View view) {
        for (int i = 0; i < trackedCount; i++) {
            if (trackedViews[i] == view) {
                return i;
            }
        }
        return -1;
    }

    private void removeTracked(int idx) {
        trackedViews[idx].removeOnLayoutChangeListener(this);
        int last = trackedCount - 1;
        if (idx != last) {
            trackedViews[idx] = trackedViews[last];
            trackedGen[idx] = trackedGen[last];
            trackedPivotSet[idx] = trackedPivotSet[last];
            trackedIdentity[idx] = trackedIdentity[last];
            System.arraycopy(trackedState, last * 10, trackedState, idx * 10, 10);
        }
        trackedViews[last] = null;
        trackedPivotSet[last] = false;
        trackedIdentity[last] = false;
        trackedCount = last;
    }

    private void resetPivot(int idx) {
        if (trackedPivotSet[idx]) {
            trackedPivotSet[idx] = false;
            View v = trackedViews[idx];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                v.resetPivot();
            } else {
                v.setPivotX(v.getWidth() * 0.5f);
            }
        }
    }

    private boolean restoreTrackedView(int idx) {
        View v = trackedViews[idx];
        int base = idx * 10;
        boolean changed = false;
        if (v.getScaleX() == trackedState[base + 4] && trackedState[base + 4] != trackedState[base]) {
            v.setScaleX(trackedState[base]);
            changed = true;
        }
        if (v.getScaleY() == trackedState[base + 5] && trackedState[base + 5] != trackedState[base + 1]) {
            v.setScaleY(trackedState[base + 1]);
            changed = true;
        }
        if (v.getAlpha() == trackedState[base + 6] && trackedState[base + 6] != trackedState[base + 2]) {
            v.setAlpha(trackedState[base + 2]);
            changed = true;
        }
        if (v.getTranslationY() == trackedState[base + 7] && trackedState[base + 7] != trackedState[base + 3]) {
            v.setTranslationY(trackedState[base + 3]);
            changed = true;
        }
        resetPivot(idx);
        return changed;
    }

    @Override
    public boolean onPreDraw() {
        LagramConfig.ensureLoaded();
        boolean enabled = LagramConfig.scrollEdgeScale;
        boolean useDrum = enabled && LagramConfig.scrollEdgeMode == LagramConfig.SCROLL_EDGE_DRUM
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
        boolean useScale = enabled && !useDrum;

        boolean hasDrumWork = useDrum || drumEffect != null;
        boolean hasScaleWork = useScale || scaleSpring[0] != 0.0f || trackedCount > 0;
        if (!hasDrumWork && !hasScaleWork) {
            lastFrameTimeMs = 0L;
            return true;
        }

        long now = AnimationUtils.currentAnimationTimeMillis();
        float dt = lastFrameTimeMs == 0L ? (1.0f / 60.0f) : Math.min(0.05f, (now - lastFrameTimeMs) / 1000.0f);
        lastFrameTimeMs = now;

        boolean settled = true;

        // --- Mode 1: Authentic Opexgram 3D Scroll Drum AGSL Shader (API 33+) ---
        if (hasDrumWork) {
            if (!useDrum || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (drumEffect != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    drumEffect.clear(recyclerView);
                }
                drumEffect = null;
                boundaryViews[0] = null;
                boundaryViews[1] = null;
                drumSpring[0] = 0.0f;
                drumSpring[1] = 0.0f;
            } else {
                if (drumEffect == null) {
                    drumEffect = DrumEffect.create();
                    if (drumEffect != null) {
                        drumSpring[0] = 1.0f;
                        drumSpring[1] = 0.0f;
                    }
                }
                if (drumEffect != null) {
                    settled = stepSpring(1.0f, 380.0f, dt, drumSpring);
                    float strength = drumSpring[0];
                    float topEdge = Math.max(0.0f, Math.min(recyclerView.getPaddingTop(), recyclerView.getHeight()));
                    float bottomEdge = recyclerView.getHeight() - recyclerView.getPaddingBottom();
                    float zone = Math.min(drumZonePx, Math.max(0.0f, (bottomEdge - topEdge) * 0.5f));
                    collectSortedVisibleChildren(true);
                    float topReach = zone > 0.0f ? computeTopReach(topEdge, zone, true) * strength : 0.0f;
                    float bottomReach = zone > 0.0f ? computeBottomReach(bottomEdge, zone, true) * strength : 0.0f;
                    for (int i = 0; i < visibleCount; i++) {
                        visibleViews[i] = null;
                    }
                    drumEffect.update(recyclerView, zone, topEdge, bottomEdge, topReach, bottomReach);
                }
            }
        }

        // --- Mode 0: Authentic Opexgram Edge Scale + Cumulative Stacking TranslationY ---
        if (hasScaleWork) {
            settled &= stepSpring(useScale ? 1.0f : 0.0f, 800.0f, dt, scaleSpring);
            float strength = scaleSpring[0];
            generation++;
            collectSortedVisibleChildren(false);

            float topEdge = Math.max(0.0f, Math.min(recyclerView.getPaddingTop(), recyclerView.getHeight()));
            float bottomEdge = recyclerView.getHeight() - recyclerView.getPaddingBottom();
            float zone = Math.min(scaleZonePx, Math.max(0.0f, (bottomEdge - topEdge) * 0.5f));
            boolean active = strength > 0.0f && zone > 0.0f && visibleCount > 0;
            float topReach = active ? computeTopReach(topEdge, zone, false) * strength : 0.0f;
            float bottomReach = active ? computeBottomReach(bottomEdge, zone, false) * strength : 0.0f;

            for (int i = 0; i < visibleCount; i++) {
                View child = visibleViews[i];
                itemScale[i] = 1.0f;
                itemAlpha[i] = 1.0f;
                itemShiftY[i] = 0.0f;
                itemShrinkH[i] = 0.0f;

                float ty = child.getTranslationY();
                int tidx = findTrackedIndex(child);
                if (tidx >= 0) {
                    int base = tidx * 10;
                    ty = (ty == trackedState[base + 7])
                            ? trackedState[base + 3]
                            : resolveExternalTranslationY(trackedState, base, ty);
                }

                float top = child.getTop() + ty;
                float bottom = child.getBottom() + ty;
                float distTop = bottom - topEdge;
                float distBottom = bottomEdge - top;
                boolean nearTop = distTop < distBottom;
                itemIsTopEdge[i] = nearTop;

                float edgeReach = nearTop ? topReach : bottomReach;
                float h = bottom - top;
                if (edgeReach > 0.0f && h > 0.0f && isEligibleChild(child)) {
                    float dist = nearTop ? distTop : distBottom;
                    float sc = computeScale(dist, zone, h, edgeReach);
                    float al = computeAlpha(dist, zone, edgeReach);
                    itemScale[i] = sc;
                    itemAlpha[i] = al;
                    itemShrinkH[i] = (1.0f - sc) * h;
                }
            }

            // Cumulative vertical compaction so scaled bubbles pack together naturally (exact gc.j math)
            float accumTop = 0.0f;
            for (int i = visibleCount - 1; i >= 0; i--) {
                if (itemIsTopEdge[i]) {
                    itemShiftY[i] = (itemShrinkH[i] * 0.5f) + accumTop;
                    accumTop += itemShrinkH[i];
                }
            }
            float accumBottom = 0.0f;
            for (int i = 0; i < visibleCount; i++) {
                if (!itemIsTopEdge[i]) {
                    itemShiftY[i] = -accumBottom - (itemShrinkH[i] * 0.5f);
                    accumBottom += itemShrinkH[i];
                }
            }

            // Apply transforms cleanly while preserving external animations
            for (int i = 0; i < visibleCount; i++) {
                View child = visibleViews[i];
                float sc = itemScale[i];
                float al = itemAlpha[i];
                float dy = itemShiftY[i];
                int tidx = findTrackedIndex(child);
                boolean isIdentity = (sc == 1.0f && al == 1.0f && dy == 0.0f);

                if (tidx < 0) {
                    if (isIdentity) {
                        visibleViews[i] = null;
                        continue;
                    }
                    if (trackedCount == trackedViews.length) {
                        int oldCap = trackedViews.length;
                        int newCap = oldCap * 2;
                        View[] nv = new View[newCap];
                        System.arraycopy(trackedViews, 0, nv, 0, trackedCount);
                        trackedViews = nv;
                        float[] ns = new float[newCap * 10];
                        System.arraycopy(trackedState, 0, ns, 0, trackedCount * 10);
                        trackedState = ns;
                        int[] ng = new int[newCap];
                        System.arraycopy(trackedGen, 0, ng, 0, trackedCount);
                        trackedGen = ng;
                        boolean[] np = new boolean[newCap];
                        System.arraycopy(trackedPivotSet, 0, np, 0, trackedCount);
                        trackedPivotSet = np;
                        boolean[] ni = new boolean[newCap];
                        System.arraycopy(trackedIdentity, 0, ni, 0, trackedCount);
                        trackedIdentity = ni;
                    }
                    tidx = trackedCount++;
                    trackedViews[tidx] = child;
                    child.addOnLayoutChangeListener(this);
                    trackedPivotSet[tidx] = false;
                    trackedIdentity[tidx] = false;
                    int b = tidx * 10;
                    float sx = child.getScaleX();
                    float sy = child.getScaleY();
                    float ca = child.getAlpha();
                    float cy = child.getTranslationY();
                    trackedState[b] = sx;
                    trackedState[b + 4] = sx;
                    trackedState[b + 1] = sy;
                    trackedState[b + 5] = sy;
                    trackedState[b + 2] = ca;
                    trackedState[b + 6] = ca;
                    trackedState[b + 3] = cy;
                    trackedState[b + 7] = cy;
                    trackedState[b + 8] = 0.0f;
                    trackedState[b + 9] = 0.0f;
                }

                trackedGen[tidx] = generation;
                trackedIdentity[tidx] = isIdentity;
                int base = tidx * 10;

                if (sc != 1.0f && !trackedPivotSet[tidx]) {
                    child.setPivotX(computePivotX(child));
                    trackedPivotSet[tidx] = true;
                }

                float curSx = child.getScaleX();
                if (curSx != trackedState[base + 4]) {
                    trackedState[base] = curSx;
                }
                float targetSx = trackedState[base] * sc;
                if (targetSx != curSx) {
                    child.setScaleX(targetSx);
                }
                trackedState[base + 4] = targetSx;

                float curSy = child.getScaleY();
                if (curSy != trackedState[base + 5]) {
                    trackedState[base + 1] = curSy;
                }
                float targetSy = trackedState[base + 1] * sc;
                if (targetSy != curSy) {
                    child.setScaleY(targetSy);
                }
                trackedState[base + 5] = targetSy;

                float curA = child.getAlpha();
                if (curA != trackedState[base + 6]) {
                    trackedState[base + 2] = curA;
                }
                float targetA = trackedState[base + 2] * al;
                if (targetA != curA) {
                    child.setAlpha(targetA);
                }
                trackedState[base + 6] = targetA;

                float curTy = child.getTranslationY();
                if (curTy != trackedState[base + 7]) {
                    trackedState[base + 3] = resolveExternalTranslationY(trackedState, base, curTy);
                }
                float targetTy = trackedState[base + 3] + dy;
                if (targetTy != curTy) {
                    child.setTranslationY(targetTy);
                }
                trackedState[base + 7] = targetTy;

                visibleViews[i] = null;
            }

            // Clean up children that left the screen or returned to identity
            for (int i = trackedCount - 1; i >= 0; i--) {
                if (trackedGen[i] != generation) {
                    restoreTrackedView(i);
                    removeTracked(i);
                } else if (trackedIdentity[i]) {
                    resetPivot(i);
                    removeTracked(i);
                }
            }
        }

        if (!settled) {
            recyclerView.postInvalidateOnAnimation();
        } else {
            lastFrameTimeMs = 0L;
        }
        return true;
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        int idx = findTrackedIndex(view);
        if (idx >= 0) {
            restoreTrackedView(idx);
            removeTracked(idx);
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (top != oldTop || bottom != oldBottom) {
            int idx = findTrackedIndex(v);
            if (idx >= 0) {
                restoreTrackedView(idx);
                removeTracked(idx);
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull View v) {
        if (observedTree == null) {
            observedTree = v.getViewTreeObserver();
            observedTree.addOnPreDrawListener(this);
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull View v) {
        if (observedTree != null) {
            if (observedTree.isAlive()) {
                observedTree.removeOnPreDrawListener(this);
            } else {
                v.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            observedTree = null;
        }
        for (int i = trackedCount - 1; i >= 0; i--) {
            restoreTrackedView(i);
            removeTracked(i);
        }
        if (drumEffect != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            drumEffect.clear(recyclerView);
        }
        drumEffect = null;
        boundaryViews[0] = null;
        boundaryViews[1] = null;
        scaleSpring[0] = 0.0f;
        scaleSpring[1] = 0.0f;
        drumSpring[0] = 0.0f;
        drumSpring[1] = 0.0f;
        lastFrameTimeMs = 0L;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private static final class DrumEffect {
        private static String shaderSource;
        private static boolean failed;

        private final RuntimeShader shader;
        private boolean applied;
        private float lastW = -1f, lastH = -1f, lastZone = -1f, lastTop = -1f, lastBottom = -1f;
        private float lastTopReach = -1f, lastBottomReach = -1f;

        private DrumEffect(RuntimeShader shader) {
            this.shader = shader;
        }

        static DrumEffect create() {
            if (failed) {
                return null;
            }
            try {
                if (shaderSource == null) {
                    shaderSource = AndroidUtilities.readRes(R.raw.lagram_scroll_drum);
                }
                RuntimeShader rs = new RuntimeShader(shaderSource);
                rs.setFloatUniform("narrow", 0.12f);
                return new DrumEffect(rs);
            } catch (Throwable t) {
                FileLog.e(t);
                failed = true;
                return null;
            }
        }

        void clear(View view) {
            if (applied) {
                view.setRenderEffect(null);
                applied = false;
            }
            lastW = lastH = lastZone = lastTop = lastBottom = lastTopReach = lastBottomReach = -1f;
        }

        void update(View view, float zone, float topEdge, float bottomEdge, float topReach, float bottomReach) {
            float qTop = Math.round(topReach * 128.0f) / 128.0f;
            float qBottom = Math.round(bottomReach * 128.0f) / 128.0f;
            if (failed || zone <= 0.0f || (qTop <= 0.0f && qBottom <= 0.0f)) {
                clear(view);
                return;
            }
            float w = view.getWidth();
            float h = view.getHeight();
            if (w <= 0f || h <= 0f) {
                return;
            }
            if (applied && w == lastW && h == lastH && zone == lastZone
                    && topEdge == lastTop && bottomEdge == lastBottom
                    && qTop == lastTopReach && qBottom == lastBottomReach) {
                return;
            }
            lastW = w;
            lastH = h;
            lastZone = zone;
            lastTop = topEdge;
            lastBottom = bottomEdge;
            lastTopReach = qTop;
            lastBottomReach = qBottom;
            try {
                shader.setFloatUniform("size", w, h);
                shader.setFloatUniform("edges", topEdge, bottomEdge);
                shader.setFloatUniform("zone", zone);
                shader.setFloatUniform("reach", qTop, qBottom);
                shader.setFloatUniform("trace", 0.0f);
                view.setRenderEffect(RenderEffect.createRuntimeShaderEffect(shader, "content"));
                applied = true;
            } catch (Throwable t) {
                FileLog.e(t);
                failed = true;
                clear(view);
            }
        }
    }
}

package org.telegram.ui.lagram.player;

/**
 * Lightweight sub-stepped harmonic spring integrator (exteraless-style)
 * for silky 120Hz button morphing and cover breathing animations.
 */
public final class Spring {

    private final float stiffness;
    private final float damping;
    private final float epsilon;
    public float value;
    public float target;
    private float velocity;

    public Spring(float value, float stiffness, float dampingRatio, float epsilon) {
        this.value = value;
        this.target = value;
        this.stiffness = stiffness;
        this.damping = 2f * dampingRatio * (float) Math.sqrt(stiffness);
        this.epsilon = epsilon;
    }

    public void snap(float v) {
        value = v;
        target = v;
        velocity = 0f;
    }

    public boolean settled() {
        return Math.abs(value - target) < epsilon && Math.abs(velocity) < epsilon * 10f;
    }

    public boolean step(float dt) {
        if (settled()) {
            value = target;
            velocity = 0f;
            return false;
        }
        float remaining = Math.min(Math.max(0f, dt), 0.064f);
        while (remaining > 0f) {
            float h = Math.min(remaining, 0.008f);
            float a = -stiffness * (value - target) - damping * velocity;
            velocity += a * h;
            value += velocity * h;
            remaining -= h;
        }
        if (settled()) {
            value = target;
            velocity = 0f;
            return false;
        }
        return true;
    }
}

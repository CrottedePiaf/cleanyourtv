package com.gathin.tvhome;

import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

/** Reduced motion (config.xml reduced_motion): the one spring and the one time limit every animation uses then. */
final class Motion {

    /** The longest any reduced-motion animation runs, in milliseconds. */
    static final int REDUCED_MS = 90;
    /** Critically damped (no bounce) and stiff enough that any move, even across the screen, settles in 5 frames. */
    static final float REDUCED_STIFFNESS = 15000f;
    /** The spring stops once it is within this many pixels of its target. */
    static final float REDUCED_MIN_CHANGE = 2f;

    private Motion() {
    }

    /** Zero means assign the final properties immediately, without scheduling an animator. */
    static int duration(boolean enabled, boolean reduced, int normalMs) {
        return !enabled ? 0 : reduced ? Math.min(normalMs, REDUCED_MS) : normalMs;
    }

    static void panel(android.view.View panel, boolean enabled, boolean reduced, float offset) {
        panel.animate().cancel();
        int ms = duration(enabled, reduced, 220);
        panel.setAlpha(ms == 0 ? 1f : 0f);
        panel.setScaleY(ms == 0 ? 1f : 0.96f);
        panel.setTranslationY(ms == 0 ? 0f : offset);
        if (ms != 0) panel.animate().alpha(1f).scaleY(1f).translationY(0f).setDuration(ms)
                .setInterpolator(new android.view.animation.DecelerateInterpolator(1.6f)).start();
    }

    static void reduce(SpringAnimation spring) {
        spring.setSpring(new SpringForce().setStiffness(REDUCED_STIFFNESS).setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY));
        spring.setMinimumVisibleChange(REDUCED_MIN_CHANGE);
    }
}

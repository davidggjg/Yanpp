package app.morphe.extension.shared.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;

/**
 * Utility methods for animating views: visibility transitions (fade in/out with
 * VISIBLE/GONE) and touch feedback.
 */
@SuppressWarnings("unused")
public final class ViewAnimations {
    private ViewAnimations() {}

    /** How far a view shrinks while held down. */
    private static final float PRESSED_SCALE = 0.94f;

    /** How much a view dims while held down. */
    private static final float PRESSED_ALPHA = 0.75f;

    private static final long PRESS_DOWN_DURATION = 90;
    private static final long PRESS_RELEASE_DURATION = 140;

    /**
     * Gives a view a press effect: it shrinks and dims slightly while held, and springs
     * back on release. Used for custom buttons that are drawn with their own background
     * and so get no ripple of their own.
     *
     * <p>The touch listener never consumes the event, so the view keeps handling its own
     * clicks - callers can still set an {@link View.OnClickListener} as usual, in either
     * order.
     */
    @SuppressLint("ClickableViewAccessibility") // Events are passed through, not consumed.
    public static void applyPressEffect(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    animatePress(v, PRESSED_SCALE, PRESSED_ALPHA, PRESS_DOWN_DURATION);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    animatePress(v, 1f, 1f, PRESS_RELEASE_DURATION);
                    break;
            }
            return false;
        });
    }

    private static void animatePress(View view, float scale, float alpha, long duration) {
        view.animate().cancel();
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .alpha(alpha)
                .setDuration(duration)
                .setListener(null)
                .start();
    }

    /**
     * Fades the view in (alpha 0→1) and sets it to {@link View#VISIBLE}.
     * No-op if the view is already fully visible.
     */
    public static void fadeIn(View view, long duration) {
        view.animate().cancel();
        if (view.getVisibility() == View.VISIBLE && view.getAlpha() == 1f) return;
        if (view.getVisibility() != View.VISIBLE) {
            view.setAlpha(0f);
            view.setVisibility(View.VISIBLE);
        }
        view.animate().alpha(1f).setDuration(duration).setListener(null).start();
    }

    /**
     * Fades the view out (alpha 1→0) then sets it to {@link View#GONE}.
     * Resets alpha to 1 after hiding so the next {@link #fadeIn} starts clean.
     */
    public static void fadeOut(View view, long duration) {
        view.animate().cancel();
        view.animate().alpha(0f).setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        view.setAlpha(1f);
                        view.animate().setListener(null);
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        view.animate().setListener(null);
                    }
                })
                .start();
    }
}

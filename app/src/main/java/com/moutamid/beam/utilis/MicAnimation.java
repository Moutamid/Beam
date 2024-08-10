package com.moutamid.beam.utilis;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class MicAnimation {
    private static boolean isAnimationCanceled = false;
    public static AnimatorSet startListeningAnimation(View foregroundView, View backgroundView) {
        // Create animations for the foreground view (fades in and out)
        ObjectAnimator foregroundIn = ObjectAnimator.ofFloat(foregroundView, "alpha", 0f, 1f);
        foregroundIn.setDuration(350);

        ObjectAnimator foregroundOut = ObjectAnimator.ofFloat(foregroundView, "alpha", 1f, 0f);
        foregroundOut.setDuration(400);

        // Create animations for the background view (fades in and out)
        ObjectAnimator backgroundIn = ObjectAnimator.ofFloat(backgroundView, "alpha", 0f, 1f);
        backgroundIn.setDuration(350);

        ObjectAnimator backgroundOut = ObjectAnimator.ofFloat(backgroundView, "alpha", 1f, 0f);
        backgroundOut.setDuration(500);

        // Create AnimatorSets to sequence the animations with a delay between them
        AnimatorSet fadeInSet = new AnimatorSet();
        fadeInSet.playSequentially(foregroundIn, backgroundIn);

        AnimatorSet fadeOutSet = new AnimatorSet();
        fadeOutSet.playSequentially(backgroundOut, foregroundOut);

        // Create the main AnimatorSet and alternate between fade in and fade out
        AnimatorSet mainSet = new AnimatorSet();
        mainSet.playSequentially(fadeInSet, fadeOutSet);

        mainSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // Make sure both views are visible before starting the animation
                foregroundView.setVisibility(View.VISIBLE);
                backgroundView.setVisibility(View.VISIBLE);
                isAnimationCanceled = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Start the animation again in a loop
                if (!isAnimationCanceled) {
                    mainSet.start(); // Restart the animation loop only if not canceled
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimationCanceled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        mainSet.start();

        return mainSet;
    }

    public static void cancelListeningAnimation(AnimatorSet animation, View foregroundView, View backgroundView) {
        if (animation != null) {
            animation.cancel();
            foregroundView.setVisibility(View.GONE);
            backgroundView.setVisibility(View.GONE);
        }
    }

}

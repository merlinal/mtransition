package com.merlin.transition.strategy;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

/**
 * Created by ncm on 2017/6/16.
 */

public class ColorStrategy extends Strategy {

    public ColorStrategy(int startColor, int endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    private int startColor;
    private int endColor;

    @Override
    public AnimatorSet loadOriginView(ImageView originView) {
        AnimatorSet anim = new AnimatorSet();
        anim.play(ObjectAnimator.ofFloat(originView, "alpha", 1f, 1f));
        anim.setDuration(200);
        return null;
    }

    @Override
    public AnimatorSet translate(View originView, View backgroundView, int translationX, int translationY, float scaleX, float scaleY) {
        AnimatorSet anim = new AnimatorSet();
        ObjectAnimator colorAnimator = ObjectAnimator.ofInt(backgroundView, "backgroundColor", startColor, endColor);
        colorAnimator.setEvaluator(new ArgbEvaluator());

        anim.playTogether(
                ObjectAnimator.ofFloat(originView, "alpha", 1f, 1f),
                ObjectAnimator.ofFloat(originView, "translationX", 0, translationX),
                ObjectAnimator.ofFloat(originView, "translationY", 0, translationY),
                ObjectAnimator.ofFloat(originView, "scaleX", scaleX),
                ObjectAnimator.ofFloat(originView, "scaleY", scaleY),
                colorAnimator
        );

        anim.setInterpolator(new AccelerateInterpolator());
        anim.setDuration(800);
        return anim;
    }

}

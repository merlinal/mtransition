package com.merlin.transition.strategy;

import android.animation.AnimatorSet;
import android.app.Activity;
import android.view.View;
import android.widget.ImageView;

import com.merlin.core.util.Util;
import com.merlin.transition.view.AnimView;
import com.merlin.transition.view.CircleAnimView;

/**
 * Created by ncm on 2017/6/16.
 */

public class Strategy {

    /**
     * 从originView到targetView的变换
     *
     * @param originView
     * @param backgroundView
     * @param translationX
     * @param translationY
     * @param scaleX
     * @param scaleY
     * @return
     */
    public AnimatorSet translate(View originView, View backgroundView, int translationX, int translationY, float scaleX, float scaleY) {
        return null;
    }

    /**
     * originView显示动画
     *
     * @param originView
     * @return
     */
    public AnimatorSet loadOriginView(ImageView originView) {
        return null;
    }

    /**
     * targetView显示动画
     *
     * @param targetView
     * @return
     */
    public AnimatorSet loadTargetView(View targetView) {
        return null;
    }

    /**
     * 背景动效view
     *
     * @param activity
     * @return
     */
    public AnimView loadAnimView(Activity activity) {
        return new CircleAnimView(activity, Util.color(com.merlin.core.R.color.background));
    }

}

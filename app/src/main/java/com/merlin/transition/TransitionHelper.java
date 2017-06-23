package com.merlin.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.merlin.transition.model.Transit;
import com.merlin.transition.model.Transits;
import com.merlin.transition.strategy.Strategy;
import com.merlin.transition.view.AnimView;

import java.util.ArrayList;

/**
 * Created by ncm on 2017/6/16.
 */

public class TransitionHelper {

    public static TransitionHelper Inst() {
        return InstHolder.helper;
    }

    private TransitionHelper() {
    }

    private static class InstHolder {
        private static TransitionHelper helper = new TransitionHelper();
    }

    private Handler handler = new Handler();

    private SparseArray<Transits> transitArray;
    private ArrayList<View> viewList;

    public void add(final View... views) {
        clear();
        if (viewList != null) {
            viewList = new ArrayList<>();
        }
        if (views != null && views.length > 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < views.length; i++) {
                        Transits transits = new Transits();
                        View view = views[i];
                        transits.originView = view;
                        Transit transit = transits.transit;
                        //状态栏高度：避免重复测量
                        if (i > 0) {
                            transits.transit.statusBarHeight = transitArray.get(0).transit.statusBarHeight;
                        } else {
                            view.getWindowVisibleDisplayFrame(transits.transit.originRect);
                            transits.transit.statusBarHeight = transit.originRect.top;
                        }
                        //原始View宽高快照
                        view.getGlobalVisibleRect(transit.originRect);
                        transit.originWidth = view.getWidth();
                        transit.originHeight = view.getHeight();
                        transit.bitmap = createBitmap(view, transit.originWidth, transit.originHeight, false);

                        transitArray.put(i, transits);
                    }
                }
            });
        }
    }

    public void start(final Activity activity, final Strategy strategy, final View... views) {
        start(activity, strategy, 3 * 1000, views);
    }

    public void start(final Activity activity, final Strategy strategy, final long clearDelay, final View... views) {
        if (strategy == null || activity == null) {
            clear();
            return;
        }
        if (transitArray == null || transitArray.size() < 1) {
            clear();
            return;
        }
        final ViewGroup parent = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
        //if TranslucentStatus is true , statusBarHeight = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (activity.getWindow().getStatusBarColor() == 0 ||
                    (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS & activity.getWindow().getAttributes().flags)
                            == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) {
                transitArray.get(0).transit.statusBarHeight = 0;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if ((WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS & activity.getWindow().getAttributes().flags)
                    == WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) {
                transitArray.get(0).transit.statusBarHeight = 0;
            }
        }
        transitArray.get(0).transit.windowWidth = parent.getWidth();
        transitArray.get(0).transit.windowHeight = parent.getHeight();
        transitArray.get(0).transit.titleHeight = parent.getTop();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (strategy == null) {
                    return;
                }
                final AnimView animView = strategy.loadAnimView(activity);
                if (animView != null) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    parent.addView(animView, params);
                    viewList.add(animView);
                }

                for (int i = 0; i < transitArray.size(); i++) {
                    final Transit transit = transitArray.get(i).transit;
                    if (transit == null) {
                        continue;
                    }
                    transit.statusBarHeight = transitArray.get(0).transit.statusBarHeight;

                    transit.windowWidth = parent.getWidth();
                    transit.windowHeight = parent.getHeight();
                    transit.titleHeight = parent.getTop();

                    //从原始View到目标View的变换
                    View targetView = i < views.length ? views[i] : null;
                    if (targetView != null) {
                        //get Target View's position
                        targetView.getGlobalVisibleRect(transit.targetRect);
                        transit.targetWidth = transit.targetRect.right - transit.targetRect.left;
                        transit.targetHeight = transit.targetRect.bottom - transit.targetRect.top;
                        transit.translationX = transit.targetRect.left + transit.targetWidth / 2 - transit.originRect.left - transit.originWidth / 2;
                        transit.translationY = transit.targetRect.top + transit.targetHeight / 2 - transit.originRect.top - transit.originHeight / 2 - transit.statusBarHeight;
                    } else {
                        transit.targetRect.left = transit.originRect.left;
                        transit.targetRect.top = transit.originRect.top;
                        transit.targetWidth = transit.originWidth;
                        transit.targetHeight = transit.originHeight;
                        transit.translationX = 0;
                        transit.translationY = 0;
                    }

                    //create a temp ImageView to replace origin view
                    final ImageView originView = new ImageView(activity);
                    viewList.add(originView);
                    if (transit.bitmap != null) {
                        originView.setImageBitmap(transit.bitmap);
                    }
                    RelativeLayout.LayoutParams originParams = new RelativeLayout.LayoutParams(transit.originWidth, transit.originHeight);
                    originParams.setMargins(transit.originRect.left, transit.originRect.top, 0, 0);
                    animView.addView(originView, originParams);

                    if (transit.originRect.top + transit.originHeight > (transit.windowHeight + transit.statusBarHeight + transit.titleHeight)) {
                        transit.scaleY = (float) transit.targetHeight /
                                (transit.windowHeight + transit.statusBarHeight + transit.titleHeight - transit.originRect.top);
                    } else {
                        transit.scaleY = (float) transit.targetHeight / transit.originHeight;
                        transit.scaleX = (float) transit.targetWidth / transit.originWidth;
                    }

                    final AnimatorSet originAnim = strategy.loadOriginView(originView);
                    final AnimatorSet targetAnim = strategy.loadTargetView(targetView);
                    final AnimatorSet translateAnim = strategy.translate(originView, animView, transit.translationX, transit.translationY, transit.scaleX, transit.scaleY);

                    //最后一个view时执行animView
                    if (i < transitArray.size() - 1) {
                        startAnim(originAnim, translateAnim, null, targetAnim, transit);
                    } else {
                        startAnim(originAnim, translateAnim, animView, targetAnim, transit);
                    }
                }
                //释放缓存
                clear(clearDelay < 1 ? 5 * 1000 : clearDelay);
            }
        });
    }

    private void startAnim(final AnimatorSet originAnim, final AnimatorSet translateAnim, final AnimView animView, final AnimatorSet targetAnim, final Transit transit) {
        if (originAnim != null) {
            startOriginAnim(originAnim, translateAnim, animView, targetAnim, transit);
        } else if (translateAnim != null) {
            startTranslateAnim(translateAnim, animView, targetAnim, transit);
        } else if (animView != null) {
            startAnimView(animView, targetAnim, transit);
        } else {
            startTargetAnim(targetAnim);
        }
    }

    private void startOriginAnim(final AnimatorSet originAnim, final AnimatorSet translateAnim, final AnimView animView, final AnimatorSet targetAnim, final Transit transit) {
        if (originAnim != null) {
            originAnim.start();
            originAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startTranslateAnim(translateAnim, animView, targetAnim, transit);
                    transit.bitmap.recycle();
                }
            });
        } else {
            startAnim(originAnim, translateAnim, animView, targetAnim, transit);
        }
    }

    private void startTranslateAnim(final AnimatorSet translateAnim, final AnimView animView, final AnimatorSet targetAnim, final Transit transit) {
        if (translateAnim != null) {
            translateAnim.start();
            translateAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    startAnimView(animView, targetAnim, transit);
                }
            });
        } else {
            startAnim(null, translateAnim, animView, targetAnim, transit);
        }
    }

    private void startAnimView(final AnimView animView, final AnimatorSet targetAnim, final Transit transit) {
        if (animView != null) {
            Drawable background = animView.getBackground();
            if (background instanceof ColorDrawable) {
                animView.setPaintColor(((ColorDrawable) background).getColor());
            }
            animView.startAnim(transit);
            animView.setAnimListener(new AnimView.AnimListener() {
                @Override
                public void onAnimEnd() {
                    startTargetAnim(targetAnim);
                }
            });
        } else {
            startAnim(null, null, animView, targetAnim, transit);
        }
    }

    private void startTargetAnim(AnimatorSet targetAnim) {
        if (targetAnim != null) {
            targetAnim.start();
        }
    }

    private void clear(long delayedTime) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                clear();
            }
        }, delayedTime);
    }

    private void clear() {
        if (transitArray != null) {
            //清除动画混存
            for (int i = 0; i < transitArray.size(); i++) {
                Transits transits = transitArray.get(i);
                transits.originView = null;
                transits.targetView = null;
                if (transits.transit != null) {
                    transits.transit.bitmap.recycle();
                    transits.transit.bitmap = null;
                }
                transits.transit = null;
            }
            transitArray.clear();
            //清除临时view
            if (viewList != null) {
                for (View view : viewList) {
                    if (view == null) {
                        continue;
                    }
                    view.destroyDrawingCache();
                }
                viewList.clear();
            }
            //回收资源
            System.gc();
        } else {
            transitArray = new SparseArray<>();
        }
    }

    private Bitmap createBitmap(View view, int width, int height, boolean needOnLayout) {
        Bitmap bitmap = null;
        if (view != null) {
            view.clearFocus();
            view.setPressed(false);

            boolean willNotCache = view.willNotCacheDrawing();
            view.setWillNotCacheDrawing(false);

            // Reset the drawing cache background color to fully transparent
            // for the duration of this operation
            int color = view.getDrawingCacheBackgroundColor();
            view.setDrawingCacheBackgroundColor(0);
            float alpha = view.getAlpha();
            view.setAlpha(1.0f);

            if (color != 0) {
                view.destroyDrawingCache();
            }

            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int heightSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
            view.measure(widthSpec, heightSpec);
            if (needOnLayout) {
                view.layout(0, 0, width, height);
            }
            view.buildDrawingCache();
            Bitmap cacheBitmap = view.getDrawingCache();
            if (cacheBitmap == null) {
                Log.e("view.ProcessImageToBlur", "failed getViewBitmap(" + view + ")", new RuntimeException());
                return null;
            }
            bitmap = Bitmap.createBitmap(cacheBitmap);
            // Restore the view
            view.setAlpha(alpha);
            view.destroyDrawingCache();
            view.setWillNotCacheDrawing(willNotCache);
            view.setDrawingCacheBackgroundColor(color);
        }
        return bitmap;
    }

}

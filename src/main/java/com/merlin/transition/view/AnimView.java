package com.merlin.transition.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.merlin.core.util.Util;
import com.merlin.transition.R;
import com.merlin.transition.model.Transit;

/**
 * Created by ncm on 2017/6/16.
 */

public abstract class AnimView extends RelativeLayout {

    public AnimView(Context context, int backgroundColor) {
        this(context);
        this.backgroundColor = backgroundColor;
    }

    public AnimView(Context context) {
        this(context, null);
    }

    public AnimView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.backgroundColor = Util.color(R.color.background);
    }

    protected int backgroundColor;
    protected Paint paint;
    protected AnimListener listener;

    public void setPaintColor(int paintColor) {
        paint.setColor(paintColor);
    }

    public abstract void startAnim(Transit transition);

    public void setAnimListener(AnimListener listener) {
        this.listener = listener;
    }

    public interface AnimListener {
        void onAnimEnd();
    }

}

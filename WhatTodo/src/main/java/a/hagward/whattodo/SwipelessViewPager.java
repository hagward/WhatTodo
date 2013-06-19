package a.hagward.whattodo;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Anders on 2013-06-18.
 */
public class SwipelessViewPager extends ViewPager {
    private boolean mSwipeable = false;

    public SwipelessViewPager(Context context) {
        super(context);
    }

    public SwipelessViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwipeable(boolean swipeable) {
        mSwipeable = swipeable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mSwipeable) return super.onInterceptTouchEvent(ev);
        else return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mSwipeable) return super.onTouchEvent(ev);
        else return false;
    }
}

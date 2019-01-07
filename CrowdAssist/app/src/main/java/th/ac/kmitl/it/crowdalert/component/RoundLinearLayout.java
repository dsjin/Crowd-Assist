package th.ac.kmitl.it.crowdalert.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import th.ac.kmitl.it.crowdalert.R;

public class RoundLinearLayout extends LinearLayout {
    private int radius = 20;
    private Path path;
    public RoundLinearLayout(Context context) {
        super(context);
        init();
    }

    public RoundLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundLinearLayout);
        radius = a.getInt(R.styleable.RoundLinearLayout_radius, 20);
        a.recycle();
        init();
    }

    public RoundLinearLayout(Context context, AttributeSet attrs,
                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        path = new Path();
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        path.reset();
        RectF rect = new RectF();
        rect.set(0, 0, w, h);
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        path.close();
    }
    @Override
    public void draw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(path);
        super.draw(canvas);
        canvas.restoreToCount(save);
    }
}

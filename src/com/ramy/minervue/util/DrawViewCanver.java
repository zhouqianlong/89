package com.ramy.minervue.util;

/**
 * Created by peter on 11/7/13.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

public class DrawViewCanver extends View {
	private Paint mPaint;
	int i = 0;
    public DrawViewCanver(Context context) {
        super(context);
        this.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Log.i("111","111");
			}
		});
    }

    public DrawViewCanver(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnClickListener(new OnClickListener() {
			
     			@Override
     			public void onClick(View arg0) {
     				Log.i("111","222");
     			}
     		});
    }

    public DrawViewCanver(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
		mPaint.setTextSize(20);
		mPaint.setColor(Color.YELLOW);
        this.setOnClickListener(new OnClickListener() {
			
     			@Override
     			public void onClick(View arg0) {
     				Log.i("111","333");
     				i++;
     				postInvalidate();
     			}
     		});
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(h, w, oldh, oldw);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec);
        setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
    }
    
	@Override
    protected void onDraw(Canvas canvas) {
    	canvas.drawText(i+"", 0,0, mPaint);
    	Log.i("111","onDraw"+i);
    }


}

package com.prime.dtvplayer.View;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;

public class CustomTextView extends androidx.appcompat.widget.AppCompatTextView {
    private final String TAG = getClass().getSimpleName();

    /*
     * Even add it by 20170518
     * dynamic setting textview's width and textsize Demo
     *
     */

    /*
     * 定义字体大小边界值
     */
    private static float DEFAULT_MIN_TEXT_SIZE = 1;
    private static float DEFAULT_MAX_TEXT_SIZE = 300;   // Johnny 20180730 change from 100 to 300

    // Attributes
    private Paint testPaint; //此类包含如何绘制的样式和颜色信息
    private float minTextSize, TextSize, maxTextSize;
    private int mSetSize = 0;

    public CustomTextView(Context context, int autoSize) {
        super(context);
        initialise(autoSize);
//        TypedArray a = context.obtainStyledAttributes( R.styleable.LeanTextView);
//        mDegrees = a.getDimensionPixelSize(R.styleable.LeanTextView_degree, 0);
//        a.recycle();
    }

    public CustomTextView(Context context, AttributeSet attrs, int autoSize) {
        super(context, attrs);
        initialise(autoSize);
    }

    private void initialise(int autoSize) {
        /*
         * limited size
         */
        testPaint = new Paint();
        testPaint.set(this.getPaint());
        TextSize = DEFAULT_MAX_TEXT_SIZE;//this.getTextSize();

        if (TextSize <= DEFAULT_MIN_TEXT_SIZE) {
            TextSize = DEFAULT_MIN_TEXT_SIZE;
        }
        if (TextSize >= DEFAULT_MAX_TEXT_SIZE) {
            TextSize = DEFAULT_MAX_TEXT_SIZE;
        }
        minTextSize = DEFAULT_MIN_TEXT_SIZE;
        maxTextSize = DEFAULT_MAX_TEXT_SIZE;
        mSetSize = autoSize;
    }

    ;

    /*
     * (non-Javadoc)
     * @see android.widget.TextView#onTextChanged(java.lang.CharSequence, int, int, int)
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int before,
                                 int after) {
        super.onTextChanged(text, start, before, after);
        refitText(text.toString(), this.getWidth());
    }

    /*
     * (non-Javadoc)
     * @see android.view.View#onSizeChanged(int, int, int, int)
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    private void refitText(String text, int textWidth) {

        if (mSetSize != 0)
            this.setTextSize(mSetSize);
        else if (textWidth > 0) {
            int availableWidth = textWidth - this.getPaddingLeft()
                    - this.getPaddingRight();
            float trySize = TextSize;
            float scaled = getContext().getResources().getDisplayMetrics().scaledDensity;
            testPaint.setTextSize(trySize * scaled);//给testPaint设置 dp转为px后的textsize
            while ((trySize > minTextSize || trySize < maxTextSize) &&
                    (testPaint.measureText(text) > availableWidth)) {
                trySize -= 1; //减小1个单位大小
                Paint.FontMetrics fm = testPaint.getFontMetrics();
                float scaled1 = (float) (this.getHeight() / (Math.ceil(fm.descent - fm.top) + 2));
                float scaled2 = (float) ((testPaint.measureText(text) / availableWidth));
                if (scaled1 >= 1.75 & scaled1 >= scaled2) {
                    break;
                }
                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                } else if (trySize >= maxTextSize) {
                    trySize = maxTextSize;
                    break;
                }
                testPaint.setTextSize(trySize * scaled);
            }
            this.setTextSize(trySize);
        }
    }


//    public int getmDegrees() {
//        return mDegrees;
//    }
//
//    public void setmDegrees(int mDegrees) {
//        this.mDegrees = mDegrees;
//        invalidate();
//    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        switch (mDegrees)
//        {
//            case 90:
//            case 270:{
//                setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
//            }break;
//            default: {
//                setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());
//            }break;
//        }
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        canvas.save();
//        canvas.translate(getCompoundPaddingLeft(), getExtendedPaddingTop());
//        canvas.rotate(mDegrees, this.getWidth() / 2f, this.getHeight() / 2f);
//        super.onDraw(canvas);
//        canvas.restore();
//
//    }
}
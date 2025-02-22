package ru.devdem.reminder.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import ru.devdem.reminder.R;

public class HoldButton extends AppCompatButton {

    public float Strength;
    public float Force;

    private boolean touch;
    private final int colorFill;
    private final int colorHoldText;
    private final String textHolded;
    private final String textHint;
    private final float radius;
    private final float marginFill;
    private final float marginFillStart;
    private final float marginFillEnd;
    private final float marginFillTop;
    private final float marginFillBottom;

    private OnClickListener listener;
    private final Context mContext;

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                touch = false;
                if (textHint != null && Strength <= 300f)
                    Toast.makeText(mContext, textHint, Toast.LENGTH_SHORT)
                            .show();
                Strength=0;
                performClick();
                break;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_DOWN: {
                touch = true;
                if (Strength >= 1000f) {
                    if (listener != null) listener.onClick(this);
                    if (textHolded != null) {
                        setText(textHolded);
                    }
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    Paint paint = new Paint();
    Paint paintText = new Paint();
    RectF rect = new RectF();
    RectF rectClip = new RectF();

    private float lastFill = 0f;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = (float) getWidth();
        float height = (float) getHeight();
        paintText.setColor(colorHoldText);
        paintText.setAntiAlias(true);
        paintText.setStyle(Paint.Style.FILL);
        paintText.setTextSize(getTextSize());
        paintText.setTextScaleX(getTextScaleX());
        paintText.setTextAlign(Paint.Align.CENTER);
        paintText.setTypeface(getTypeface());
        paint.setColor(colorFill);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        float newFill = lastFill - (lastFill - Strength / 650f) * 0.016f * 3f;
        rect.set(marginFill+marginFillStart, marginFill+marginFillTop,
                width-marginFill-marginFillEnd,
                height-marginFill- marginFillBottom);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            rectClip.set(width * newFill+marginFill+marginFillStart,
                    marginFill+marginFillTop,
                    width-marginFill-marginFillEnd,
                    height-marginFill-marginFillBottom);
            canvas.clipOutRect(rectClip);
            canvas.drawRoundRect(rect, radius, radius, paint);
            if (textHolded != null) {
                canvas.drawText(textHolded.toUpperCase(getTextLocale()), width / 2f,
                        height / 2f, paintText);
            }
        } else {
            rectClip.set(marginFill+marginFillStart, marginFill+marginFillTop,
                    width * newFill-marginFill-marginFillEnd,
                    height-marginFill-marginFillBottom);
            canvas.drawRoundRect(rectClip, radius, radius, paint);
        }
        lastFill = newFill;
        if (touch || Strength < 0)
            Strength += Force;
        invalidate();
    }

    public void setHoldDownListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public HoldButton(@NonNull Context context) {
        this(context, null);
    }

    public HoldButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.buttonStyle);
    }

    public HoldButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HoldButton);
        textHint = typedArray.getString(R.styleable.HoldButton_hint);
        textHolded = typedArray.getString(R.styleable.HoldButton_textHolded);
        Strength = typedArray.getFloat(R.styleable.HoldButton_strength, 0f);
        Force = typedArray.getFloat(R.styleable.HoldButton_force, 20f);
        colorHoldText = typedArray.getColor(R.styleable.HoldButton_textHoldedColor, Color.parseColor("#ffffff"));
        String colorFillString = typedArray.getString(R.styleable.HoldButton_fillColor);
        if (colorFillString != null) colorFill = Color.parseColor(colorFillString);
        else colorFill = Color.parseColor("#ff0000");
        radius = typedArray.getFloat(R.styleable.HoldButton_radius, Math.min(getWidth(), getHeight()) * 0.2f);
        marginFill = typedArray.getFloat(R.styleable.HoldButton_marginFill, 0f);
        marginFillStart = typedArray.getFloat(R.styleable.HoldButton_marginFillStart, 0f);
        marginFillEnd = typedArray.getFloat(R.styleable.HoldButton_marginFillEnd, 0f);
        marginFillTop = typedArray.getFloat(R.styleable.HoldButton_marginFillTop, 0f);
        marginFillBottom = typedArray.getFloat(R.styleable.HoldButton_marginFillBottom, 0f);
        typedArray.recycle();
    }

}

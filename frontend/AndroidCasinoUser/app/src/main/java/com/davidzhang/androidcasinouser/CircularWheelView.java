package com.davidzhang.androidcasinouser;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

//ChatGPT usage: Yes
public class CircularWheelView extends View {
    // Constants for the wheel
    private static final int NUM_SLOTS = 37;

    // Paint for drawing
    private Paint paint;
    private RectF wheelBounds;
    private RectF tickerBounds;


    // Animation related variables
    private ValueAnimator spinAnimator;
    private float rotation = 0f;
    private int targetSlot = 0;
    private boolean spinning = false;

    // Text Paint for slot numbers
    private Paint textPaint;
    private Paint tickerPaint;

    private String[] wheelColors =
            {
                    "green",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red",
                    "black",
                    "red"
            };

    private AnimationCallback callback;

    //ChatGPT usage: Yes
    public CircularWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    //ChatGPT usage: Yes
    public interface AnimationCallback {
        void onAnimationFinished();
    }

    // Setter method to set the callback
    //ChatGPT usage: Yes
    public void setAnimationCallback(AnimationCallback callback) {

        this.callback = callback;
    }

    //ChatGPT usage: Yes
    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        wheelBounds = new RectF();

        // Initialize text paint for slot numbers
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        tickerPaint = new Paint();
        tickerPaint.setColor(Color.GRAY);
        tickerPaint.setStyle(Paint.Style.FILL);

        tickerBounds = new RectF();
    }

    //ChatGPT usage: Yes
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewWidth = getWidth();
        int viewHeight = getHeight();
        int centerX = viewWidth / 2;
        int centerY = viewHeight / 2;
        int radius = Math.min(centerX, centerY);

        float slotAngle = 360f / NUM_SLOTS;
        float startAngle = 0;

        for (int i = 0; i < NUM_SLOTS; i++) {
            if (wheelColors[i] == "green") {
                paint.setColor(Color.GREEN);
            }
            else if (wheelColors[i] == "red") {
                paint.setColor(Color.RED);
            } else {
                paint.setColor(Color.BLACK);
            }

            wheelBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            canvas.drawArc(wheelBounds, startAngle + rotation, slotAngle, true, paint);

            // Calculate the position to display the number in the center of the slot
            float slotCenterAngle = startAngle + rotation + (slotAngle / 2);
            float textX = centerX + (float) (radius * 0.7 * Math.cos(Math.toRadians(slotCenterAngle)));
            float textY = centerY + (float) (radius * 0.7 * Math.sin(Math.toRadians(slotCenterAngle)));

            // Display the number in the center of the slot
            canvas.drawText(String.valueOf(i), textX, textY, textPaint);

            startAngle += slotAngle;
        }

        // Draw the ticker to indicate the selected slot
        float tickerAngle = (targetSlot * slotAngle) + (slotAngle / 2) + rotation;
        float tickerX = centerX + (float) (radius * 0.7 * Math.cos(Math.toRadians(tickerAngle)));
        float tickerY = centerY + (float) (radius * 0.7 * Math.sin(Math.toRadians(tickerAngle)));
        float tickerSize = 20f; // Adjust the size of the ticker
        tickerBounds.set(tickerX - tickerSize, tickerY - tickerSize, tickerX + tickerSize, tickerY + tickerSize);
        canvas.drawOval(tickerBounds, tickerPaint);
    }


    //ChatGPT usage: Yes ... with some minor custom modifications to the degree logic.
    public void spin(final int target) {
        if (!spinning) {
            final float numSpins = 2.74f;
            final float degreesPerSpin = 360f * numSpins;
            final float degreesToTarget = (37f-target) * (360f/37f);
            Log.d("TARGET", String.valueOf(target));
            targetSlot = target;

            spinAnimator = ValueAnimator.ofFloat(rotation, rotation + degreesPerSpin + degreesToTarget);
            spinAnimator.setDuration(2000); // Adjust the duration as needed
            spinAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // Adjust the interpolator as needed
            spinAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    rotation = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            spinAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (callback != null) {
                        callback.onAnimationFinished();
                    }
                }
            });


            spinAnimator.start();
        }
    }
}

package com.tetsworks.pocketlive.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class StepButton extends View {

    private boolean active = false;
    private boolean current = false;
    private int trackColor = 0xFF1E88E5;

    private final Paint paintActive   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintInactive = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintCurrent  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public StepButton(Context context) { super(context); init(); }
    public StepButton(Context context, AttributeSet attrs) { super(context, attrs); init(); }

    private void init() {
        paintInactive.setColor(0xFF2A2A2A);
        paintCurrent.setColor(0x44FFFFFF);
    }

    public void setActive(boolean active) { this.active = active; invalidate(); }
    public void setCurrent(boolean current) { this.current = current; invalidate(); }
    public void setTrackColor(int color) { this.trackColor = color; invalidate(); }
    public boolean isActive() { return active; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float radius = getHeight() * 0.18f;
        rect.set(2f, 2f, getWidth() - 2f, getHeight() - 2f);
        paintActive.setColor(trackColor);
        canvas.drawRoundRect(rect, radius, radius, active ? paintActive : paintInactive);
        if (current) canvas.drawRoundRect(rect, radius, radius, paintCurrent);
    }
}

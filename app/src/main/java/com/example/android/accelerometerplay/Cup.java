package com.example.android.accelerometerplay;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by GWM on 6/1/17.
 */

public class Cup extends View {
    private float mPosX = (float) Math.random();
    private float mPosY = (float) Math.random();
    private float mVelX;
    private float mVelY;

    public Cup(Context context) {
        super(context);
    }

    public Cup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Cup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Cup(Context context, AttributeSet attrs, int defStyleAttr,
                    int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void computePhysics(float sx, float sy, float dT) {

        final float ax = -sx/5;
        final float ay = -sy/5;

        mPosX += mVelX * dT + ax * dT * dT / 2;
        mPosY += mVelY * dT + ay * dT * dT / 2;

        mVelX += ax * dT;
        mVelY += ay * dT;
    }

    /*
     * Resolving constraints and collisions with the Verlet integrator
     * can be very simple, we simply need to move a colliding or
     * constrained particle in such way that the constraint is
     * satisfied.
     */
    public boolean hasCollision(int otherX, int xWidth, int otherY, int yHeight) {
        final float x = mPosX;
        final float y = mPosY;

        return  x > otherX &&
                x < otherX + xWidth &&
                y > otherY &&
                y < otherY + yHeight;
    }
}
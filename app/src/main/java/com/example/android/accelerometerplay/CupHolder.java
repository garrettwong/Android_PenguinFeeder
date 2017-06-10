package com.example.android.accelerometerplay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.constants.Direction;

/**
 * Created by GWM on 6/1/17.
 */

public class CupHolder {
    private Cup cup;
    private Direction curDirection;


    public CupHolder(FrameLayout frame, Context context) {
        cup = new Cup(context);
        cup.setBackgroundResource(R.drawable.duck);
        cup.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        frame.addView(cup, new ViewGroup.LayoutParams(120, 120));
    }

    public Cup getCup() {
        return cup;
    }

    public void setDirection(Direction newDirection) {
        curDirection = newDirection;
    }

    public Direction getDirection() {
        return this.curDirection;
    }

    // how to position this?

    // how to determine collision and remove balls on collision
    public boolean hasCollision(int x, int y, int width, int height) {
        return this.cup.hasCollision(x, width, y, height);
    }
}
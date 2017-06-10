package com.core.score;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.android.accelerometerplay.R;

/**
 * Created by GWM on 6/1/17.
 */
//
//public class ScoreView extends View {
//    private float mPosX = (float) Math.random();
//    private float mPosY = (float) Math.random();
//    private float mVelX;
//    private float mVelY;
//
//    public ScoreView(FrameLayout frame, Context context) {
//        super(context);
//
//        this.setBackgroundResource(R.drawable.duck);
//        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
//
//        ((FrameLayout) findViewById(R.layout.activity_main)).addView(mEditText, params);
//
//        frame.addView(this, new ViewGroup.LayoutParams(120, 120));
//    }
//}
package com.example.bounceprogressbar;

import android.view.animation.Interpolator;

/**
 * Created by 29579 on 2017/11/28.
 */

public class DampingInterpolatpr implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        return (float)(1-Math.exp(-3*input)*Math.cos(10*input));
    }
}

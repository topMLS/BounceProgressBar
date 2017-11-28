package com.example.bounceprogressbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;

import static android.R.attr.width;

/**
 * Created by 29579 on 2017/11/25.
 */

public class BounceProgressBar extends SurfaceView implements SurfaceHolder.Callback {
    public static final int STATE_DOWN = 1;
    public static final int STATE_UP = 2;
    private Paint mPaint;
    private Path mPath;
    private int mLineColor;
    private int mPointColor;
    private int mLineWith;
    private int mLineHeight;
    private float mDownDistance;
    private float mUpDistance;
    private float freeBallDistance;
    //向下运动
    private ValueAnimator downController;
    //向上运动
    private ValueAnimator upController;
    //自由落体
    private ValueAnimator freeDownCotroller;
    private AnimatorSet animatorSet;
    private int state;
    private boolean isBounced = false;
    private boolean isBallFreeUp = false;
    private boolean isUpControllerDied = false;
    private boolean isAnimationShowing = false;

    public BounceProgressBar(Context context) {
        super(context);
        init(context, null);
    }

    public BounceProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BounceProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        initAttributes(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mLineHeight);//画笔的粗细
        mPaint.setStrokeCap(Paint.Cap.ROUND);//圆形的线帽
        mPath = new Path();
        getHolder().addCallback(this);

        initController();

    }

    private void initController() {
        downController=ValueAnimator.ofFloat(0,1);
        downController.setDuration(500);
        downController.setInterpolator(new DecelerateInterpolator());
        downController.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDownDistance=50*(float)animation.getAnimatedValue();
                postInvalidate();
            }
        });
        downController.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                state=STATE_DOWN;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        upController=ValueAnimator.ofFloat(0,1);
        upController.setDuration(900);
        upController.setInterpolator(new DampingInterpolatpr());
        upController.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mUpDistance=50*(float)animation.getAnimatedValue();
                if(mUpDistance>=50){
                    //进入自由落体状态
                    isBounced=true;
                    if (!freeDownCotroller.isRunning()&&!freeDownCotroller.isStarted()&&!isBallFreeUp){
                        freeDownCotroller.start();
                    }

                }
                postInvalidate();
            }
        });
        upController.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                state=STATE_UP;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isUpControllerDied=true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        freeDownCotroller=ValueAnimator.ofFloat(0,1);
        freeDownCotroller.setDuration(900);
        freeDownCotroller.setInterpolator(new DecelerateInterpolator());
        freeDownCotroller.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float t=(float)animation.getAnimatedValue();
                freeBallDistance=34*t-5*t*t;
                if(isUpControllerDied){
                    postInvalidate();
                }

            }
        });
        freeDownCotroller.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isBallFreeUp=true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimationShowing=false;
                startTotalAnimations();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet = new AnimatorSet();
        animatorSet.play(downController).before(upController);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimationShowing=true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    public void startTotalAnimations() {
        if(isAnimationShowing){
            return;
        }
        if (animatorSet.isRunning()){
            animatorSet.end();
            animatorSet.cancel();
        }
        isBounced=false;
        isBallFreeUp=false;
        isUpControllerDied=false;
        animatorSet.start();
    }

    private void initAttributes(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BounceProgressBar);
        mLineColor = typedArray.getColor(R.styleable.BounceProgressBar_line_color, Color.WHITE);
        mPointColor = typedArray.getColor(R.styleable.BounceProgressBar_point_color, Color.WHITE);
        mLineHeight = typedArray.getResourceId(R.styleable.BounceProgressBar_line_height, 2);
        mLineWith = typedArray.getResourceId(R.styleable.BounceProgressBar_line_width, 200);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //一天绳子(一条绳子用左右俩部分的二阶贝塞尔曲线绘制组合而成)
        mPaint.setColor(mLineColor);
        mPath.reset();
        mPath.moveTo(getWidth() / 2 - mLineWith / 2, getHeight() / 2);
        if (state == STATE_DOWN) {
            //下坠
            //左边的贝塞尔
            mPath.quadTo((float) (getWidth() / 2 - mLineWith / 2 + mLineWith * 0.375), getHeight() / 2 + mDownDistance, getWidth() / 2, getHeight() / 2 + mDownDistance);
            //右边贝塞尔
            mPath.quadTo((float) (getWidth() / 2 +mLineWith / 2 - mLineWith * 0.375), getHeight() / 2 + mDownDistance, getWidth() / 2 + mLineWith/2, getHeight() / 2);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath,mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mPointColor);
            canvas.drawCircle(getWidth()/2,getHeight()/2+mDownDistance-10,10,mPaint);
        } else if (state == STATE_UP) {
            //向上弹
            //左边的贝塞尔
            mPath.quadTo((float) (getWidth() / 2 - mLineWith / 2 + mLineWith * 0.375), getHeight() / 2 + (50 - mUpDistance), getWidth() / 2, getHeight() / 2 + (50 - mUpDistance));
            //右边贝塞尔
            mPath.quadTo((float) (getWidth() / 2 + mLineWith / 2 - mLineWith * 0.375), getHeight() / 2 + (50 - mUpDistance), getWidth() / 2 + mLineWith/2, getHeight() / 2);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath,mPaint);

            //第三种状态-自由落体
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mPointColor);
            if (!isBounced) {
                //正常上升
                canvas.drawCircle(getWidth() / 2, getHeight() / 2 + (50 - mUpDistance) - 10, 10, mPaint);
            } else {
                //自由落体
                canvas.drawCircle(getWidth() / 2, getHeight() / 2 - freeBallDistance - 10, 10, mPaint);
            }
        }
        //弹性小球
        mPaint.setColor(mPointColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(getWidth() / 2 - mLineWith / 2, getHeight() / 2, 10, mPaint);
        canvas.drawCircle(getWidth() / 2 + mLineWith / 2, getHeight() / 2, 10, mPaint);
        //俩边的固定点的圆
        super.onDraw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //锁定画布
        Canvas canvas = holder.lockCanvas();
        draw(canvas);
        //解除锁定
        holder.unlockCanvasAndPost(canvas);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}

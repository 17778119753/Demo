package com.example.camera2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.Nullable;

/** @author chenhan on 2020/5/24. */
public class FeedKanRecordButton extends View implements View.OnTouchListener {

  /** 返回参数：正常 */
  public static final int NORMAL = 0;

  /** 返回参数：录制时间太短 */
  public static final int RECORD_SHORT = 1;

  /** RecordButton的监听器 */
  public interface RecordButtonListener {

    /** 点击事件监听 */
    void onClick();

    /** 长按事件监听 */
    void onLongClick();

    /**
     * 长按事件结束
     *
     * @param result 返回值状态码 0：正常录制 1：录制时间过短
     */
    void onLongClickFinish(int result);
  }

  /** 触摸延迟为300毫秒 */
  private static final long TOUCH_DELAY_DEFAULT = 300;

  private long mTouchDelay = TOUCH_DELAY_DEFAULT;

  /** 动画持续时间 */
  private static final long DURING_TIME = 200;

  /** 录制时间15s */
  private static final long RECORD_TIME_DEFAULT = 15000;

  private long mRecordTime = RECORD_TIME_DEFAULT;

  /** 录制过短时间 */
  private static final long MIN_RECORD_TIME_DEFAULT = TOUCH_DELAY_DEFAULT + DURING_TIME + 500;

  private long mMinRecordTime = MIN_RECORD_TIME_DEFAULT;

  /** 缩放系数 取值于探探UI，暂不提供外部修改接口 可直接修改源码 */
  private static final float SCALE_NUM = 0.9f;
  /** 初始状态下内圆占总宽度比例，暂不提供外部修改接口 可直接修改源码 */
  private static final float INNER_EXTERNAL = 0.625f;

  /** 触摸时间 */
  private long mTouchDown;
  /** 触摸动作完成开关 */
  private volatile boolean isDone = true;
  /** view的宽度 */
  private int mViewLength = 0;
  /** 内圆，外圆，进度条画笔 */
  private Paint mInnerPaint, mExternalPaint, mProgressPaint;
  /** progress的rectf */
  private RectF mRectF;
  /** 外圆的半径 */
  private int mExternalCircleRadius;
  /** 内圆的半径 */
  private int mInnerCircleRadius;
  /** 同心圆的中心点 */
  private int mCircleCenterX, mCircleCenterY;
  /** progress的宽度 */
  private float mStrokeWidth = MetricsUtil.dp(6);
  /** 进度条的角度 */
  private float mSweepAngle = 0;
  /** 触摸事件的监听器 */
  private RecordButtonListener mRecordButtonListener;
  /** 延时任务 */
  private LongClickRunnable mLongClickRunnable;

  private Handler mHandler = new Handler();

  public FeedKanRecordButton(Context context) {
    this(context, null);
  }

  public FeedKanRecordButton(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FeedKanRecordButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context, attrs);
  }

  /**
   * 初始化画笔及其他参数
   *
   * @param context 上下文
   * @param attrs 属性
   */
  private void init(Context context, AttributeSet attrs) {
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FeedRecordButton);
    mRecordTime = (long) a.getFloat(R.styleable.FeedRecordButton_recordTime, RECORD_TIME_DEFAULT);
    mTouchDelay =
        (long) a.getFloat(R.styleable.FeedRecordButton_touchDelay, MIN_RECORD_TIME_DEFAULT);
    mMinRecordTime =
        (long) a.getFloat(R.styleable.FeedRecordButton_minRecordTime, MIN_RECORD_TIME_DEFAULT)
            + mTouchDelay
            + DURING_TIME;
    a.recycle();

    // 初始化内部圆的画笔
    mInnerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mInnerPaint.setColor(Color.parseColor("#ff3a00"));

    // 初始化外圈圆的画笔
    mExternalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mExternalPaint.setAntiAlias(true);
    mExternalPaint.setStyle(Paint.Style.STROKE);
    mExternalPaint.setStrokeWidth(mStrokeWidth);
    mExternalPaint.setColor(Color.parseColor("#ffffff"));
    // 初始化弧形画笔
    mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mProgressPaint.setAntiAlias(true);
    mProgressPaint.setStyle(Paint.Style.STROKE);
    mProgressPaint.setStrokeWidth(mStrokeWidth);
    mProgressPaint.setColor(Color.parseColor("#ff3a00"));

    setBackgroundResource(R.color.transparent);
    setOnTouchListener(this);
  }

  /** 重置参数 */
  private void reSetParameters() {
    mExternalCircleRadius = (int) ((mViewLength / 2) * SCALE_NUM);

    // LogUtils.e("mExternalCircleRadius=" + mExternalCircleRadius + "；mViewLength=" + mViewLength);
    mInnerCircleRadius = (int) (mExternalCircleRadius * INNER_EXTERNAL);
    mSweepAngle = 0;
    mRectF =
        new RectF(
            mStrokeWidth / 2,
            mStrokeWidth / 2,
            mViewLength - (mStrokeWidth / 2),
            mViewLength - (mStrokeWidth / 2));
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
    int viewHeigth = MeasureSpec.getSize(heightMeasureSpec);

    mViewLength = Math.min(viewWidth, viewHeigth);
    mCircleCenterX = mViewLength / 2;
    mCircleCenterY = mViewLength / 2;

    reSetParameters();

    setMeasuredDimension(mViewLength, mViewLength);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // LogUtils.e("mExternalCircleRadius=" + mExternalCircleRadius + "；mViewLength=" + mViewLength);
    // 画外圈圆
    canvas.drawCircle(
        mCircleCenterX,
        mCircleCenterY,
        mExternalCircleRadius - (int) mStrokeWidth / 2,
        mExternalPaint);
    // 画内圈圆
    if (!isDone) {
      canvas.drawCircle(
          mCircleCenterX, mCircleCenterY, (mViewLength / 2) * INNER_EXTERNAL, mInnerPaint);
    }
    // 画弧形
    canvas.drawArc(mRectF, -90, mSweepAngle, false, mProgressPaint);
  }

  @Override
  public boolean onTouch(View view, MotionEvent motionEvent) {
    if (null == mRecordButtonListener) {
      return false;
    }
    switch (motionEvent.getAction()) {
      case MotionEvent.ACTION_DOWN:
        onActionDown();
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        onActionUp();
        break;
      default:
    }
    return true;
  }

  /** 按下时执行操作 */
  private void onActionDown() {
    // 拦截父ViewPager的滑动事件
    getParent().requestDisallowInterceptTouchEvent(true);
    if (null == mRecordButtonListener) {
      return;
    }
    // 清楚上一个正在执行的任务
    if (null != mLongClickRunnable) {
      mHandler.removeCallbacks(mLongClickRunnable);
    }
    // 记录按下时间
    mTouchDown = System.currentTimeMillis();
    // 完成置为false
    isDone = false;
    // 提交延时任务
    mLongClickRunnable = new LongClickRunnable();
    mHandler.postDelayed(mLongClickRunnable, mTouchDelay);
  }

  /** 抬起时执行操作 手势抬起的时间用于点击判断 */
  private void onActionUp() {
    // 完成开关置为true
    isDone = true;
    // 清除动画效果
    clearAnimation();
    postInvalidate();
    mExternalPaint.setAlpha(255);
    long mTouchTime = System.currentTimeMillis() - mTouchDown;

    // 如果时间小于touch_delay就是点击事件
    if (mTouchTime < mTouchDelay) {
      if (mRecordButtonListener != null) mRecordButtonListener.onClick();
    }
  }

  /** 结束动作后执行的逻辑 动画结束后的时间用于长按判断 */
  private void onActionEndAction() {
    long mTouchTime = System.currentTimeMillis() - mTouchDown;

    // 完成开关置为true
    isDone = true;
    postInvalidate();
    mExternalPaint.setAlpha(255);
    // 执行结束动画效果
    startEndCircleAnimation();
    // 如果触摸事件小于RECORD_SHORT_TIME就是录制过短
    if (mTouchTime < mMinRecordTime) {
      if (mRecordButtonListener != null) mRecordButtonListener.onLongClickFinish(RECORD_SHORT);
    } else {
      if (mRecordButtonListener != null) mRecordButtonListener.onLongClickFinish(NORMAL);
    }
  }

  private class LongClickRunnable implements Runnable {

    @Override
    public void run() {
      // 如果延迟过后，触摸动作还没有结束
      if (!isDone) {
        // 开启开始动画
        startBeginCircleAnimation();
      }
    }
  }

  /** 开启开始动画 */
  private void startBeginCircleAnimation() {
    // 每次缩放动画之前重置参数，防止出现ui错误
    reSetParameters();
    // 这里是内圈圆半径获取和赋值
    ValueAnimator animator =
        ValueAnimator.ofInt(mInnerCircleRadius, (int) (mInnerCircleRadius * SCALE_NUM));
    // LogUtils.e("mInnerCircleRadius=" + mInnerCircleRadius);
    animator.setInterpolator(new LinearInterpolator());
    animator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mInnerCircleRadius = (int) valueAnimator.getAnimatedValue();
            if (isDone) {
              // 如果在开始动画执行的过程中停止触摸动作，及时取消动画
              valueAnimator.cancel();
            }
            // 更新ui
            postInvalidate();
          }
        });
    // 这里是外圈圆半径获取和赋值
    ValueAnimator animator1 =
        ValueAnimator.ofInt(mExternalCircleRadius, (int) (mExternalCircleRadius / SCALE_NUM));
    animator1.setInterpolator(new LinearInterpolator());
    animator1.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mExternalCircleRadius = (int) valueAnimator.getAnimatedValue();
            if (isDone) {
              // 如果在开始动画执行的过程中停止触摸动作，及时取消动画
              valueAnimator.cancel();
            }
          }
        });
    AnimatorSet set = new AnimatorSet();
    set.playTogether(animator, animator1);
    set.setDuration(DURING_TIME);
    set.setInterpolator(new LinearInterpolator());
    set.start();
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (isDone) {
              return;
            }
            mExternalPaint.setAlpha(204);
            // 同时调用长按点击事件
            if (mRecordButtonListener != null) {
              mRecordButtonListener.onLongClick();
            }
            // 开始进度条动画
            startProgressAnimation();
          }
        });
  }

  /** 开始进度条动画 */
  private void startProgressAnimation() {
    // 这里是进度条进度获取和赋值
    ValueAnimator animator = ValueAnimator.ofFloat(0, 360);
    animator.setDuration(mRecordTime);
    // Log.e("chenhan", "mRecordTime=" + mRecordTime);
    animator.setInterpolator(new LinearInterpolator());
    animator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mSweepAngle = isDone ? 0 : (float) valueAnimator.getAnimatedValue();
            // 更新ui
            postInvalidate();
            // 如果动作结束了，结束动画
            if (isDone) {
              valueAnimator.cancel();
            }
          }
        });
    animator.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            onActionEndAction();
          }
        });
    animator.start();
  }

  /** 开启结束动画 */
  private void startEndCircleAnimation() {
    // 每次缩放动画之前重置参数，防止出现ui错误
    reSetParameters();
    // 这里是内圈圆半径获取和赋值
    ValueAnimator animator =
        ValueAnimator.ofInt((int) (mInnerCircleRadius * SCALE_NUM), mInnerCircleRadius);
    animator.setInterpolator(new LinearInterpolator());
    animator.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mInnerCircleRadius = (int) valueAnimator.getAnimatedValue();
            if (!isDone) {
              // 如果在结束动画播放过程中再次点击，及时停止动画
              valueAnimator.cancel();
            }
            // 更新ui
            postInvalidate();
          }
        });
    // 这里是外圈圆半径获取和赋值
    ValueAnimator animator1 =
        ValueAnimator.ofInt((int) (mExternalCircleRadius / SCALE_NUM), mExternalCircleRadius);
    animator1.setInterpolator(new LinearInterpolator());
    animator1.addUpdateListener(
        new ValueAnimator.AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(ValueAnimator valueAnimator) {
            mExternalCircleRadius = (int) valueAnimator.getAnimatedValue();
            if (!isDone) {
              // 如果在结束动画播放过程中再次点击，及时停止动画
              valueAnimator.cancel();
            }
          }
        });
    AnimatorSet set = new AnimatorSet();
    set.playTogether(animator, animator1);
    set.setDuration(DURING_TIME);
    set.setInterpolator(new LinearInterpolator());
    set.start();
    set.addListener(
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            postInvalidate();
          }
        });
  }

  public void release() {
    clearAnimation();
    if (mRecordButtonListener != null) {
      mRecordButtonListener = null;
    }
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
    }
  }

  /**
   * 设置触摸事件的监听器
   *
   * @param recordButtonListener 触摸监听器
   */
  public void setRecordButtonListener(RecordButtonListener recordButtonListener) {
    this.mRecordButtonListener = recordButtonListener;
  }

  /**
   * 设置触摸延迟时间，区分点击还是长按
   *
   * @param touchDelay 触摸延迟时间（毫秒）
   */
  public void setTouchDelay(long touchDelay) {
    this.mTouchDelay = touchDelay;
  }

  /**
   * 设置最长录制时间
   *
   * @param recordTime 最长录制时间（毫秒）
   */
  public void setRecordTime(long recordTime) {
    // 视频保存时系统编解码处理会有一定损耗，最终导致保存完成视频大小比设置的要小，这里加上500ms的缓存情况，后面有好的处理办法再修改
    this.mRecordTime = recordTime;
  }

  /**
   * 设置多长时间之内算是录制过短
   *
   * @param minRecordTime 录制过短时间（毫秒）
   */
  public void setMinRecordTime(long minRecordTime) {
    this.mMinRecordTime = minRecordTime + mTouchDelay + DURING_TIME;
  }
}

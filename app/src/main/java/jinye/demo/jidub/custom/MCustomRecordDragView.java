package jinye.demo.jidub.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import jinye.demo.jidub.R;
import jinye.demo.jidub.model.MRecordScaleModel;
import jinye.demo.jidub.model.MScalePointModel;

/**
 * 作者:今夜犬吠
 * 时间:2019/7/23 23:03
 * 自定义录音拖动条
 */
public class MCustomRecordDragView extends View {
  /*屏幕像素密度*/private int mDensity = (int) this.getResources().getDisplayMetrics().density;
  /*上下文*/private Context mContext;
  /*画笔*/private Paint mUnitePaint;
  /*控件宽高-用于计算刻度位置*/private PointF mViewWidthHeight;
  /*录音刻度线集合*/private List<MRecordScaleModel> mRecordScaleModelList;

  /*刻度线宽度 px*/private float mTickMarkWidth = 1 * mDensity + 0.5f;
  /*刻度线间隔宽度*/private float mTickMarkIntervalWidth = 1 * mDensity + 0.5f;
  /*是否允许滑动*/private boolean mSlideSwitch = true;
  /*滑动系数-阻尼*/private float mDamping = 1 / 2f;
  /*滑动允许超出边界的距离*/private float mCrossingDistance = 64 * mDensity + 0.5f;
  /*最小滑动距离*/ private int mTouchSlop;
  /*开始时间戳*/private long mStartTimestamp;

  /*平滑滚动控制*/private Scroller mScroller;


  public MCustomRecordDragView(Context context) {
    super(context);
    this.mContext = context;
    toolInit();
  }

  public MCustomRecordDragView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.mContext = context;
    toolInit();
  }

  public MCustomRecordDragView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    this.mContext = context;
    toolInit();
  }

  private void toolInit() {
    /*系统可识别最小滑动距离*/
    mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
    /*自动滑动*/
    mScroller = new Scroller(mContext);
    /*初始化刻度线集合*/
    mRecordScaleModelList = new ArrayList<MRecordScaleModel>();
    /*初始化画笔*/
    mUnitePaint = new Paint();
    /*设置画笔颜色*/
    mUnitePaint.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
    /*设置画笔样式*/
    mUnitePaint.setStyle(Paint.Style.FILL);
    /*设置画笔粗细*/
    mUnitePaint.setStrokeWidth(mTickMarkWidth);

    /*文字居中*/
    mUnitePaint.setTextAlign(Paint.Align.CENTER);
    /*使用抗锯齿*/
    mUnitePaint.setAntiAlias(true);
    /*使用防抖动*/
    mUnitePaint.setDither(true);
    /*设置笔触样式-圆*/
    mUnitePaint.setStrokeCap(Paint.Cap.ROUND);
    /*设置结合处为圆弧*/
    mUnitePaint.setStrokeJoin(Paint.Join.ROUND);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    /*获取控件的宽高*/
    mViewWidthHeight = new PointF(getWidth(), getHeight());
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    /*绘制轴线*/
    toolDrawAxis(canvas);
    /*绘制刻度线*/
    toolDrawTickmark(canvas);
    /*绘制标尺*/
    toolDrawBenchmark(canvas);
    /*绘制时间*/
    toolDrawTime(canvas);
  }

  /*按下的X坐标*/private float mPressX;

  /*滑出边界的位置X*/private float mCrossingX;

  /*越界状态 0没有越界 1左越界 2右越界*/private int mSlidingDirectionStatus;

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mCrossingX = 0;
        mSlidingDirectionStatus = 0;
        mPressX = event.getX();
        mScroller.abortAnimation();
        break;
      case MotionEvent.ACTION_MOVE:
        /*判断是否滑动到左右顶部*/
        if (toolBoundaryMeasure(event.getX() - mPressX)) {
          mPendingEditData = true;
          /*移动刻度线*/
          toolMoveTickmark((event.getX() - mPressX) * mDamping);
        }

        mPressX = event.getX();
        break;
      case MotionEvent.ACTION_UP:
        /*松手判断是否滑出边界*/
        if (mSlidingDirectionStatus != 0) {
          if (mSlidingDirectionStatus == 1) {
            mCrossingX = mRecordScaleModelList.get(0)
                .getmTopScalePointM().getmPointX();
          } else if (mSlidingDirectionStatus == 2) {
            mCrossingX = mRecordScaleModelList.get(mRecordScaleModelList.size() - 1)
                .getmTopScalePointM().getmPointX();
          }
          mScroller.startScroll((int) mCrossingX, 0
              , (int) (mCrossingX - mViewWidthHeight.x / 2), 0, 1000);

          invalidate();
          //属性动画 定义一个不断变化的动画
        }

        break;
      default:
        break;
    }
    return true;
  }


  /*添加刻度测试调度*/private Disposable mAddTickmarkDb;
  /*是否有待编辑数据*/private boolean mPendingEditData = false;
  /*停止的时间*/private long mStopTime;
  /*停止时间上一次*/private long mLastStopTime;
  /*中断的时间*/private long mInterruptTime;
  /*中断的时间*/private boolean mInterruptTimeIs;
  /*是否重置时间*/private boolean mResetData;
  /*上传*/
  /**
   * 构建测试数据
   */
  @SuppressLint("CheckResult")
  public void toolBuildData() {
    /*停止上一次的*/
    //toolStopBuildData();
    /*重新开始前清除标尺右侧的刻度线*/
    if (mPendingEditData) {
      mInterruptTimeIs=true;
      toolClearBenchmarkRightData();
    }

    /*是否重置时间*/
    if(mResetData){
      mStopTime = System.currentTimeMillis() - mStopTime;
      Log.d("关闭时保存计算",""+mStopTime);
      mResetData=false;
    }
    if (mRecordScaleModelList != null) {
      mAddTickmarkDb = Observable.interval(0,30, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
              int mSize = mRecordScaleModelList.size();
              if (mSize > 0) {
                for (int i = 0; i < mSize; i++) {
                  mRecordScaleModelList.get(i)
                      .getmTopScalePointM().setmPointX(mRecordScaleModelList.get(i)
                      .getmTopScalePointM().getmPointX() - (mTickMarkIntervalWidth + mTickMarkWidth));
                  mRecordScaleModelList.get(i)
                      .getmBottomScalePointM().setmPointX(mRecordScaleModelList.get(i)
                      .getmBottomScalePointM().getmPointX() - (mTickMarkIntervalWidth + mTickMarkWidth));
                }
                MRecordScaleModel mRecordScaleModel = new MRecordScaleModel();
                mRecordScaleModel.setmColor(ContextCompat.getColor(mContext, R.color.Tickmark));
                mRecordScaleModel.setmRecordPosition(aLong.intValue());
                if(mInterruptTimeIs){
                  mInterruptTime=System.currentTimeMillis()-mInterruptTime;
                  mInterruptTimeIs=false;
                  mStopTime=0;
                  mLastStopTime=0;
                }
                mRecordScaleModel.setmRecordTimestamp(System.currentTimeMillis()-mInterruptTime - mStopTime-mLastStopTime);
                mRulerTimestamp = mRecordScaleModel.getmRecordTimestamp();
                float mRecordHeight = (float) ((Math.random() * mViewWidthHeight.y + 1));
                mRecordScaleModel.setmRecordHeight(mRecordHeight);
                MScalePointModel mScalePointModelOfTop = new MScalePointModel();
                mScalePointModelOfTop.setmPointX(mViewWidthHeight.x / 2 - (mTickMarkIntervalWidth + mTickMarkWidth));
                mScalePointModelOfTop.setmPointY((mViewWidthHeight.y - mRecordScaleModel.getmRecordHeight()) / 2);
                mRecordScaleModel.setmTopScalePointM(mScalePointModelOfTop);

                MScalePointModel mScalePointModelOfBottom = new MScalePointModel();
                mScalePointModelOfBottom.setmPointX(mRecordScaleModel.getmTopScalePointM().getmPointX() + mTickMarkWidth);
                mScalePointModelOfBottom.setmPointY(mRecordScaleModel.getmTopScalePointM().getmPointY()
                    + mRecordScaleModel.getmRecordHeight());
                mRecordScaleModel.setmBottomScalePointM(mScalePointModelOfBottom);

                mRecordScaleModelList.add(mRecordScaleModel);
                invalidate();
              } else {
                MRecordScaleModel mRecordScaleModel = new MRecordScaleModel();
                mRecordScaleModel.setmColor(ContextCompat.getColor(mContext, R.color.Tickmark));
                mRecordScaleModel.setmRecordPosition(aLong.intValue());
                mRecordScaleModel.setmRecordTimestamp(System.currentTimeMillis() - mStopTime);
                mRulerTimestamp = mRecordScaleModel.getmRecordTimestamp();
                mStartTimestamp = mRecordScaleModel.getmRecordTimestamp();
                float mRecordHeight = (float) ((Math.random() * mViewWidthHeight.y + 1));
                mRecordScaleModel.setmRecordHeight(mRecordHeight);
                MScalePointModel mScalePointModelOfTop = new MScalePointModel();
                mScalePointModelOfTop.setmPointX(mViewWidthHeight.x / 2 - (mTickMarkIntervalWidth + mTickMarkWidth));
                mScalePointModelOfTop.setmPointY((mViewWidthHeight.y - mRecordScaleModel.getmRecordHeight()) / 2);
                mRecordScaleModel.setmTopScalePointM(mScalePointModelOfTop);

                MScalePointModel mScalePointModelOfBottom = new MScalePointModel();
                mScalePointModelOfBottom.setmPointX(mRecordScaleModel.getmTopScalePointM().getmPointX() + mTickMarkWidth);
                mScalePointModelOfBottom.setmPointY(mRecordScaleModel.getmTopScalePointM().getmPointY()
                    + mRecordScaleModel.getmRecordHeight());
                mRecordScaleModel.setmBottomScalePointM(mScalePointModelOfBottom);

                mRecordScaleModelList.add(mRecordScaleModel);
                invalidate();
              }

            }
          });
    }

  }


  /**
   * 重新开始前清除标尺右侧的刻度线
   */
  private void toolClearBenchmarkRightData() {
    List<MRecordScaleModel> mRecordScaleModels = new ArrayList<>();
    if (mRecordScaleModelList != null
        && !mRecordScaleModelList.isEmpty()) {
      int mSize = mRecordScaleModelList.size();
      for (int i = 0; i < mSize; i++) {
        if (mRecordScaleModelList.get(i).getmTopScalePointM().getmPointX() >= mViewWidthHeight.x / 2) {
          mInterruptTime=mRecordScaleModelList.get(i).getmRecordTimestamp();
          toolResetTimestamp();
          break;
        }
        mRecordScaleModels.add(mRecordScaleModelList.get(i));
      }
      mRecordScaleModelList.clear();
      mRecordScaleModelList.addAll(mRecordScaleModels);
      mPendingEditData = false;
      invalidate();
    }
  }

  /**
   * 重新计算所有时间戳
   */
  private void toolResetTimestamp() {

  }

  /**
   * 停止构建
   */
  public void toolStopBuildData() {
    if (mAddTickmarkDb != null
        && !mAddTickmarkDb.isDisposed()) {
      mAddTickmarkDb.dispose();
      mLastStopTime+=mStopTime;
      mStopTime = System.currentTimeMillis();
      mResetData=true;
    }
  }

  /**
   * 测量边界
   *
   * @return
   */
  private boolean toolBoundaryMeasure(float mMoveDirection) {
    if (mRecordScaleModelList != null
        && !mRecordScaleModelList.isEmpty()) {

//      if(Math.abs(mMoveDirection)<mTouchSlop){
//        return false;
//      }
      if (mMoveDirection > 0 && mRecordScaleModelList.get(0)
          .getmTopScalePointM().getmPointX() <= mViewWidthHeight.x / 2 + mCrossingDistance) {
        if (mRecordScaleModelList.get(0)
            .getmTopScalePointM().getmPointX() >= mViewWidthHeight.x / 2) {
          /*超出左边界*/
          mSlidingDirectionStatus = 1;
          /*超出边界后 提升阻尼*/
          mDamping = 1 / 4f;
        } else {
          mDamping = 1 / 2f;
        }
        return true;
      }
      if (mMoveDirection < 0 && mRecordScaleModelList.get(mRecordScaleModelList.size() - 1)
          .getmTopScalePointM().getmPointX() >= mViewWidthHeight.x / 2 - mCrossingDistance) {
        if (mRecordScaleModelList.get(mRecordScaleModelList.size() - 1)
            .getmTopScalePointM().getmPointX() <= mViewWidthHeight.x / 2) {
          /*超出右边界*/
          /*超出左边界*/
          mSlidingDirectionStatus = 2;
          /*超出边界后 提升阻尼*/
          mDamping = 1 / 4f;
        } else {
          mDamping = 1 / 2f;
        }
        return true;
      }

    }
    return false;
  }

  /*绘制刻度线*/
  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  private void toolDrawTickmark(Canvas canvas) {
    if (canvas != null) {
      if (mUnitePaint != null && mViewWidthHeight != null) {
        if (mRecordScaleModelList != null
            && !mRecordScaleModelList.isEmpty()) {
          int mSize = mRecordScaleModelList.size();
          for (MRecordScaleModel mRecordScaleModel : mRecordScaleModelList) {
            mUnitePaint.setColor(mRecordScaleModel.getmColor());
            canvas.drawRoundRect(mRecordScaleModel.getmTopScalePointM().getmPointX()
                , mRecordScaleModel.getmTopScalePointM().getmPointY()
                , mRecordScaleModel.getmBottomScalePointM().getmPointX()
                , mRecordScaleModel.getmBottomScalePointM().getmPointY()
                , mTickMarkWidth / 2, mTickMarkWidth / 2, mUnitePaint);
//            canvas.drawRect(mRecordScaleModel.getmTopScalePointM().getmPointX()
//                , mRecordScaleModel.getmTopScalePointM().getmPointY()
//                , mRecordScaleModel.getmBottomScalePointM().getmPointX()
//                , mRecordScaleModel.getmBottomScalePointM().getmPointY(),mUnitePaint);
////            canvas.drawLine(mRecordScaleModel.getmTopScalePointM().getmPointX()
//                , mRecordScaleModel.getmTopScalePointM().getmPointY()
//                , mRecordScaleModel.getmBottomScalePointM().getmPointX()
//                , mRecordScaleModel.getmBottomScalePointM().getmPointY(), mUnitePaint);
          }
        }
      }
    }
  }

  /*是否回调*/private boolean mIsCallB = true;

  /**
   * 移动刻度线
   */
  private void toolMoveTickmark(float mMoveX) {
    mIsCallB = true;
    if (mRecordScaleModelList != null && !mRecordScaleModelList.isEmpty()) {
      int mSize = mRecordScaleModelList.size();
      for (int i = 0; i < mSize; i++) {
        mRecordScaleModelList.get(i)
            .getmTopScalePointM().setmPointX(mRecordScaleModelList.get(i)
            .getmTopScalePointM().getmPointX() + mMoveX);
        mRecordScaleModelList.get(i)
            .getmBottomScalePointM().setmPointX(mRecordScaleModelList.get(i)
            .getmBottomScalePointM().getmPointX() + mMoveX);
        /*回调进度api*/
        if (mIsCallB && mRecordScaleModelList.get(i).getmTopScalePointM().getmPointX() >= mViewWidthHeight.x / 2) {
          toolCallBackTimeProgress(mRecordScaleModelList.get(i).getmRecordTimestamp() -
              mRecordScaleModelList.get(0).getmRecordTimestamp());
          mRulerTimestamp = mRecordScaleModelList.get(i).getmRecordTimestamp();
          mIsCallB = false;
        }
        /*滑入标尺右侧 变色*/
        if (mRecordScaleModelList.get(i).getmTopScalePointM().getmPointX() > mViewWidthHeight.x / 2) {
          mRecordScaleModelList.get(i).setmColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
          mRecordScaleModelList.get(i).setmColor(ContextCompat.getColor(mContext, R.color.Tickmark));
        }

      }
      invalidate();
    }
  }

  /*绘制轴线*/
  private void toolDrawAxis(Canvas canvas) {
    if (canvas != null) {
      if (mUnitePaint != null && mViewWidthHeight != null) {
        mUnitePaint.setStrokeWidth(1 * mDensity);
        mUnitePaint.setColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        canvas.drawLine(0, mViewWidthHeight.y / 2
            , mViewWidthHeight.x
            , mViewWidthHeight.y / 2
            , mUnitePaint);
      }
    }
  }

  /*绘制标尺*/
  private void toolDrawBenchmark(Canvas canvas) {
    if (canvas != null) {
      if (mUnitePaint != null && mViewWidthHeight != null) {
        mUnitePaint.setStrokeWidth(3 * mDensity);
        mUnitePaint.setColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        canvas.drawLine(mViewWidthHeight.x / 2
            , 0, mViewWidthHeight.x / 2
            , mViewWidthHeight.y - mViewWidthHeight.y / 4, mUnitePaint);
      }
    }
  }

  /*标尺处的时间戳*/private long mRulerTimestamp = 0;

  /**
   * 绘制时间
   */
  private void toolDrawTime(Canvas canvas) {
    if (canvas != null) {
      String mTime = getHMSS(mRulerTimestamp - mStartTimestamp);
      float mTimeWidth = mUnitePaint.measureText(mTime);
      mUnitePaint.setColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
      mUnitePaint.setTextSize(20);
      canvas.drawText(mTime, (mViewWidthHeight.x - mTimeWidth) / 2 + mTimeWidth / 2, mViewWidthHeight.y - mTimeWidth / mTime.length(), mUnitePaint);
    }
  }

  /**
   * 转换时间戳
   */
  private String getHMSS(long timestamp) {
    SimpleDateFormat sdf = new SimpleDateFormat("mm:ss:SSS");
    String time = null;
    try {
      return sdf.format(new Date(timestamp));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return time.trim();
  }

  /**
   * 回调时间刻度
   */
  private void toolCallBackTimeProgress(long mTime) {
    if (jTimeProgressCallB != null) {
      jTimeProgressCallB.toolProgress(mTime);
    }
  }

  /**
   * 平滑调整滑动位置
   */
  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      if (mRecordScaleModelList != null) {
        int mSize = mRecordScaleModelList.size();
        for (int i = 0; i < mSize; i++) {
          if (mSlidingDirectionStatus == 1) {
            mRecordScaleModelList.get(i)
                .getmTopScalePointM().setmPointX(mRecordScaleModelList.get(i)
                .getmTopScalePointM().getmPointX() - (mScroller.getCurrX() - mCrossingX));
            mRecordScaleModelList.get(i).getmBottomScalePointM().setmPointX(
                mRecordScaleModelList.get(i).getmBottomScalePointM().getmPointX() - (mScroller.getCurrX() - mCrossingX)
            );
          } else if (mSlidingDirectionStatus == 2) {
            mRecordScaleModelList.get(i)
                .getmTopScalePointM().setmPointX(mRecordScaleModelList.get(i)
                .getmTopScalePointM().getmPointX() + (mCrossingX - mScroller.getCurrX()));
            mRecordScaleModelList.get(i).getmBottomScalePointM().setmPointX(
                mRecordScaleModelList.get(i).getmBottomScalePointM().getmPointX() + (mCrossingX - mScroller.getCurrX())
            );
          }

        }
        mCrossingX = mScroller.getCurrX();
      }
      postInvalidate();
    }
    super.computeScroll();
  }

  /**
   * 设置滑动开关
   */
  public void toolSetSlideSwitch(boolean mStatus) {
    this.mSlideSwitch = mStatus;
  }

  /*回调进度接口*/private JTimeProgressCallB jTimeProgressCallB;

  /**
   * 传递时间进度回调接口
   *
   * @param jTimeProgressCallB
   */
  public void toolSendTimeProgressCallB(JTimeProgressCallB jTimeProgressCallB) {
    this.jTimeProgressCallB = jTimeProgressCallB;
  }

  /**
   * 时间刻度接口
   */
  public interface JTimeProgressCallB {
    void toolProgress(long mProgress);
  }
}

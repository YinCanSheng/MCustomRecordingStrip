package jinye.demo.jidub.model;

import java.io.Serializable;

/**
 * 作者:今夜犬吠
 * 时间:2019/7/31 22:53
 * 录音刻度模型 一根竖线|
 */
public class MRecordScaleModel implements Serializable {
  /*时间戳-表示录制的时间点-很重要*/private long mRecordTimestamp;
  /*颜色*/private int mColor;
  /*刻度高度*/private float mRecordHeight;
  /*下标-表示是几根竖线*/private int mRecordPosition;
  /*竖线左上点的坐标*/private MScalePointModel mTopScalePointM;
  /*竖线右下点的坐标*/private MScalePointModel mBottomScalePointM;

  public long getmRecordTimestamp() {
    return mRecordTimestamp;
  }

  public void setmRecordTimestamp(long mRecordTimestamp) {
    this.mRecordTimestamp = mRecordTimestamp;
  }

  public int getmColor() {
    return mColor;
  }

  public void setmColor(int mColor) {
    this.mColor = mColor;
  }

  public float getmRecordHeight() {
    return mRecordHeight;
  }

  public void setmRecordHeight(float mRecordHeight) {
    this.mRecordHeight = mRecordHeight;
  }

  public int getmRecordPosition() {
    return mRecordPosition;
  }

  public void setmRecordPosition(int mRecordPosition) {
    this.mRecordPosition = mRecordPosition;
  }

  public MScalePointModel getmTopScalePointM() {
    return mTopScalePointM;
  }

  public void setmTopScalePointM(MScalePointModel mTopScalePointM) {
    this.mTopScalePointM = mTopScalePointM;
  }

  public MScalePointModel getmBottomScalePointM() {
    return mBottomScalePointM;
  }

  public void setmBottomScalePointM(MScalePointModel mBottomScalePointM) {
    this.mBottomScalePointM = mBottomScalePointM;
  }
}

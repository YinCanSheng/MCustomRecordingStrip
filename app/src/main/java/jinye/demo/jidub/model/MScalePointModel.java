package jinye.demo.jidub.model;

import java.io.Serializable;

/**
 * 作者:今夜犬吠
 * 时间:2019/7/31 23:11
 * 刻度线 坐标点
 */
public class MScalePointModel implements Serializable {
  /*X坐标*/private float mPointX;
  /*Y坐标*/private float mPointY;

  public float getmPointX() {
    return mPointX;
  }

  public void setmPointX(float mPointX) {
    this.mPointX = mPointX;
  }

  public float getmPointY() {
    return mPointY;
  }

  public void setmPointY(float mPointY) {
    this.mPointY = mPointY;
  }
}

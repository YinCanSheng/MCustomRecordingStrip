package jinye.demo.jidub;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding3.view.RxView;
import com.jakewharton.rxbinding3.widget.RxCompoundButton;
import com.jakewharton.rxbinding3.widget.RxTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import jinye.demo.dynamiclyrics.widget.AbstractLrcView;
import jinye.demo.dynamiclyrics.widget.ManyLyricsView;
import jinye.demo.jidub.custom.MCustomRecordDragView;
import kotlin.Unit;

import static jinye.demo.dynamiclyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_SEEKTO;

/**
 * 要严谨
 * 作者 今夜犬吠
 * 时间 2019/7/30 18:00
 */
public class MainActivity extends AppCompatActivity {

  /*加载本地库*/
  static {
    System.loadLibrary("jinye-lib");
  }

  /*动感歌词*/private ManyLyricsView makeLyricsView;
  /*原唱*/private TextView mOriginalSingTex;
  /*倒计时*/private TextView mCountdownTextV;
  /*开始录音*/private ImageView mRecordImageV;

  /*暂停*/private ImageView mStopImageV;
  /*播放*/private ImageView mPlayImageV;
  /*重置*/private TextView mRestartTextV;
  /*试听*/private TextView mAuditionTextV;

  /*拖动条*/private AppCompatSeekBar mLyricsSeekBar;
/*正則*/private AppCompatEditText mRegularEditT;
  /**/private TextView mTextTextV;
  /*自定义刻度条*/private MCustomRecordDragView mCustomRecordDragView;
  /*指示条时间配置*/private AppCompatCheckBox mIndicatorStatusCheckBox;
  /*松手后继续滑动*/private AppCompatCheckBox mContinueSlideCheckBox;
  /*改变滑动速度*/private AppCompatCheckBox mAccelerateCheckBox;
  /*播放速度*/private int mPlaySpeed = 1000;

  /*是否开始播放*/private boolean mIsPlay;

  /*是否开启变速*/private boolean mIsAccelerate;

  /*开始变速前的进度*/private int mBeforeAccelerateProgress;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolInitView();
    toolConfig();
    toolBuildTestData();
    toolSetListener();
  }

  /**
   * 初始化View
   */
  private void toolInitView() {

    mTextTextV=findViewById(R.id.TextView_MainActivity_text);
    mRegularEditT=findViewById(R.id.EditText_MainActivity_Regular);
    mTextTextV.setText(mDataText);
    makeLyricsView = findViewById(R.id.View_MainActivity_Dynamiclyrics);
    mCustomRecordDragView = findViewById(R.id.View_MainActivity_CustomRecordDrag);
    mIndicatorStatusCheckBox = findViewById(R.id.CheckBox_MainActivity_IndicatorStatus);
    mContinueSlideCheckBox = findViewById(R.id.CheckBox_MainActivity_ContinueSlide);
    mOriginalSingTex = findViewById(R.id.TextView_MainActivity_HeadOriginalSing);
    mAccelerateCheckBox = findViewById(R.id.CheckBox_MainActivity_Accelerate);
    mCountdownTextV = findViewById(R.id.TextView_MainActivity_Countdown);
    mRecordImageV = findViewById(R.id.ImageView_MainActivity_Record);
    mStopImageV = findViewById(R.id.ImageView_MainActivity_Stop);
    mPlayImageV = findViewById(R.id.ImageView_MainActivity_Play);
    mRestartTextV = findViewById(R.id.TextView_MainActivity_Restart);
    mAuditionTextV = findViewById(R.id.TextView_MainActivity_audition);
    mLyricsSeekBar = findViewById(R.id.View_MainActivity_RecordDrag);
  }

  /**
   * 配置View
   */
  private void toolConfig() {
    int paintColor = ContextCompat.getColor(this, R.color.lyrcolorPrimary);
    /*空行间隔-px*/
    makeLyricsView.setSpaceLineHeight(16 * getResources().getDisplayMetrics().density + 0.5f);
    /*是否绘制时间指示器*/
    makeLyricsView.setIsDrawIndicator(true);
    /*绘制歌词文本大小*/
    // makeLyricsView.setFontSize(20);
    //makeLyricsView.setSize(25,25);
    /*默认颜色*/
    makeLyricsView.setPaintColor(new int[]{paintColor, paintColor}, false);
    int paintLineColor = ContextCompat.getColor(this, R.color.colorAccent);
    /*线颜色*/
    makeLyricsView.setPaintLineColor(paintLineColor);
    /*高亮颜色*/
    int paintHLColor = ContextCompat.getColor(this, R.color.colorPrimary);
    makeLyricsView.setPaintHLColor(new int[]{paintHLColor, paintHLColor}, false);

    /*设置高亮位置*/
    /*mHlPositionOffset 为控件的百分比 0.5 为控件居中 */
    makeLyricsView.toolSetHlPositionOffset(0.1f);
  }

  /*测试文本数据*/
  private String mDataText = "北冥有鱼，其名为鲲，鲲之大，不知其几千里也；化而为鸟，其名为鹏，鹏之背，不知其几千里也；怒而飞，其翼若垂天之云，是鸟也，海运则将徙于南冥，南冥者，天池也，《齐谐》者，志怪者也，《谐》之言曰：“鹏之徙于南冥也，水击三千里，抟扶摇而上者九万里，去以六月息者也，”野马也，尘埃也，生物之以息相吹也，天之苍苍，其正色邪？其远而无所至极邪？其视下也，亦若是则已矣，且夫水之积也不厚，则其负大舟也无力。覆杯水于坳堂之上，则芥为之舟，置杯焉则胶，水浅而舟大也，风之积也不厚，则其负大翼也无力，故九万里，则风斯在下矣，而后乃今培风；背负青天，而莫之夭阏者，而后乃今将图南，蜩与学鸠笑之曰：“我决起而飞，抢榆枋而止，时则不至，而控于地而已矣，奚以之九万里而南为？”适莽苍者，三餐而反，腹犹果然；适百里者，宿舂粮；适千里者，三月聚粮，之二虫又何知！小知不及大知，小年不及大年。奚以知其然也？朝菌不知晦朔，蟪蛄不知春秋，此小年也，楚之南有冥灵者，以五百岁为春，五百岁为秋；上古有大椿者，以八千岁为春，八千岁为秋，此大年也，而彭祖乃今以久特闻，众人匹之，不亦悲乎！汤之问棘也是已，穷发之北，有冥海者，天池也，有鱼焉，其广数千里，未有知其修者，其名为鲲，有鸟焉，其名为鹏，背若泰山，翼若垂天之云，抟扶摇羊角而上者九万里，绝云气，负青天，然后图南，且适南冥也，斥鴳笑之曰：“彼且奚适也？我腾跃而上，不过数仞而下，翱翔蓬蒿之间，此亦飞之至也，而彼且奚适也？”此小大之辩也，故夫知效一官，行比一乡，德合一君。而征一国者，其自视也，亦若此矣，而宋荣子犹然笑之，且举世誉之而不加劝，举世非之而不加沮，定乎内外之分，辩乎荣辱之境，斯已矣，彼其于世，未数数然也，虽然，犹有未树也，夫列子御风而行，泠然善也，旬有五日而后反，彼于致福者，未数数然也，此虽免乎行，犹有所待者也，若夫乘天地之正，而御六气之辩，以游无穷者，彼且恶乎待哉？故曰：至人无己，神人无功，圣人无名。";
  /**
   * 构建测试数据-歌词
   */
  private void toolBuildTestData() {
    /*获取屏幕宽度*/
    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    DisplayMetrics displayMetrics = new DisplayMetrics();
    display.getMetrics(displayMetrics);
    int screensWidth = displayMetrics.widthPixels;
    /*是否允许松手后继续滑动*/
    makeLyricsView.setmIsAllowLooseSliding(false);
    /*时候滑动结束后多久隐藏指示条ms*/
    makeLyricsView.setmResetDuration(500);
    /*设置滚动速度*/
    makeLyricsView.toolSetSpeed(500);
    /*设置每一行允许绘制的最大宽度/可以不写/默认使用屏幕宽度的2/3宽*/
    //设置歌词的最大宽度
    int textMaxWidth = (int) (screensWidth-(32 * displayMetrics.density + 0.5f)*2);
    makeLyricsView.setTextMaxWidth(textMaxWidth);
    /*把歌词数据加载到自定义视图/原始文本/正则符号/是否使用正则匹配/如果是false则自动计算文本长度适配控件宽/建议使用false*/
    makeLyricsView.toolSetData(mDataText, "！", true);

  }

  @SuppressLint("CheckResult")
  private void toolSetListener() {
    /*开始录音*/
    RxView.clicks(mRecordImageV)
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe(new Consumer<Unit>() {
          @Override
          public void accept(Unit unit) throws Exception {
            //mOriginalSingTex.setText(toolAdditionFromJNI(20, 40) + "这是来自C层的数据计算");
            /*开始录音倒计时*/
            toolRecordCountdown();

          }
        });

    /*暂停录音*/
    RxView.clicks(mStopImageV)
        .throttleFirst(500, TimeUnit.MICROSECONDS)
        .subscribe(new Consumer<Unit>() {
          @Override
          public void accept(Unit unit) throws Exception {
            mStopImageV.setVisibility(View.GONE);
            mPlayImageV.setVisibility(View.VISIBLE);
            mRestartTextV.setVisibility(View.VISIBLE);
            mAuditionTextV.setVisibility(View.VISIBLE);
            makeLyricsView.pause();
            if (mProgressDp != null && !mProgressDp.isDisposed()) {
              mProgressDp.dispose();
            }
            mCustomRecordDragView.toolStopBuildData();
          }
        });

    /*播放录音*/
    RxView.clicks(mPlayImageV)
        .throttleFirst(500, TimeUnit.MICROSECONDS)
        .subscribe(new Consumer<Unit>() {
          @Override
          public void accept(Unit unit) throws Exception {
            /*如果拖动了音轨 则弹框判断是否需要覆盖*/
            /*否则接着录音*/
            /*继续滚动*/
            //toolLyricsScroll();
            mPlayImageV.setVisibility(View.GONE);
            mRestartTextV.setVisibility(View.GONE);
            mAuditionTextV.setVisibility(View.GONE);
            mStopImageV.setVisibility(View.VISIBLE);
            if (mAccelerateCheckBox.isChecked()) {
              mBeforeAccelerateProgress = (int) (makeLyricsView.getmPlayerSpendTime()
                  + makeLyricsView.getmCurPlayingTime() + 100);
              makeLyricsView.pause();
              mPlaySpeed = 1000;
              mIsAccelerate = true;
              mAccelerateCheckBox.setText("变速滚动(" + mPlaySpeed / 1000 + "m)");
              toolLyricsScroll();
            } else {
              makeLyricsView.resume();
            }
            mCustomRecordDragView.toolBuildData();
          }
        });
    /*重置录音*/
    RxView.clicks(mRestartTextV)
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe(new Consumer<Unit>() {
          @Override
          public void accept(Unit unit) throws Exception {
            /*弹框提示 是否重新录制 是则删除原录音文件 重新录制*/
          }
        });
    /*试听*/
    RxView.clicks(mAuditionTextV)
        .throttleFirst(1, TimeUnit.SECONDS)
        .subscribe(new Consumer<Unit>() {
          @Override
          public void accept(Unit unit) throws Exception {
            /*判断拖动条所在音轨的位置 从位置处开始播放*/
          }
        });

    /*指示条配置*/
    RxCompoundButton.checkedChanges(mIndicatorStatusCheckBox)
        .subscribe(new Consumer<Boolean>() {
          @Override
          public void accept(Boolean aBoolean) throws Exception {
            /*动感歌词显示指示条*/
            makeLyricsView.setIsDrawIndicator(aBoolean);
          }
        });
    /*松手后继续滑动*/
    RxCompoundButton.checkedChanges(mContinueSlideCheckBox)
        .subscribe(new Consumer<Boolean>() {
          @Override
          public void accept(Boolean aBoolean) throws Exception {
            /*是否允许松手后继续滑动*/
            makeLyricsView.setmIsAllowLooseSliding(aBoolean);
          }
        });
    /*是否开启变速*/
    RxCompoundButton.checkedChanges(mAccelerateCheckBox)
        .subscribe(new Consumer<Boolean>() {
          @Override
          public void accept(Boolean aBoolean) throws Exception {
            if (aBoolean) {
              if (makeLyricsView.getLrcPlayerStatus() == AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
                mBeforeAccelerateProgress = (int) (makeLyricsView.getmPlayerSpendTime()
                    + makeLyricsView.getmCurPlayingTime() + 100);
                makeLyricsView.pause();
                mPlaySpeed = 1000;
                mIsAccelerate = true;
                mAccelerateCheckBox.setText("变速滚动(" + mPlaySpeed / 1000 + "m)");
                toolLyricsScroll();
              }
            } else {
              if (mProgressDp != null && !mProgressDp.isDisposed()) {
                mProgressDp.dispose();
                makeLyricsView.resume();
                mBeforeAccelerateProgress = 0;
                mAccelerateCheckBox.setText("变速滚动(" + makeLyricsView.getmPlaySpeed() / 1000 + "m)");
              }
            }

          }
        });
    /*歌词点击*/
    makeLyricsView.setOnLrcClickListener(new ManyLyricsView.OnLrcClickListener() {
      @Override
      public void onLrcPlayClicked(int seekProgress) {
        if (makeLyricsView != null) {
          Toast.makeText(getApplicationContext(), "点击了第" + makeLyricsView.getLineLrc(seekProgress), Toast.LENGTH_LONG)
              .show();
          makeLyricsView.seekto(seekProgress);
          mOriginalSingTex.setText(makeLyricsView.getLineLrc(seekProgress + 100));
          mPlayImageV.setVisibility(View.GONE);
          mRestartTextV.setVisibility(View.GONE);
          mAuditionTextV.setVisibility(View.GONE);
          mStopImageV.setVisibility(View.VISIBLE);
          mRecordImageV.setVisibility(View.GONE);
          mIsPlay = true;
          makeLyricsView.resume();
          mCustomRecordDragView.toolBuildData();
        }
      }
    });
    /*手指触摸监听*/
    makeLyricsView.toolSetScrollTouchListener(new ManyLyricsView.OnScrollingTouchListener() {
      @Override
      public void toolBuildDataEnd() {
        /*数据构造完成回调指文本数据已绘制到页面*/
        mLyricsSeekBar.setMax((int) makeLyricsView.toolGetTotalTime());
      }

      @Override
      public void toolDown() {
        /*按下回调*/
      }

      @Override
      public void toolup() {
        /*松手回调*/
        if (makeLyricsView.getIsAllowLooseSliding()) {
          mPlayImageV.setVisibility(View.GONE);
          mRecordImageV.setVisibility(View.GONE);
          mRestartTextV.setVisibility(View.GONE);
          mAuditionTextV.setVisibility(View.GONE);
          mStopImageV.setVisibility(View.VISIBLE);
          if (mAccelerateCheckBox.isChecked()) {
            mBeforeAccelerateProgress = (int) (makeLyricsView.getmPlayerSpendTime()
                + makeLyricsView.getmCurPlayingTime() + 100);
            makeLyricsView.pause();
            mPlaySpeed = 1000;
            mIsAccelerate = true;
            mAccelerateCheckBox.setText("变速滚动(" + mPlaySpeed / 1000 + "m)");
            toolLyricsScroll();
          }
          mCustomRecordDragView.toolBuildData();
        }
      }
    });
    /*滑动指示进度监听*/
    makeLyricsView.setOnIndicatorListener(new ManyLyricsView.OnIndicatorListener() {
      @Override
      public void indicatorVisibleToUser(boolean isVisibleToUser, int scrollLrcProgress) {
        if (isVisibleToUser) {
          //Log.e("动感歌词指示条显示", "" +makeLyricsView.getmCurPlayingTime());
          mOriginalSingTex.setText(makeLyricsView.getLineLrc(scrollLrcProgress + 100));

          mLyricsSeekBar.setProgress(scrollLrcProgress + 100);

          /*更新动感歌词 使其高亮*/
          makeLyricsView.seekto(scrollLrcProgress + 100);

          /*保存当前进度*/
          mNowProgress = scrollLrcProgress + 100;

          /*如果播放状态 则暂停*/
          if (makeLyricsView.getLrcPlayerStatus() == AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
            mStopImageV.setVisibility(View.GONE);
            mPlayImageV.setVisibility(View.VISIBLE);
            mRestartTextV.setVisibility(View.VISIBLE);
            mAuditionTextV.setVisibility(View.VISIBLE);
            makeLyricsView.pause();
          }

          /*关闭自动滚动*/
          if (mProgressDp != null
              && !mProgressDp.isDisposed()) {
            mProgressDp.dispose();
            mStopImageV.setVisibility(View.GONE);
            mPlayImageV.setVisibility(View.VISIBLE);
            mRestartTextV.setVisibility(View.VISIBLE);
            mAuditionTextV.setVisibility(View.VISIBLE);
          }
          mCustomRecordDragView.toolStopBuildData();
        } else {
          /*更新seekbar/滑动到最后停止刷新*/
          if (makeLyricsView.getLrcPlayerStatus() == AbstractLrcView.LRCPLAYERSTATUS_PLAY) {

            Observable.just("")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                  @Override
                  public void accept(String s) throws Exception {
                    mLyricsSeekBar.setProgress((int) (makeLyricsView.getmPlayerSpendTime() + makeLyricsView.getmCurPlayingTime() + 100));
                  }
                });
            if (makeLyricsView.getmPlayerSpendTime() + makeLyricsView.getmCurPlayingTime()
                >= makeLyricsView.toolGetTotalTime()) {
              makeLyricsView.setmLrcPlayerStatus(AbstractLrcView.LRCPLAYERSTATUS_INIT);
              mStopImageV.setVisibility(View.GONE);
              mPlayImageV.setVisibility(View.VISIBLE);
              mRestartTextV.setVisibility(View.VISIBLE);
              mAuditionTextV.setVisibility(View.VISIBLE);
              mCustomRecordDragView.toolStopBuildData();
            }
          }
          mOriginalSingTex.setText(makeLyricsView.getLineLrc((int) (makeLyricsView.getmPlayerSpendTime() + makeLyricsView.getmCurPlayingTime() + 100)));
        }

      }
    });

    /*拖动条监听*/
    mLyricsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (makeLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
          makeLyricsView.seekto(progress);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {

      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {


      }
    });
    /*自定义拖动条*/
    mCustomRecordDragView.toolSendTimeProgressCallB(new MCustomRecordDragView.JTimeProgressCallB() {
      @Override
      public void toolProgress(long mProgress) {
        if (makeLyricsView.getLrcPlayerStatus() != AbstractLrcView.LRCPLAYERSTATUS_PLAY) {
          makeLyricsView.seekto((int) mProgress);
        }
      }
    });

    /*正則改變*/
    RxTextView.textChanges(mRegularEditT)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<CharSequence>() {
          @Override
          public void accept(CharSequence charSequence) throws Exception {
            if(charSequence.length()>0){
              makeLyricsView.toolSetData(mDataText,charSequence.toString(),true);
              makeLyricsView.toolChangeData();
            }

          }
        });
  }

  /*倒计时调度*/private Disposable mRecordCountdownDp;
  /*进度调度*/private Disposable mProgressDp;

  /**
   * 录音倒计时
   */
  @SuppressLint("CheckResult")
  private void toolRecordCountdown() {
    /*倒计时开始录音*/
    mRecordCountdownDp = Observable.interval(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe(new Consumer<Disposable>() {
          @Override
          public void accept(Disposable disposable) throws Exception {
            mRecordImageV.setEnabled(false);
          }
        })
        .doFinally(new Action() {
          @Override
          public void run() throws Exception {
          }
        })
        .subscribe(new Consumer<Long>() {
          @Override
          public void accept(Long aLong) throws Exception {
            mCountdownTextV.setText(3 - aLong.intValue() + "s");
            if (3 - aLong <= 0) {
              /*震动一下*/
              mRecordImageV.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS
                  , HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
              mRecordImageV.setVisibility(View.GONE);
              mStopImageV.setVisibility(View.VISIBLE);
              mRecordCountdownDp.dispose();
              makeLyricsView.play(mNowProgress);
              mIsPlay = true;
              mCustomRecordDragView.toolBuildData();
            }
          }
        });
  }

  /*当前进度值*/private int mNowProgress;

  /**
   * 歌词开始滚动
   */
  private void toolLyricsScroll() {
    /*定时刷新歌词*/
    mProgressDp = Observable.interval(0, mPlaySpeed, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<Long>() {
          @SuppressLint("CheckResult")
          @Override
          public void accept(Long aLong) throws Exception {
            /*要精准控制需计算具体的时间进度从未确认累加值*/
            /*推进给进度*/
            makeLyricsView.seekto((int) (mBeforeAccelerateProgress + (aLong * makeLyricsView.getmPlaySpeed())));
            // makeLyricsView.play(aLong.intValue()*100);
            /*提取当前进度的文本*/
            mOriginalSingTex.setText(makeLyricsView.getLineLrc(mBeforeAccelerateProgress + aLong.intValue() * makeLyricsView.getmPlaySpeed()));
            /*推进拖动进度条*/
            // mLyricsSeekBar.setProgress((int) (aLong.intValue() * 1000));
            /*保存当前进度*/
            mNowProgress = (int) (aLong.intValue() * 1000);
            if (makeLyricsView.getLrcPlayerStatus() == LRCPLAYERSTATUS_SEEKTO) {

              Observable.just("")
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                      mLyricsSeekBar.setProgress((int) (makeLyricsView.getmPlayerSpendTime() + makeLyricsView.getmCurPlayingTime() + 100));
                    }
                  });
              if (makeLyricsView.getmPlayerSpendTime() + makeLyricsView.getmCurPlayingTime()
                  >= makeLyricsView.toolGetTotalTime()) {
                makeLyricsView.setmLrcPlayerStatus(AbstractLrcView.LRCPLAYERSTATUS_INIT);
                if (mProgressDp != null && !mProgressDp.isDisposed()) {
                  mProgressDp.dispose();
                }
                mStopImageV.setVisibility(View.GONE);
                mPlayImageV.setVisibility(View.VISIBLE);
                mRestartTextV.setVisibility(View.VISIBLE);
                mAuditionTextV.setVisibility(View.VISIBLE);
                mCustomRecordDragView.toolStopBuildData();
              }
            }

          }
        });
  }

  List<String> mStrings = new ArrayList<>();
//  public int mTestNum = 9;
//
//  public native String stringFromJNI();
//
//  public native int toolAdditionFromJNI(int mOne, int mTwo);

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mRecordCountdownDp != null
        && !mRecordCountdownDp.isDisposed()) {
      mRecordCountdownDp.dispose();
    }
  }


}

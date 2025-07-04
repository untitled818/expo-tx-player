package com.tencent.liteav.demo.superplayer;

import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_AUTO_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_MANUAL_PLAY;
import static com.tencent.liteav.demo.superplayer.SuperPlayerModel.PLAY_ACTION_PRELOAD;

import expo.modules.txplayer.PipPlayerActivity;
import expo.modules.txplayer.R;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tencent.liteav.demo.superplayer.helper.ContextUtils;
import com.tencent.liteav.demo.superplayer.helper.IntentUtils;
import com.tencent.liteav.demo.superplayer.helper.PictureInPictureHelper;
import com.tencent.liteav.demo.superplayer.model.ISuperPlayerListener;
import com.tencent.liteav.demo.superplayer.model.SuperPlayer;
import com.tencent.liteav.demo.superplayer.model.SuperPlayerImpl;
import com.tencent.liteav.demo.superplayer.model.SuperPlayerObserver;
import com.tencent.liteav.demo.superplayer.model.VipWatchModel;
import com.tencent.liteav.demo.superplayer.model.entity.DynamicWaterConfig;
import com.tencent.liteav.demo.superplayer.model.entity.PlayImageSpriteInfo;
import com.tencent.liteav.demo.superplayer.model.entity.PlayKeyFrameDescInfo;
import com.tencent.liteav.demo.superplayer.model.entity.VideoQuality;
import com.tencent.liteav.demo.superplayer.model.utils.NetWatcher;
import com.tencent.liteav.demo.superplayer.permission.PermissionManager;
import com.tencent.liteav.demo.superplayer.ui.helper.VolumeChangeHelper;
import com.tencent.liteav.demo.superplayer.ui.player.FloatPlayer;
import com.tencent.liteav.demo.superplayer.ui.player.FullScreenPlayer;
import com.tencent.liteav.demo.superplayer.ui.player.Player;
import com.tencent.liteav.demo.superplayer.ui.player.WindowPlayer;
import com.tencent.liteav.demo.superplayer.ui.view.DanmuView;
import com.tencent.liteav.demo.superplayer.ui.view.DynamicWatermarkView;
import com.tencent.liteav.txcplayer.model.TXSubtitleRenderModel;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.TXTrackInfo;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.rtmp.ui.TXSubtitleView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Super player view
 * <p>
 * It has basic player functions, as well as functions such as screen
 * orientation switching, floating window playback,
 * video quality switching, hardware acceleration, speed playback, mirror
 * playback, and gesture control.
 * It supports both live and VOD. The usage is extremely simple.
 * Just import and get this control in the layout file, and pass in
 * {@link SuperPlayerModel} through
 * {@link #playWithModelNeedLicence(SuperPlayerModel)} to achieve video
 * playback.
 * <p>
 * 1. Play video {@link #playWithModelNeedLicence(SuperPlayerModel)}
 * 2. Set callback {@link #setPlayerViewCallback(OnSuperPlayerViewCallback)}
 * 3. Controller callback implementation {@link #mControllerCallback}
 * 4. Exit playback to release memory {@link #resetPlayer()}
 *
 * 超级播放器view
 * <p>
 * 具备播放器基本功能，此外还包括横竖屏切换、悬浮窗播放、画质切换、硬件加速、倍速播放、镜像播放、手势控制等功能，同时支持直播与点播
 * 使用方式极为简单，只需要在布局文件中引入并获取到该控件，通过{@link #playWithModelNeedLicence(SuperPlayerModel)}传入
 * {@link SuperPlayerModel}即可实现视频播放
 * <p>
 * 1、播放视频{@link #playWithModelNeedLicence(SuperPlayerModel)}
 * 2、设置回调{@link #setPlayerViewCallback(OnSuperPlayerViewCallback)}
 * 3、controller回调实现{@link #mControllerCallback}
 * 4、退出播放释放内存{@link #resetPlayer()}
 */
public class SuperPlayerView extends RelativeLayout
    implements PermissionManager.OnStoragePermissionGrantedListener,
    PictureInPictureHelper.OnPictureInPictureClickListener,
    VolumeChangeHelper.VolumeChangeListener {
  private static final String TAG = "SuperPlayerView";
  private static final int OP_SYSTEM_ALERT_WINDOW = 24;

  private Context mContext;
  private ViewGroup mRootView;
  private TXCloudVideoView mTXCloudVideoView;
  private FullScreenPlayer mFullScreenPlayer;
  private WindowPlayer mWindowPlayer;
  private FloatPlayer mFloatPlayer;
  private DanmuView mDanmuView;
  private ViewGroup.LayoutParams mLayoutParamWindowMode;
  private ViewGroup.LayoutParams mLayoutParamFullScreenMode;
  private LayoutParams mVodControllerWindowParams;
  private LayoutParams mVodControllerFullScreenParams;
  private WindowManager mWindowManager;
  private WindowManager.LayoutParams mWindowParams;
  private OnSuperPlayerViewCallback mPlayerViewCallback;
  private NetWatcher mWatcher;
  private SuperPlayer mSuperPlayer;
  private SuperPlayerModel mCurrentSuperPlayerModel;
  private int mPlayAction;
  private int mPlayIndex;
  private boolean mIsLoopPlayList;
  private List<SuperPlayerModel> mSuperPlayerModelList;
  private long mDuration;
  private long mProgress;
  private boolean mIsPlayInit;
  private boolean isCallResume = false;
  private LinearLayout mDynamicWatermarkLayout;
  private DynamicWatermarkView mDynamicWatermarkView;
  private ISuperPlayerListener mSuperPlayerListener;
  private PermissionManager mStoragePermissionManager;
  private TXSubtitleView mSubtitleView;
  private VolumeChangeHelper mVolumeChangeHelper;
  private PictureInPictureHelper mPictureInPictureHelper;
  private long mPlayAble;
  private float mPlayableDuration = 0f;

  public SuperPlayerView(Context context) {
    super(context);
    initialize(context);
  }

  public SuperPlayerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  public SuperPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  private void initialize(Context context) {
    mContext = context;
    initView();
    initPlayer();
  }

  private void initView() {
    mRootView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.superplayer_vod_view, null);
    mTXCloudVideoView = (TXCloudVideoView) mRootView.findViewById(R.id.superplayer_cloud_video_view);
    mFullScreenPlayer = (FullScreenPlayer) mRootView.findViewById(R.id.superplayer_controller_large);
    mWindowPlayer = (WindowPlayer) mRootView.findViewById(R.id.superplayer_controller_small);
    mFloatPlayer = (FloatPlayer) mRootView.findViewById(R.id.superplayer_controller_float);
    mDanmuView = (DanmuView) mRootView.findViewById(R.id.superplayer_danmuku_view);
    mSubtitleView = (TXSubtitleView) mRootView.findViewById(R.id.subtitle_view);

    mSuperPlayerModelList = new ArrayList<>();
    mDynamicWatermarkLayout = mRootView.findViewById(R.id.superplayer_dynamic_watermark_layout);
    mDynamicWatermarkView = mRootView.findViewById(R.id.superplayer_dynamic_watermark);

    mVodControllerWindowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    mVodControllerFullScreenParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

    mFullScreenPlayer.setCallback(mControllerCallback);
    mWindowPlayer.setCallback(mControllerCallback);
    mFloatPlayer.setCallback(mControllerCallback);

    removeAllViews();
    mRootView.removeView(mDanmuView);
    mRootView.removeView(mTXCloudVideoView);
    mRootView.removeView(mWindowPlayer);
    mRootView.removeView(mFullScreenPlayer);
    mRootView.removeView(mFloatPlayer);
    mRootView.removeView(mDynamicWatermarkLayout);
    mRootView.removeView(mSubtitleView);

    addView(mTXCloudVideoView);
    addView(mDynamicWatermarkLayout);
    addView(mDanmuView);
    addView(mSubtitleView);
    mStoragePermissionManager = new PermissionManager(getContext(), PermissionManager.PermissionType.STORAGE);
    mStoragePermissionManager.setOnStoragePermissionGrantedListener(this);
    if (mContext instanceof PipPlayerActivity) {
      mPictureInPictureHelper = new PictureInPictureHelper(mContext);
      mPictureInPictureHelper.setListener(this);
    }
  }

  private void initPlayer() {
    mSuperPlayer = new SuperPlayerImpl(mContext, mTXCloudVideoView);
    mSuperPlayer.setObserver(new PlayerObserver());
    mSuperPlayer.setSubTitleView(mSubtitleView);
    if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.FULLSCREEN) {
      addView(mFullScreenPlayer);
      mFullScreenPlayer.post(() -> {
        mFullScreenPlayer.hide();
      });
    } else if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.WINDOW) {
      addView(mWindowPlayer);
      mWindowPlayer.post(() -> {
        mWindowPlayer.hide();
      });
    }

    post(new Runnable() {
      @Override
      public void run() {
        if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.WINDOW) {
          mLayoutParamWindowMode = getLayoutParams();
        }
        try {
          Class parentLayoutParamClazz = getLayoutParams().getClass();
          Constructor constructor = parentLayoutParamClazz.getDeclaredConstructor(int.class, int.class);
          mLayoutParamFullScreenMode = (ViewGroup.LayoutParams) constructor.newInstance(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT);
          Log.d(TAG, "mLayoutParamFullScreenMode = width: "
              + mLayoutParamFullScreenMode.width + ", height: " + mLayoutParamFullScreenMode.height);

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    if (mWatcher == null) {
      mWatcher = new NetWatcher(mContext);
    }

    mVolumeChangeHelper = new VolumeChangeHelper(mContext);
    mVolumeChangeHelper.registerVolumeChangeListener(this);
  }

  /**
   *
   * Play video list
   * Note: Starting from version 10.7, you need to set the License through
   * {@link com.tencent.rtmp.TXLiveBase#setLicence} to play successfully.
   * Otherwise, the playback will fail
   * (black screen). Set it once globally.
   * Live License, short video License and video playback License can all be used.
   * If you have not obtained the
   * above License, you can
   * <a href="https://www.tencentcloud.com/zh/document/product/266/51098">quickly
   * and freely apply
   * for License</a> to play normally.
   * 
   * @param models         SuperPlayerModel list
   * @param isLoopPlayList Whether to loop
   * @param index          The index of the video to start playing
   *
   *
   *                       播放视频列表
   *
   *                       注意：10.7版本开始，需要通过{@link com.tencent.rtmp.TXLiveBase#setLicence}
   *                       设置 License后方可成功播放， 否则将播放失败
   *                       （黑屏），全局仅设置一次即可。
   *                       直播License、短视频Licence和视频播放Licence均可使用，若您暂未获取上述Licence，可
   *                       <a href=
   *                       "https://cloud.tencent.com/document/product/881/74588#.E8.B4.AD.E4.B9.B0.E5.B9.B6.E6.96.B0.E5.BB.BA.E6.AD.A3.E5.BC.8F.E7.89.88-license">快速免费申请Licence</a>以正常播放
   * @param models         superPlayerModel列表
   * @param isLoopPlayList 是否循环
   * @param index          开始播放的视频索引
   */
  public void playWithModelListNeedLicence(List<SuperPlayerModel> models, boolean isLoopPlayList, int index) {
    mSuperPlayerModelList = models;
    mIsLoopPlayList = isLoopPlayList;
    playModelInList(index);
  }

  /**
   * Play video
   * Note: Starting from version 10.7, you need to set the License through
   * {@link com.tencent.rtmp.TXLiveBase#setLicence} to play successfully.
   * Otherwise, the playback will fail
   * (black screen). Set it once globally.
   * Live License, short video License and video playback License can all be used.
   * If you have not obtained
   * the above License, you can
   * <a href="https://www.tencentcloud.com/zh/document/product/266/51098">quickly
   * and freely apply for
   * License</a> to play normally.
   * 
   * @param model Play data model
   *
   *              播放视频
   *              注意：10.7版本开始，需要通过{@link com.tencent.rtmp.TXLiveBase#setLicence}
   *              设置 Licence后方可成功播放， 否则将播放失败（黑屏）
   *              ，全局仅设置一次即可。
   *              直播License、短视频Licence和视频播放Licence均可使用，若您暂未获取上述Licence，可
   *              <a
   *              href="https://cloud.tencent.com/document/product/881/74588#.E8.B4.AD.E4.B9.B0.E5.B9.B6.E6.96.B0.E5.BB.BA.E6.AD.A3.E5.BC.8F.E7.89.88-license>快速免费申请Licence</a>以正常播放
   * @param model 播放数据模型
   */
  public void playWithModelNeedLicence(SuperPlayerModel model) {
    isCallResume = false;
    mIsPlayInit = false;
    mSuperPlayer.stop();
    mIsLoopPlayList = false;
    mWindowPlayer.setPlayNextButtonVisibility(false);
    mFullScreenPlayer.setPlayNextButtonVisibility(false);
    mSuperPlayerModelList.clear();
    mCurrentSuperPlayerModel = model;
    playWithModelInner(mCurrentSuperPlayerModel);
    mIsPlayInit = true;
  }

  private void playModelInList(int index) {
    mIsPlayInit = false;
    mSuperPlayer.stop();
    mPlayIndex = index;
    if (mSuperPlayerModelList.size() > 1) {
      mWindowPlayer.setPlayNextButtonVisibility(true);
      mFullScreenPlayer.setPlayNextButtonVisibility(true);
    } else if (mSuperPlayerModelList.size() == 1) {
      mWindowPlayer.setPlayNextButtonVisibility(false);
      mFullScreenPlayer.setPlayNextButtonVisibility(false);
    }
    mCurrentSuperPlayerModel = mSuperPlayerModelList.get(mPlayIndex);
    playWithModelInner(mCurrentSuperPlayerModel, false);
    mIsPlayInit = true;
  }

  private void playWithModelInner(SuperPlayerModel model) {
    playWithModelInner(model, true);
  }

  private void playWithModelInner(SuperPlayerModel model, boolean needChangeUI) {
    if (needChangeUI) {
      // 默认不展示 pip 图标
      // mWindowPlayer.showPIPIV(false);
      mWindowPlayer.showPIPIV(model.vipWatchMode == null && TextUtils.isEmpty(model.coverPictureUrl));
    }
    mPlayAction = mCurrentSuperPlayerModel.playAction;
    if (mPlayAction == PLAY_ACTION_AUTO_PLAY || mPlayAction == PLAY_ACTION_PRELOAD) {
      mSuperPlayer.play(model);
    } else {
      mSuperPlayer.reset();
    }
    mFullScreenPlayer.preparePlayVideo(model);
    mWindowPlayer.preparePlayVideo(model);

    boolean isShowDownloadView = model.isEnableCache && (model.videoId != null || model.videoIdV2 != null);
    mFullScreenPlayer.updateDownloadViewShow(isShowDownloadView);
    mFullScreenPlayer.setVipWatchModel(model.vipWatchMode);
    mWindowPlayer.setVipWatchModel(model.vipWatchMode);
    mFloatPlayer.setVipWatchModel(model.vipWatchMode);
    mDynamicWatermarkView.setData(model.dynamicWaterConfig);
    mDynamicWatermarkView.hide();
  }

  /**
   * Set the VipWatchModel data. Pass in null to hide the displayed VIP page.
   *
   * 设置VipWatchModel 数据，传入null可隐藏掉展示的VIP页面
   *
   * @param vipWatchModel
   */
  public void setVipWatchModel(VipWatchModel vipWatchModel) {
    mFullScreenPlayer.setVipWatchModel(vipWatchModel);
    mWindowPlayer.setVipWatchModel(vipWatchModel);
    mFloatPlayer.setVipWatchModel(vipWatchModel);
  }

  /**
   * Set the configuration information for dynamic watermark.
   *
   * 设置动态水印的配置信息
   *
   * @param dynamicWaterConfig
   */
  public void setDynamicWatermarkConfig(DynamicWaterConfig dynamicWaterConfig) {
    mDynamicWatermarkView.setData(dynamicWaterConfig);
    mDynamicWatermarkView.hide();
  }

  /**
   * Update the title
   * 
   * @param title Video name
   *
   *              更新标题
   *
   * @param title 视频名称
   */
  private void updateTitle(String title) {
    mWindowPlayer.updateTitle(title);
    mFullScreenPlayer.updateTitle(title);
  }

  /**
   * Used to determine whether the VIP preview page has been displayed.
   *
   * 用于判断VIP试看页面是否已经展示出来了
   */
  public boolean isShowingVipView() {
    return mFullScreenPlayer.isShowingVipView()
        || mWindowPlayer.isShowingVipView()
        || mFloatPlayer.isShowingVipView();
  }

  /**
   * Resume lifecycle callback.
   *
   * resume生命周期回调
   */
  public void onResume() {
    if (mDanmuView != null && mDanmuView.isPrepared() && mDanmuView.isPaused()) {
      mDanmuView.resume();
    }
    if (mPlayAction == PLAY_ACTION_MANUAL_PLAY && mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
      return;
    }
    mSuperPlayer.resume();
    isCallResume = true;
    if (null != mFullScreenPlayer) {
      mFullScreenPlayer.checkIsNeedRefreshCacheMenu();
    }
  }

  /**
   * Pause lifecycle callback.
   *
   * pause生命周期回调
   */
  public void onPause() {
    if (mDanmuView != null && mDanmuView.isPrepared()) {
      mDanmuView.pause();
    }
    if (mPlayAction == PLAY_ACTION_MANUAL_PLAY && mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
      return;
    }
    mSuperPlayer.pauseVod();
  }

  /**
   * Reset the player.
   *
   * 重置播放器
   */
  public void resetPlayer() {
    mSuperPlayerModelList.clear();
    if (mDanmuView != null) {
      mDanmuView.release();
      mDanmuView = null;
    }
    stopPlay();
  }

  /**
   * Pause the barrage in the feed stream requirement
   *
   * 在feed 流需求中使用，将弹幕 暂停
   */
  public void revertUI() {
    if (mDanmuView != null) {
      mDanmuView.toggle(false);
      mDanmuView.removeAllDanmakus(true);
    }
    mSuperPlayer.revertSettings();
    mFullScreenPlayer.revertUI();
    if (mDynamicWatermarkView != null) {
      mDynamicWatermarkView.hide();
    }
  }

  // 关闭弹幕：停止发送 + 清空 UI 弹幕
  public void closeDanmu() {
    mFullScreenPlayer.mBarrageOn = false;
    if (mDanmuView != null) {
      mDanmuView.toggle(false); // 停止发送
      mDanmuView.removeAllDanmakus(true); // 清空屏幕弹幕
    }
  }

  // 清除弹幕
  public void removeAllDanmakus() {
    mDanmuView.removeAllDanmakus(true); // 清空屏幕弹幕
    // mDanmuView.reprepareDanmaku();
    // mDanmuView = (DanmuView)
    // mRootView.findViewById(R.id.superplayer_danmuku_view);
  }

  // 开启弹幕：允许发送
  public void openDanmu() {
    // mFullScreenPlayer.mBarrageOn = true;
    mFullScreenPlayer.toggleBarrage();
    if (mDanmuView != null) {
      mDanmuView.toggle(true); // 继续发送
    }
  }

  // 发送弹幕
  public void sendDanmu(String content, boolean withBorder) {
    if (!mFullScreenPlayer.mBarrageOn || mDanmuView == null)
      return;
    mDanmuView.addDanmaku(content, withBorder);
  }

  /**
   * Stop playback.
   *
   * 停止播放
   */
  public void stopPlay() {
    mSuperPlayer.stop();
    if (mWatcher != null) {
      mWatcher.stop();
    }
  }

  public void seek(float position) {
    mSuperPlayer.seek((int) position);
  }

  /**
   * Set the callback for the SuperPlayer.
   *
   * 设置超级播放器的回调
   *
   * @param callback
   */
  public void setPlayerViewCallback(OnSuperPlayerViewCallback callback) {
    mPlayerViewCallback = callback;

    // 把回调传进去 SuperPlayerImpl 里
    if (mSuperPlayer != null) {
      mSuperPlayer.setPlayerViewCallback(callback); // 👈 重点
    }
  }

  /**
   * Set the callback for the VOD player and live player in the SuperPlayer
   *
   * 设置超级播放器中点播播放器和直播播放器的回调
   *
   * @param superPlayerListener
   */
  public void setSuperPlayerListener(ISuperPlayerListener superPlayerListener) {
    mSuperPlayerListener = superPlayerListener;
    if (mSuperPlayer != null) {
      mSuperPlayer.setSuperPlayerListener(mSuperPlayerListener);
    }
  }

  /**
   * Control whether to display in full screen.
   *
   * 控制是否全屏显示
   */
  private void fullScreen(boolean isFull) {
    Log.d(TAG, "fullScreen: " + isFull);
    if (getContext() instanceof Activity activity) {
      if (isFull) {
        View decorView = activity.getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
          decorView.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
          int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
          decorView.setSystemUiVisibility(uiOptions);
          ((Activity) getContext()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
      } else {
        View decorView = activity.getWindow().getDecorView();
        if (decorView == null)
          return;
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
          decorView.setSystemUiVisibility(View.VISIBLE);
        } else if (Build.VERSION.SDK_INT >= 19) {
          decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
      }
    }
  }

  /**
   * Hide or show the back button in window mode, which is displayed by default
   *
   * 隐藏或展示窗口模式下的返回按钮，默认是展示的
   *
   * @param isShow
   */
  public void showOrHideBackBtn(boolean isShow) {
    if (mWindowPlayer != null) {
      mWindowPlayer.showOrHideBackBtn(isShow);
      mWindowPlayer.showPIPIV(false);
    }
  }

  private void onSwitchFullMode(SuperPlayerDef.PlayerMode playerMode) {
    if (mLayoutParamFullScreenMode == null) {
      return;
    }
    removeView(mWindowPlayer);
    addView(mFullScreenPlayer, mVodControllerFullScreenParams);
    setLayoutParams(mLayoutParamFullScreenMode);
    Log.d(TAG, "Applying full screen layout params: width = "
        + mLayoutParamFullScreenMode.width + ", height = " + mLayoutParamFullScreenMode.height);
    setLayoutParams(mLayoutParamFullScreenMode);

    if (mPlayerViewCallback != null) {
      mPlayerViewCallback.onStartFullScreenPlay();
    }
    rotateScreenOrientation(SuperPlayerDef.Orientation.LANDSCAPE);
    mSuperPlayer.switchPlayMode(playerMode);
  }

  private void onSwitchWindowMode(SuperPlayerDef.PlayerMode playerMode) {
    if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.FLOAT) {
      try {
        Context viewContext = getContext();
        Intent intent = null;
        if (viewContext instanceof Activity) {
          intent = new Intent(viewContext, viewContext.getClass());
        } else {
          showToast(R.string.superplayer_float_play_fail);
          return;
        }
        IntentUtils.safeStartActivity(mContext, intent);
        mSuperPlayer.pause();
        if (mLayoutParamWindowMode == null) {
          return;
        }
        mFloatPlayer.removeDynamicWatermarkView();
        mDynamicWatermarkLayout.addView(mDynamicWatermarkView);
        mWindowManager.removeView(mFloatPlayer);
        mSuperPlayer.setPlayerView(mTXCloudVideoView);
        if (!isShowingVipView()) { // Do not perform resume operation when the preview function is displayed.
          mSuperPlayer.resume();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } else if (mSuperPlayer.getPlayerMode() == SuperPlayerDef.PlayerMode.FULLSCREEN) {
      if (mLayoutParamWindowMode == null) {
        return;
      }
      WindowManager.LayoutParams attrs = ((Activity) getContext()).getWindow().getAttributes();
      attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
      ((Activity) getContext()).getWindow().setAttributes(attrs);
      ((Activity) getContext()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
      removeView(mFullScreenPlayer);
      addView(mWindowPlayer, mVodControllerWindowParams);
      setLayoutParams(mLayoutParamWindowMode);
      rotateScreenOrientation(SuperPlayerDef.Orientation.PORTRAIT);
      if (mPlayerViewCallback != null) {
        mPlayerViewCallback.onStopFullScreenPlay();
      }
    }
    mSuperPlayer.switchPlayMode(playerMode);
  }

  private void onSwitchFloatMode(SuperPlayerDef.PlayerMode playerMode) {
    SuperPlayerGlobalConfig prefs = SuperPlayerGlobalConfig.getInstance();
    if (!prefs.enableFloatWindow) {
      return;
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 6.0动态申请悬浮窗权限
      if (!Settings.canDrawOverlays(mContext)) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + mContext.getPackageName()));
        IntentUtils.safeStartActivity(mContext, intent);
        return;
      }
    } else {
      if (!checkOp(mContext, OP_SYSTEM_ALERT_WINDOW)) {
        showToast(R.string.superplayer_enter_setting_fail);
        return;
      }
    }
    mSuperPlayer.pause();

    mWindowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    mWindowParams = new WindowManager.LayoutParams();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      mWindowParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      mWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
    }
    mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    mWindowParams.format = PixelFormat.TRANSLUCENT;
    mWindowParams.gravity = Gravity.LEFT | Gravity.TOP;

    SuperPlayerGlobalConfig.TXRect rect = prefs.floatViewRect;
    mWindowParams.x = rect.x;
    mWindowParams.y = rect.y;
    mWindowParams.width = rect.width;
    mWindowParams.height = rect.height;
    try {
      mWindowManager.addView(mFloatPlayer, mWindowParams);
    } catch (Exception e) {
      showToast(R.string.superplayer_float_play_fail);
      return;
    }
    mDynamicWatermarkLayout.removeAllViews();
    mFloatPlayer.addDynamicWatermarkView(mDynamicWatermarkView);
    TXCloudVideoView videoView = mFloatPlayer.getFloatVideoView();
    if (videoView != null) {
      mSuperPlayer.setPlayerView(videoView);
      mSuperPlayer.resume();
    }
    mSuperPlayer.switchPlayMode(playerMode);
  }

  private void handleSwitchPlayMode(SuperPlayerDef.PlayerMode playerMode) {
    Log.d(TAG, "handleSwitchPlayMode: " + playerMode);
    fullScreen(playerMode == SuperPlayerDef.PlayerMode.FULLSCREEN);
    this.postDelayed(() -> {
      Log.d(TAG, "Delayed hide views for mode: " + playerMode);
      mFullScreenPlayer.hide();
      mWindowPlayer.hide();
      mFloatPlayer.hide();
    }, 300); // 你可以根据测试情况调整延迟时间，比如 500ms
    if (playerMode == SuperPlayerDef.PlayerMode.FULLSCREEN) {
      onSwitchFullMode(playerMode);
    } else if (playerMode == SuperPlayerDef.PlayerMode.WINDOW) {
      onSwitchWindowMode(playerMode);
    } else if (playerMode == SuperPlayerDef.PlayerMode.FLOAT) {
      onSwitchFloatMode(playerMode);
    }
  }

  private final Player.Callback mControllerCallback = new Player.Callback() {
    @Override
    public void onSwitchPlayMode(SuperPlayerDef.PlayerMode playerMode) {
      Log.d(TAG, "onSwitchPlayMode: " + playerMode);
      handleSwitchPlayMode(playerMode);
    }

    @Override
    public void onBackPressed(SuperPlayerDef.PlayerMode playMode) {
      switch (playMode) {
        case FULLSCREEN:// 当前是全屏模式，返回切换成窗口模式 Switch to window mode when returning from full screen mode.
          onSwitchPlayMode(SuperPlayerDef.PlayerMode.WINDOW);
          break;
        case WINDOW:// 当前是窗口模式，返回退出播放器 Exit the player when returning from window mode.
          if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onClickSmallReturnBtn();
          }
          break;
        case FLOAT:// 当前是悬浮窗，退出 Exit the floating window
          mWindowManager.removeView(mFloatPlayer);
          if (mPlayerViewCallback != null) {
            mPlayerViewCallback.onClickFloatCloseBtn();
          }
          break;
        default:
          break;
      }
    }

    @Override
    public void onFloatPositionChange(int x, int y) {
      mWindowParams.x = x;
      mWindowParams.y = y;
      mWindowManager.updateViewLayout(mFloatPlayer, mWindowParams);
    }

    @Override
    public void onPause() {
      mSuperPlayer.pause();
      if (mSuperPlayer.getPlayerType() != SuperPlayerDef.PlayerType.VOD) {
        if (mWatcher != null) {
          mWatcher.stop();
        }
      }
    }

    @Override
    public void onResume() {
      handleResume();
    }

    @Override
    public void onSeekTo(int position) {
      mSuperPlayer.seek(position);
    }

    @Override
    public void onResumeLive() {
      mSuperPlayer.resumeLive();
    }

    @Override
    public void onDanmuToggle(boolean isOpen) {
      if (mDanmuView != null) {
        mDanmuView.toggle(isOpen);
        mDanmuView.removeAllDanmakus(true);
      }
    }

    @Override
    public void onSnapshot() {
      mStoragePermissionManager.checkoutIfShowPermissionIntroductionDialog();
    }

    @Override
    public void onQualityChange(VideoQuality quality) {
      mFullScreenPlayer.updateVideoQuality(quality);
      mSuperPlayer.switchStream(quality);
    }

    @Override
    public void onSpeedChange(float speedLevel) {
      mSuperPlayer.setRate(speedLevel);
    }

    @Override
    public void onMirrorToggle(boolean isMirror) {
      mSuperPlayer.setMirror(isMirror);
    }

    @Override
    public void onHWAccelerationToggle(boolean isAccelerate) {
      mSuperPlayer.enableHardwareDecode(isAccelerate);
    }

    @Override
    public void onClickHandleVip() {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse("https://cloud.tencent.com/document/product/454/18872"));
      if (getContext() instanceof Activity) {
        IntentUtils.safeStartActivity(getContext(), intent);
      }
    }

    @Override
    public void onClickVipTitleBack(SuperPlayerDef.PlayerMode playerMode) {
      if (playerMode == SuperPlayerDef.PlayerMode.FULLSCREEN) {
        mFullScreenPlayer.hideVipView();
        return;
      }
      mFullScreenPlayer.hideVipView();
      mWindowPlayer.hideVipView();
      mFloatPlayer.hideVipView();
    }

    @Override
    public void onClickVipRetry() {
      mControllerCallback.onSeekTo(0);
      mControllerCallback.onResume();
      postDelayed(new Runnable() {
        @Override
        public void run() {
          mFullScreenPlayer.hideVipView();
          mWindowPlayer.hideVipView();
          mFloatPlayer.hideVipView();
        }
      }, 500);

    }

    @Override
    public void onCloseVipTip() {
      mFullScreenPlayer.hideTipView();
      mWindowPlayer.hideTipView();
      mFloatPlayer.hideTipView();
    }

    @Override
    public void playNext() {
      playNextVideo();
    }

    @Override
    public List<SuperPlayerModel> getPlayList() {
      if (null == mSuperPlayerModelList || mSuperPlayerModelList.isEmpty()) {
        return new ArrayList<SuperPlayerModel>() {
          {
            add(mCurrentSuperPlayerModel);
          }
        };
      }
      return mSuperPlayerModelList;
    }

    @Override
    public SuperPlayerModel getPlayingVideoModel() {
      return mCurrentSuperPlayerModel;
    }

    @Override
    public void onShowDownloadList() {
      if (null != mPlayerViewCallback) {
        mPlayerViewCallback.onShowCacheListClick();
      }
    }

    @Override
    public void onClickSoundTrackItem(TXTrackInfo clickInfo) {
      mSuperPlayer.onClickSoundTrackItem(clickInfo);
    }

    @Override
    public void onClickSubtitleItem(TXTrackInfo clickInfo) {
      mSuperPlayer.onClickSubTitleItem(clickInfo);
    }

    @Override
    public void onClickSubtitleViewDoneButton(TXSubtitleRenderModel model) {
      mSuperPlayer.onSubtitleSettingDone(model);
    }

    @Override
    public void enterPictureInPictureMode() {
      Log.d(TAG, "enterPictureInPictureMode: 1");
      if (mPlayerViewCallback != null) {
        mPlayerViewCallback.onEnterPictureInPicture(); // 让 Activity 来处理
      }
      // mPictureInPictureHelper.enterPictureInPictureMode(getPlayerState(),
      // mTXCloudVideoView);
    }

    @Override
    public void onPlayBackward() {
      mSuperPlayer.playBackward((int) mProgress);
    }

    @Override
    public void onPlayForward() {
      mSuperPlayer.playForward();
    }

    @Override
    public void onActionUp() {
      mSuperPlayer.revertSpeedRate();
    }

    @Override
    public void onCast() {
      if (mPlayerViewCallback != null) {
        mPlayerViewCallback.onCastButtonPressed();
      }
    }
  };

  private void handleResume() {
    if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.LOADING
        && mPlayAction == PLAY_ACTION_PRELOAD) {
      mSuperPlayer.resume();
    } else if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.INIT) {
      if (mPlayAction == PLAY_ACTION_PRELOAD) {
        mSuperPlayer.resume();
      } else if (mPlayAction == PLAY_ACTION_MANUAL_PLAY) {
        mSuperPlayer.play(mCurrentSuperPlayerModel);
      }
    } else if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.END
        || mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.ERROR) { // 重播 or 失败
      mSuperPlayer.reStart();
    } else if (mSuperPlayer.getPlayerState() == SuperPlayerDef.PlayerState.PAUSE) { // 继续播放
      mSuperPlayer.resume();
    }
  }

  private void playNextVideo() {
    if (!mIsLoopPlayList && (mPlayIndex == mSuperPlayerModelList.size() - 1)) {
      return;
    }
    mPlayIndex = (++mPlayIndex) % mSuperPlayerModelList.size();
    playModelInList(mPlayIndex);
  }

  /**
   * Display the screenshot window.
   *
   * 显示截图窗口
   *
   * @param bmp
   */
  private void showSnapshotWindow(final Bitmap bmp) {
    final PopupWindow popupWindow = new PopupWindow(mContext);
    popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
    popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    View view = LayoutInflater.from(mContext).inflate(R.layout.superplayer_layout_new_vod_snap, null);
    ImageView imageView = (ImageView) view.findViewById(R.id.superplayer_iv_snap);
    imageView.setImageBitmap(bmp);
    popupWindow.setContentView(view);
    popupWindow.setOutsideTouchable(true);
    popupWindow.showAtLocation(mRootView, Gravity.TOP, 1800, 300);
    AsyncTask.execute(new Runnable() {
      @Override
      public void run() {
        if (Build.VERSION.SDK_INT >= 30) {
          save2MediaStoreForAndroidQAbove(mContext, bmp);
        } else {
          save2MediaStore(mContext, bmp);
        }
      }
    });
    postDelayed(new Runnable() {
      @Override
      public void run() {
        popupWindow.dismiss();
      }
    }, 3000);
  }

  /**
   * Rotate the screen orientation
   *
   * 旋转屏幕方向
   */
  private void rotateScreenOrientation(SuperPlayerDef.Orientation orientation) {
    switch (orientation) {
      case LANDSCAPE:
        ((Activity) mContext)
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        break;
      case PORTRAIT:
        ((Activity) mContext)
            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
    }
  }

  /**
   * Check floating window permission
   * API <18, there is no need to process it by default. It cannot receive touch
   * and key events, and does
   * not require permissions and cannot receive touch event source code analysis.
   * API >= 19, can receive touch and key events
   * API >=23, you need to apply for permission in the manifest, and check whether
   * you have the permission
   * every time you need to use it, because the user can cancel it at any time.
   * API >25, TYPE_TOAST has been sanctioned by Google and will automatically
   * disappear.
   *
   * 检查悬浮窗权限
   * <p>
   * API <18，默认有悬浮窗权限，不需要处理。无法接收无法接收触摸和按键事件，不需要权限和无法接受触摸事件的源码分析
   * API >= 19 ，可以接收触摸和按键事件
   * API >=23，需要在manifest中申请权限，并在每次需要用到权限的时候检查是否已有该权限，因为用户随时可以取消掉。
   * API >25，TYPE_TOAST 已经被谷歌制裁了，会出现自动消失的情况
   */
  private boolean checkOp(Context context, int op) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
      try {
        Method method = AppOpsManager.class.getDeclaredMethod("checkOp", int.class, int.class, String.class);
        return AppOpsManager.MODE_ALLOWED == (int) method.invoke(manager, op, Binder.getCallingUid(),
            context.getPackageName());
      } catch (Exception e) {
        Log.e(TAG, Log.getStackTraceString(e));
      }
    }
    return true;
  }

  /**
   * Callback interface for SuperPlayerView.
   *
   * SuperPlayerView的回调接口
   */
  public interface OnSuperPlayerViewCallback {

    /**
     * Start full screen playback.
     *
     * 开始全屏播放
     */
    void onStartFullScreenPlay();

    /**
     * End full screen playback
     *
     * 结束全屏播放
     */
    void onStopFullScreenPlay();

    /**
     * Click the x button in floating window mode
     *
     * 点击悬浮窗模式下的x按钮
     */
    void onClickFloatCloseBtn();

    /**
     * Click the back button in small player mode
     *
     * 点击小播放模式的返回按钮
     */
    void onClickSmallReturnBtn();

    /**
     * Start floating window playback
     * <p>
     * 开始悬浮窗播放
     */
    default void onStartFloatWindowPlay() {

    }

    /**
     * Playback start callback
     *
     * 开始播放回调
     */
    void onPlaying();

    /**
     * Playback end
     *
     * 播放结束
     */
    void onPlayEnd();

    /**
     * Callback when playback fails
     *
     * 当播放失败的时候回调
     *
     * @param code
     */
    void onError(int code, String message);

    /**
     * Clicked on the cache list button on the download page.
     *
     * 下载页面，点击了缓存列表按钮
     */
    void onShowCacheListClick();

    /**
     * pip回调
     */
    void onEnterPictureInPicture();

    void onExitPictureInPicture();

    /**
     * 播放状态变更回调
     * 
     * @param status
     */
    void onStatusChange(String status);

    /**
     * 视频是否正在播放回调
     */

    void onPlayingChange(Boolean isPlaying);

    void onCastButtonPressed();

  }

  public void release() {
    if (mVolumeChangeHelper != null) {
      mVolumeChangeHelper.unRegisterVolumeChangeListener();
    }
    if (mPictureInPictureHelper != null) {
      mPictureInPictureHelper.release();
    }
    if (mWindowPlayer != null) {
      mWindowPlayer.release();
    }
    if (mFullScreenPlayer != null) {
      mFullScreenPlayer.release();
    }
    if (mFloatPlayer != null) {
      mFloatPlayer.release();
    }
    if (mDynamicWatermarkView != null) {
      mDynamicWatermarkView.release();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    try {
      release();
    } catch (Throwable e) {
      Log.e(TAG, Log.getStackTraceString(e));
    }
  }

  public void mPictureInPictureHelperEnterPIP() {
    mPictureInPictureHelper.enterPictureInPictureMode(getPlayerState(), mTXCloudVideoView);
  }

  public void switchPlayMode(SuperPlayerDef.PlayerMode playerMode) {

    if (playerMode == SuperPlayerDef.PlayerMode.WINDOW) {

      if (mControllerCallback != null) {

        mControllerCallback.onSwitchPlayMode(SuperPlayerDef.PlayerMode.WINDOW);
      }
    } else if (playerMode == SuperPlayerDef.PlayerMode.FLOAT) {

      if (mPlayerViewCallback != null) {

        mPlayerViewCallback.onStartFloatWindowPlay();
      }
      if (mControllerCallback != null) {

        mControllerCallback.onSwitchPlayMode(SuperPlayerDef.PlayerMode.FLOAT);
      }
    }
  }

  public SuperPlayerDef.PlayerMode getPlayerMode() {
    return mSuperPlayer.getPlayerMode();
  }

  public SuperPlayerDef.PlayerState getPlayerState() {
    return mSuperPlayer.getPlayerState();
  }

  public SuperPlayerModel getCurrentSuperPlayerModel() {
    return mCurrentSuperPlayerModel;
  }

  private void actonOfPreloadOnPlayPrepare() {
    if (mPlayAction != PLAY_ACTION_PRELOAD) {
      mWindowPlayer.prepareLoading();
      mFullScreenPlayer.prepareLoading();
    }
  }

  class PlayerObserver extends SuperPlayerObserver {

    @Override
    public void onPlayPrepare() {
      mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.INIT);
      mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.INIT);
      actonOfPreloadOnPlayPrepare();
      if (mWatcher != null) {
        mWatcher.stop();
      }
    }

    @Override
    public void onPlayBegin(String name) {
      mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);
      mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);
      // sync Start-State to PIP when automatically playing the next episode
      if (mPictureInPictureHelper != null) {
        mPictureInPictureHelper.updatePictureInPictureActions(R.drawable.superplayer_ic_vod_pause_normal, "",
            PictureInPictureHelper.PIP_CONTROL_TYPE_PAUSE, PictureInPictureHelper.PIP_REQUEST_TYPE_PAUSE);
      }

      updateTitle(name);
      mWindowPlayer.hideBackground();
      if (mDanmuView != null && mDanmuView.isPrepared() && mDanmuView.isPaused()) {
        mDanmuView.resume();
      }
      if (mWatcher != null) {
        mWatcher.exitLoading();
      }
      notifyCallbackPlaying();
    }

    @Override
    public void onPlayPause() {
      mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);
      mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);
    }

    @Override
    public void onPlayStop() {
      if (mCurrentSuperPlayerModel != null/* && mCurrentSuperPlayerModel.dynamicWaterConfig != null */) {
        mDynamicWatermarkView.hide();
      }
      if (mSuperPlayerModelList.size() >= 1 && mIsPlayInit && mIsLoopPlayList) {
        playNextVideo();
      } else {
        mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.END);
        mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.END);
        if (mPictureInPictureHelper != null) {
          mPictureInPictureHelper.updatePictureInPictureActions(R.drawable.superplayer_ic_vod_play_normal, "",
              PictureInPictureHelper.PIP_CONTROL_TYPE_PLAY, PictureInPictureHelper.PIP_REQUEST_TYPE_PLAY);
        }
        // sync End-State to PIP

        if (mWatcher != null) {
          mWatcher.stop();
        }
      }
      notifyCallbackPlayEnd();
    }

    @Override
    public void onPlayError() {
      mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.ERROR);
      mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.ERROR);
    }

    @Override
    public void onPlayLoading() {
      if (mPlayAction == PLAY_ACTION_PRELOAD) {
        if (isCallResume) {
          mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
          mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
        }
      } else {
        mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
        mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.LOADING);
      }
      if (mWatcher != null) {
        mWatcher.enterLoading();
      }
    }

    @Override
    public void onPlayProgress(long current, long duration, long playable) {
      mProgress = current;
      mDuration = duration;
      mPlayAble = playable;
      mWindowPlayer.updateVideoProgress(current, duration, playable);
      mFullScreenPlayer.updateVideoProgress(current, duration, playable);
      mFloatPlayer.updateVideoProgress(current, duration, playable);
    }

    @Override
    public void onSeek(int position) {
      if (mSuperPlayer.getPlayerType() != SuperPlayerDef.PlayerType.VOD) {
        if (mWatcher != null) {
          mWatcher.stop();
        }
      } else {
        mWindowPlayer.updateVipInfo(position);
        mFullScreenPlayer.updateVipInfo(position);
        mFloatPlayer.updateVipInfo(position);
      }
    }

    @Override
    public void onSwitchStreamStart(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
      if (playerType == SuperPlayerDef.PlayerType.LIVE) {
        if (success) {
          Toast.makeText(mContext, "Switching to" + quality.title + "...", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(mContext, "Failed to switch" + quality.title
              + " video quality. Please try again later.", Toast.LENGTH_SHORT).show();
        }
      }
    }

    @Override
    public void onSwitchStreamEnd(boolean success, SuperPlayerDef.PlayerType playerType, VideoQuality quality) {
      if (playerType == SuperPlayerDef.PlayerType.LIVE) {
        if (success) {
          Toast.makeText(mContext, "Successfully switched video quality", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(mContext, "Failed to switch video quality", Toast.LENGTH_SHORT).show();
        }
      }
    }

    @Override
    public void onPlayerTypeChange(SuperPlayerDef.PlayerType playType) {
      mWindowPlayer.updatePlayType(playType);
      mFullScreenPlayer.updatePlayType(playType);
      mFloatPlayer.updatePlayType(playType);
    }

    @Override
    public void onPlayTimeShiftLive(TXLivePlayer player, String url) {
      if (mWatcher == null) {
        mWatcher = new NetWatcher(mContext);
      }
      mWatcher.start(url, player);
    }

    @Override
    public void onVideoQualityListChange(List<VideoQuality> videoQualities, VideoQuality defaultVideoQuality) {
      mFullScreenPlayer.setVideoQualityList(videoQualities);
      mFullScreenPlayer.updateVideoQuality(defaultVideoQuality);
    }

    @Override
    public void onVideoImageSpriteAndKeyFrameChanged(PlayImageSpriteInfo info, List<PlayKeyFrameDescInfo> list) {
      mFullScreenPlayer.updateImageSpriteInfo(info);
      mFullScreenPlayer.updateKeyFrameDescInfo(list);
    }

    @Override
    public void onError(int code, String message) {

      showToast(message);
      notifyCallbackPlayError(code, message);
    }

    @Override
    public void onRcvFirstIframe() {
      super.onRcvFirstIframe();
      mWindowPlayer.toggleCoverView(false);
      boolean curIsInPipMode = mPictureInPictureHelper != null && mPictureInPictureHelper.isInPipMode();
      if (!TextUtils.isEmpty(mCurrentSuperPlayerModel.coverPictureUrl) && !curIsInPipMode) {
        mWindowPlayer.showPIPIV(mCurrentSuperPlayerModel.vipWatchMode == null);
      }
      mFullScreenPlayer.toggleCoverView(false);
      if (mDynamicWatermarkView != null) {
        mDynamicWatermarkView.show();
      }
    }

    @Override
    public void onRcvTrackInformation(List<TXTrackInfo> infoList, TXTrackInfo lastSelected) {
      super.onRcvTrackInformation(infoList, lastSelected);
      mFullScreenPlayer.setVodSelectionViewPositionAndData(infoList, lastSelected);
    }

    @Override
    public void onRcvSubTitleTrackInformation(List<TXTrackInfo> infoList) {
      super.onRcvSubTitleTrackInformation(infoList);
      mFullScreenPlayer.setVodSubtitlesViewPositionAndData(infoList);
    }

    @Override
    public void onRcvWaterMark(String text, long duration) {
      if (!TextUtils.isEmpty(text)) {
        DynamicWaterConfig dynamicWaterConfig = new DynamicWaterConfig(text, 30, Color.parseColor("#30FFFFFF"));
        dynamicWaterConfig.durationInSecond = duration;
        dynamicWaterConfig.setShowType(DynamicWaterConfig.GHOST_RUNNING);
        setDynamicWatermarkConfig(dynamicWaterConfig);
      }
    }
  }

  private void showToast(String message) {
    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
  }

  private void showToast(int resId) {
    Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
  }

  private void notifyCallbackPlaying() {
    if (mPlayerViewCallback != null) {
      mPlayerViewCallback.onPlaying();
    }
  }

  private void notifyCallbackPlayEnd() {
    if (mPlayerViewCallback != null) {
      mPlayerViewCallback.onPlayEnd();
    }
  }

  private void notifyCallbackPlayError(int code, String message) {
    if (mPlayerViewCallback != null) {
      mPlayerViewCallback.onError(code, message);
    }
  }

  public static void save2MediaStore(Context context, Bitmap image) {
    File file;
    long dateSeconds = System.currentTimeMillis() / 1000;
    String bitName = dateSeconds + ".jpg";
    File externalStorageDirectory = Environment.getExternalStorageDirectory();
    if (externalStorageDirectory == null) {
      Log.e(TAG, "getExternalStorageDirectory is null");
      return;
    }
    File appDir = new File(externalStorageDirectory.getPath(), "superplayer");
    if (!appDir.exists()) {
      appDir.mkdir();
    }
    file = new File(appDir, bitName);
    FileOutputStream out;
    try {
      out = new FileOutputStream(file);
      if (image.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
        out.flush();
        out.close();
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
    Uri uri = Uri.fromFile(file);
    intent.setData(uri);
    context.sendBroadcast(intent);
  }

  private void save2MediaStoreForAndroidQAbove(Context context, Bitmap image) {
    long dateSeconds = System.currentTimeMillis();
    String fileName = dateSeconds + ".jpg";

    final ContentValues values = new ContentValues();
    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
    values.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
    values.put(MediaStore.MediaColumns.DATE_ADDED, dateSeconds / 1000);
    values.put(MediaStore.MediaColumns.DATE_MODIFIED, dateSeconds / 1000);
    values.put(MediaStore.MediaColumns.DATE_EXPIRES, (dateSeconds + DateUtils.DAY_IN_MILLIS) / 1000);
    values.put(MediaStore.MediaColumns.IS_PENDING, 1);

    ContentResolver resolver = context.getContentResolver();
    final Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    try {
      try (OutputStream out = resolver.openOutputStream(uri)) {
        if (!image.compress(Bitmap.CompressFormat.PNG, 100, out)) {
          throw new IOException("Failed to compress");
        }
      }
      values.clear();
      values.put(MediaStore.MediaColumns.IS_PENDING, 0);
      values.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
      resolver.update(uri, values, null, null);

    } catch (IOException e) {
      Log.e(TAG, Log.getStackTraceString(e));
    }
  }

  public void disableGesture(boolean flag) {
    if (null != mFullScreenPlayer) {
      mFullScreenPlayer.disableGesture(flag);
    }
    if (null != mWindowPlayer) {
      mWindowPlayer.disableGesture(flag);
    }
  }

  /**
   * Set whether to display the video quality, default is to display.
   *
   * 设置是否显示清晰度，默认显示
   */
  public void setQualityVisible(boolean isShow) {
    if (null != mFullScreenPlayer) {
      mFullScreenPlayer.setVideoQualityVisible(isShow);
    }
  }

  public void setNeedToPause(boolean value) {
    mSuperPlayer.setNeedToPause(value);
  }

  public void setIsAutoPlay(boolean b) {
    mSuperPlayer.setAutoPlay(b);
  }

  public void setStartTime(double startTime) {
    mSuperPlayer.setStartTime((float) startTime);
  }

  public void setLoop(boolean b) {
    mSuperPlayer.setLoop(b);
  }

  @Override
  public void onStoragePermissionGranted() {
    mSuperPlayer.snapshot(new TXLivePlayer.ITXSnapshotListener() {
      @Override
      public void onSnapshot(Bitmap bitmap) {
        if (bitmap != null) {
          showSnapshotWindow(bitmap);
        } else {
          showToast(R.string.superplayer_screenshot_fail);
        }
      }
    });
  }

  public void onRequestPermissionsResult(int requestCode, @NonNull int[] grantResults) {
    mStoragePermissionManager.onRequestPermissionsResult(requestCode, grantResults);
  }

  @Override
  public void onVolumeChange(int volume) {
    Log.d("音量，", "volume" + volume);
    mWindowPlayer.onVolumeChange(volume);
    mFullScreenPlayer.onVolumeChange(volume);
  }

  public long getProgress() {
    return mProgress;
  }

  @Override
  public void onClickPIPPlay() {
    mSuperPlayer.resume();
    mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);
    mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.PLAYING);
  }

  @Override
  public void onClickPIPPause() {
    mSuperPlayer.pause();
    mWindowPlayer.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);
    mFullScreenPlayer.updatePlayState(SuperPlayerDef.PlayerState.PAUSE);
  }

  @Override
  public void onClickPIPPlayBackward() {
    mProgress = mProgress + mPictureInPictureHelper.getTimeShiftInterval();
    mProgress = (long) Math.min(mProgress, mSuperPlayer.getVodDuration());
    mSuperPlayer.seek((int) mProgress);
    mWindowPlayer.updateVideoProgress(mProgress, mDuration, mPlayAble);
    mFullScreenPlayer.updateVideoProgress(mProgress, mDuration, mPlayAble);
  }

  @Override
  public void onClickPIPPlayForward() {
    mProgress = mProgress - mPictureInPictureHelper.getTimeShiftInterval();
    mProgress = Math.max(0, mProgress);
    mSuperPlayer.seek((int) mProgress);
    mWindowPlayer.updateVideoProgress(mProgress, mDuration, mPlayAble);
    mFullScreenPlayer.updateVideoProgress(mProgress, mDuration, mPlayAble);
  }

  public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
    if (mPictureInPictureHelper != null) {
      mPictureInPictureHelper.onPictureInPictureModeChanged(isInPictureInPictureMode);
    }
  }

  public void showPIPIV(boolean isShow) {
    mWindowPlayer.showPIPIV(isShow);
  }

  // 向外暴露音量方法
  public void setVolume(int volume) {
    Log.d("声音", "volume: " + volume);
    mFullScreenPlayer.mVodMoreView.mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
  }

  // 设置静音方法
  public void setMute(boolean mute) {
    ((SuperPlayerImpl) mSuperPlayer).setMute(mute);
  }

  // 恢复播放
  public void resume() {
    mSuperPlayer.resume();
  }

  // 暂停播放
  public void pause() {
    mSuperPlayer.pause();
  }

  // 获取播放器状态
  public String status() {
    SuperPlayerDef.PlayerState state = mSuperPlayer.getPlayerState();
    Log.d("播放器状态", "当前状态是: " + state);
    return switch (state) {
      case PLAYING -> "playing";
      case PAUSE -> "paused";
      case LOADING -> "buffering";
      case END -> "stopped";
      case INIT -> "preparing";
      case ERROR -> "failed";
      default -> "unknown";
    };
  }

  // 获取视频缓冲区
  public Float playableDuration() {
    return ((SuperPlayerImpl) mSuperPlayer).playableDuration();
  }

  // 设置视频布局格式
  public void setContentFit(String mode) {
    SuperPlayerGlobalConfig config = SuperPlayerGlobalConfig.getInstance();
    if ("contain".equals(mode)) {
      config.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION; // 保持比例，可能留黑边
    } else if ("cover".equals(mode) || "fill".equals(mode)) {
      config.renderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN; // 填充全屏，可能裁剪
    } else {
      config.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION; // 默认 fallback
    }

    ((SuperPlayerImpl) mSuperPlayer).setContentFit();

  }

  public void detachPlayerView() {
    ((SuperPlayerImpl) mSuperPlayer).detachPlayerView();
  }

  public void refreshVodView() {
    ((SuperPlayerImpl) mSuperPlayer).vodPlayerRefreshView();
  }
}

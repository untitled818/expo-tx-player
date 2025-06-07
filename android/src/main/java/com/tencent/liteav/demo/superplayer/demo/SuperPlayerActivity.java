package com.tencent.liteav.demo.superplayer.demo;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;

import androidx.fragment.app.FragmentActivity;

import com.tencent.liteav.demo.superplayer.SuperPlayerDef;
import com.tencent.liteav.demo.superplayer.SuperPlayerView;

import com.tencent.liteav.demo.superplayer.SuperPlayerModel;
import com.tencent.liteav.demo.superplayer.helper.PictureInPictureHelper;

import expo.modules.txplayer.R;

public class SuperPlayerActivity extends FragmentActivity {
    private static final String TAG = "SuperPlayerActivity";
    
    private SuperPlayerView mSuperPlayerView;
    private boolean mIsEnteredPIPMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.superplayer_activity_supervod_player);
        
        mSuperPlayerView = findViewById(R.id.superVodPlayerView);
        
        // 初始化播放器
        initPlayer();
    }

    
    private void initPlayer() {
        // 设置播放器回调
        mSuperPlayerView.setPlayerViewCallback(new SuperPlayerView.OnSuperPlayerViewCallback() {
            @Override
            public void onStartFullScreenPlay() {
                // 全屏播放处理
            }
            
            @Override
            public void onStopFullScreenPlay() {
                // 退出全屏处理
            }
            
            @Override
            public void onClickFloatCloseBtn() {
                // 关闭悬浮窗
                finish();
            }
            
            @Override
            public void onClickSmallReturnBtn() {
                // 小窗返回按钮
                enterPIPMode();
            }
            
            @Override
            public void onStartFloatWindowPlay() {
                // 开始悬浮窗播放
                enterPIPMode();
            }

            @Override
            public void onPlaying() {

            }

            @Override
            public void onPlayEnd() {

            }

            @Override
            public void onError(int code, String message) {

            }

            @Override
            public void onShowCacheListClick() {

            }

            @Override
            public void onEnterPictureInPicture() {
                Log.d(TAG, "SuperPlayerView 请求进入 PictureInPicture");
                tryEnterPip(); // 调用下面封装的方法
            }

            @Override
            public void onExitPictureInPicture() {

            }

            @Override
            public void onStatusChange(String status) {

            }

            @Override
            public void onPlayingChange(Boolean isPlaying) {

            }

            @Override
            public void onCastButtonPressed() {

            }
        });
        
        // 加载并播放默认视频
        playDefaultVideo();
    }

    private void tryEnterPip() {
        Log.d("画中画", "SuperPlayerView 请求进入 PictureInPicture");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PictureInPictureHelper.hasPipPermission(this)) {
                try {
                    PictureInPictureParams params = new PictureInPictureParams.Builder()
                            .setAspectRatio(new Rational(16, 9)) // 可选，设置画中画比例
                            .build();
                    enterPictureInPictureMode(params);
                } catch (IllegalStateException e) {
                    Log.e(TAG, "进入PiP失败，当前Activity不支持: " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "进入PiP失败: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "没有PiP权限");
            }
        }
    }
    
    private void playDefaultVideo() {
        SuperPlayerModel model = new SuperPlayerModel();
        model.url = "http://your-video-url.mp4"; // 替换为你的视频URL
        model.title = "测试视频";
        mSuperPlayerView.playWithModelNeedLicence(model);
    }
    
    // 进入画中画模式
    private void enterPIPMode() {
        Log.d(TAG, "Activity -> SuperPlayerView.enterPictureInPictureMode()");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (PictureInPictureHelper.hasPipPermission(this)) {
                try {
                    PictureInPictureParams params = new PictureInPictureParams.Builder()
                            .build();
                    enterPictureInPictureMode(params);
                } catch (Exception e) {
                    Log.e(TAG, "enterPIPMode error: " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // 当用户按下Home键时自动进入PIP模式
        if (mSuperPlayerView.getPlayerState() == SuperPlayerDef.PlayerState.PLAYING) {
            enterPIPMode();
        }
    }
    
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        mIsEnteredPIPMode = isInPictureInPictureMode;
        
        if (isInPictureInPictureMode) {

            // 进入PIP模式时的处理
//            mSuperPlayerView.hideAllViewsExceptPlayer();
        } else {
            // 退出PIP模式时的处理
//            mSuperPlayerView.showAllViews();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (mIsEnteredPIPMode) {
            // PIP模式下不暂停播放
            return;
        }
        mSuperPlayerView.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (mIsEnteredPIPMode) {
            mIsEnteredPIPMode = false;
            return;
        }
        mSuperPlayerView.onResume();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuperPlayerView.release();
    }
}
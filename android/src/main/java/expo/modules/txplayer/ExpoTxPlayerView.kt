package expo.modules.txplayer

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.tencent.liteav.demo.superplayer.SuperPlayerDef
import com.tencent.liteav.demo.superplayer.SuperPlayerModel
import com.tencent.liteav.demo.superplayer.SuperPlayerView
import com.tencent.liteav.demo.superplayer.helper.ContextUtils
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView

import com.tencent.liteav.demo.superplayer.SuperPlayerView
import com.tencent.liteav.demo.superplayer.SuperPlayerModel

import android.graphics.Color

class ExpoTxPlayerView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {

  private val TAG = "ExpoTxPlayer"

  private val onLoad by EventDispatcher()
  private val resolvedActivity = ContextUtils.getActivityFromContext(context) as? Activity

  private var originalParent: ViewGroup? = null
  private var originalIndex: Int = -1

  private val playerView = SuperPlayerView(resolvedActivity ?: context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
  }



  fun playWithUrl(url: String, appId: Int) {
    val context = appContext.reactContext ?: return  // 等待 react context 准备好
    val model = SuperPlayerModel().apply {
      this.appId = appId
      this.url = url
    }
    playerView.playWithModelNeedLicence(model)
    // 派发事件通知 JS，已开始播放
    onLoad(mapOf("url" to url))
  }
  fun onHostConfigurationChanged(newConfig: Configuration) {
    Log.d("ExpoTxPlayer", "onHostConfigurationChanged: orientation=${newConfig.orientation}")
  }
  fun resetPlayer() {
    playerView.resetPlayer()
  }


  fun sendDanmaku(content: String, withBorder: Boolean = false) {
    playerView.sendDanmu(content, withBorder);
    Log.d("ExpoTxPlayer", "sendDanmaku called with content: $content")
  }

  fun hideDanmaku() {
    playerView.closeDanmu();
  }

  fun showDanmaku() {
    playerView.openDanmu();
  }



  fun toggleDanmakuBarrage() {
    try {
      val playerView = getFullScreenPlayer() ?: return  // 获取 FullScreenPlayer 实例
      val method = playerView.javaClass.getDeclaredMethod("toggleBarrage")
      Log.d("ExpoTxPlayer", "sendDanmaku called with content: 触发了吗")
      method.isAccessible = true
      method.invoke(playerView)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun getFullScreenPlayer(): Any? {
    return try {
      val field = playerView.javaClass.getDeclaredField("mFullScreenPlayer")
      field.isAccessible = true
      field.get(playerView)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }



//  private fun tryInitDanmakuViewOnce() {
//    if (!danmakuInited) {
//      try {
//        playerView.tryInitDanmakuView()
//        danmakuInited = true
//      } catch (e: Exception) {
//        Log.e("ExpoTxPlayer", "弹幕初始化失败: ${e.message}")
//      }
//    }
//  }


  init {
    addView(playerView)
    playerView.openDanmu();
    ExpoTxPlayerHolder.playerView = this

    playerView.setPlayerViewCallback(object : SuperPlayerView.OnSuperPlayerViewCallback {
      override fun onStartFullScreenPlay() {
        Log.d(TAG, "进入全屏播放，view size: ${playerView.width}x${playerView.height}")
        enterFullScreen()
      }

      override fun onStopFullScreenPlay() {
        Log.d(TAG, "退出全屏播放")
        exitFullScreen()
      }

      override fun onClickFloatCloseBtn() {
        Log.d(TAG, "点击悬浮窗关闭按钮")
      }

      override fun onClickSmallReturnBtn() {
        Log.d(TAG, "点击小窗口返回按钮")
      }

      override fun onStartFloatWindowPlay() {
        Log.d(TAG, "开始悬浮窗播放")
      }

      override fun onPlaying() {
        Log.d(TAG, "开始播放")
      }

      override fun onPlayEnd() {
        Log.d(TAG, "播放结束")
      }

      override fun onError(code: Int) {
        Log.e(TAG, "播放出错, code=$code")
      }

      override fun onShowCacheListClick() {
        Log.d(TAG, "点击缓存列表按钮")
      }

      override fun onEnterPictureInPicture() {
      }
    })
  }

  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    if (ExpoTxPlayerHolder.playerView === this) {
      ExpoTxPlayerHolder.playerView = null
      Log.d("ExpoTxPlayerView", "播放器已卸载并清除全局引用")
    }
  }
}

package expo.modules.txplayer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.*
import android.view.WindowInsetsController
import com.tencent.liteav.demo.superplayer.SuperPlayerModel
import com.tencent.liteav.demo.superplayer.SuperPlayerView
import com.tencent.liteav.demo.superplayer.helper.ContextUtils
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView

class ExpoTxPlayerView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private var originalParent: ViewGroup? = null
  private var originalIndex: Int = -1

  private val onLoad by EventDispatcher()

  private val resolvedActivity = ContextUtils.getActivityFromContext(context).also {
    Log.d("ExpoTxPlayer", "ContextUtils.getActivityFromContext(context) -> $it")
  }

  private val playerView = SuperPlayerView(resolvedActivity ?: context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    setBackgroundColor(Color.BLACK) // 确保背景不透明
  }

  fun enterFullScreen() {
    val activity = resolvedActivity ?: return
    val rootView = activity.findViewById<ViewGroup>(android.R.id.content) ?: return

    // 保存原始父视图和索引
    val parent = playerView.parent as? ViewGroup ?: return
    originalParent = parent
    originalIndex = parent.indexOfChild(playerView)

    // 从原始容器中移除并添加到全屏容器
    parent.removeView(playerView)
    rootView.addView(
      playerView,
      ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
    )

    // 设置全屏 UI
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val controller = activity.window.insetsController
      controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
      controller?.systemBarsBehavior =
        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    } else {
      @Suppress("DEPRECATION")
      activity.window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_FULLSCREEN or
          View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
          View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
      @Suppress("DEPRECATION")
      activity.window.setFlags(
        WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN
      )
    }
  }

  fun exitFullScreen() {
    val activity = resolvedActivity ?: return
    val parent = originalParent ?: return

    (playerView.parent as? ViewGroup)?.removeView(playerView)
    parent.addView(playerView, originalIndex)

    // 恢复非全屏 UI
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      val controller = activity.window.insetsController
      controller?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
    } else {
      @Suppress("DEPRECATION")
      activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
      @Suppress("DEPRECATION")
      activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }
  }

  fun playWithUrl(url: String, appId: Int) {
    val context = appContext.reactContext ?: return
    val model = SuperPlayerModel().apply {
      this.appId = appId
      this.url = url
    }
    playerView.playWithModelNeedLicence(model)
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
        Log.d("ExpoTxPlayer", "进入全屏播放，view size: ${playerView.width}x${playerView.height}")
        enterFullScreen()
      }

      override fun onStopFullScreenPlay() {
        Log.d("ExpoTxPlayer", "退出全屏播放")
        exitFullScreen()
      }

      override fun onClickFloatCloseBtn() {
        Log.d("ExpoTxPlayer", "点击悬浮窗关闭按钮")
      }

      override fun onClickSmallReturnBtn() {
        Log.d("ExpoTxPlayer", "点击小窗口返回按钮")
      }

      override fun onStartFloatWindowPlay() {
        Log.d("ExpoTxPlayer", "开始悬浮窗播放")
      }

      override fun onPlaying() {
        Log.d("ExpoTxPlayer", "开始播放")
      }

      override fun onPlayEnd() {
        Log.d("ExpoTxPlayer", "播放结束")
      }

      override fun onError(code: Int) {
        Log.e("ExpoTxPlayer", "播放出错, code=$code")
      }

      override fun onShowCacheListClick() {
        Log.d("ExpoTxPlayer", "点击缓存列表按钮")
      }

      override fun onEnterPictureInPicture() {
        Log.d("ExpoTxPlayer", "onEnterPictureInPicture")
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
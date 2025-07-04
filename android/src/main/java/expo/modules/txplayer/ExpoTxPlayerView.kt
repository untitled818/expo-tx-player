package expo.modules.txplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import com.tencent.liteav.demo.superplayer.SuperPlayerGlobalConfig
import com.tencent.liteav.demo.superplayer.SuperPlayerModel
import com.tencent.liteav.demo.superplayer.SuperPlayerView
import com.tencent.liteav.demo.superplayer.helper.ContextUtils
import com.tencent.rtmp.TXLiveConstants
import com.tencent.rtmp.TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView

@SuppressLint("MissingConstructor", "ViewConstructor")
class ExpoTxPlayerView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
//  val contextForBroadcast: Context
//    get() = context
  val onFullscreenEnter by EventDispatcher()
  val onFullscreenEnd by EventDispatcher()
  val onPIPStart by EventDispatcher()
  val onPIPStop by EventDispatcher()
  val onPlayingChange by EventDispatcher()
  val onStatusChange by EventDispatcher()
  val onCastButtonPressed by EventDispatcher()
  val onError by EventDispatcher()
  val onBack by EventDispatcher()


  private var originalParent: ViewGroup? = null
  private var originalIndex: Int = -1

  private var pendingContentFit: String? = null

  private val onLoad by EventDispatcher()

  private val resolvedActivity = ContextUtils.getActivityFromContext(context).also {
    Log.d("ExpoTxPlayer", "ContextUtils.getActivityFromContext(context) -> $it")
  }


  private val playerView: SuperPlayerView;


  init {
    ExpoTxPlayerHolder.updatePlayerView(this)
    playerView = SuperPlayerView(resolvedActivity ?: context).apply {
      layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
      setBackgroundColor(Color.BLACK);
    }
    applyContentFitIfNeeded();
    addView(playerView)
    playerView.openDanmu();
    initPlayer();
  }

  private fun initPlayer() {
    ExpoTxPlayerHolder.playerView = this
    PipPlayerManager.onPipClosed = {
      Log.d("ExpoTxPlayerView", "PipPlayerActivity 已关闭")
//      playerView.resetPlayer()
//      playerView.onResume();
      onPIPStop(mapOf());
      play();
    }
    playerView.setPlayerViewCallback(object : SuperPlayerView.OnSuperPlayerViewCallback {
      override fun onStartFullScreenPlay() {
        Log.d("ExpoTxPlayer", "进入全屏播放，view size: ${playerView.width}x${playerView.height}")
        onFullscreenEnter(mapOf());
        enterFullScreen()
      }

      override fun onStopFullScreenPlay() {
        Log.d("ExpoTxPlayer", "退出全屏播放")
        onFullscreenEnd(mapOf());
        exitFullScreen()
      }

      override fun onClickFloatCloseBtn() {
        Log.d("ExpoTxPlayer", "点击悬浮窗关闭按钮")
      }

      override fun onClickSmallReturnBtn() {
        onBack(mapOf());
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

      override fun onError(code: Int, message: String) {
        Log.e("ExpoTxPlayer", "播放出错, code=$code, message=$message")
        onError(mapOf(
          "message" to (message.ifEmpty { "未知错误" })
        ))
      }

      override fun onShowCacheListClick() {
        Log.d("ExpoTxPlayer", "点击缓存列表按钮")
      }

      override fun onEnterPictureInPicture() {
        Log.d("SuperPlayer", "onEnterPictureInPicture")
        pause();
        val context = resolvedActivity ?: this@ExpoTxPlayerView.context
        val intent = Intent(context, PipPlayerActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
          putExtra(
            PipPlayerActivity.EXTRA_VIDEO_URL,
            playerView.currentSuperPlayerModel?.url
          )
        }
        context.startActivity(intent)
        onPIPStart(mapOf())
      }

      override fun onExitPictureInPicture() {
        TODO("Not yet implemented")
      }

      override fun onStatusChange(status: String?) {
        Log.d("ExpoTxPlayer", "播放器状态变化: $status")
        onStatusChange(mapOf("status" to (status ?: "unknown")))
      }

      override fun onCastButtonPressed() {
        onCastButtonPressed(mapOf());
      }

      override fun onPlayingChange(isPlaying: Boolean) {
        Log.d("ExpoTxPlayer", "播放状态变化: $isPlaying")
        onPlayingChange(mapOf("value" to isPlaying))
      }
    })
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
        MATCH_PARENT,
        MATCH_PARENT
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
    removeDanmaku();
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
    removeDanmaku();
  }

  private fun applyContentFitIfNeeded() {
    val config = SuperPlayerGlobalConfig.getInstance()
    when (pendingContentFit) {
      "contain" -> config.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION
      "cover", "fill" -> config.renderMode = TXLiveConstants.RENDER_MODE_FULL_FILL_SCREEN
      else -> config.renderMode = TXLiveConstants.RENDER_MODE_ADJUST_RESOLUTION
    }
  }

  fun playWithUrl(url: String, appId: Int) {
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

  fun removeDanmaku() {
    playerView.removeAllDanmakus()
  }

  // 设置音量
  fun setVolume(volume: Int) {
    println("volume: $volume")
    playerView.setVolume(volume);
  }

  // 设置静音
  fun setMute(mute: Boolean) {
    println("mute: $mute");
    playerView.setMute(mute);
  }

  // 恢复播放
  fun play() {
    println("播放");
    playerView.resume();
  }

  // 暂停播放
  fun pause() {
    println("暂停");
    playerView.pause();
  }

  // 获取播放器状态
  fun getStatus(): String {
    println("获取播放器状态");
    return playerView.status();
  }

  // 获取视频缓冲区
  fun bufferedPosition(): Float {
    println("获取视频缓冲区");
    return playerView.playableDuration();
  }

  // 设置播放的url
  fun setVideoURL(url: String) {
    Log.d("ExpoTxPlayer", "🎬 设置视频地址: $url")

    // 先重置播放器
    playerView.resetPlayer()

    // 创建 SuperPlayerModel 并设置 URL
    val model = SuperPlayerModel().apply {
      this.url = url
    }

    // 播放（如果你需要使用 license，可替换为 playWithModelNeedLicence）
    playerView.playWithModelNeedLicence(model)

    // 可选：触发 JS 端或事件回调
    onLoad(mapOf("url" to url))
  }

  // 切换播放源
  fun switchSource(url: String) {
    Log.d("ExpoTxPlayer", "🎬 切换视频地址为: $url")

    // 获取当前 model，如果为空则新建一个
    val currentModel = playerView.currentSuperPlayerModel ?: SuperPlayerModel()

    // 更新 URL
    currentModel.url = url

    playerView.playWithModelNeedLicence(currentModel)

    // 可选：通知前端
    onLoad(mapOf("url" to url))
  }
  //    playerView.refreshVodView();

  // 设置视频分布
  fun setContentFit(mode: String) {
    Log.d("ExpoTxPlayer", "设置 contentFit: $mode")
    playerView.setContentFit(mode);
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

  fun detachPlayerView() {
    playerView.detachPlayerView();
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


  override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    Log.d("ExpoTxPlayerView", "onDetachedFromWindow")
  }
}
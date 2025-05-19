package expo.modules.txplayer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import com.tencent.liteav.demo.superplayer.SuperPlayerView
import com.tencent.liteav.demo.superplayer.SuperPlayerModel
import expo.modules.txplayer.R

class ExpoTxPlayerView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  private val onLoad by EventDispatcher()

  private var playerView: SuperPlayerView? = null
  private var pendingUrl: String? = null
  private var isInitialized = false

  /**
   * 设置视频 URL，如果 view 还没初始化完成，就延迟播放
   */
  fun setVideoUrl(url: String) {
    Log.d("ExpoTxPlayerView", "setVideoUrl: $url")
    pendingUrl = url
    tryInitializeAndPlay()
  }

  /**
   * 等视图 attach 后再执行初始化和播放逻辑
   */
  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    tryInitializeAndPlay()
  }

  /**
   * 初始化播放器视图和播放逻辑（仅执行一次）
   */
  private fun tryInitializeAndPlay() {
    if (!isAttachedToWindow || isInitialized) return

    val activityContext = appContext.currentActivity ?: run {
      Log.e("ExpoTxPlayerView", "Activity context is null, cannot initialize player")
      return
    }

    // 加载布局，绑定播放器 view
    LayoutInflater.from(activityContext).inflate(R.layout.txplayer_view, this, true)
    playerView = findViewById(R.id.superVodPlayerView)

    isInitialized = true

    // 确保播放器已经 attach 后再调用播放
    post {
      pendingUrl?.let { url ->
        Log.d("ExpoTxPlayerView", "开始播放: $url")
        val model = SuperPlayerModel().apply {
            appId = 1308280968 // 替换成你的 AppId
            this.url = url
          }
        playerView?.playWithModelNeedLicence(model)
      }
    }
  }
}
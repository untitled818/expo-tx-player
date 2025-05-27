package expo.modules.txplayer

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import com.tencent.liteav.demo.superplayer.SuperPlayerModel
import com.tencent.liteav.demo.superplayer.SuperPlayerView


class ExpoTxPlayerView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  // Creates and initializes an event dispatcher for the `onLoad` event.
  // The name of the event is inferred from the value and needs to match the event name defined in the module.
  private val onLoad by EventDispatcher()
//  private val playerView = SuperPlayerView(context)

  fun playWithUrl(url: String, appId: Int) {
//    val model = SuperPlayerModel().apply {
//      this.url = url
//      this.appId = appId
//    }
//    playerView.playWithModelNeedLicence(model)
  }
  fun resetPlayer() {
//    playerView.resetPlayer()
  }


  init {
    // Adds the WebView to the view hierarchy.
//    addView(playerView)
  }
}

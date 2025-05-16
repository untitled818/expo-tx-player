package expo.modules.txplayer

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import expo.modules.kotlin.AppContext
import expo.modules.kotlin.viewevent.EventDispatcher
import expo.modules.kotlin.views.ExpoView
import com.tencent.liteav.demo.superplayer.SuperPlayerView
import com.tencent.liteav.demo.superplayer.SuperPlayerModel
import android.util.Log
import android.view.ViewGroup
import android.util.TypedValue
class ExpoTxPlayerView(context: Context, appContext: AppContext) : ExpoView(context, appContext) {
  // Creates and initializes an event dispatcher for the `onLoad` event.
  // The name of the event is inferred from the value and needs to match the event name defined in the module.
  private val onLoad by EventDispatcher()

  // Defines a WebView that will be used as the root subview.
  internal val webView = WebView(context).apply {
    layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
    webViewClient = object : WebViewClient() {
      override fun onPageFinished(view: WebView, url: String) {
        // Sends an event to JavaScript. Triggers a callback defined on the view component in JavaScript.
        onLoad(mapOf("url" to url))
      }
    }
  }

  // 创建 SuperPlayerView 实例
  // private val playerView = SuperPlayerView(context).apply {
  //   layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
  // }

  // fun setVideoUrl(url: String) {
  //   val model = SuperPlayerModel().apply {
  //     appId = 1308280968 // 替换为你的实际 AppId
  //     this.url = url
  //   }
  //   playerView.playWithModelNeedLicence(model)
  // }

  init {
    try {
    Log.d("ExpoTxPlayerView", "Initializing playerView...")
    val playerView = SuperPlayerView(context).apply {
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,  // width
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            200f, 
            resources.displayMetrics
        ).toInt() // height=200dp
      )
    }
    // addView(webView)
    addView(playerView)
  } catch (e: Exception) {
    Log.e("ExpoTxPlayerView", "Error creating SupessrPlayer6View: ${e.message}", e)
    throw e
    }
  }
  // private val playerView: SuperPlayerView
  // init {
  //   // 1. 创建基础视图容器
  //   playerView = SuperPlayerView(context).apply {
  //       layoutParams = ViewGroup.LayoutParams(
  //           ViewGroup.LayoutParams.MATCH_PARENT,
  //           ViewGroup.LayoutParams.MATCH_PARENT
  //       )
  //   }
    
  //   // 2. 反射跳过内部布局加载
  //   try {
  //       val clazz = Class.forName("com.tencent.liteav.demo.superplayer.SuperPlayerView")
  //       val field = clazz.getDeclaredField("mLayoutInflateSuccess")
  //       field.isAccessible = true
  //       field.setBoolean(playerView, true)
  //   } catch (e: Exception) {
  //       Log.e("Player", "跳过布局加载失败", e)
  //   }
    
  //   addView(playerView)
  // }
}

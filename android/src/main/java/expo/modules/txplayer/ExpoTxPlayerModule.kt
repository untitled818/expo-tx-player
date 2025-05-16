package expo.modules.txplayer

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.net.URL
import android.util.Log
import com.tencent.rtmp.TXLiveBase
import com.tencent.rtmp.TXLiveBaseListener

class ExpoTxPlayerModule : Module() {
  companion object {
    private const val TAG = "ExpoTxPlayer"
  }

  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoTxPlayer')` in JavaScript.
    Name("ExpoTxPlayer")

    // Initialize Tencent Live SDK
    OnCreate {
      val licenseURL = "https://license.vod2.myqcloud.com/license/v2/1258384072_1/v_cube.license" // 获取到的 license url
      val licenseKey = "4c71bd88da95af202a8f3b2743c7e4e4" // 获取到的 license key
      
      TXLiveBase.getInstance().setLicence(appContext.reactContext, licenseURL, licenseKey)
      Log.d("ExpoTxPlayerModuless", "setLicence: $licenseURL, $licenseKey")
      TXLiveBase.setListener(object : TXLiveBaseListener() {
       override fun onLicenceLoaded(result: Int, reason: String?) {
         Log.i(TAG, "onLicenceLoaded: result:$result, reason:$reason")
       }
     })
    }


    // Sets constant properties on the module. Can take a dictionary or a closure that returns a dictionary.
    Constants(
      "PI" to Math.PI
    )

    // Defines event names that the module can send to JavaScript.
    Events("onChange")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    Function("hello") {
      "Hello world! 👋"
    }

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("setValueAsync") { value: String ->
      // Send an event to JavaScript.
      sendEvent("onChange", mapOf(
        "value" to value
      ))
    }

    // Enables the module to be used as a native view. Definition components that are accepted as part of
    // the view definition: Prop, Events.
    View(ExpoTxPlayerView::class) {
      // Defines a setter for the `url` prop.
      Prop("url") { view: ExpoTxPlayerView, url: URL ->
        view.webView.loadUrl(url.toString())
        // Log.d("ExpoTxPlayerModule", "Calling setVideoUrl with: $url")
        // view.setVideoUrl(url.toString())
      }
      // Log.d("ExpoTxPlayerModule触发了吗")
      // Defines an event that the view can send to JavaScript.
      Events("onLoad")
    }
  }
}

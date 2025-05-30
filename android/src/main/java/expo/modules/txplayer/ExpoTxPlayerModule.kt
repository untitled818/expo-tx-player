package expo.modules.txplayer

import android.util.Log
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Record
import expo.modules.kotlin.records.Field
import java.net.URL
import com.tencent.rtmp.TXLiveBase

class ExpoTxPlayerModule : Module() {
  private var appId: Int? = null
  private val TAG = "ExpoTxPlayer"

  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('ExpoTxPlayer')` in JavaScript.
    Name("ExpoTxPlayer")

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

    Function("reset") { view: ExpoTxPlayerView ->
      view.resetPlayer()
    }

    Function("setLicense") { params: LicenseParams ->
      val context = appContext.reactContext ?: return@Function
      Log.d(TAG, "setLicense() called with appId: ${params.appId}, url: ${params.url}")
      TXLiveBase.getInstance().setLicence(context, params.url, params.key)
      TXLiveBase.setConsoleEnabled(false)
      TXLiveBase.setLogLevel(4)
      appId = params.appId
    }

    // Enables the module to be used as a native view. Definition components that are accepted as part of
    // the view definition: Prop, Events.
    View(ExpoTxPlayerView::class) {
      // Defines a setter for the `url` prop.
      Prop("url") { view: ExpoTxPlayerView, url: String ->
        val id = appId ?: throw IllegalStateException("appId is not set. Please call setLicense() before setting the URL.")
        Log.d(TAG, "Prop 'url' set to: $url with appId: $id")
        view.playWithUrl(url, id)
      }
      // Defines an event that the view can send to JavaScript.
      Events("onLoad")
    }
  }
}

class LicenseParams : Record {
  @Field
  var url: String = ""

  @Field
  var key: String = ""

  @Field
  var appId: Int = 0

}

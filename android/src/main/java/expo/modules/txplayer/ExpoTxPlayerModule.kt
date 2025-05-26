package expo.modules.txplayer

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Record
import expo.modules.kotlin.records.Field
import java.net.URL
import com.tencent.rtmp.TXLiveBase

class ExpoTxPlayerModule : Module() {
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
      TXLiveBase.getInstance().setLicence(context, params.url, params.key)
    }

    // Enables the module to be used as a native view. Definition components that are accepted as part of
    // the view definition: Prop, Events.
    View(ExpoTxPlayerView::class) {
      // Defines a setter for the `url` prop.
      Prop("url") { view: ExpoTxPlayerView, url: String ->
        view.playWithUrl(url, 1308280968)
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
}

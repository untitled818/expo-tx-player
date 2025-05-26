import ExpoModulesCore
import TXLiteAVSDK_Player

func runOnMain(_ block: @escaping () -> Void) {
  if Thread.isMainThread {
    block()
  } else {
    DispatchQueue.main.async {
      block()
    }
  }
}

public class ExpoTxPlayerModule: Module {
    
    private var currentPlayer: ExpoTxPlayerView?
    
    private var isLicenseSet = false
    // Each module class must implement the definition function. The definition consists of components
    // that describes the module's functionality and behavior.
    // See https://docs.expo.dev/modules/module-api for more details about available components.
    public func definition() -> ModuleDefinition {
        // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
        // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
        // The module will be accessible from `requireNativeModule('ExpoTxPlayer')` in JavaScript.
        Name("ExpoTxPlayer")
        
        Function("setLicense") { (license: [String: String]) -> Void in
            if isLicenseSet {
                print("Warning: License already set, ignoring repeated call")
                throw NSError(domain: "ExpoTxPlayer", code: 1, userInfo: [NSLocalizedDescriptionKey: "License already set"])
                
            }
            guard let url = license["url"], let key = license["key"] else {
                print("setLicense 参数缺少 url 或 key")
                throw NSError(domain: "ExpoTxPlayer", code: 2, userInfo: [NSLocalizedDescriptionKey: "License url or key missing"])
                
            }
            TXLiveBase.setLicenceURL(url, key: key)
            isLicenseSet = true
            
            print("License set: url=\(url), key=\(key)")
        }
        
        Function("setVolume") { (volume: Int) in
            print("setVolume 触发");
            runOnMain {
                ExpoTxPlayerView.currentInstance?.setVolume(volume);
              }
        }
        
        Function("setMute") { (mute: Bool) in
            print("setMute 触发");
            runOnMain {
                ExpoTxPlayerView.currentInstance?.setMute(mute);
              }
        }
        
        Function("getStatus") { () -> String in
            return ExpoTxPlayerView.currentInstance?.getStatus() ?? "unknown"
        }
        
        Function("bufferedPosition") { () -> Float in
            return ExpoTxPlayerView.currentInstance?.bufferedPosition() ?? 0;
        }
        
        
        Function("play") { () in
            runOnMain {
                ExpoTxPlayerView.currentInstance?.play()
              }
        }
        
        Function("pause") { () in
            runOnMain {
                ExpoTxPlayerView.currentInstance?.pause()
              }
        }
        
        Function("setVideoURL") { (url: String) in
            runOnMain {
                ExpoTxPlayerView.currentInstance?.setVideoURL(url)
              }
        }
        
        Function("switchSource") { (url: String) in
            runOnMain {
                ExpoTxPlayerView.currentInstance?.switchSource(url)
          }
        }
        
        
        
        View(ExpoTxPlayerView.self) {
            Events("onCastButtonPressed", "onFullscreenEnter", "onFullscreenEnd","onPIPStart", "onPIPStop", "onError", "onStatusChange", "onBufferedChange", "onPlayingChange")
//            Events("onLoad")
            // Defines a setter for the `url` prop.
            Prop("url") { (view: ExpoTxPlayerView, url: String) in
                if !self.isLicenseSet {
                    print("Warning: setVideoURL called before license is set. Ignoring.")
//                    view.onError(["message": "setVideoURL called before license is set."])
                    return
                }
                view.setVideoURL(url)
            }
            
            Prop("contentFit") { (view: ExpoTxPlayerView, contentFit: String) in
                print("contentFit");
              view.setContentFit(contentFit)
            }
            
        }
    }
}

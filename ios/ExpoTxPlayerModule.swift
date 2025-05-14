import ExpoModulesCore
import SuperPlayer
import TXLiteAVSDK_Player

public class ExpoTxPlayerModule: Module {

    
    public func definition() -> ModuleDefinition {
        Name("ExpoTxPlayer")
        
        OnCreate {
            let licenceURL = "https://license.vod2.myqcloud.com/license/v2/1258384072_1/v_cube.license"
            let licenceKey = "4c71bd88da95af202a8f3b2743c7e4e4"
            
            TXLiveBase.setLicenceURL(licenceURL, key: licenceKey)
            TXLiveBase.sharedInstance().delegate = self as? TXLiveBaseDelegate  // ✅ 类型转换
            print("TXLiveBase SDK Version: \(TXLiveBase.getLicenceInfo())")
        }
        
        
        View(ExpoTxPlayerView.self) {
            Prop("url") { (view: ExpoTxPlayerView, url: String) in
                guard !url.isEmpty else {
                    print("❌ videoUrl is empty.")
                    return
                }

                view.setVideoURL(url)
            }
            
            AsyncFunction("pause") { (view: ExpoTxPlayerView) in
                view.pause()
            }
            
            AsyncFunction("resume") { (view: ExpoTxPlayerView) in
                view.resume()
            }
            
            AsyncFunction("stop") { (view: ExpoTxPlayerView) in
                view.stop()
            }
        }
    }
    
    
}

import ExpoModulesCore
import TXLiteAVSDK_Player

// This view will be used as a native component. Make sure to inherit from `ExpoView`
// to apply the proper styling (e.g. border radius and shadows).
class ExpoTxPlayerView: ExpoView, SuperPlayerDelegate {
    let playerView = SuperPlayerView()
    
    required init(appContext: AppContext? = nil) {
        super.init(appContext: appContext)
        playerView.frame = self.bounds
        playerView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        playerView.delegate = self;

        playerView.fatherView = self
                
        playerView.playerConfig.enableLog = false
        TXLiveBase.setConsoleEnabled(false)
        TXLiveBase.setLogLevel(.LOGLEVEL_ERROR)
        self.addSubview(playerView)
    }
    
    public func setVideoURL(_ url: String) {
        print("[ExpoTxPlayer] 🎬 设置视频地址: \(url)")
        let model = SuperPlayerModel()
        model.videoURL = url
        
        playerView.play(withModelNeedLicence: model)
    }
    
    override func layoutSubviews() {
        print("layoutSubviews")
    }
    
    func screenRotation(_ fullScreen: Bool) {
        print("屏幕旋转，是否全屏: \(fullScreen)")
        // 你可以在这里处理全屏和非全屏的 UI 变化
    }
    
    func superPlayerCaseAction(_ player: SuperPlayerView) {
        print("[ExpoTxPlayer] superPlayerCaseAction 投屏事件触发")
        // 你可以在这里处理投屏按钮事件，比如触发事件通知 JS 层
    }

    
    
}

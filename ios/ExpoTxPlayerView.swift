import ExpoModulesCore
import SuperPlayer
import TXLiteAVSDK_Player

public class ExpoTxPlayerView: ExpoView, SuperPlayerDelegate {
    let playerView = SuperPlayerView()
    
    let castButton = UIButton(type: .custom)
    
    public required init(appContext: AppContext? = nil) {
        super.init(appContext: appContext)
        playerView.frame = self.bounds
        playerView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        playerView.delegate = self;
        
        if let controlView = playerView.controlView as? SPDefaultControlView {
            controlView.startBtn.setTitle("开始播放", for: .normal) // Modify the title
            controlView.startBtn.backgroundColor = .blue // Modify the background color
            
            // 为按钮添加点击事件，点击时调用 fullScreenButtonTapped 方法
//            if let fullScreenButton = controlView.fullScreenBtn {
//                fullScreenButton.addTarget(self, action: #selector(fullScreenButtonTapped), for: .touchUpInside)
//            }
        }

        playerView.playerConfig.enableLog = false
        TXLiveBase.setConsoleEnabled(ObjCBool(false).boolValue)
        TXLiveBase.setLogLevel(.LOGLEVEL_ERROR)
        
        self.addSubview(playerView)
//        setupCastButton()
        
        print("[ExpoTxPlayer] ✅ ExpoTxPlayerView initialized with frame: \(self.bounds)")
    }
    
    // 设置视频地址
    func setVideoURL(_ url: String) {
        print("[ExpoTxPlayer] 🎬 Setting video URL: \(url)")
        
        let model = SuperPlayerModel()
        model.videoURL = url
        playerView.play(withModelNeedLicence: model)
        print("[ExpoTxPlayer] 🎬 Video URL set and playback started.")
        
    }
    
    // 提供播放控制方法（可供 Module 层调用）
    func pause() {
        print("[ExpoTxPlayer] ⏸️ Pause called.")
        playerView.pause()
    }
    
    func resume() {
        print("[ExpoTxPlayer] ▶️ Resume called.")
        
        playerView.resume()
    }
    
    func stop() {
        print("[ExpoTxPlayer] ⏹️ Stop called.")
        
        playerView.resetPlayer()
    }
    
    private func setupCastButton() {
        // 配置投屏按钮样式
        castButton.setImage(UIImage(named: "cast_icon"), for: .normal)
        castButton.frame = CGRect(x: self.bounds.width - 50, y: 10, width: 40, height: 40)
        castButton.autoresizingMask = [.flexibleLeftMargin, .flexibleBottomMargin]
        castButton.addTarget(self, action: #selector(onCastButtonTapped), for: .touchUpInside)
        castButton.backgroundColor = UIColor.black.withAlphaComponent(0.5)
        castButton.layer.cornerRadius = 20
        castButton.clipsToBounds = true
        
        self.addSubview(castButton)
    }
    
    // 实现 SuperPlayerDelegate 协议的 screenRotation 方法
    public func screenRotation(_ fullScreen: Bool) {
        if fullScreen {
            playerView.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height)
        } else {
            playerView.frame = CGRect(x: 0, y: 0, width: 402, height: 389)  // 恢复原尺寸
        }
        print("[ExpoTxPlayer] 📐 屏幕旋转：\(fullScreen ? "进入全屏" : "退出全屏")")
        print("[ExpoTxPlayer] 当前视图尺寸：\(self.bounds)")
        print("[ExpoTxPlayer] playerView 尺寸：\(playerView.frame)")
       

        // iOS 13+ 使用 windowScene 接口
        if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
            print("[ExpoTxPlayer] 当前界面方向：\(scene.interfaceOrientation.rawValue)")
        } else {
            print("[ExpoTxPlayer] 当前界面方向：未知（非 UIWindowScene）")
        }

        print("[ExpoTxPlayer] 控制视图 frame：\(playerView.frame)")

        if let keyWindow = UIApplication.shared.connectedScenes
            .compactMap({ $0 as? UIWindowScene })
            .flatMap({ $0.windows })
            .first(where: { $0.isKeyWindow }) {

            print("[ExpoTxPlayer] rootViewController: \(String(describing: keyWindow.rootViewController))")
            print("[ExpoTxPlayer] presentedViewController: \(String(describing: keyWindow.rootViewController?.presentedViewController))")
        } else {
            print("[ExpoTxPlayer] 未找到 keyWindow")
        }

        print("[ExpoTxPlayer] 当前播放器所在 viewController: \(String(describing: self.viewController))")
    }


    
    @objc private func onCastButtonTapped() {
        print("[ExpoTxPlayer] 📡 Cast button tapped.")
        // TODO: 可通过 delegate / NotificationCenter / callback 发事件给 JS 层
    }
    
    
    public func superPlayerDidStart(_ player: SuperPlayerView!) {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss" // Define your desired format
        let formattedDate = formatter.string(from: Date())
        print("[ExpoTxPlayer] 🚀 Player did start at \(formattedDate): Playing")
    }
    
    public func superPlayerDidEnd(_ player: SuperPlayerView!) {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd HH:mm:ss" // Define your desired format
        let formattedDate = formatter.string(from: Date())
        print("[ExpoTxPlayer] 🛑 Player did end at \(formattedDate).")
    }
    func bufferingStarted() {
        print("[ExpoTxPlayer] ⏳ Buffering started.")
    }
    
    func bufferingEnded() {
        print("[ExpoTxPlayer] ✔️ Buffering ended.")
    }
    
    
}

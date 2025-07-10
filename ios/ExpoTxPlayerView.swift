import ExpoModulesCore
import TXLiteAVSDK_Player

class ExpoTxPlayerView: ExpoView, SuperPlayerDelegate, CFDanmakuDelegate {
    func danmakuViewGetPlayTime(_ danmakuView: CFDanmakuView!) -> TimeInterval {
        return playerView.playCurrentTime
    }
    
    func danmakuViewIsBuffering(_ danmakuView: CFDanmakuView!) -> Bool {
        return playerView.state == .StateBuffering
    }
    
    public static var currentInstance: ExpoTxPlayerView?
    private var currentDanmakuDensity: CFDanmakuDensity = .high
    private var danmakuView: CFDanmakuView?
    private var danmakuBtn: UIButton?
    let playerView = SuperPlayerView()
    public let onCastButtonPressed = EventDispatcher()
    public let onFullscreenEnter = EventDispatcher()
    public let onFullscreenEnd = EventDispatcher()
    public let onPIPStart = EventDispatcher()
    public let onPIPStop = EventDispatcher()
    public let onError = EventDispatcher()
    public let onPlayingChange = EventDispatcher()
    public let onStatusChange = EventDispatcher()
    public let onBack = EventDispatcher()
    public let onHomeClick = EventDispatcher()
    public let onShareClick = EventDispatcher()
    
    @objc enum DanmakuDensity: Int {
        case low, medium, high
    }

    
    required init(appContext: AppContext? = nil) {
        super.init(appContext: appContext)
        
        ExpoTxPlayerView.currentInstance = self
        playerView.frame = self.bounds
        playerView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        playerView.delegate = self;
        
        playerView.disableGesture = true;
        playerView.disableVolumControl = true;

        playerView.fatherView = self
                
        playerView.playerConfig.enableLog = false
        TXLiveBase.setConsoleEnabled(false)
        TXLiveBase.setLogLevel(.LOGLEVEL_ERROR)
        self.addSubview(playerView)
    }
    
    public func setVideoURL(_ url: String) {
        print("[ExpoTxPlayer] 🎬 设置视频地址: \(url)")
        playerView.resetPlayer();
        let model = SuperPlayerModel()
        model.videoURL = url
        
        playerView.play(withModelNeedLicence: model)
    }
    
    public func resetPlayer() {
        playerView.resetPlayer();
    }
    
    public func switchSource(_ url: String) {
        print("[ExpoTxPlayer] 🎬 切换视频地址为: \(url)")

        if let model = playerView.playerModel {
            model.videoURL = url
            playerView.play(withModelNeedLicence: model)
        } else {
            let newModel = SuperPlayerModel()
            newModel.videoURL = url
            playerView.play(withModelNeedLicence: newModel)
        }
    }

    override func layoutSubviews() {
        super.layoutSubviews()
        
        if let dv = playerView.controlView as? NSObject,
               dv.isKind(of: NSClassFromString("SPDefaultControlView")!),
               let btn = dv.value(forKey: "danmakuBtn") as? UIButton {
                if danmakuBtn == nil {
                    danmakuBtn = btn
                    btn.isSelected = true
                    btn.addTarget(self, action: #selector(danmakuButtonToggled), for: .touchUpInside)
                }
            }
        
        if danmakuView == nil {
            danmakuView = CFDanmakuView(frame: self.bounds)
            danmakuView?.delegate = self
            danmakuView?.backgroundColor = .clear

            danmakuView?.duration = 10.0
            danmakuView?.centerDuration = 3.0
            danmakuView?.lineHeight = 25.0
            danmakuView?.lineMargin = 4.0
            danmakuView?.maxShowLineCount = 5
            
            // 设置初始轨道数（默认半屏密度）
            danmakuView?.setDensity(currentDanmakuDensity, inFrame: self.bounds)

            // 添加多条弹幕
            self.insertSubview(danmakuView!, aboveSubview: playerView)
            danmakuView?.start()
        }
    }
    
    func sendDanmaku(_ text: String, color: UIColor = .white, isSelf: Bool) {
//        print("JS 端的代码文字", text);
        let attr = NSAttributedString(string: text, attributes: [
            .foregroundColor: color,
            .font: UIFont.systemFont(ofSize: 14)
        ])
        let danmaku = CFDanmaku()
        danmaku.contentStr = attr;
        danmaku.timePoint = 0;
        danmaku.isSelf = isSelf;
        danmakuView?.sendDanmakuSource(danmaku);
    }
    
    func pauseDanmaku() {
        danmakuView?.pause();
    }
    
    // 显示弹幕
    func showDanmaku() {
        danmakuView?.showDanmaku();
    }
    
    // 关闭弹幕
    func hideDanmaku() {
        danmakuView?.hideDanmaku()
    }
    
    @objc func danmakuButtonToggled(_ sender: UIButton) {
            if sender.isSelected {
                print("🔵 开启弹幕");
                showDanmaku();
            } else {
                print("🔴 关闭弹幕");
                hideDanmaku();
            }
    }

    
    func screenRotation(_ fullScreen: Bool) {
        print("屏幕旋转，是否全屏: \(fullScreen)")
        // 你可以在这里处理全屏和非全屏的 UI 变化
    }
    
    func superPlayerCaseAction(_ player: SuperPlayerView) {
        print("[ExpoTxPlayer] superPlayerCaseAction 投屏事件触发")
        // 你可以在这里处理投屏按钮事件，比如触发事件通知 JS 层
        onCastButtonPressed();
    }
    
    
    func superPlayerHomeAction(_ player: SuperPlayerView!) {
        print("[ExpoTxPlayer] superPlayerCaseAction home click 事件触发")
        onHomeClick();
    }
    
    func superPlayerShareAction(_ player: SuperPlayerView!) {
        print("[ExpoTxPlayer] superPlayerCaseAction share click 事件触发")
        onShareClick();
    }
    
    func superPlayerDidEnterPicture(inPicture player: SuperPlayerView ) -> Void {
        print("[ExpoTxPlayer] 画中画进入事件触发")
        onPIPStart();
    }
    
    func superPlayerDidExitPicture(inPicture player: SuperPlayerView) {
        print("[ExpoTxPlayer] 画中画离开事件触发")
        onPIPStop();
    }
    
    func superPlayerFullScreenChanged(_ player: SuperPlayerView) {
        if (player.isFullScreen) {
            print("[ExpoTxPlayer] 进入全屏触发")
            onFullscreenEnter();
        } else {
            print("[ExpoTxPlayer] 退出全屏触发")
            onFullscreenEnd();
        }
        
        guard let danmaku = danmakuView else { return }
        
        // 清除当前弹幕 UI，不清除队列
        for subview in danmaku.subviews {
            if subview is UILabel {
                subview.layer.removeAllAnimations()
                subview.removeFromSuperview()
            }
        }


        if player.isFullScreen {
                print("[ExpoTxPlayer] 进入全屏：添加弹幕到全屏 view")
                if let fullscreenView = player.superview {
                    fullscreenView.addSubview(danmaku)
                    danmaku.frame = fullscreenView.bounds
                    danmaku.autoresizingMask = [.flexibleWidth, .flexibleHeight]
                    danmaku.setDensity(currentDanmakuDensity, inFrame: fullscreenView.bounds) // 设置高密度
                }
            } else {
                print("[ExpoTxPlayer] 退出全屏：还原弹幕到原位")
                self.insertSubview(danmaku, aboveSubview: playerView)
                danmaku.frame = self.bounds
                danmaku.autoresizingMask = [.flexibleWidth, .flexibleHeight]
                danmaku.setDensity(currentDanmakuDensity, inFrame: self.bounds) // 设置中密度
            }
    }
    
    func superPlayerBackAction(_ player: SuperPlayerView) {
        print("[ExpoTxPlayer] 竖屏视频返回触发");
        onBack();
        
    }
    
    func superPlayerError(_ player: SuperPlayerView!, errCode code: Int32, errMessage why: String!) {
        print("[ExpoTxPlayer] 播放错误: \(code) - \(why ?? "未知错误")")
        // 将错误传给 JS 层
        self.onError(["message": why ?? "未知错误"])
    }
    
    func superPlayerPlayingStateDidChange(_ player: SuperPlayerView!, isPlaying: Bool) {
      print("[ExpoTxPlayer] 播放状态变化：\(isPlaying)")
      // 触发 JS 层事件
        self.onPlayingChange(["value": isPlaying])
    }
    
    public func superPlayerStatusDidChange(_ player: SuperPlayerView, status: String) {
        print("[ExpoTxPlayer] status 改变为: \(status)")
        self.onStatusChange(["status": status])
      }
    
    
    
    func setVolume(_ volume: Int) {
        playerView.setVolume(Int32(volume));
            print("[ExpoTxPlayer] 设置音量: \(volume)")
    }
    
    func setMute(_ mute: Bool) {
        playerView.setMute(mute);
        print("[ExpoTxPlayer] 设置静音: \(mute)")
    }
    
    func getStatus() -> String {
        print("[ExpoTxPlayer] 获取到播放状态")
        return playerView.status()
      }
    
    public func bufferedPosition() -> Float {
        print("[ExpoTxPlayer] 获取到视频缓存");
        return playerView.playableDuration();
    }
    
    public func play() -> Void {
        print("[ExpoTxPlayer] 视频播放");
        playerView.resume();
    }
    
    public func pause() -> Void {
        print("[ExpoTxPlayer] 视频暂停");
        playerView.pause();
    }
    func setContentFit(_ mode: String) {
      print("[ExpoTxPlayer] 设置 contentFit: \(mode)")
        switch mode {
          case "contain":
            playerView.playerConfig.renderMode = 1;
          case "cover":
            print("触发 cover");
            playerView.playerConfig.renderMode = 0;
          case "fill":
            playerView.playerConfig.renderMode = 0;
          default:
            playerView.playerConfig.renderMode = 1;
          }
    }
    
    func detachPlayerView() {
        playerView.detachOnly();
    }
    
    func setCategoryAndTitle(category: String, title: String) {
        playerView.setCategoryAndTitle(category, title: title);
    }
    
    
    deinit {
        print("[ExpoTxPlayer] 🧹 资源释放")
//        if ExpoTxPlayerView.currentInstance === self {
//            ExpoTxPlayerView.currentInstance = nil
//        }
    }
}

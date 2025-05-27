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
    private var danmakuView: CFDanmakuView?
    private var isDanmakuEnabled = false
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
    
    required init(appContext: AppContext? = nil) {
        super.init(appContext: appContext)
        
        ExpoTxPlayerView.currentInstance = self
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
        playerView.resetPlayer();
        let model = SuperPlayerModel()
        model.videoURL = url
        
        playerView.play(withModelNeedLicence: model)
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
                    btn.addTarget(self, action: #selector(danmakuButtonToggled), for: .touchUpInside)
                    
                    // ✅ 设置初始为“弹幕开启状态”
                    danmakuView?.isHidden = false
                    danmakuView?.resume()
                }
            }
        
        if danmakuView == nil {
            danmakuView = CFDanmakuView(frame: self.bounds)
            danmakuView?.delegate = self
            danmakuView?.backgroundColor = .clear

            danmakuView?.duration = 5.0
            danmakuView?.centerDuration = 3.0
            danmakuView?.lineHeight = 25.0
            danmakuView?.lineMargin = 4.0
            danmakuView?.maxShowLineCount = 12
            danmakuView?.maxCenterLineCount = 1

            // 添加多条弹幕
            var danmakus: [CFDanmaku] = []
            let baseTime = playerView.playCurrentTime
            
            for i in 0..<50 {
                let content = NSAttributedString(string: "弹幕 \(i)", attributes: [
                    .foregroundColor: UIColor.white,
                    .font: UIFont.systemFont(ofSize: 14)
                ])
                let danmaku = CFDanmaku()
                danmaku.contentStr = content
                danmaku.timePoint = baseTime + Double(i) * 0.1 // 每 0.1 秒发一条
                danmakus.append(danmaku)
            }

            danmakuView?.prepareDanmakus(danmakus)
            self.insertSubview(danmakuView!, aboveSubview: playerView)
            danmakuView?.start()
        }
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
            self.danmakuBtn?.sendActions(for: .touchUpInside)
        }
    }
    
    @objc func danmakuButtonToggled(_ sender: UIButton) {
        // 你只做行为，UI 交给腾讯原有的逻辑控制
            if sender.isSelected {
                print("🔵 开启弹幕")
//                danmakuView?.resume()
//                danmakuView?.isHidden = false
//                }
            } else {
                print("🔴 关闭弹幕")
//                danmakuView?.pause()
//                danmakuView?.isHidden = true
            }
    }
    
    @objc func danmakuShow(_ sender: UIButton) {
        sender.isSelected.toggle();
        print("🔵 弹幕按钮被点击")
        // 调用 JS 回调 or 插入弹幕数据
        print("[ExpoTxPlayer] 弹幕按钮点击")
        
        if sender.isSelected {
                print("✅ 开启弹幕")
                danmakuView?.resume()
            } else {
                print("⛔️ 关闭弹幕")
                danmakuView?.pause()
        }
        
        isDanmakuEnabled.toggle()
            print("[ExpoTxPlayer] 弹幕按钮点击，当前状态：\(isDanmakuEnabled ? "开启" : "关闭")")

            if isDanmakuEnabled {
                // 开启弹幕：展示 view + start()
                if let danmakuView = danmakuView {
                    self.insertSubview(danmakuView, aboveSubview: playerView)
                    danmakuView.start()
                }
            } else {
                // 关闭弹幕：隐藏 view + pause()
                danmakuView?.pause()
                danmakuView?.removeFromSuperview()
            }

//        let content = NSAttributedString(string: "弹幕走起", attributes: [
//                .foregroundColor: UIColor.green,
//                .font: UIFont.boldSystemFont(ofSize: 14)
//            ])
//
//            let danmaku = CFDanmaku()
//            danmaku.contentStr = content
//            danmaku.timePoint = playerView.playCurrentTime // 当前时间点
//            danmaku.position = CFDanmakuPositionCenterTop
//
//            danmakuView?.sendDanmakuSource(danmaku)
    }
    
    func sendDanmaku(_ text: String) {
        let attr = NSAttributedString(string: text, attributes: [
            .foregroundColor: UIColor.white,
            .font: UIFont.systemFont(ofSize: 14)
        ])
        let danmaku = CFDanmaku()
        danmaku.contentStr = attr
        danmaku.timePoint = CACurrentMediaTime()
        danmakuView?.sendDanmakuSource(danmaku)
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

        if player.isFullScreen {
                print("[ExpoTxPlayer] 进入全屏：添加弹幕到全屏 view")
                if let fullscreenView = player.superview {
                    fullscreenView.addSubview(danmaku)
                    danmaku.frame = fullscreenView.bounds
                    danmaku.autoresizingMask = [.flexibleWidth, .flexibleHeight]

                    // ✅ 可选：触发一次重新布局轨道
                    // danmaku.resetLayout()  // 如果你在 CFDanmakuView 里加了这个方法
                }
            } else {
                print("[ExpoTxPlayer] 退出全屏：还原弹幕到原位")
                self.insertSubview(danmaku, aboveSubview: playerView)
                danmaku.frame = self.bounds
                danmaku.autoresizingMask = [.flexibleWidth, .flexibleHeight]

                // ✅ 可选：重新布局
                // danmaku.resetLayout()
            }
    }
    
    func superPlayerBackAction(_ player: SuperPlayerView) {
        print("[ExpoTxPlayer] 竖屏视频返回触发");
        
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
            playerView.playerConfig.renderMode = 0; // 你也可以自己定义逻辑
          default:
            playerView.playerConfig.renderMode = 1; // 默认 fallback
          }
    }
    
    
    deinit {
        print("[ExpoTxPlayer] 🧹 资源释放")
//        if ExpoTxPlayerView.currentInstance === self {
//            ExpoTxPlayerView.currentInstance = nil
//        }
    }
}

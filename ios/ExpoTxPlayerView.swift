import ExpoModulesCore
import TXLiteAVSDK_Player

// This view will be used as a native component. Make sure to inherit from `ExpoView`
// to apply the proper styling (e.g. border radius and shadows).
class ExpoTxPlayerView: ExpoView, SuperPlayerDelegate {
    public static var currentInstance: ExpoTxPlayerView?
    let playerView = SuperPlayerView()
    public let onCastButtonPressed = EventDispatcher()
    public let onFullscreenEnter = EventDispatcher()
    public let onFullscreenEnd = EventDispatcher()
    public let onPIPStart = EventDispatcher()
    public let onPIPStop = EventDispatcher()
    public let onError = EventDispatcher()
    public let onPlayingChange = EventDispatcher()
    
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
        print("layoutSubviews")
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
    }
    
    func superPlayerBackAction(_ player: SuperPlayerView) {
        print("[ExpoTxPlayer] 竖屏视频返回触发");
        
    }
    
    func superPlayerError(_ player: SuperPlayerView!, errCode code: Int32, errMessage why: String!) {
        print("[ExpoTxPlayer] 播放错误: \(code) - \(why)")
        // 将错误传给 JS 层
        self.onError(["message": why]);
    }
    
    func superPlayerPlayingStateDidChange(_ player: SuperPlayerView!, isPlaying: Bool) {
      print("[ExpoTxPlayer] 播放状态变化：\(isPlaying)")
      // 触发 JS 层事件
        onPlayingChange(["value": isPlaying])
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
        // ✅ 如果当前释放的实例就是 global 的，就清空
        if ExpoTxPlayerView.currentInstance === self {
            ExpoTxPlayerView.currentInstance = nil
        }
    }
}

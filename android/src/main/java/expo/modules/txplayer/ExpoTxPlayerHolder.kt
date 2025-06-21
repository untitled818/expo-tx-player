package expo.modules.txplayer

object ExpoTxPlayerHolder {
    var playerView: ExpoTxPlayerView? = null

    fun updatePlayerView(newView: ExpoTxPlayerView) {
        if (playerView != null && playerView !== newView) {
            android.util.Log.d("ExpoTxPlayerHolder", "🧹 清理旧播放器实例")
            playerView?.resetPlayer()
        }
        playerView = newView
    }

    fun clear() {
        playerView?.resetPlayer()
        playerView = null
    }
}
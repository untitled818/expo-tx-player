package expo.modules.txplayer

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.app.AppCompatActivity
import com.tencent.liteav.demo.superplayer.SuperPlayerModel
import com.tencent.liteav.demo.superplayer.SuperPlayerView

class PipPlayerActivity : AppCompatActivity() {
    private var playerView: SuperPlayerView? = null
    companion object {
        const val EXTRA_VIDEO_URL = "EXTRA_VIDEO_URL"
        private const val TAG = "PipPlayerActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 取出传入的视频地址
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        val videoWidth = intent.getIntExtra("EXTRA_VIDEO_WIDTH", 16)
        val videoHeight = intent.getIntExtra("EXTRA_VIDEO_HEIGHT", 9)
        Log.d(TAG, "接收到播放器宽高: $videoWidth x $videoHeight")

        if (videoUrl.isNullOrEmpty()) {
            Log.e(TAG, "PipPlayerActivity启动失败，缺少视频URL")
            finish()
            return
        }

        // 创建播放器视图，传入当前Activity作为Context
        playerView = SuperPlayerView(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }

        // 设置播放器参数，播放视频
        val model = SuperPlayerModel().apply {
            url = videoUrl
        }
        playerView?.playWithModelNeedLicence(model)

        setContentView(playerView)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            Log.d(TAG, "设置画中画宽高比: $aspectRatio")
            setPictureInPictureParams(pipParams)
        }

        playerView?.mPictureInPictureHelperEnterPIP()

    }



    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        Log.d(TAG, "画中画模式变化: $isInPictureInPictureMode")
        if (!isInPictureInPictureMode) {
            Log.d(TAG, "finish pipActivity")
            // 退出画中画时关闭Activity
            finish()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        playerView?.resetPlayer()
        playerView = null
        Log.d(TAG, "onDestroy pipActivity")
        PipPlayerManager.onPipClosed?.invoke()

    }
}
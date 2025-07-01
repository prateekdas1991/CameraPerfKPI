package io.github.prateekdas1991

import android.app.Service
import android.content.*
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.os.*
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import java.util.*
import java.util.concurrent.TimeUnit

class StopwatchOverlayService : Service() {

    companion object {
        const val ACTION_TIMER_START = "io.github.prateekdas1991.TIMER_START"
        const val ACTION_TIMER_STOP = "io.github.prateekdas1991.TIMER_STOP"
    }

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: TextView

    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var startTime: Long = 0L

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_TIMER_START -> startStopwatch()
                ACTION_TIMER_STOP -> {
                    stopStopwatch()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Helps Android restart the service if needed
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()

        overlayView = TextView(this).apply {
            textSize = 20f
            setPadding(16, 8, 16, 8)
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xC0000000.toInt())
            typeface = Typeface.MONOSPACE
        }

        val params = WindowManager.LayoutParams(
            350, 100,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 200

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        windowManager.addView(overlayView, params)

        val filter = IntentFilter().apply {
            addAction(ACTION_TIMER_START)
            addAction(ACTION_TIMER_STOP)
        }
        registerReceiver(receiver, filter, RECEIVER_EXPORTED)
    }

    private fun startStopwatch() {
        stopStopwatch() // Reset if running
        startTime = SystemClock.elapsedRealtime()
        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                overlayView.text = String.format(Locale.US,
                    "%02d:%02d.%03d",
                    TimeUnit.MILLISECONDS.toMinutes(elapsed),
                    TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60,
                    elapsed % 1000
                )
                handler?.postDelayed(this, 30)
            }
        }
        handler?.post(runnable!!)
    }

    private fun stopStopwatch() {
        handler?.removeCallbacks(runnable!!)
        handler = null
        runnable = null
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        windowManager.removeView(overlayView)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

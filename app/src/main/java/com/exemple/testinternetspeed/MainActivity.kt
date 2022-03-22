package com.exemple.testinternetspeed

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.exemple.testinternetspeed.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import java.math.BigDecimal

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    var byte = BigDecimal(8)
    var kByte = BigDecimal(1024)

    private val viewBinding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewBinding.speed.text = "0.00 Mbps"
        viewBinding.btnStartTest.setOnClickListener {
            startSpeedTest(viewBinding.speed)
        }
        viewBinding.btnBattery.setOnClickListener {
            val lp = this.window.attributes
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
            this.window.attributes = lp
        }
    }

    private fun startSpeedTest(speed: TextView) {
        val thread = Thread {
            try {
                val speedTestSocket = SpeedTestSocket()
                // add a listener to wait for speedtest completion and progress
                speedTestSocket.addSpeedTestListener(object : ISpeedTestListener {
                    override fun onCompletion(report: SpeedTestReport) {
                        // called when download/upload is finished
                        Log.v("speedtest", "[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                        Log.v("speedtest", "[COMPLETED] rate in bit/s   : " + report.transferRateBit)
                        runOnUiThread {
                            val speedTest = String.format("%.2f", (report.transferRateBit / kByte/ kByte))
                            speed.text = "$speedTest Mbps"
                        }
                    }

                    override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                        // called when a download/upload error occur
                        runOnUiThread {
                            speed.text = speedTestError.name
                        }
                    }


                    override fun onProgress(percent: Float, report: SpeedTestReport) {
                        // called to notify download/upload progress
                        Log.v("speedtest", "[PROGRESS] progress : $percent%")
                        Log.v("speedtest", "[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                        Log.v("speedtest", "[PROGRESS] rate in bit/s   : " + report.transferRateBit)
                        runOnUiThread {
                            val speedTest = String.format("%.2f", (report.transferRateBit / kByte/ kByte))
                            speed.text = "$speedTest Mbps"
                        }
                    }
                })
                speedTestSocket.startDownload("http://ipv4.ikoula.testdebit.info/1M.iso")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        thread.start()
    }
}

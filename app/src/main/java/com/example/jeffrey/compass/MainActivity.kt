package com.example.jeffrey.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView

class MainActivity : AppCompatActivity(), SensorEventListener {

    private val imageView = lazy { findViewById<ImageView>(R.id.activity_main_img_compass) }
    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var azimuth = 0f
    private var currentAzimuth = 0f
    private lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        val alpha = 0.97f
        synchronized(this) {
            if(p0?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
                for(i in 0..2) {
                    gravity[i] = alpha*gravity[i] + (1-alpha)*p0.values[i]
                }
            }

            if(p0?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
                for(i in 0..2) {
                    geomagnetic[i] = alpha*geomagnetic[i] + (1-alpha)*p0.values[i]
                }
            }

            val r = FloatArray(9)
            val i = FloatArray(9)
            val success = SensorManager.getRotationMatrix(r, i, gravity, geomagnetic)
            if(success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)

                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                azimuth = (azimuth+360) % 360

                val anim = RotateAnimation(-currentAzimuth, -azimuth, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f)
                anim.duration = 500
                anim.fillAfter = true
                imageView.value.startAnimation(anim)

                currentAzimuth = azimuth
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}
}
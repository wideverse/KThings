package com.wideverse.kthings.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.wideverse.kthings.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

const val LED_PIN = 1
const val BUTTON_PIN = 2

const val RGB_PIN_RED = 3
const val RGB_PIN_GREEN = 4
const val RGB_PIN_BLUE = 5


class MainActivity : AppCompatActivity() {

    private val kthings = Kthings()

    private val led = GpioPin(LED_PIN)

    private val rgbLed = GpioRgb(RGB_PIN_RED, RGB_PIN_GREEN, RGB_PIN_BLUE)
    private val button = GpioButton(BUTTON_PIN, 1000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch(Dispatchers.IO) {
            initPins()

            observeButton()
            blinkLed()
        }
    }

    private suspend fun initPins() {
        with(kthings) {
            initRgb(rgbLed)
            initButton(button)
            initPin(led)
        }
    }

    private suspend fun blinkLed() {
        with (kthings) {
            while (true) {
                pinUp(led)
                delay(1000)
                pinDown(led)
                delay(1000)
            }
        }
    }

    private suspend fun rgbRedAndBlue() {
        with(kthings) {
            setRgbState(
                rgbLed.set(
                    red = PinState.UP,
                green = PinState.DOWN,
                blue = PinState.UP)
            )
        }
    }

    private suspend fun rgbRedThenGreenThenBlue() {
        with (kthings) {
            setRgbState(rgbLed.setOnly(red = PinState.UP))
            delay(5000)
            setRgbState(rgbLed.setOnly(green = PinState.UP))
            delay(5000)
            setRgbState(rgbLed.setOnly(blue = PinState.UP))
        }
    }

    private suspend fun rgbRed() {
        kthings.setRgbState(
                rgbLed.setOnly(red = PinState.UP)
        )
    }

    private suspend fun observeButton() {
        kthings.observeButton(button).collect {
            when (it) {
                PinState.UP -> Log.d("kthings", "Button is pressed")
                PinState.DOWN -> Log.d("kthings", "Button is released")
            }
        }
    }
}

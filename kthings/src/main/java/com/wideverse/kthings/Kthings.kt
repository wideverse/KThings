/*
 * Copyright (c) 2020 Wideverse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wideverse.kthings

import android.util.Log
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

enum class PinDirection { INPUT, OUTPUT }
enum class PinState {UP, DOWN}

const val GPIO_SYS_PATH = "/sys/class/gpio"

class Kthings {
    private val gpioState = HashMap<Int, GpioPin>()

    // === BUTTON ===
    suspend fun initButton(button: GpioButton) {
        initPin(button.pin)
    }

    @ExperimentalCoroutinesApi
    fun observeButton(button: GpioButton) = callbackFlow {
        while(true) {
            try {
                val value = File(pinValuePath(button.pin.number)).readText().trim()
                val output = if (value == "1") PinState.UP else PinState.DOWN

                offer(output)
                delay(button.updateFrequency)
            } catch (e: Exception) {
                Log.e("hub", e.localizedMessage)
            }
        }
    }

    // === RGB ===
    suspend fun setRgbState(gpioRgb: GpioRgb){
        gpioRgb.getPins().forEach { pin ->
            val number = pin.number
            var state = pin.state

            when (state){
                PinState.UP -> pinUp(number)
                PinState.DOWN -> pinDown(number)
            }
        }
    }

    suspend fun initRgb(vararg gpioRgb: GpioRgb) {
        gpioRgb.forEach {
            it.getPins().forEach { initPin(it) }
        }
    }


    // === PIN ===
    suspend fun initPin(vararg pin: GpioPin) {
        pin.forEach {
            exportPin(it.number)
            setPinDir(it.direction, it.number)
            pinReverse(it.number, it.isReversed)
            gpioState[it.number] = it
        }
    }

    suspend fun pinUp(pin: GpioPin){
        pinUp(pin.number)
    }

    suspend fun pinDown(pin: GpioPin){
        pinDown(pin.number)
    }

    private suspend fun setPinDir(direction: PinDirection, vararg pin: Int) {
        val dir = if (direction == PinDirection.INPUT) "in" else { "out" }

        pin.forEach { runAsRoot(pinDirCommand(it, dir)) }
    }

    suspend fun pinReverse(pin: Int, reverse: Boolean) {
        val value = if (reverse) 1 else 0
        runAsRoot(pinActiveLow(pin, value))
    }

    private suspend fun pinUp(vararg pin: Int) {
        pin.forEach {
            runAsRoot(pinHighCommand(it))
            gpioState[it]?.state = PinState.UP
        }
    }

    private suspend fun pinDown(vararg pin: Int) {
        pin.forEach {
            runAsRoot(pinLowCommand(it))
            gpioState[it]?.state = PinState.DOWN
        }
    }

    private suspend fun exportPin(vararg pin: Int) {
        pin.forEach {
            runAsRoot(exportCommand(it))
        }
    }

    private suspend fun unexportPin(vararg pin: Int) {
        pin.forEach {
            runAsRoot(unexportCommand(it))
        }
    }

    suspend fun releasePin(vararg pin: Int) {
        pin.forEach {
            unexportPin(it)
            gpioState.remove(it)
        }
    }

    suspend fun releaseAll() {
        for ((pin, state) in gpioState) {
            releasePin(pin)
        }
    }

    // === SHELL ===
    private suspend fun runAsRoot(vararg input: String): MutableList<String> = suspendCancellableCoroutine {
        // LOG PURPOSES
        Shell.su(*input).submit { result ->
            it.resume(result.out)
        }
    }

    private fun pinHighCommand(pin: Int) = "echo 1 > $GPIO_SYS_PATH/gpio$pin/value"
    private fun pinLowCommand(pin: Int) = "echo 0 > $GPIO_SYS_PATH/gpio$pin/value"
    private fun exportCommand(pin: Int) = "echo $pin > $GPIO_SYS_PATH/export"
    private fun unexportCommand(pin: Int) = "echo $pin > $GPIO_SYS_PATH/unexport"
    private fun pinDirCommand(pin: Int, dir: String) = "echo $dir > $GPIO_SYS_PATH/gpio$pin/direction"
    private fun pinActiveLow(pin: Int, value: Int) = "echo $value > $GPIO_SYS_PATH/gpio$pin/active_low"
    private fun pinValuePath(pin: Int) = "$GPIO_SYS_PATH/gpio$pin/value/"
}
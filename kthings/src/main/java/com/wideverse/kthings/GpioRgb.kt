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

data class GpioRgb(
    var redPin: GpioPin,
    val greenPin: GpioPin,
    val bluePin: GpioPin,
    val isReverse: Boolean = true) {

    constructor(redPin: Int, greenPin: Int, bluePin: Int): this(
        GpioPin(number = redPin, direction = PinDirection.OUTPUT),
        GpioPin(number = greenPin, direction = PinDirection.OUTPUT),
        GpioPin(number = bluePin, direction = PinDirection.OUTPUT)
    )

    init {
        if (isReverse) {
            getPins().forEach { it.isReversed = true }
        }
    }

    fun set(red: PinState? = null, green: PinState? = null, blue: PinState? = null): GpioRgb {
        red?.let { redPin.state = red }
        green?.let { greenPin.state = green }
        blue?.let { bluePin.state = blue }

        return this
    }

    fun setOnly(
        red: PinState = PinState.DOWN,
        green: PinState = PinState.DOWN,
        blue: PinState = PinState.DOWN): GpioRgb {
        redPin.state = red
        greenPin.state = green
        bluePin.state = blue

        return this
    }

    fun setOff(): GpioRgb {
        redPin.state = PinState.DOWN
        greenPin.state = PinState.DOWN
        bluePin.state = PinState.DOWN

        return this
    }

    fun getPins() = listOf(redPin, greenPin, bluePin)
    fun getPinNumbers() = listOf(redPin.number, greenPin.number, bluePin.number)
}
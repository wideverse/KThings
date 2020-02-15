# KThings
## Kotlin friendly GPIO support on Andorid

![](https://github.com/paolorotolo/kthings/blob/master/images/kthings.gif)
```kotlin
suspend fun blinkLed() {
    with (kthings) {
        while (true) {
            pinUp(led)
            delay(1000)
            pinDown(led)
            delay(1000)
        }
    }
}
```

## What
KThings is a Kotlin library that aims to create a comfortable interface to manage GPIO on Android. It's still an early stage project that supports for now, generic GPIO pins, RGB Leds and Buttons. ***Root is required for this library to work.***

### Coroutines
Each function that operates on hardware is `suspend`. In this way, you can compose more complex instruction like make a led blink after some delay easily. Also, events from GPIO are exposed as Kotlin's `flow`.

# Library Usage
1. **Add the JitPack repository to your build file**

 Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. **Add the dependency**

```gradle
dependencies {
    implementation 'com.github.wideverse:kthings:1.0.0'
}
```

3. **Initialize the library**

```kotlin
val kthings = Kthings()
```
## Generic GPIO PIN
First create a new generic pin:
```kotlin
const val PIN = 42
val led = GpioPin(PIN)
```

You may also set `direction` and `reverse` parameters that corrisponds to Linux `active_low` and `direction` [values for GPIO](https://www.kernel.org/doc/Documentation/gpio/sysfs.txt).
```kotlin
val pin = GpioPin(LED_PIN,
  direction = PinDirection.OUTPUT,
  isReversed = true)
```

The first time before using the pin, you need to init it (equals to Linux's `export`):
```kotlin
kthings.initPin(led)
```

Then, it's possible to set the pin Up and Down with `pinUp(led)` and `pinDown(led)`.
Both functions are suspend. For example, this makes a led blink:
```kotlin
suspend fun blinkLed() {
    with (kthings) {
        while (true) {
            pinUp(led)
            delay(1000)
            pinDown(led)
            delay(1000)
        }
    }
}
```

## RGB
RGB constructor takes 3 different integers for each PIN:
```kotlin 
  val rgbLed = GpioRgb(PIN_RED, PIN_GREEN, PIN_BLUE)
  kthings.initRgb(rgbLed)
```
Alternatively, you can pass 3 GpioPin objects each one with its attributes as explained above.
Since the majority of RGB leds have inverted values for High/Low, the parameter `isReverse` is `true` by default.

The state of an RGB can be updated with `kthings.setRgbState(rgb)` method. You can manipulate the state of each color using `rgb.set()`:
```kotlin
// Activate RED and BLUE pin
suspend fun rgbRedAndBlue() {
    with(kthings) {
        setRgbState(
            rgbLed.set(
              red = PinState.UP, 
              green = PinState.DOWN, 
              blue = PinState.UP)
        )}
}
```
`rgb.setOnly` can be used to set only one value UP while keeping the others DOWN.
```kotlin
// Activate only RED pin
suspend fun rgbRed() {
  with (kthings) {
    setRgbState(rgbLed.setOnly(red = PinState.UP))
  }
}
    
//  Blink with each RGB color each 5 seconds
suspend fun redThenGreenThenBlue() {
  with (kthings) {
      setRgbState(rgbLed.setOnly(red = PinState.UP))
      delay(5000)
      setRgbState(rgbLed.setOnly(green = PinState.UP))
      delay(5000)
      setRgbState(rgbLed.setOnly(blue = PinState.UP))
  }
}
```

## Button
Init a new button with:
```kotlin
val button = GpioButton(BUTTON_PIN, 1000)
kthings.initButton(button)
```
The second parameter is the flow emission frequency in milliseconds. `initButton` will take care of all the pin settings. Alternativly, you can construct a new GpioButton passing a GpioPin with its direction set to `direction.INPUT`.

Observe button state changes:
```kotlin
suspend fun observeButton() {
    kthings.observeButton(button).collect {
        when (it) {
            PinState.UP -> log("Button is pressed")
            PinState.DOWN -> log("Button is released")
        }
    }
}
```

## About
This library has been developed at [Wideverse](https://www.wideverse.com/it/home-it/) @ [Polytechnic University of Bari](https://www.poliba.it/).

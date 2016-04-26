This page is continuously undergoing changes.

# Base API

## Program flow

#### `TODO` exit()
Immediately exits the entire program.

## Input

#### `TODO` getButtonStates()
Returns the number representing the state of the buttons. The binary representation determines which buttons are pressed and which buttons aren't. If the digit in the binary representation equals `1` it is pressed, and if it is `0` it is not. For example if the method returns `01101011`, then you know buttons 0, 1, 3, 5 and 6 are pressed.

#### `TODO` isButtonPressed(num)
Checks if the button with index *num* is pressed. It will be evaluated as a boolean expression. It will return *true* if the button is pressed and *false* if the button is not pressed. Buttons are zero-indexed.

## Output

#### `TODO` setLEDs(val)
Sets the states of all 8 LEDs. The value is a binary number where every digit (from right to left) determines if the LED is on or off. Having a value of `1` means the LED is on, a value of `0` means the LED should be turned off.

#### `TODO` setLED(num, val)
Changes the state of the LED with index num (zero-indexed from right to left) to on if *val* equals `1`, or to off if *val* equals 0. This method ignores the states of all other LEDs.

#### `TODO` set7Segment(dig, val)
Shows the given value *val* at the place of digit *dig*, again zero-indexed. *val* must be a value between 0 and 15 inclusive.

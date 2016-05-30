This page is continuously undergoing changes.

# Base API

## Program flow

#### exit()
Immediately exits the entire program.

## Input

#### getInputStates()
Returns the number representing the state of the input. The binary representation determines which inputs are on and which inputs aren't. If the digit in the binary representation equals `1` it is on, and if it is `0` it is not. For example if the method returns `01101011`, then you know inputs 0, 1, 3, 5 and 6 are on.

#### isInputOn(num)
Checks if the input *num* is on. It will be evaluated as a boolean expression. It will (quasi) return *true* if the input is on and *false* if the input is not on. The index *num* is determined by using the binary representation of the numbers. E.g. `0b000100000` checks for input #5.

#### getAnalogStates()
Returns the concatenated values of the two A/D-converters in binary format. The 8 least significant bits represent the value of the first A/D-converter and the remaining 8 bits represent the value of the second A/D-converter. Hence the values are in the range 0-255 inclusive.

#### getAnalog(num)
Returns the value of the A/D-converter specified by `num`. `num` can either be `0` or `1`, where `0` is the first and `1` is the second A/D-converter. An unsigned 8-bit number is returned in binary format, hence in the range 0-255 inclusive.

## Output

### Output pins

#### setOutput(val)
Sets the states of all 8 outputs. The value is a binary number where every digit (from right to left) determines if the output is on or off. Having a value of `1` means the output is on, a value of `0` means the output should be turned off.

#### setSingleOutput(num, val)
Changes the state of the output with index *num* as binary representation (see also isInputOn(num)) to on if *val* equals `1`, or to off if *val* equals 0. This method ignores the states of all other outputs.

### 7 Segment Display

#### set7Segment(dig, pattern)
Shows the given pattern *pattern* at the place of digit *dig*, both as binary representation (see also isInputOn(num)). The indices of the *pattern* from right to left represent: center, top left, bottom left, bottom center, bottom right, top right, top center, dot. A value of `1` means the segment is turned on, a value of `0` means the segment is turned off.

## Timer

Hook onto the PP2's timer system. The built in timer will always tick down and will tick *once every 0,1ms* (0,0001s).

#### setTimer(val)
Sets the new value of the timer to *val*.

#### addTimer(val)
Sets the value of the timer to the current value of the timer plus *val*.

#### getTimer()
Fetches the current value of the timer.

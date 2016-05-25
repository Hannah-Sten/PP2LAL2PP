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

## Output

#### setOutput(val)
Sets the states of all 8 outputs. The value is a binary number where every digit (from right to left) determines if the output is on or off. Having a value of `1` means the output is on, a value of `0` means the output should be turned off.

#### setSingleOutput(num, val)
Changes the state of the output with index *num* as binary representation (see also isInputOn(num)) to on if *val* equals `1`, or to off if *val* equals 0. This method ignores the states of all other outputs.

#### set7Segment(dig, val)
Shows the given value *val* at the place of digit *dig*, again as binary representation (see also isInputOn(num)). *val* must be a value between 0 and 15 inclusive (it will modualised (lol) by 16 actually as a matter of fact in case you didn't know).

# The PP2LAL2PP-language specification

An example document can be found in the [examples-folder](../examples).

## General code layout
Every single statement must be placed on a seperate line. Blank lines will be ignored. Multiple spaces or tabs will be interpreted as 1 space.

It is recommended for PP2LAL2PP source files to either have a `.pp2` or `.pp2lal2pp` file extension.

Also, use camelCase.

## File inclusion (`include`)

The entire contents of another file can be included in your code with the `include` keyword at the exact place you put the statement. Usage: `include <fileName>`.

*fileName* must be the name of the source file. If the source file has a `.pp2` or `.pp2lal2pp` file extension, you do not need to specify the extension. E.g. you can include the file "constants.pp2" by using `include constants`. When both a `.pp2` and `.pp2lal2pp` file exist, the `.pp2lal2pp` file will be chosen. Files from different directories must include their directory names. The name does not have to be between qoutes. E.g. the file "peanut saussage.cheese" can be includied using `include peanut saussage.cheese`.

## Global variables (`global`)

Global variables are stored in the Global Base and can be used from any scope in the program. You can declare global variables only in the outermost scope using `global <varName> = <value>`.

*varName* is the name used for the variable and must be unique. There cannot be local or global variables with the same name. Variables can't have the same name as reserved keywords.

*value (optional)* this is an equal sign followed by a number. This means that the global variable has an initial value of *value*. If no value is specified (i.e. `global <varName>`) the value will be initialised with 0 being an abbreviation of `global <varName> = 0` effectively.

## Local variables (`var`)

Local variables have their values stored on the stack and are only usable in their local scope. Meaning that as soon as the block of code ends where the local variable is declared, the variable cannot be used anymore. Local variables can only be declared in a scope other than the outermost scope and are declared as follows: `var <varName> = <value>`, analogous to global variables. Variables can't have the same name as reserved keywords.

## Blocks

A block of code is code that is within squiggly brackets `{` and `}`. Local variables declared in the block will be no more when the block finishes.

## Functions

Functions are a block of code that can be called from another place in the program. Unlike 'normal' programming languages, functions declared in the PP2LAL2PP-language will run continuously until the method is manually exited. Meaning that when the method reaches the end of its block, the method will run again.

Functions are declared as follows:

```
<functionName>(<arg0>, ... <argN>) {
    # Code to execute
    return <returnValue>
}
```

*functionName* this is the name of the function and must be unique and not be "main".

*argN (optional)* is a list of arguments seperated by a comma. You can have as many arguments as you like. If you don't want to have arguments you must leave the space between the parenthesis blank like `()`.

*return* exits the function-loop. If return is not called, the function would start over again after the end of the block has been reached. If you add a *returnValue* beind return the function will return said value. However, if you don't want to return a value you can ommit it.

## Main function (`main()`)

The main function is a special function, as this is where the program will start.

## Loops (`loop`)

Loops are blocks of code that can be executed a said amount of times. The loop can count up and down. This is determined from context. The syntax is as follows:

```
loop (<variable> from <beginning> to <endInclusive> [step <stepSize>]) {
    # Do epic things.
}
```

*variable* is the name of the variable that is used for the loop condition. This value can be referenced in the loop's body. Meaning that if you have a standard loop from 1 to 10, the value of *variable* will be 1 in the first iteration, 2 in the second iteration all the way up to 10 in the 10th iteration.

*beginning* is the starting value of the variable.

*endInclusive* is the value where the loop should end. That means that if the variable has a value that surpasses the end value, the loop will terminate. If the loop variable has the same value as endInclusive, it will enter its last iteration. If the value is greater/smaller (depends on if the loop counts up or down) the loop will not have a terminal iteration.

*stepSize (optional)* by default the loop will have a step size of either 1 or -1. However, adding `step <stepSize>` will change the amount by which the variable changes.

### Examples

#### Counting from 100 to 23
```
loop (i from 100 to 23) {
    doSomethingWith(i)
}
```

#### Counting all odd numbers from -23 to 123
```
loop (i from -23 to 123 step 2) {
    doSomethingWith(i)
}
```

## If-statements (`if`/`else`)

If-statements allow you to execute certain code solely when a certain condition holds. Syntax:

```
if (<expression1>) {
    # Execute code when expression 1 holds.
}
else if (<expression2>) {
    # Execute code when expression 1 doesn't hold, but expression 2 does.
}
else {
    # Execute code when neither expression 1 or expression 2 hold.
}
```

*expression* this is a boolean expression using the relational operators mentioned down below. You can chain multiple boolean expressions using logical operators (e.g. `expression1 and expression2` if exp1 and exp2 must hold).

## Numbers

**Decimal numbers** can only be typed using characters in the set `{1,2,3,4,5,6,7,8,9,0}` and cannot start with a `0`. Examples: `3`, `123`, `598123`, 

**Binary numbers** are written with the characters `{0,1}` after the prefix `0b`. Meaning that the number 10 would become `0b1010` or `0b00001010`for example as prefix zeros are allowed.

**Hexadecimal numbers** are written with the characters `{0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F,a,b,c,d,e,f}` after the prefix `0x`. Meaning that the number 334 would become `0x14E` or `0x0014e` as prefix zeros are allowed.

## Definitions (`define`)

Definitions can be used for named constants that must be declared in the outermost scope. Defined constants cannot be changed afterwards. Usage: `define <name> <value>`.

*name* is the name of the definition. It is convention to use ALL_CAPS_AND_SPACES for the name.

*value* this is the numerical value of *name*.

## Comments

Comments are extra pieces of information that are functionally ignored by the compiler. However, it is planned to make the compiler smart enough to place the comments in the generated assembly files.

### Line comments
Line commments will make a whole line ignored by the compiler. You start a line comment with a hash sign `#`.

### Multiline comments
Multiline comments are able to let a whole block of text be ignored by the compiler. You start a block by typing `#;` and you close it again with `;#`.

### Example
```
someFunction();
;# THIS ALL WILL
beee  ignooorreeed YAA LLOOO YOLOOO
OO ;#
anotherFunction();
# Also ignored
howeverThisFunctionDoesnt()
```

## Operators

### Assignment

Usage:
* `<variable> = <number>` assigns the given number to the variable.
* `<variable> = <variable2>` assigns the value of *variable2* to *variable*.

Variants (thank Sten...):
* `:=` has the exact same effect as `=`.
* `=:` assigns the left value to the right variable. E.g. `<variable2> =: <variable>`.

### Arithmetic operators

The following arithmatic operators are supported:
* `+` Addition
* `-` Substraction
* `*` Multiplication
* `/` Integer division
* `%` Remainder
* `**` Power (`2**3` equals 8)

So if you want to store the product of 5 and 7 in a variable a, do this: `a = 5 * 7`.

To make it even better, if you want to do arithmetics on a variable and want to store it directly back in the same variable, you can add a `=`-sign to make it automatically assign. Example:

```
a = 7 * 3   # a equals 21
a -= 15     # a equals 6
a **= 3     # a equals 216
```

### Bitwise operators

You can do some epic shizzles with binary numbers. These operators are supported on a bitwise level:
* `|` Bitwise OR
* `&` Bitwise AND
* `^` Bitwise XOR
* `~` Bitwise NOT

Like the arithmetic operators, you can use them in conjunction with the `=`-sign (e.g. `^=`).

### Relational operators

Relational operators are only used in boolean expressions (in If-statements). The following exist:
* `<` Strictly lesser than
* `<=` Lesser than or equal to
* `>` Strictly greater than
* `>=` Strictly greater than or equal to
* `==` Equals
* `!=` Does not equal

#### Example expressions

```
3 < 4       # true
5 >= 1      # true

a = 3
5 == a      # false
5 != a      # true
```

### Logical expressions

These are used to tie boolean expressions together.
* `and` means that the expressions to the left and to the right must be true.
* `or` means that at least one of the expressions to the left and to the right must be true.

#### Examples

```
3 < 5 and 5 <= 21           # true
4 > 5 or 7 != 10            # true
6 > 7 and 7 > 8 and 9 > 5   # false
```

### Unary operators
These are placed right in front of a variable or number.
* `-` marks a positive number or variable as negative, or a negative number positive.
* `!` negates a boolean expression. Must be used with parentheses around it (`!(3 == 1)` is true).

## Assembly injection (`inject`)

*Warning: dangerous!* Assembly injection lets you inject natie assembly code in your program. The lines will be placed exactly in the 'compiled' file exactly as you type them. This makes it really powerful, but also really dangerous to use. We do not recommend using it. It is solely meant for people who want to know what it all can do. No idea if it is useful, but it was easy to implement haha. Yolo.

Usage:
```
inject {
    LOAD  R2  0
    STOR  R2  [GB+123]
    ; More of your assembly shizzles.
}
```

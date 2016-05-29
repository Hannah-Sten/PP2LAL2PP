;#  
;#  Example file of how a PP2LAL2PP language file looks like.
;#  This is a descriptive doc comment
;#  
@CODE

IOAREA          EQU     -16                         ; Address of the I/O-Area, modulo 2^18.
INPUT           EQU     7                           ; Position of the input buttons (relative to IOAREA).
OUTPUT          EQU     11                          ; Relative position of the power outputs.
DSPDIG          EQU     9                           ; Relative position of the 7-segment display's digit selector.
DSPSEG          EQU     8                           ; Relative position of the 7-segment display's segments.
TIMER           EQU     13                          ; Timer address relative to IOAREA.
ADCONV          EQU     6                           ; A/D-converter address relative to IOAREA.

MAGIC_VALUE     EQU     42                          ; Include contents of const.pp2 or const.pp2lal2pp
DISPLAY_MAX     EQU     16                          ; Other constants

countOne        EQU     1                           ; Global variables. Variables will initialise to 0 when nothing is declared.
countTwo        EQU     2                           ; Global variables. Variables will initialise to 0 when nothing is declared.
previousState   EQU     3                           ; Global variables. Variables will initialise to 0 when nothing is declared.
currentState    EQU     4                           ; Global variables. Variables will initialise to 0 when nothing is declared.

;#
;#  Initialisation of the program.
;#
init:           LOAD    R5  IOAREA                  ; Store the address of the IOAREA for later use.
                LOAD    R0  0                       ; Default value to load in global base.
                STOR    R0  [GB+countOne]           ; Give global variable countOne initial value 0.
                STOR    R0  [GB+countTwo]           ; Give global variable countTwo initial value 0.
                STOR    R0  [GB+previousState]      ; Give global variable previousState initial value 0.
                STOR    R0  [GB+currentState]       ; Give global variable currentState initial value 0.

;#  
;#  Main loop that continuously executes until exit() is called or the machine gets
;#  shut down.
;#  
main:           LOAD    R0  [GB+currentState]       ; Mark the current state as old {previousState = currentState}
                STOR    R0  [GB+previousState]      ; >
                BRS     getButtonState              ; Call function getButtonState.
                PUSH    R4                          ; Update LEDs to accomodate the new buttons states {declare buttonState}
                LOAD    R0  [SP+0]                  ; Load the value of variable buttonState.
                PUSH    R0                          ; Push the value onto the stack.
                BRS     setLEDs                     ; Call function setLEDs.
                ADD     SP  1                       ; Reset the stack pointer position.
                LOAD    R0  [SP+0]                  ; Isolate the last 2 buttons {currentState = (buttonState & 3)}
                AND     R0  3                       ; >
                STOR    R0  [GB+currentState]       ; >
                LOAD    R0  0                       ; Determine what button has been pressed. {declare button}
                PUSH    R0                          ; Save the initial value of button.
                LOAD    R0  [GB+previousState]      ; Check if previousState != 3 (if-statement #52).
                CMP     R0  3                       ; >
                BNE     if52_true                   ; Branch to if-block when previousState != 3.
                BRA     if52_end                    ; Skip the if-block.
if52_true:      LOAD    R0  [GB+previousState]      ; Operation button = (previousState ^ currentState).
                XOR     R0  [GB+currentState]       ; >
                STOR    R0  [SP+0]                  ; >
if52_end:       LOAD    R0  0                       ; Dummy instruction to always make the label work.
                LOAD    R0  0                       ; Inject raw assembly                          
                LOAD    R0  [GB+countOne]           ; Makes sure the counters don't exceed the display maximum. {countOne %= DISPLAY_MAX}
                MOD     R0  DISPLAY_MAX             ; >
                STOR    R0  [GB+countOne]           ; >
                LOAD    R0  [GB+countTwo]           ; Operation countTwo %= DISPLAY_MAX.
                MOD     R0  DISPLAY_MAX             ; >
                STOR    R0  [GB+countTwo]           ; >
                LOAD    R0  [SP+0]                  ; Display the right counters
                CMP     R0  1                       ; Check if button == 1 (if-statement #71).
                BEQ     if71_true                   ; Branch to if-block when button == 1.
                LOAD    R0  [SP+0]                  ; Display the right counters
                CMP     R0  2                       ; Check if button == 2 (if-statement #69).
                BEQ     if69_true                   ; Branch to if-block when button == 2.
                BRA     if69_end                    ; Skip the if-block.
if69_true:      LOAD    R3  2                       ; Load the index of the display on the 7Segment display.
                LOAD    R0  [GB+countTwo]           ; Load the value to display on the 7Segment display.
                BRS     set7Segment                 ; Call function set7Segment.
if69_end:       LOAD    R0  0                       ; Dummy instruction to always make the label work.
                BRA     if71_end                    ; Skip the if-block.
if71_true:      LOAD    R3  1                       ; Load the index of the display on the 7Segment display.
                LOAD    R0  [GB+countOne]           ; Load the value to display on the 7Segment display.
                BRS     set7Segment                 ; Call function set7Segment.
if71_end:       LOAD    R0  0                       ; Dummy instruction to always make the label work.
                ADD     SP  2                       ; Reset stack pointer.
                BRA     main                        ; Repeat function main.

;#
;#  Routine Hex7Seg maps a number in the range [0..15] to its hexadecimal
;#  representation pattern for the 7-segment display.
;#  R0 : upon entry, contains the number
;#  R1 : upon exit,  contains the resulting pattern
;#
Hex7Seg:        BRS     Hex7Seg_bgn                 ; Push address(tbl) onto stack and proceed at "bgn".
Hex7Seg_tbl:    CONS    %01111110                   ; 7-segment pattern for '0'.
                CONS    %00110000                   ; 7-segment pattern for '1'.
                CONS    %01101101                   ; 7-segment pattern for '2'.
                CONS    %01111001                   ; 7-segment pattern for '3'.
                CONS    %00110011                   ; 7-segment pattern for '4'.
                CONS    %01011011                   ; 7-segment pattern for '5'.
                CONS    %01011111                   ; 7-segment pattern for '6'.
                CONS    %01110000                   ; 7-segment pattern for '7'.
                CONS    %01111111                   ; 7-segment pattern for '8'.
                CONS    %01111011                   ; 7-segment pattern for '9'.
                CONS    %01110111                   ; 7-segment pattern for 'A'.
                CONS    %00011111                   ; 7-segment pattern for 'b'.
                CONS    %01001110                   ; 7-segment pattern for 'C'.
                CONS    %00111101                   ; 7-segment pattern for 'd'.
                CONS    %01001111                   ; 7-segment pattern for 'E'.
                CONS    %01000111                   ; 7-segment pattern for 'F'.
Hex7Seg_bgn:    AND     R0  %01111                  ; R0 := R0 MOD 16 , just to be safe...
                LOAD    R1  [SP++]                  ; R1 = address(tbl) (retrieve from stack)
                LOAD    R1  [R1+R0]                 ; R1 = tbl[R0]
                RTS

;#
;#  Set the value of a digit on the 7 Segment-display.
;#  R0 : upon entry, contains the number to display.
;#  R3 : upon entry, contains the index of the display.
;#
set7Segment:    BRS     Hex7Seg                     ; Translate (value in) R0 into a display pattern.
                STOR    R1  [R5+DSPSEG]             ; And place this in the Display Element.
                STOR    R3  [R5+DSPDIG]             ; Activate Display Element #0.
                RTS                                 ; Exit set7Segment function.

@END
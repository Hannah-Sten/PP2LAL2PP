# PP2 Assembly Language Style Guide
Some rules and guidelines for writing code in PP2 Assembly. This is an _advisory_ styleguide, mainly for ensuring a proper and consistent code style accross developers within the same project.

### Whitespace

* Use **spaces** instead of tabs.
* Use Unix-style line endings: `\n`.

### Comments

Use meaningful and explainatory comments. Since Assembly is very low-level, almost every line needs explaining on a higher level. However, do not repeat what one can already deduce from the Assembly code in front of it. For instance, the following comment should be omitted:

```assembly
LOAD    R0  R1              ; Load the contents of register 1 into register 0
```

In this case, it would be better to explain _why_ you load `R1` into `R0`.

When a comment applies to multiple lines, put the comment on the first line and comment the following lines with `; >`.

Use as a general rule that _every line_ needs a comment, no exceptions.

There is always a single space between the `;` and the start of the comment text

### File Header and Inline Documentation
We denote comments that are used for documentation purposes with `;#`, a doc-comment for short. Every doc-comment begins and ends with an empty line, for example:
```assembly
;#
;# A nice doc-comment
;# With an aditional line
;#
```

Every file starts with a doc-comment, explaining briefly the purpose of the file and mentioning the author. Between the file header and the `@CODE` or `@DATA` desriptor is no blank line. Also, labels that are used as subroutines are documented with a doc-comment in the lines directly above. This comment explains the behaviour of the subroutine and its input and output.

### Labels
Labels are placed on the same line as the first instruction after the label. Labels are typed in camelCase and are at most 14 characters long (not including the trailing colon). There is no space between the label text and the trailing colon. Example:
```assembly
;#
;# Example label
;#
exampleLabel:   LOAD    R0  R1              ; Just to illustrate the label example
                RTS                         ; >
```

### Descriptors
Descriptors are statements that start with `@`, like `@CODE`, `@END`, `@DATA`. These are surrounded by a single blank line above and below (exception: directly after the file header; see the corresponding section). `@END` should always be the last statement of the file. Note: this means that the file ends with a single blank line.

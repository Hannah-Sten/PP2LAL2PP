<img style="float: right;" width="143" src="http://i.imgur.com/Sno5FKo.png">

The **P**racticum **P**rocessor **2** **L**earn **A**ssembly **L**anguage **2** **P**reserve **P**rosperity language is an awesome, but extremely basic little language for a horrible processor. The goal for this language and supplied (in development) compiler is to minimise the work needed to build and debug applications for the PP2-processor used solely in TU/e projects.
The compiler will be able (in the future) to generate nice `.asm`-files all complete with comments to make it understandable even in assembly-form.
Not really that much thought went into it; it is mostly a funny side project.

All hail the perfectly annoying palindrome :)

## Installation and setup

### Getting the files
Download the [release](https://github.com/Ruben-Sten/PP2LAL2PP/releases) of your choice and unpack the contents to a directory of your choice.

### Getting Java 8+
You need at least Java 8 in order to run PP2LAL2PP. You can get java [here](https://java.com/download/).

## How to compile

### Starting the program
You can start the PP2LAL2PP program using the following command in the command prompt:
```
java -jar PP2LAL2PP.jar
```
If you run this exact command you'll be welcomed by the help screen.

### Compiling your program
Use the command below to compile your program.
```
java -jar PP2LAL2PP.jar [-args] <fileName>
```
**fileName** is the name of your PP2LAL2PP source file and is required in order to compile. *-args* is an optional list of flags (see section below) that modify the default behaviour of the program. Without any flags, the program will compile the PP2LAL2PP source to an assembly file with the same name. However, with a `.asm` extension.

## How to refactor
Not possible (yet).

## Syntax and language features
See [Language Specification.md](docs/Language Specification.md).

## Base API
See [Base API.md](docs/Base API.md).

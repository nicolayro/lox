# Lox Programming Language - Crafting Interpreters

Interpreter and compiler implementation in Java and C based on [Crafting Interpreters](https://craftinginterpreters.com/introduction.html) book. The main goal of this project is to get a better understanding of interpreters, compilers and programming languages, as well as improving general programming skills, especially in C.

## Project Overview

- jlox: Interpreter implementation in Java.
- clox: Compiler implementation in C.
- examples/tests: Working example Lox programs, which are also used for testing.
- notes: Some thoughts along the way.
- vim: basic syntax highlighting of .lox files for vim.

## Language Overview

Simple Hello, World program:
```lox
print "Hello, World";
```

### Features
- Variable declaration, assignment and reassignment.
- First-class functions
- Classes (with inheritance)
- Control-flow (if, else, while, forL)
- Addition, subtraction, multiplication and division
- Closures
- Dynamic typing
- Static variable resolution and error detection.
- Garbage collection

Example programs can be found in the [examples](examples) folder.

## Jlox

> Interpreter of the Lox programming language, written in Java.

### Installation

Clone the repository to local machine:
```shell
git clone https://github.com/nicolayro/lox.git
```

Compile source code:
```shell
./compile
```

You're now ready to run Lox scripts!

### Usage

The Java interpreter runs Lox programs. You can find some examples of these
in the [examples](examples) folder.

Run `.lox` source code:
```shell
./run <file>
```

You can also run it interactively in a REPL:
```shell
./run
```
Use `Crtl-C`  to exit.

### Program flow
The interpreter is structured in the following way, from source code to execution:

<img alt="flow.png|50" src="flow.png" width="600"/>


## Tests

 The current test suite for this project is a simple output checker for programs in [examples](examples) directory.
 It compares the output of each program with the expected output, defined in the similarily named
 `.txt` file.

To run the tests:

 ```shell
 ./test
 ```


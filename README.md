# Interpreter

Interpreter written based on [Crafting Interpreters](https://craftinginterpreters.com/introduction.html) book. The main goal of this project is to get a better understanding of interpreters, compilers and programming languages, as well as improving general programming skills, especially in C.

## Contents

- jlox: Simple implementation in Java.
- clox: Full implementation in C.
- examples: Working example Lox programs.
- notes: Some thoughs from along the way

## Usage (jlox)

To run jlox, simply compile and run Jlox.java. For example:

Compile:
```sh
javac -d out/ Jlox.java
```

Run
```sh
java -cp out/ Jlox
```

Run `.lox` source code:
```sh
java -cp out/ Jlox <file>
```


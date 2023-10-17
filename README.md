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
```shell
javac -d target/ jlox/Jlox.java
```

Run
```shell
java -cp target/ jlox.Jlox
```

Run `.lox` source code:
```shell
java -cp target/ jlox.Jlox <file>
```

There is also a small helper script `compile_and_run.sh`
```shell
./compile_and_run <file>
```


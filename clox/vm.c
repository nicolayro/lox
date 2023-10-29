#include <stdio.h>

#include "chunk.h"
#include "common.h"
#include "compiler.h"
#include "debug.h"
#include "vm.h"

VM vm;

static void resetStack() {
    vm.stackTop = vm.stack;
}

void initVM() {
    resetStack();
}

void freeVM() {}

void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

Value pop() {
    vm.stackTop--;
    return *vm.stackTop;
}

static InterpretResult run() {
    #define READ_BYTE() (*vm.ip++)
    #define READ_CONST() (vm.chunk->constants.values[READ_BYTE()])

    for (;;) {
    #ifdef DEBUG_TRACE_EXECUTION
        printf("          ");
        for (Value* slot = vm.stack; slot < vm.stackTop; slot++) {
            printf("[ ");
            printValue(*slot);
            printf(" ]");
        }
        printf("\n");
        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
    #endif

        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONST: {
                Value constant = READ_CONST();
                push(constant);
                break;
            }
            case OP_ADD: {
                Value a = pop();
                Value b = pop();
                push(a + b);
                break;
            }
            case OP_SUBTRACT: {
                Value a = pop();
                Value b = pop();
                push(b - a);
                break;
            }
            case OP_MULTIPLY: {
                Value a = pop();
                Value b = pop();
                push(b * a);
                break;
            }
            case OP_DIVIDE: {
                Value a = pop();
                Value b = pop();
                push(b / a);
                break;
            }
            case OP_NEGATE: {
                Value a = pop();
                push(-a);
                break;
            }
            case OP_RETURN: {
                printValue(pop());
                printf("\n");
                return INTERPRET_OK;
            }
        }
    }

    #undef READ_BYTE
    #undef READ_CONST
}

InterpretResult interpret(const char* source) {
    compile(source);
    return INTERPRET_OK;
}       

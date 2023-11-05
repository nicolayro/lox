CC=gcc
CFLAGS+= -Wall -std=c11

OBJECTS:= main.o chunk.o debug.o memory.o value.o vm.o compiler.o lexer.o \
		  object.o table.o

clox: $(OBJECTS) 
	$(CC) $(CFLAGS) -o $@ $(OBJECTS)

.PHONY: clean run

clean:
	rm -f clox $(OBJECTS)

run:
	make clox
	./clox

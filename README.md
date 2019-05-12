# Nand2Tetris

Nand2Tetris is a course provided by Professors Shimon Schocken and Noam Nisan in which the student builds a 16-bit computer from the ground up.  
Part 1 of the course focuses on hardware, and in Part 2 you implement the software.

## Project 1

Project 1 involved creating 15 basic chips, including the elementary logic gates, and some (de)multiplexors. 

## Project 2

In Project 2, I built the ALU, after building half, full and 16 bit adders, as well as an incrementer.

## Project 3

In Project 3, focusing on sequential logic, I built a single bit register from elementary data flip flops, and expanded that up to RAM with 16K 16-bit registers. I also implemented a Program Counter out of DMux, a Register and an Incrementer.

## Project 4

In Project 4, I used the HACK machine language to write Mult.asm, which multiplies 2 numbers together, and Fill.asm, which fills the screen with black pixels whilst a key is pressed, and empties it if a key is not pressed.

## Project 5

Project 5 completed the Hardware implementation, by building a CPU, memory, and combining them with a builtin ROM32K chip in order to complete the Hack computer.

## Project 6

In Project 6, I implemented a 2 pass assembler for the Hack machine language in Java. To run the assembler, build/compile it and then run with the path to a .asm file (written in the Hack assembly language) that you wish to assemble as an argument.

## Project 7

In Project 7, I started on the software layer for the Hack computer, writing a VM translator which can handle arithmetic, boolean and logical commands, as well as memory access commands, and can convert .vm files into the Hack assembly language.

## Project 8

In Project 8, I completed the VM translator, implementing functions and branching, as well as the ability to process directories. Please note that if you choose to translate a directory, you *MUST* supply a Sys.vm file containing Sys.init, otherwise the translation will be incorrect. Individual files will be translated fine without, though.    

I also implemented some bug fixes. Note that this means my implementation of Project 7 DOES contain bugs. They will be left in as a learning exercise - see, for example, the change from .equals to .contains in the parser, to handle inline comments.

## Project 9 

Project 9 involved writing programs in the high level Jack language. I did not implement this project because my goal is to understand computing systems, rather than programming in some
arbitrary high level language.

## Project 10

In Project 10, I implemented the syntax analysis function of the Jack compiler, which takes a .jack file or directory containing multiple .jack files, and converts them into .xml parse trees. Originally, I left the tokenisation and parsing as two separate passes through the file, which was inefficient. I fixed this by refactoring the program to tokenise and parse in the same pass by creating the tokeniser in the compilation class.

## Project 11

Project 11 is still a work in progress - it is mostly complete however will not compile Pong or ComplexArrays correctly yet. It's just a case of ironing out the last couple of bugs. 


### More Coming Soon ###
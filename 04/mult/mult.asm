// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

  // Store 0 in 'R2'.
  @0  // Select register 0.
  D=A // Store 0 in data register.
  @R2  // Select RAM[2].
  M=D // Store contents of data register (0) in R2.

  // initialise i to 1.
  @1 // Select register 1.
  D=A // Store 1 in data register. 
  @i  // Select register i.
  M=D // Store contents of data register (1) in i.

  // initialise n to value of R1.
  @R1 // Select RAM[1].
  D=M // Store contents of RAM[1] in data register.
  @n  // Select register n.
  M=D // Store contents of data register (contents of RAM[1]) in n.

(LOOP)
  // Break loop if i = n, else add contents of R0 to R2.
  @i  // Select i register.
  D=M // Store value of i register in data register.
  @n // Select n register.
  D=D-M // Stores in the data register the value of i - n
  @STOP // Select the 'stop' register.
  D;JGT// Jump to STOP if i > n.

  @R0 // Select RAM[0].
  D=M // Store contents of RAM[0] in data register.
  @R2  // Select register R2.
  M=D+M // Store in register R2 the current value plus the contents of the data register (contents of RAM[0]).

  // Increment the loop.  
  @i  // Select i register.
  M=M+1 // Increment value in i register by 1.
  @LOOP // Select LOOP instruction.
  0;JMP // Jump to LOOP instruction.

(STOP)
  0;JMP // Jump to STOP instruction in infinite loop (will always have STOP in A here.)











  
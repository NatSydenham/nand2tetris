// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

(START)
  // Prep - Store the current pixel in a variable.
  @SCREEN // Select the screen.
  D=A // Store the address of the first screen pixel in data variable.
  @pix // Select a register used to hold current pixel.
  M=D // Store current pixel in register.

(LOOP)
  // View contents of keyboard and execute appropriately.
  @KBD // Select the keyboard.
  D=M // Probe the value of the keyboard.
  @WHITE // Select WHITE label.
  D;JEQ // Jump to WHITE if keyboard is 0.
  @BLACK // Select BLACK label.
  0;JMP // Jump to BLACK.

(BLACK)
  // Fill pixel.
  @colour // Select colour register.
  M=-1 // Fill colour register with black.
  @FILL // Select FILL label.
  0;JMP // Jump to FILL label.

(WHITE)
  // Empty pixel.
  @colour // Select colour register.
  M=0 // Fill colour register with white.
  @FILL // Select FILL label.
  0;JMP // Jump to FILL label.

(FILL)
  @colour // Select the colour.
  D=M // Store colour in data register
  @pix // Select the pixel.
  A=M // Select register pointed to by pix.
  M=D // Set pixel to correct colour.

  // If not at end of screen, select next pixel and jump to fill instruction. Else, jump to beginning of loop.
  @pix // Select pix register.
  M=M+1 // Increment value.
  D=M // Store new value of pix in data register.
  @KBD // Select Keyboard.
  D=D-A // Store address of keyboard - address of pixel in data register.
  @START // Select START label.
  D;JEQ // If D == 0, we have filled the screen and should start again.
  @FILL // Select LOOP label.
  0;JMP // We have not filled the screen so we should fill next pixel.
















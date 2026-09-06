package com.gstncaruso.tabpro.format.guitarpro;

/** Un canal MIDI tal como lo guarda el archivo, todavia sin puerto ni numero. */
record GuitarProChannel(int program, int volume, int pan, int chorus, int reverb, int phaser, int tremolo) {
}

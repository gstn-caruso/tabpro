package com.gstncaruso.tabpro.midi;

interface Delay {

    void after(long millis, Runnable action);
}

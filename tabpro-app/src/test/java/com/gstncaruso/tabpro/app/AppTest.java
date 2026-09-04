package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void greetsWithTheProjectName() {
        assertEquals("tabpro — editor de tablaturas", new App().greeting());
    }
}

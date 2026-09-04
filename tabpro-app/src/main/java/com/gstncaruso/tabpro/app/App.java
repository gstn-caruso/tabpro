package com.gstncaruso.tabpro.app;

public class App {

    public String greeting() {
        return "tabpro — editor de tablaturas";
    }

    public static void main(String[] args) {
        System.out.println(new App().greeting());
    }
}

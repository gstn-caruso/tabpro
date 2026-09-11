package com.gstncaruso.tabpro.core.model;

public sealed interface TuningName {

    record UserNamed(String name) implements TuningName {
    }

    record Custom() implements TuningName {
    }
}

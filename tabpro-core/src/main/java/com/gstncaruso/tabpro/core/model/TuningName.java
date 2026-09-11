package com.gstncaruso.tabpro.core.model;

public sealed interface TuningName {

    record Library(String id) implements TuningName {
    }

    record UserNamed(String name) implements TuningName {
    }

    record Custom() implements TuningName {
    }
}

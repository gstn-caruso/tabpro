package com.gstncaruso.tabpro.ui.theme;

import java.util.List;

public interface ThemeSwitch {

    ThemeSwitch NONE = new ThemeSwitch() {

        @Override
        public List<String> names() {
            return List.of();
        }

        @Override
        public String current() {
            return "";
        }

        @Override
        public void apply(String name) {
        }

        @Override
        public void useFontSize(int points) {
        }

        @Override
        public void useHighContrast(boolean enabled) {
        }

        @Override
        public void useAnimations(boolean enabled) {
        }
    };

    List<String> names();

    String current();

    void apply(String name);

    void useFontSize(int points);

    void useHighContrast(boolean enabled);

    void useAnimations(boolean enabled);
}

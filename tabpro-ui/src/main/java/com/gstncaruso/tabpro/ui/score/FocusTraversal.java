package com.gstncaruso.tabpro.ui.score;

import java.awt.Component;
import java.awt.KeyboardFocusManager;

interface FocusTraversal {

    void next(Component component);

    void previous(Component component);

    static FocusTraversal usingKeyboardFocusManager() {
        return new FocusTraversal() {
            @Override
            public void next(Component component) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(component);
            }

            @Override
            public void previous(Component component) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusPreviousComponent(component);
            }
        };
    }
}

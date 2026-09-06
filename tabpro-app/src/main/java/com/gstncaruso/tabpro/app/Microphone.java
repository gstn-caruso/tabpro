package com.gstncaruso.tabpro.app;

import com.gstncaruso.tabpro.midi.MicrophonePitch;
import com.gstncaruso.tabpro.ui.actions.Ports;
import java.util.function.Consumer;

/** La entrada de audio de la maquina, tal como la usa el afinador digital. */
final class Microphone implements Ports.Microphone {

    private final MicrophonePitch input = new MicrophonePitch();

    @Override
    public boolean isAvailable() {
        return MicrophonePitch.isAvailable();
    }

    @Override
    public void startListening(Consumer<Ports.HeardPitch> heard) {
        try {
            input.start(detected -> heard.accept(new Ports.HeardPitch(
                    detected.isAudible(), detected.nearestMidiNumber(), detected.frequencyHz())));
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            heard.accept(Ports.HeardPitch.nothing());
        }
    }

    @Override
    public void stopListening() {
        input.close();
    }
}

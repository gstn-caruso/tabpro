package com.gstncaruso.tabpro.core.tuning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gstncaruso.tabpro.core.model.Pitch;
import org.junit.jupiter.api.Test;

class PitchDetectorTest {

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLES = 8192;

    private final PitchDetector detector = new PitchDetector(SAMPLE_RATE);

    @Test
    void hearsTheAOfFourFortyHertz() {
        DetectedPitch heard = detector.detect(sine(440));

        assertTrue(heard.isAudible());
        assertEquals(440, heard.frequencyHz(), 2);
        assertEquals(69, heard.nearestMidiNumber());
    }

    @Test
    void hearsTheLowStringOfAGuitar() {
        DetectedPitch heard = detector.detect(sine(DetectedPitch.frequencyOf(new Pitch(40))));

        assertEquals(40, heard.nearestMidiNumber());
    }

    @Test
    void hearsTheHighStringOfAGuitar() {
        DetectedPitch heard = detector.detect(sine(DetectedPitch.frequencyOf(new Pitch(64))));

        assertEquals(64, heard.nearestMidiNumber());
    }

    @Test
    void hearsAStringWithItsHarmonics() {
        double fundamental = DetectedPitch.frequencyOf(new Pitch(45));
        double[] samples = new double[SAMPLES];
        for (int index = 0; index < samples.length; index++) {
            double time = index / (double) SAMPLE_RATE;
            samples[index] = Math.sin(2 * Math.PI * fundamental * time)
                    + 0.5 * Math.sin(4 * Math.PI * fundamental * time)
                    + 0.25 * Math.sin(6 * Math.PI * fundamental * time);
        }

        assertEquals(45, detector.detect(samples).nearestMidiNumber());
    }

    @Test
    void silenceIsNotAString() {
        assertFalse(detector.detect(new double[SAMPLES]).isAudible());
    }

    @Test
    void noiseIsNotAString() {
        java.util.Random random = new java.util.Random(7);
        double[] noise = new double[SAMPLES];
        for (int index = 0; index < noise.length; index++) {
            noise[index] = random.nextDouble() * 2 - 1;
        }

        assertFalse(detector.detect(noise).isAudible());
    }

    @Test
    void aStringSlightlySharpReadsAsSharp() {
        Pitch target = new Pitch(64);
        double quarterToneUp = DetectedPitch.frequencyOf(target) * Math.pow(2, 25 / 1200.0);

        int cents = detector.detect(sine(quarterToneUp)).centsFrom(target);

        assertTrue(cents > 15 && cents < 35, "leyó " + cents + " centésimas");
    }

    @Test
    void aStringSlightlyFlatReadsAsFlat() {
        Pitch target = new Pitch(64);
        double quarterToneDown = DetectedPitch.frequencyOf(target) * Math.pow(2, -25 / 1200.0);

        assertTrue(detector.detect(sine(quarterToneDown)).centsFrom(target) < 0);
    }

    @Test
    void aStringInTuneReadsAsZero() {
        Pitch target = new Pitch(50);

        assertEquals(0, detector.detect(sine(DetectedPitch.frequencyOf(target))).centsFrom(target), 3);
    }

    private static double[] sine(double frequencyHz) {
        double[] samples = new double[SAMPLES];
        for (int index = 0; index < samples.length; index++) {
            samples[index] = Math.sin(2 * Math.PI * frequencyHz * index / SAMPLE_RATE);
        }
        return samples;
    }
}

package com.gstncaruso.tabpro.core.playback;

public record SpeedTrainer(int startTempo, int endTempo, int incrementPerLap) {

    public SpeedTrainer {
        if (startTempo <= 0) {
            throw new IllegalArgumentException("startTempo must be > 0: " + startTempo);
        }
        if (endTempo < startTempo) {
            throw new IllegalArgumentException("endTempo cannot be less than startTempo");
        }
        if (incrementPerLap <= 0) {
            throw new IllegalArgumentException("incrementPerLap must be > 0: " + incrementPerLap);
        }
    }

    public int tempoForLap(int lap) {
        long tempo = (long) startTempo + (long) incrementPerLap * lap;
        return (int) Math.min(tempo, endTempo);
    }

    public boolean reachedFinalTempo(int lap) {
        return tempoForLap(lap) == endTempo;
    }
}

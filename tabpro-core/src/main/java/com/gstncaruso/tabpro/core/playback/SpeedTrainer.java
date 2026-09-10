package com.gstncaruso.tabpro.core.playback;

public record SpeedTrainer(int startTempo, int endTempo, int incrementPerLap) {

    public SpeedTrainer {
        if (startTempo <= 0) {
            throw new IllegalArgumentException("startTempo debe ser > 0: " + startTempo);
        }
        if (endTempo < startTempo) {
            throw new IllegalArgumentException("endTempo no puede ser menor que startTempo");
        }
        if (incrementPerLap <= 0) {
            throw new IllegalArgumentException("incrementPerLap debe ser > 0: " + incrementPerLap);
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

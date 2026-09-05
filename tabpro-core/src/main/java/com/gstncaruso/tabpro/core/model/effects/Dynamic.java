package com.gstncaruso.tabpro.core.model.effects;

/** Cuan fuerte se toca una nota, de muy suave a muy fuerte. */
public enum Dynamic {
    PIANO_PIANISSIMO("ppp", 15),
    PIANISSIMO("pp", 31),
    PIANO("p", 47),
    MEZZO_PIANO("mp", 63),
    MEZZO_FORTE("mf", 79),
    FORTE("f", 95),
    FORTISSIMO("ff", 111),
    FORTE_FORTISSIMO("fff", 127);

    private final String symbol;
    private final int velocity;

    Dynamic(String symbol, int velocity) {
        this.symbol = symbol;
        this.velocity = velocity;
    }

    public static Dynamic defaultDynamic() {
        return MEZZO_FORTE;
    }

    public String symbol() {
        return symbol;
    }

    public int velocity() {
        return velocity;
    }

    public Velocity intensity() {
        return new Velocity(velocity);
    }

    public Dynamic louder() {
        return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    public Dynamic softer() {
        return values()[Math.max(0, ordinal() - 1)];
    }

    public Velocity accented() {
        return intensity().accented();
    }

    public Velocity ghosted() {
        return intensity().ghosted();
    }
}

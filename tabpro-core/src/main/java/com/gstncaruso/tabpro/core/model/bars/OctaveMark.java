package com.gstncaruso.tabpro.core.model.bars;

/**
 * 8va, 8vb, 15ma y 15mb del manual: cambian donde se escribe una nota en el pentagrama, no como
 * suena ni que dice la tablatura. Sirven para no llenar el pentagrama de lineas adicionales -8va
 * y 15ma escriben mas abajo lo que suena muy agudo (sigue sonando una o dos octavas mas arriba de
 * lo escrito); 8vb y 15mb escriben mas arriba lo que suena muy grave.
 *
 * <p>El pentagrama de guitarra ya se escribe una octava arriba de lo que suena (convencion fija,
 * ver {@code StaffPosition}); esta marca es un corrimiento adicional, puramente de dibujo, sobre
 * esa base.
 */
public enum OctaveMark {
    NONE("", 0, false),
    OTTAVA_ALTA("8va", -7, true),
    OTTAVA_BASSA("8vb", 7, false),
    QUINDICESIMA_ALTA("15ma", -14, true),
    QUINDICESIMA_BASSA("15mb", 14, false);

    private final String label;
    private final int staffStepShift;
    private final boolean aboveTheStaff;

    OctaveMark(String label, int staffStepShift, boolean aboveTheStaff) {
        this.label = label;
        this.staffStepShift = staffStepShift;
        this.aboveTheStaff = aboveTheStaff;
    }

    public String label() {
        return label;
    }

    /** Cuantos grados diatonicos hay que correr la posicion escrita (7 por octava). */
    public int staffStepShift() {
        return staffStepShift;
    }

    /** 8va/15ma van arriba del pentagrama (para lo agudo); 8vb/15mb, abajo (para lo grave). */
    public boolean aboveTheStaff() {
        return aboveTheStaff;
    }
}

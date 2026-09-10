package com.gstncaruso.tabpro.core.model.bars;

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

    public int staffStepShift() {
        return staffStepShift;
    }

    public boolean aboveTheStaff() {
        return aboveTheStaff;
    }
}

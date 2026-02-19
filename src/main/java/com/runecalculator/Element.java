package com.runecalculator;

public enum Element
{
    AIR(1),
    WATER(1 << 1),
    EARTH(1 << 2),
    FIRE(1 << 3);

    private final int mask;

    Element(int mask) {
        this.mask = mask;
    }

    public int mask() {
        return mask;
    }
}

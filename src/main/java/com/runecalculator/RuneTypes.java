package com.runecalculator;

import java.util.List;

public enum RuneTypes {
    AIR(Element.AIR),
    MIND,
    WATER(Element.WATER),
    MIST(Element.AIR, Element.WATER),
    EARTH(Element.EARTH),
    DUST(Element.AIR, Element.EARTH),
    MUD(Element.WATER, Element.EARTH),
    FIRE(Element.FIRE),
    SMOKE(Element.AIR, Element.FIRE),
    STEAM(Element.WATER, Element.FIRE),
    BODY,
    LAVA(Element.EARTH, Element.FIRE),
    COSMIC,
    SUNFIRE,
    CHAOS,
    ASTRAL,
    NATURE,
    LAW,
    DEATH,
    BLOOD,
    SOUL,
    AETHER,
    WRATH
    ;

    private final int mask;

    RuneTypes() {
        this.mask = 0;
    }

    RuneTypes(Element... elements) {
        int m = 0;
        for (Element e : elements) {
            m |= e.mask();
        }
        this.mask = m;
    }

    public int mask() {
        return mask;
    }

    @Override
    public String toString() {
        return this.name().isEmpty() ? this.name() : this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
}
package com.runecalculator;

public enum RuneTypes {
    AIR,
    MIND,
    WATER,
    MIST,
    EARTH,
    DUST,
    MUD,
    FIRE,
    SMOKE,
    STEAM,
    BODY,
    LAVA,
    COSMIC,
    SUNFIRE,
    CHAOS,
    ASTRAL,
    NATURE,
    LAW,
    DEATH,
    BLOOD,
    AETHER,
    SOUL,
    WRATH
    ;

    @Override
    public String toString() {
        return this.name().isEmpty() ? this.name() : this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
}
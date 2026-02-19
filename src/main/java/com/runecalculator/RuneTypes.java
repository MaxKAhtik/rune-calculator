package com.runecalculator;

import lombok.Getter;
import net.runelite.api.gameval.ItemID;

@Getter
public enum RuneTypes
{
    AIR(ItemID.AIRRUNE, Element.AIR),
    MIND(ItemID.MINDRUNE),
    WATER(ItemID.WATERRUNE, Element.WATER),
    MIST(ItemID.MISTRUNE, Element.AIR, Element.WATER),
    EARTH(ItemID.EARTHRUNE, Element.EARTH),
    DUST(ItemID.DUSTRUNE, Element.AIR, Element.EARTH),
    MUD(ItemID.MUDRUNE, Element.WATER, Element.EARTH),
    FIRE(ItemID.FIRERUNE, Element.FIRE),
    SMOKE(ItemID.SMOKERUNE, Element.AIR, Element.FIRE),
    STEAM(ItemID.STEAMRUNE, Element.WATER, Element.FIRE),
    BODY(ItemID.BODYRUNE),
    LAVA(ItemID.LAVARUNE, Element.EARTH, Element.FIRE),
    COSMIC(ItemID.COSMICRUNE),
    SUNFIRE(ItemID.SUNFIRERUNE),
    CHAOS(ItemID.CHAOSRUNE),
    ASTRAL(ItemID.ASTRALRUNE),
    NATURE(ItemID.NATURERUNE),
    LAW(ItemID.LAWRUNE),
    DEATH(ItemID.DEATHRUNE),
    BLOOD(ItemID.BLOODRUNE),
    SOUL(ItemID.SOULRUNE),
    AETHER(ItemID.AETHERRUNE),
    WRATH(ItemID.WRATHRUNE)
    ;

    private final int id;
    private final int mask;

    RuneTypes(int id)
    {
        this.id = id;
        this.mask = 0;
    }

    RuneTypes(int id, Element... elements)
    {
        this.id = id;

        int m = 0;
        for (Element e : elements)
        {
            m |= e.mask();
        }
        this.mask = m;
    }

    public boolean isElemental()
    {
        return mask != 0;
    }

    @Override
    public String toString()
    {
        return this.name().isEmpty() ? this.name() : this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
}
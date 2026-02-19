package com.runecalculator;

import lombok.Getter;

import net.runelite.api.gameval.SpriteID;

import java.util.EnumSet;

import static com.runecalculator.RuneTypes.*;

@Getter
public enum SpellData
{
    WIND_STRIKE("Wind Strike", SpriteID.Magicon.WIND_STRIKE, AIR, MIND),
    CONFUSE("Confuse", SpriteID.Magicon.CONFUSE, WATER, EARTH, BODY),
    ENCHANT_OPAL_BOLT("Enchant Opal Bolt", SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, AIR, COSMIC),
    WATER_STRIKE("Water Strike", SpriteID.Magicon.WATER_STRIKE, AIR, WATER, MIND),
    ARCEUUS_LIBRARY_TELEPORT("Arceuus Library Teleport", SpriteID.MagicNecroOn.ARCEUUS_LIBRARY_TELEPORT, EARTH, LAW),
    ENCHANT_SAPPHIRE_BOLT("Enchant Sapphire Bolt", SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, WATER, COSMIC, MIND),
    ENCHANT_SAPPHIRE_JEWELLERY("Enchant Sapphire Jewellery", SpriteID.Magicon.LVL_1_ENCHANT, WATER, COSMIC),
    EARTH_STRIKE("Earth Strike", SpriteID.Magicon.EARTH_STRIKE, AIR, EARTH, MIND),
    WEAKEN("Weaken", SpriteID.Magicon.WEAKEN, EARTH, WATER, BODY),
    FIRE_STRIKE("Fire Strike", SpriteID.Magicon.FIRE_STRIKE, AIR, FIRE, MIND),
    ENCHANT_JADE_BOLT("Enchant Jade Bolt", SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, EARTH, COSMIC)
    ;

    private final String spellName;
    private final int spriteID;
    private final EnumSet<RuneTypes> runes;

    SpellData(String name, int id, RuneTypes... runes)
    {
        this.spellName = name;
        this.spriteID = id;
        this.runes = EnumSet.of(runes[0], runes);
    }
}
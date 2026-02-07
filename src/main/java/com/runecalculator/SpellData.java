package com.runecalculator;

import lombok.AllArgsConstructor;
import lombok.Getter;

import net.runelite.api.gameval.SpriteID;

import java.util.EnumSet;

import static com.runecalculator.RuneTypes.*;

@AllArgsConstructor
@Getter
public enum SpellData {
    WIND_STRIKE("Wind Strike", SpriteID.Magicon.WIND_STRIKE, SpellRunes.WIND_STRIKE),
    CONFUSE("Confuse", SpriteID.Magicon.CONFUSE, SpellRunes.CONFUSE),
    ENCHANT_OPAL_BOLT("Enchant Opal Bolt", SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, SpellRunes.ENCHANT_OPAL_BOLT),
    WATER_STRIKE("Water Strike", SpriteID.Magicon.WATER_STRIKE, SpellRunes.WATER_STRIKE),
    ARCEUUS_LIBRARY_TELEPORT("Arceuus Library Teleport", SpriteID.MagicNecroOn.ARCEUUS_LIBRARY_TELEPORT, SpellRunes.ARCEUUS_LIBRARY_TELEPORT),
    ENCHANT_SAPPHIRE_BOLT("Enchant Sapphire Bolt", SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, SpellRunes.ENCHANT_SAPPHIRE_BOLT),
    ENCHANT_SAPPHIRE_JEWELLERY("Enchant Sapphire Jewellery", SpriteID.Magicon.LVL_1_ENCHANT, SpellRunes.ENCHANT_SAPPHIRE_JEWELRY),
    EARTH_STRIKE("Earth Strike", SpriteID.Magicon.EARTH_STRIKE, SpellRunes.EARTH_STRIKE),
    WEAKEN("Weaken", SpriteID.Magicon.WEAKEN, SpellRunes.WEAKEN),
    FIRE_STRIKE("Fire Strike", SpriteID.Magicon.FIRE_STRIKE, SpellRunes.FIRE_STRIKE)
    ;

    private final String spellName;
    private final int spriteID;
    private final SpellRunes runes;

    @Getter
    private enum SpellRunes {
        WIND_STRIKE(AIR, MIND),
        CONFUSE(WATER, EARTH, BODY),
        ENCHANT_OPAL_BOLT(AIR, COSMIC),
        WATER_STRIKE(AIR, WATER, MIND),
        ARCEUUS_LIBRARY_TELEPORT(EARTH, LAW),
        ENCHANT_SAPPHIRE_BOLT(WATER, COSMIC, MIND),
        ENCHANT_SAPPHIRE_JEWELRY(WATER, COSMIC),
        EARTH_STRIKE(AIR, EARTH, MIND),
        WEAKEN(EARTH, WATER, BODY),
        FIRE_STRIKE(AIR, FIRE, MIND)
        ;

        private final EnumSet<RuneTypes> runes;

        SpellRunes(RuneTypes... runes) {
            this.runes = runes.length == 0
                ? EnumSet.noneOf(RuneTypes.class)
                : EnumSet.of(runes[0], runes);
        }
    }
}

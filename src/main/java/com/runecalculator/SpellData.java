package com.runecalculator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.gameval.SpriteID;

import java.util.EnumSet;

@AllArgsConstructor
@Getter
public enum SpellData {
    WIND_STRIKE("Wind Strike", SpriteID.Magicon.WIND_STRIKE, SpellRunes.WIND_STRIKE),
    CONFUSE("Confuse", SpriteID.Magicon.CONFUSE, SpellRunes.CONFUSE),
    ENCHANT_OPAL_BOLT("Enchant Opal Bolt", SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, SpellRunes.ENCHANT_OPAL_BOLT)
    ;

    private final String spellName;
    private final int spriteID;
    private final SpellRunes runes;

    @Getter
    private enum SpellRunes {
        WIND_STRIKE(RuneTypes.AIR, RuneTypes.MIND),
        CONFUSE(RuneTypes.WATER, RuneTypes.EARTH, RuneTypes.BODY),
        ENCHANT_OPAL_BOLT(RuneTypes.AIR, RuneTypes.COSMIC)
        ;

        private final EnumSet<RuneTypes> runes;

        SpellRunes(RuneTypes... runes) {
            this.runes = runes.length == 0
                ? EnumSet.noneOf(RuneTypes.class)
                : EnumSet.of(runes[0], runes);
        }
    }
}

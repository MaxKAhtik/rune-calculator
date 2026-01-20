package com.runecalculator;

import lombok.AllArgsConstructor;
import net.runelite.api.gameval.SpriteID;

@AllArgsConstructor
public enum SpellData {
    WIND_STRIKE("Wind Strike", SpriteID.Magicon.WIND_STRIKE, SpellRunes.of(RuneTypes.AIR, RuneTypes.MIND)),
    CONFUSE("Confuse", SpriteID.Magicon.CONFUSE, SpellRunes.of(RuneTypes.WATER, RuneTypes.EARTH, RuneTypes.BODY)),
    ENCHANT_OPAL_BOLT("Enchant Opal Bolt", SpriteID.Magicon2.ENCHANT_CROSSBOW_BOLT, SpellRunes.of(RuneTypes.AIR, RuneTypes.COSMIC))
    ;

    private final String spellName;
    private final int spriteID;
    private final SpellRunes runes;
}

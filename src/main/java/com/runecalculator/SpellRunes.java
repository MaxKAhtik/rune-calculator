package com.runecalculator;

import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.EnumSet;

@AllArgsConstructor
@Getter
public class SpellRunes {
    private final EnumSet<RuneTypes> runes;

    public static SpellRunes of(RuneTypes... runeTypes) {
        return new SpellRunes(runeTypes.length == 0 ? EnumSet.noneOf(RuneTypes.class) : EnumSet.of(runeTypes[0], runeTypes));
    }

}

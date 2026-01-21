package com.runecalculator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.util.EnumSet;

@AllArgsConstructor
@Getter
@Setter
class RuneCalculator {
    private EnumSet<SpellData> spellSet;
    private EnumSet<RuneTypes> usableRunes;
    private static EnumSet<RuneTypes> initialUsableRunes = EnumSet.of(
        RuneTypes.AIR,
        RuneTypes.MIND,
        RuneTypes.WATER,
        RuneTypes.EARTH,
        RuneTypes.FIRE,
        RuneTypes.BODY,
        RuneTypes.COSMIC,
        RuneTypes.CHAOS,
        RuneTypes.ASTRAL,
        RuneTypes.NATURE,
        RuneTypes.LAW,
        RuneTypes.DEATH,
        RuneTypes.BLOOD,
        RuneTypes.SOUL,
        RuneTypes.WRATH
    );

    public RuneCalculator() {
        this.usableRunes = EnumSet.copyOf(initialUsableRunes);
        this.spellSet = EnumSet.noneOf(SpellData.class);
    }

    void adjustRuneSet(JCheckBox target, EnumSet<RuneTypes> runes)
    {
        if (target.isSelected())
        {
            usableRunes.addAll(runes);
        }
        else
        {
            usableRunes.removeAll(runes);
        }
        calculateRunes();
    }

    private void calculateRunes() {

    }
}
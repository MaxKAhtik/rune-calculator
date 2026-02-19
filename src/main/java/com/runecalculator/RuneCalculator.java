package com.runecalculator;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.swing.*;
import java.util.*;
import java.util.List;

import static com.runecalculator.RuneTypes.*;

@Slf4j
class RuneCalculator extends JPanel
{
    private static final EnumSet<RuneTypes> INITIAL_USABLE_RUNES = EnumSet.of(
        AIR,
        MIND,
        WATER,
        EARTH,
        FIRE,
        BODY,
        COSMIC,
        CHAOS,
        ASTRAL,
        NATURE,
        LAW,
        DEATH,
        BLOOD,
        SOUL,
        WRATH
    );

    private static final RuneTypes[] ELEMENTAL_OPTIONS = Arrays.stream(RuneTypes.values())
        .filter(RuneTypes::isElemental)
        .toArray(RuneTypes[]::new);

    private final EnumSet<RuneTypes> usableRunes;
    private final EnumSet<RuneTypes> infiniteRuneSources;
    private final EnumSet<SpellData> spellSet = EnumSet.noneOf(SpellData.class);
    private final List<EnumSet<RuneTypes>> runeSets = new ArrayList<>();

    @Inject
    RuneCalculator()
    {
        usableRunes = EnumSet.copyOf(INITIAL_USABLE_RUNES);
        infiniteRuneSources = EnumSet.noneOf(RuneTypes.class);
    }

    public EnumSet<SpellData> getSpellSet()
    {
        return spellSet;
    }

    public List<EnumSet<RuneTypes>> getRuneSets()
    {
        return runeSets;
    }

    public void addUsableRunes(EnumSet<RuneTypes> runes)
    {
        usableRunes.addAll(runes);
        calculateRunes();
    }

    public void removeUsableRunes(EnumSet<RuneTypes> runes)
    {
        usableRunes.removeAll(runes);
        calculateRunes();
    }

    public void addInfiniteRuneSources(EnumSet<RuneTypes> runes)
    {
        infiniteRuneSources.addAll(runes);
        calculateRunes();
    }

    public void removeInfiniteRuneSources(EnumSet<RuneTypes> runes)
    {
        infiniteRuneSources.removeAll(runes);
        calculateRunes();
    }

    public void clearSelectedSpells()
    {
        for (SpellData spell : spellSet)
        {
            toggleSpellNoCalculate(spell);
        }
        calculateRunes();
    }

    public void toggleSpell(SpellData spellData)
    {
        toggleSpellNoCalculate(spellData);
        calculateRunes();
    }

    private void toggleSpellNoCalculate(SpellData spellData)
    {
        if (isSpellSelected(spellData))
        {
            spellSet.remove(spellData);
        }
        else
        {
            spellSet.add(spellData);
        }
    }

    public boolean isSpellSelected(SpellData spellData)
    {
        return spellSet.contains(spellData);
    }

    private void calculateRunes()
    {
        runeSets.clear();

        EnumSet<RuneTypes> requiredRunes = EnumSet.noneOf(RuneTypes.class);
        for (SpellData spell : spellSet)
        {
            requiredRunes.addAll(spell.getRunes());
        }

        if (requiredRunes.isEmpty())
        {
            return;
        }

        if (usableRunes.contains(SUNFIRE) && requiredRunes.contains(FIRE))
        {
            requiredRunes.add(SUNFIRE);
            requiredRunes.remove(FIRE);
        }

        if (usableRunes.contains(AETHER) && requiredRunes.contains(COSMIC) && requiredRunes.contains(SOUL))
        {
            requiredRunes.add(AETHER);
            requiredRunes.remove(COSMIC);
            requiredRunes.remove(SOUL);
        }

        for (RuneTypes rune : infiniteRuneSources)
        {
            requiredRunes.remove(rune);
        }

        EnumSet<RuneTypes> elementalRunes = EnumSet.of(AIR, WATER, EARTH, FIRE);
        EnumSet<RuneTypes> requiredElementalRunes = EnumSet.copyOf(requiredRunes);
        requiredElementalRunes.retainAll(elementalRunes);

        // If any elemental combination rune (e.g. MIST) is in usableRunes, they all are
        if (usableRunes.contains(MIST) && !requiredElementalRunes.isEmpty())
        {
            EnumSet<RuneTypes> maskedRequiredRunes = EnumSet.copyOf(requiredRunes);
            maskedRequiredRunes.removeAll(elementalRunes);

            List<EnumSet<RuneTypes>> requiredComboRunes = calculateComboRunes(requiredElementalRunes);

            for (EnumSet<RuneTypes> elementalOptions : requiredComboRunes)
            {
                EnumSet<RuneTypes> combinedSet = EnumSet.copyOf(maskedRequiredRunes);
                combinedSet.addAll(elementalOptions);
                runeSets.add(combinedSet);
            }
        }
        else
        {
            runeSets.add(requiredRunes);
        }
    }

    // Brute force the optimal cover from the 2^10 possibilities
    private List<EnumSet<RuneTypes>> calculateComboRunes(EnumSet<RuneTypes> requiredElementalRunes)
    {
        int requiredBits = toBits(requiredElementalRunes);

        List<EnumSet<RuneTypes>> optimalCovers = new ArrayList<>();
        int optimalSize = Integer.MAX_VALUE;
        int numStates = 1 << ELEMENTAL_OPTIONS.length;

        // Enumerate all possible covers
        for (int state = 0; state < numStates; state++)
        {
            int union = 0;
            int count = 0;
            int sumOfContributions = 0;
            EnumSet<RuneTypes> cover = EnumSet.noneOf(RuneTypes.class);

            // An efficient cover only uses combination runes when they reduce the cardinality of the cover
            boolean efficientCover = true;

            for (int i = 0; i < ELEMENTAL_OPTIONS.length; i++)
            {
                if ((state & (1 << i)) != 0)
                {
                    int optionMask = ELEMENTAL_OPTIONS[i].getMask();
                    union |= optionMask;
                    count++;
                    cover.add(ELEMENTAL_OPTIONS[i]);

                    int runesContributed = optionMask & requiredBits;
                    int numContributed = Integer.bitCount(runesContributed);
                    sumOfContributions += numContributed;

                    boolean isComboRune = Integer.bitCount(optionMask) > 1;

                    if (isComboRune && numContributed < 2)
                    {
                        efficientCover = false;
                        break;
                    }
                }
            }

            if (!efficientCover)
            {
                continue;
            }

            int requiredContributions = Integer.bitCount(requiredBits);

            if ((union & requiredBits) == requiredBits && sumOfContributions == requiredContributions)
            {
                if (count < optimalSize)
                {
                    optimalSize = count;
                    optimalCovers.clear();
                    optimalCovers.add(cover);
                }
                else if (count == optimalSize)
                {
                    optimalCovers.add(cover);
                }
            }
        }

        return optimalCovers;
    }

    private int toBits(EnumSet<RuneTypes> runeSet)
    {
        int bits = 0;
        for (RuneTypes rune : runeSet)
        {
            bits += rune.getMask();
        }
        return bits;
    }
}
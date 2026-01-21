package com.runecalculator;

import lombok.AllArgsConstructor;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.EnumSet;

@AllArgsConstructor
class RuneCalculatorPanel extends PluginPanel {

    private final RuneCalculator calculator;

    private static final class CheckBoxData {
        final String label;
        final EnumSet<RuneTypes> runes;

        CheckBoxData(String label, RuneTypes... runes) {
            this.label = label;
            this.runes = EnumSet.of(runes[0], runes);
        }
    }

    private static final List<CheckBoxData> checkBoxes = List.of(
        new CheckBoxData("elemental combination", RuneTypes.MIST, RuneTypes.DUST, RuneTypes.MUD, RuneTypes.SMOKE, RuneTypes.STEAM, RuneTypes.LAVA),
        new CheckBoxData("aether", RuneTypes.AETHER),
        new CheckBoxData("sunfire", RuneTypes.SUNFIRE)
    );

    private JPanel buildCheckBoxes() {
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.PAGE_AXIS));

        for (CheckBoxData checkBox : checkBoxes) {
           JPanel uiCheckBox = buildOptionalRuneCheckbox(checkBox.label, checkBox.runes);
           checkBoxPanel.add(uiCheckBox);
        }

        return checkBoxPanel;
    }

    private JPanel buildOptionalRuneCheckbox(String label, EnumSet<RuneTypes> runes)
    {
        JPanel uiOption = new JPanel(new BorderLayout());
        JLabel uiLabel = new JLabel("Include " + label + " runes");
        JCheckBox uiCheckbox = new JCheckBox();

        uiLabel.setForeground(Color.WHITE);
        uiLabel.setFont(FontManager.getRunescapeSmallFont());

        uiOption.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 0));

        uiCheckbox.addActionListener(event -> calculator.adjustRuneSet(uiCheckbox, runes));

        uiOption.add(uiLabel, BorderLayout.WEST);
        uiOption.add(uiCheckbox, BorderLayout.EAST);

        return uiOption;
    }


}
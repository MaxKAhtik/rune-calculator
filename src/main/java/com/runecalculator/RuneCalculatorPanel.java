package com.runecalculator;

import net.runelite.client.ui.PluginPanel;
import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

class RuneCalculatorPanel extends PluginPanel {
    @Inject
    RuneCalculatorPanel(RuneCalculator calculator) {
        super();

        getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        add(calculator);
    }
}
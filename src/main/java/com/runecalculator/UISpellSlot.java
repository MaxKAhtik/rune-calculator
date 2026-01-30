package com.runecalculator;

import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Getter
public class UISpellSlot extends JPanel {
    private static final Border BORDER = new CompoundBorder(
        BorderFactory.createMatteBorder(0, 4, 0, 0, (ColorScheme.DARK_GRAY_COLOR).darker()),
        BorderFactory.createEmptyBorder(7, 12, 7, 7));

    private static final Dimension ICON_SIZE = new Dimension(32, 32);

    private final JLabel spellIcon;
    private final SpellData spellData;
    private boolean isSelected = false;

    UISpellSlot(JLabel spellIcon, SpellData spellData) {
        this.spellIcon = spellIcon;
        this.spellData = spellData;

        MouseListener hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                if (!isSelected) {
                    setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
                }
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                if (!isSelected) {
                    setBackground(ColorScheme.DARKER_GRAY_COLOR);
                }
            }
        };

        addMouseListener(hoverListener);

        spellIcon.setMinimumSize(ICON_SIZE);
        spellIcon.setMaximumSize(ICON_SIZE);
        spellIcon.setPreferredSize(ICON_SIZE);
        spellIcon.setHorizontalAlignment(JLabel.CENTER);

        JShadowedLabel uiSpellName = new JShadowedLabel();
        uiSpellName.setText(spellData.getSpellName());
        uiSpellName.setForeground(Color.WHITE);

        setLayout(new BorderLayout());
        setBorder(BORDER);
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        add(spellIcon, BorderLayout.LINE_START);
        add(uiSpellName, BorderLayout.CENTER);
    }

    void setSelected(boolean selected) {
        isSelected = selected;
        this.updateBackground();
    }

    private void updateBackground() {
        setBackground(this.isSelected ? ColorScheme.DARKER_GRAY_HOVER_COLOR.brighter() : ColorScheme.DARKER_GRAY_COLOR);
    }
}
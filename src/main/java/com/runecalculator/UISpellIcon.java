package com.runecalculator;

import lombok.Getter;
import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

@Getter
public class UISpellIcon extends JPanel {
    private static final Border BORDER = new CompoundBorder(
        BorderFactory.createMatteBorder(0, 4, 0, 0, (ColorScheme.DARK_GRAY_COLOR).darker()),
        BorderFactory.createEmptyBorder(7, 12, 7, 7));

    private static final Dimension ICON_SIZE = new Dimension(32, 32);

    private final SpellData spellData;

    UISpellIcon(JLabel spellIcon, SpellData spellData) {
        this.spellData = spellData;

        MouseListener hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                setBackground(ColorScheme.DARKER_GRAY_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                setBackground(ColorScheme.DARKER_GRAY_COLOR);
            }
        };

        addMouseListener(hoverListener);

        spellIcon.setMinimumSize(ICON_SIZE);
        spellIcon.setMaximumSize(ICON_SIZE);
        spellIcon.setPreferredSize(ICON_SIZE);
        spellIcon.setHorizontalAlignment(JLabel.CENTER);

        setLayout(new BorderLayout());
        setBorder(BORDER);
        setBackground(ColorScheme.DARKER_GRAY_COLOR);

        add(spellIcon, BorderLayout.LINE_START);
    }
}

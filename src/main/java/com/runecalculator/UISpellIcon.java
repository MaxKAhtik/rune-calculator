package com.runecalculator;

import lombok.Getter;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Getter
public class UISpellIcon extends JPanel implements SpellSource {
    public static final int ICON_SIZE = 32;
    private static final Dimension ICON_DIMENSION = new Dimension(ICON_SIZE, ICON_SIZE);

    private final SpellData spellData;
    private final JLabel spellIcon = new JLabel();

    UISpellIcon(SpellData spellData) {
        this.spellData = spellData;

        this.setMinimumSize(ICON_DIMENSION);
        this.setMaximumSize(ICON_DIMENSION);
        this.setPreferredSize(ICON_DIMENSION);

        spellIcon.setHorizontalAlignment(JLabel.CENTER);

        setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(spellIcon);//, BorderLayout.LINE_START);

        setToolTipText(this.spellData.getSpellName());
    }

    public void setIcon(BufferedImage sprite) {
        spellIcon.setIcon(new ImageIcon(sprite));
    }
}
package com.runecalculator;

import lombok.Getter;

import net.runelite.client.ui.ColorScheme;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

@Getter
public class UISpellIcon extends JPanel implements SpellSource {
    private static final Border BORDER = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

    public static final int ICON_SIZE = 24;
    private static final Dimension ICON_DIMENSION = new Dimension(ICON_SIZE, ICON_SIZE);

    private final SpellData spellData;
    private final JLabel spellIcon = new JLabel();

    UISpellIcon(SpellData spellData) {
        this.spellData = spellData;

        MouseListener hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                setBackground(ColorScheme.DARK_GRAY_COLOR);
            }
        };

        addMouseListener(hoverListener);

        spellIcon.setMinimumSize(ICON_DIMENSION);
        spellIcon.setMaximumSize(ICON_DIMENSION);
        spellIcon.setPreferredSize(ICON_DIMENSION);
        spellIcon.setHorizontalAlignment(JLabel.CENTER);

        setBorder(BORDER);
        setBackground(ColorScheme.DARK_GRAY_COLOR);

        add(spellIcon, BorderLayout.LINE_START);
    }

    public void setIcon(BufferedImage sprite) {
        spellIcon.setIcon(new ImageIcon(sprite));
    }

    public void resetBackground() {
        setBackground(ColorScheme.DARK_GRAY_COLOR);
    }
}

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
    public static final int ICON_SIZE = 32;
    private static final Dimension ICON_DIMENSION = new Dimension(ICON_SIZE, ICON_SIZE);

    private final SpellData spellData;
    private final JLabel spellIcon = new JLabel();

    UISpellIcon(SpellData spellData) {
        this.spellData = spellData;

        //TODO: remove this and the commented stuff below too
        /*
        MouseListener hoverListener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) { setBackground(ColorScheme.DARK_GRAY_HOVER_COLOR); }

            @Override
            public void mouseExited(MouseEvent mouseEvent) { setBackground(ColorScheme.DARK_GRAY_COLOR); }
        };

        addMouseListener(hoverListener);
         */

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

    //TODO: remove
    /*
    public void resetBackground() {
        setBackground(ColorScheme.DARK_GRAY_COLOR);
    }*/
}
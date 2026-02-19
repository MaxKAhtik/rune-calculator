package com.runecalculator;

import lombok.Getter;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.shadowlabel.JShadowedLabel;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

@Getter
public class UISpellSlot extends JPanel implements SpellSource {
    private static final Border BORDER = new EmptyBorder(3, 12, 3, 7);
    private static final Dimension ICON_SIZE = new Dimension(32, 32);

    private final SpellData spellData;
    private final JLabel spellIcon = new JLabel();
    private boolean isSelected = false;

    UISpellSlot(SpellData spellData) {
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

    public void setIcon(BufferedImage sprite) {
        spellIcon.setIcon(new ImageIcon(sprite));
    }

    void setSelected(boolean selected) {
        isSelected = selected;
        this.updateBackground();
    }

    private void updateBackground() {
        setBackground(this.isSelected ? ColorScheme.DARKER_GRAY_HOVER_COLOR.brighter() : ColorScheme.DARKER_GRAY_COLOR);
    }
}
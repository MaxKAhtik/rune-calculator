package com.runecalculator;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

class RuneCalculator extends JPanel {
    //TODO: remove later
    private static final Logger logger = Logger.getLogger(RuneCalculator.class.getName());

    private final ClientThread clientThread;
    private final SpriteManager spriteManager;

    private final EnumSet<SpellData> spellSet = EnumSet.noneOf(SpellData.class);
    private final EnumSet<RuneTypes> usableRunes;
    private final EnumSet<RuneTypes> infiniteRuneSources;
    private final IconTextField searchBar = new IconTextField();
    private final List<UISpellSlot> uiSpellSlots = new ArrayList<>();
    private final List<UISpellIcon> uiSpellIcons = new ArrayList<>();
    private final JPanel selectedSpellIconsPanel;
    private final Map<SpellData, BufferedImage> spellSpriteCache = new HashMap<>();
    private final Map<SpellData, UISpellIcon> uiSpellIconCache = new HashMap<>();
    private final Map<SpellData, UISpellSlot> uiSpellSlotCache = new HashMap<>();

    private static final EnumSet<RuneTypes> initialUsableRunes = EnumSet.of(
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

    private static final class CheckBoxData {
        final String label;
        final boolean initialState;
        final EnumSet<RuneTypes> runes;

        CheckBoxData(String label, boolean initialState, RuneTypes... runes) {
            this.label = label;
            this.initialState =  initialState;
            this.runes = EnumSet.of(runes[0], runes);
        }
    }

    private static final List<CheckBoxData> optionalUsableRuneCheckBoxes = List.of(
        new CheckBoxData("Include elemental combination runes", true, RuneTypes.MIST, RuneTypes.DUST, RuneTypes.MUD, RuneTypes.SMOKE, RuneTypes.STEAM, RuneTypes.LAVA),
        new CheckBoxData("Include aether runes", true, RuneTypes.AETHER),
        new CheckBoxData("Use sunfire runes", true, RuneTypes.SUNFIRE)
    );

    private static final List<CheckBoxData> infiniteRuneSourcesCheckBoxes = List.of(
        new CheckBoxData("Infinite source of air runes", false, RuneTypes.AIR),
        new CheckBoxData("Infinite source of water runes", false, RuneTypes.WATER),
        new CheckBoxData("Infinite source of earth runes", false, RuneTypes.EARTH),
        new CheckBoxData("Infinite source of fire runes", false, RuneTypes.FIRE)
    );

    @Inject
    RuneCalculator(ClientThread clientThread, SpriteManager spriteManager) {
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;

        usableRunes = EnumSet.copyOf(initialUsableRunes);
        infiniteRuneSources = EnumSet.noneOf(RuneTypes.class);

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        selectedSpellIconsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 7, 7));
        selectedSpellIconsPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JPanel checkBoxArea = buildCheckBoxes();
        add(checkBoxArea);

        //build selected spells area?
        JPanel selectedSpellsArea = buildSelectedSpellsArea();
        add(selectedSpellsArea);

        //probably put a "clear selected spells" button here too
        //maybe this is also the runes required area?

        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.addClearListener(this::onSearch);
        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                onSearch();
            }
        });

        add(searchBar);

        //then build spell slots area
        buildSpellSlots();
        uiSpellSlots.forEach(this::add);

        //then build the spell icon list
        buildSpellIcons();

        // Load spell sprites only after the UISpellSlots and UISpellIcons have been created
        loadAndSetSpellSprites();
    }

    private JPanel buildCheckBoxes() {
        JPanel uiCheckBoxesPanel = new JPanel();
        uiCheckBoxesPanel.setLayout(new BoxLayout(uiCheckBoxesPanel, BoxLayout.PAGE_AXIS));

        JPanel checkBoxGroup1 = buildCheckBoxGroup(optionalUsableRuneCheckBoxes, this::adjustUsableRuneSet);
        uiCheckBoxesPanel.add(checkBoxGroup1);

        // Visually separate the checkbox groups
        uiCheckBoxesPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel checkBoxGroup2 = buildCheckBoxGroup(infiniteRuneSourcesCheckBoxes, this::adjustInfiniteRuneSourcesSet);
        uiCheckBoxesPanel.add(checkBoxGroup2);

        // Visually separate the checkbox panel from following UI elements
        uiCheckBoxesPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        return uiCheckBoxesPanel;
    }

    private JPanel buildCheckBoxGroup(List<CheckBoxData> checkBoxDataList, ActionListener listener) {
        JPanel uiCheckBoxGroupPanel = new JPanel();
        uiCheckBoxGroupPanel.setLayout(new BoxLayout(uiCheckBoxGroupPanel, BoxLayout.PAGE_AXIS));

        for (CheckBoxData checkBox : checkBoxDataList) {
            JPanel uiCheckBox = buildCheckBox(checkBox, listener);
            uiCheckBoxGroupPanel.add(uiCheckBox);
        }

        return uiCheckBoxGroupPanel;
    }

    private JPanel buildCheckBox(CheckBoxData checkBoxData, ActionListener listener) {
        JPanel uiCheckBoxPanel = new JPanel(new BorderLayout());
        JLabel uiLabel = new JLabel(checkBoxData.label);

        JCheckBox uiCheckBox = new JCheckBox("", checkBoxData.initialState);
        uiCheckBox.putClientProperty("runes", checkBoxData.runes);
        uiCheckBox.addActionListener(listener);

        uiLabel.setForeground(Color.WHITE);
        uiLabel.setFont(FontManager.getRunescapeSmallFont());

        uiCheckBoxPanel.setBorder(BorderFactory.createEmptyBorder(1, 7, 1, 0));

        uiCheckBoxPanel.add(uiLabel, BorderLayout.WEST);
        uiCheckBoxPanel.add(uiCheckBox, BorderLayout.EAST);

        return uiCheckBoxPanel;
    }

    private JPanel buildSelectedSpellsArea() {
        JPanel selectedSpellsPanel = new JPanel();
        selectedSpellsPanel.setLayout(new BoxLayout(selectedSpellsPanel, BoxLayout.PAGE_AXIS));

        JLabel selectedSpellsLabel = new JLabel("Selected spells:", SwingConstants.LEADING);
        selectedSpellsPanel.add(selectedSpellsLabel);

        selectedSpellsPanel.add(selectedSpellIconsPanel);

        return selectedSpellsPanel;
    }

    private void buildSpellSlots() {
        for (SpellData spell : SpellData.values()) {
            UISpellSlot slot = new UISpellSlot(spell);
            uiSpellSlots.add(slot);
            uiSpellSlotCache.put(spell, slot);

            slot.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    toggleSpell(slot.getSpellData());

                    //TODO: remove later
                    logger.info(debug_spellSetToString());

                    calculateRunes();
                    refreshUI();
                }
            });
        }
    }

    private void buildSpellIcons() {
        for (SpellData spell : SpellData.values()) {
           UISpellIcon spellIcon = new UISpellIcon(spell);
           uiSpellIcons.add(spellIcon);
           uiSpellIconCache.put(spell, spellIcon);

           spellIcon.addMouseListener(new MouseAdapter() {
               @Override
               public void mousePressed(MouseEvent e) {
                   toggleSpell(spellIcon.getSpellData());

                   spellIcon.resetBackground();

                   //TODO: remove later
                   logger.info(debug_spellSetToString());

                   calculateRunes();
                   refreshUI();
               }
           });
        }
    }

    private void loadAndSetSpellSprites() {
        clientThread.invokeLater(() -> {
            for (SpellData spell : SpellData.values()) {
                BufferedImage img = spriteManager.getSprite(spell.getSpriteID(), 0);

                if (img != null) {
                    spellSpriteCache.put(spell, img);
                }
            }

            spellSpriteCache.forEach((spell, sprite) -> {
                UISpellSlot slot = uiSpellSlotCache.get(spell);
                UISpellIcon icon = uiSpellIconCache.get(spell);

                SwingUtilities.invokeLater(() -> {
                    if (slot != null) {
                        slot.setIcon(sprite);
                    }

                    if (icon != null) {
                        icon.setIcon(sprite);
                    }
                });
            });
        });
    }

    private void adjustUsableRuneSet(ActionEvent e) {
        JCheckBox checkBox = (JCheckBox) e.getSource();

        @SuppressWarnings("unchecked")
        EnumSet<RuneTypes> runes = (EnumSet<RuneTypes>) checkBox.getClientProperty("runes");

        if (checkBox.isSelected()) {
            usableRunes.addAll(runes);
        }
        else {
            usableRunes.removeAll(runes);
        }

        calculateRunes();
        refreshUI();
    }

    private void adjustInfiniteRuneSourcesSet(ActionEvent e) {
        JCheckBox checkBox = (JCheckBox) e.getSource();

        @SuppressWarnings("unchecked")
        EnumSet<RuneTypes> runes = (EnumSet<RuneTypes>) checkBox.getClientProperty("runes");

        if (checkBox.isSelected()) {
            infiniteRuneSources.addAll(runes);
        }
        else {
            infiniteRuneSources.removeAll(runes);
        }

        calculateRunes();
        refreshUI();
    }

    private boolean isSpellSelected(SpellData spellData) {
        return spellSet.contains(spellData);
    }

    private void toggleSpell(SpellData spellData) {
        if (isSpellSelected(spellData)) {
            spellSet.remove(spellData);
        }
        else {
            spellSet.add(spellData);
        }
    }

    private void updateSpellSlotsUI() {
        for (UISpellSlot slot : uiSpellSlots) {
            slot.setSelected(isSpellSelected(slot.getSpellData()));
        }
    }

    private void updateSelectedSpellIconsUI() {
        selectedSpellIconsPanel.removeAll();

        for (SpellData spell : spellSet) {
            UISpellIcon icon = uiSpellIconCache.get(spell);
            icon.setIcon(spellSpriteCache.get(spell));
            selectedSpellIconsPanel.add(icon);
        }
    }

    private void updateRuneSetsUI() {
        //TODO
    }

    private void refreshUI() {
        updateSpellSlotsUI();
        updateSelectedSpellIconsUI();
        updateRuneSetsUI();
        revalidate();
        repaint();
    }

    private void calculateRunes() {
        //set cover algorithm
        //with checks to all the special rune types and stuff
    }

    private void onSearch()
    {
        uiSpellSlots.forEach(slot ->
        {
            if (spellSlotContainsText(slot, searchBar.getText()))
            {
                super.add(slot);
            }
            else
            {
                super.remove(slot);
            }

            revalidate();
        });
    }

    private static boolean spellSlotContainsText(UISpellSlot slot, String text)
    {
        return slot.getSpellData().getSpellName().toLowerCase().contains(text.toLowerCase());
    }

    //TODO: delete later
    private String debug_spellSetToString() {
        StringBuilder debugMessage = new StringBuilder();
        for (SpellData spell : spellSet) {
            debugMessage.append(spell.getSpellName());
        }
        return debugMessage.toString();
    }
}
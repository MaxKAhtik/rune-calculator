package com.runecalculator;

import lombok.extern.slf4j.Slf4j;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.SwingUtil;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

@Slf4j
class RuneCalculator extends JPanel {
    private final ClientThread clientThread;
    private final SpriteManager spriteManager;

    private final EnumSet<SpellData> spellSet = EnumSet.noneOf(SpellData.class);
    private final EnumSet<RuneTypes> usableRunes;
    private final EnumSet<RuneTypes> infiniteRuneSources;
    private final IconTextField searchBar = new IconTextField();
    private final List<UISpellSlot> uiSpellSlots = new ArrayList<>();
    //TODO: check if I actually need this
    //private final List<UISpellIcon> uiSpellIcons = new ArrayList<>();
    private final JButton clearButton = createXButton();
    private static final int iconBufferSize = 3;
    private static final int spellIconGridColumns = (int) Math.floor(((float) PluginPanel.PANEL_WIDTH - 20)/(UISpellIcon.ICON_SIZE + iconBufferSize));
    private final JPanel selectedSpellIconsPanel = new JPanel(new GridLayout(0, spellIconGridColumns, iconBufferSize, iconBufferSize));;
    //TODO: remove if i don't actually need this
    //private final Map<SpellData, BufferedImage> spellSpriteMap = new HashMap<>();
    private final Set<SpellData> pendingSprites = EnumSet.allOf(SpellData.class);
    private final Map<SpellData, UISpellIcon> uiSpellIconMap = new HashMap<>();
    private final Map<SpellData, UISpellSlot> uiSpellSlotMap = new HashMap<>();

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

    private static final List<CheckBoxData> optionalRuneData = List.of(
        new CheckBoxData("Include elemental combination runes", true, RuneTypes.MIST, RuneTypes.DUST, RuneTypes.MUD, RuneTypes.SMOKE, RuneTypes.STEAM, RuneTypes.LAVA),
        new CheckBoxData("Include aether runes", true, RuneTypes.AETHER),
        new CheckBoxData("Use sunfire runes", false, RuneTypes.SUNFIRE)
    );

    private static final List<CheckBoxData> infiniteRuneData = List.of(
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

        JPanel checkBoxArea = buildCheckBoxes();
        add(checkBoxArea);

        JPanel selectedSpellsArea = buildSelectedSpellsArea();
        add(selectedSpellsArea);

        //TODO: buildNeededRunesArea();

        buildSearchBar();
        add(searchBar);

        buildSpellUIElements();
        uiSpellSlots.forEach(this::add);

        // Load spell sprites only after the Spell UI elements have been created
        loadAndSetSpellSprites();
    }

    private JPanel buildCheckBoxes() {
        JPanel uiCheckBoxesPanel = new JPanel();
        uiCheckBoxesPanel.setLayout(new BoxLayout(uiCheckBoxesPanel, BoxLayout.PAGE_AXIS));

        JPanel checkBoxGroup1 = buildCheckBoxGroup(optionalRuneData, this::adjustUsableRuneSet);
        uiCheckBoxesPanel.add(checkBoxGroup1);

        // Visually separate the checkbox groups
        uiCheckBoxesPanel.add(new JSeparator(JSeparator.HORIZONTAL));

        JPanel checkBoxGroup2 = buildCheckBoxGroup(infiniteRuneData, this::adjustInfiniteRuneSourcesSet);
        uiCheckBoxesPanel.add(checkBoxGroup2);

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
        JPanel selectedSpellsPanel = new JPanel(new BorderLayout());
        selectedSpellsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        selectedSpellsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel selectedSpellsHeader = new JPanel(new BorderLayout());
        selectedSpellsHeader.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel selectedSpellsLabel = new JLabel("Selected spells:");
        selectedSpellsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        selectedSpellsHeader.add(selectedSpellsLabel, BorderLayout.WEST);

        clearButton.setText("×");
        clearButton.addActionListener(e -> {

            // Deselect all currently-selected spells
            for (SpellData spell : spellSet) {
                toggleSpell(spell);
            }

            calculateRunes();
            refreshUI();
        });

        selectedSpellsHeader.add(clearButton, BorderLayout.EAST);

        selectedSpellsPanel.add(selectedSpellsHeader, BorderLayout.NORTH);

        selectedSpellIconsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        selectedSpellIconsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        selectedSpellsPanel.add(selectedSpellIconsPanel, BorderLayout.CENTER);

        return selectedSpellsPanel;
    }

    private JButton createXButton()
    {
        JButton xButton = new JButton();
        xButton.setPreferredSize(new Dimension(30, 0));
        xButton.setFont(FontManager.getRunescapeBoldFont());
        xButton.setBorder(null);
        xButton.setRolloverEnabled(true);
        SwingUtil.removeButtonDecorations(xButton);
        xButton.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);

        xButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                xButton.setForeground(Color.PINK);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                xButton.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
            }
        });

        xButton.setVisible(false);

        return xButton;
    }

    private void buildSearchBar() {
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
    }

    private void buildSpellUIElements() {
        for (SpellData spell : SpellData.values()) {
            UISpellSlot slot = new UISpellSlot(spell);

            // Maintain an ordered list of UISpellSlots for the search display
            uiSpellSlots.add(slot);

            uiSpellSlotMap.put(spell, slot);

            UISpellIcon spellIcon = new UISpellIcon(spell);
            uiSpellIconMap.put(spell, spellIcon);

            MouseListener adjustSpellSet = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    SpellSource src = (SpellSource) e.getSource();
                    toggleSpell(src.getSpellData());

                    //TODO: remove later
                    log.info(debug_spellSetToString());

                    calculateRunes();
                    refreshUI();
                }
            };

            slot.addMouseListener(adjustSpellSet);
            spellIcon.addMouseListener(adjustSpellSet);
        }
    }

    private void loadAndSetSpellSprites() {
        clientThread.invokeLater(() -> {
            Iterator<SpellData> it = pendingSprites.iterator();

            while (it.hasNext()) {
                SpellData spell = it.next();

                BufferedImage img = spriteManager.getSprite(spell.getSpriteID(), 0);

                if (img == null) {
                    // Ignore the failed sprite load for now
                    continue;
                }

                UISpellSlot slot = uiSpellSlotMap.get(spell);
                UISpellIcon icon = uiSpellIconMap.get(spell);

                SwingUtilities.invokeLater(() -> {
                    if (slot != null) {
                        slot.setIcon(img);
                    }

                    if (icon != null) {
                        icon.setIcon(img);
                    }
                });

                it.remove();
            }
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
            UISpellIcon icon = uiSpellIconMap.get(spell);
            icon.resetBackground();
            selectedSpellIconsPanel.add(icon);
        }

        clearButton.setVisible(!spellSet.isEmpty());
    }

    private void updateRuneSetsUI() {
        //TODO
    }

    private void refreshUI() {
        if (!pendingSprites.isEmpty()) {
            loadAndSetSpellSprites();
        }

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
            if (spellSlotContainsText(slot, searchBar.getText())) {
                super.add(slot);
            }
            else {
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
            debugMessage.append(", ");
        }
        return debugMessage.toString();
    }
}
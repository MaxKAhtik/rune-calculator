package com.runecalculator;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.util.SwingUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static com.runecalculator.RuneTypes.*;
import static javax.swing.ScrollPaneConstants.*;

class RuneCalculatorPanel extends PluginPanel
{
    private static final int ICON_BUFFER_SIZE = 3;
    private static final int GRID_COLUMNS = (int) Math.floor(((float) PluginPanel.PANEL_WIDTH - 20)/(UISpellIcon.ICON_SIZE + ICON_BUFFER_SIZE));
    private static final int BASE_SCROLL_ROWS_VISIBLE = 2;
    private static final int BASE_SCROLL_HEIGHT = BASE_SCROLL_ROWS_VISIBLE * (UISpellIcon.ICON_SIZE + ICON_BUFFER_SIZE) + 10;

    private final RuneCalculator calc;
    private final ClientThread clientThread;
    private final SpriteManager spriteManager;
    private final ItemManager itemManager;

    private final Set<SpellData> pendingSpellSprites = EnumSet.allOf(SpellData.class);
    private final Set<RuneTypes> pendingRuneSprites = EnumSet.allOf(RuneTypes.class);

    private final Map<SpellData, UISpellIcon> uiSpellIconMap = new HashMap<>();
    private final Map<SpellData, UISpellSlot> uiSpellSlotMap = new HashMap<>();
    private final Map<RuneTypes, BufferedImage> uiRuneIconMap = new HashMap<>();

    private final JButton clearButton = createXButton();
    private final JPanel selectedSpellIconsPanel = new JPanel(new GridLayout(0, GRID_COLUMNS, ICON_BUFFER_SIZE, ICON_BUFFER_SIZE));
    private final JPanel neededRunesGroups =  new JPanel(new DynamicGridLayout(0, 1, 0, 5));
    private final IconTextField searchBar = new IconTextField();
    private final List<UISpellSlot> uiSpellSlots = new ArrayList<>();

    private static final class CheckBoxData
    {
        final String label;
        final boolean initialState;
        final EnumSet<RuneTypes> runes;

        CheckBoxData(String label, boolean initialState, RuneTypes... runes)
        {
            this.label = label;
            this.initialState =  initialState;
            this.runes = EnumSet.of(runes[0], runes);
        }
    }

    private static final List<CheckBoxData> optionalRuneData = List.of(
        new CheckBoxData("Include elemental combination runes", false, MIST, DUST, MUD, SMOKE, STEAM, LAVA),
        new CheckBoxData("Include aether runes", false, AETHER),
        new CheckBoxData("Use sunfire runes", false, SUNFIRE)
    );

    private static final List<CheckBoxData> infiniteRuneData = List.of(
        new CheckBoxData("Infinite source of air runes", false, AIR),
        new CheckBoxData("Infinite source of water runes", false, WATER),
        new CheckBoxData("Infinite source of earth runes", false, EARTH),
        new CheckBoxData("Infinite source of fire runes", false, FIRE)
    );

    @Inject
    RuneCalculatorPanel(RuneCalculator calculator, ClientThread clientThread, SpriteManager spriteManager, ItemManager itemManager)
    {
        super();
        this.calc = calculator;
        this.clientThread = clientThread;
        this.spriteManager = spriteManager;
        this.itemManager = itemManager;

        getScrollPane().setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new DynamicGridLayout(0, 1, 0, 5));

        JPanel checkBoxArea = buildCheckBoxes();
        add(checkBoxArea);

        JPanel selectedSpellsArea = buildSelectedSpellsArea();
        add(selectedSpellsArea);

        JPanel neededRunesArea = buildNeededRunesArea();
        add(neededRunesArea);

        buildSearchBar();
        add(searchBar);

        buildSpellUIElements();
        uiSpellSlots.forEach(this::add);

        loadRuneSprites();

        // Load spell sprites only after the Spell UI elements have been created
        loadAndSetSpellSprites();
    }

    private JPanel buildCheckBoxes()
    {
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

    private JPanel buildCheckBoxGroup(List<CheckBoxData> checkBoxDataList, ActionListener listener)
    {
        JPanel uiCheckBoxGroupPanel = new JPanel();
        uiCheckBoxGroupPanel.setLayout(new BoxLayout(uiCheckBoxGroupPanel, BoxLayout.PAGE_AXIS));

        for (CheckBoxData checkBox : checkBoxDataList)
        {
            JPanel uiCheckBox = buildCheckBox(checkBox, listener);
            uiCheckBoxGroupPanel.add(uiCheckBox);
        }

        return uiCheckBoxGroupPanel;
    }

    private JPanel buildCheckBox(CheckBoxData checkBoxData, ActionListener listener)
    {
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

    private void adjustUsableRuneSet(ActionEvent e)
    {
        JCheckBox checkBox = (JCheckBox) e.getSource();

        @SuppressWarnings("unchecked")
        EnumSet<RuneTypes> runes = (EnumSet<RuneTypes>) checkBox.getClientProperty("runes");

        if (checkBox.isSelected())
        {
            calc.addUsableRunes(runes);
        }
        else
        {
            calc.removeUsableRunes(runes);
        }

        refreshUI();
    }

    private void adjustInfiniteRuneSourcesSet(ActionEvent e)
    {
        JCheckBox checkBox = (JCheckBox) e.getSource();

        @SuppressWarnings("unchecked")
        EnumSet<RuneTypes> runes = (EnumSet<RuneTypes>) checkBox.getClientProperty("runes");

        if (checkBox.isSelected())
        {
            calc.addInfiniteRuneSources(runes);
        }
        else
        {
            calc.removeInfiniteRuneSources(runes);
        }

        refreshUI();
    }

    private JPanel buildSelectedSpellsArea()
    {
        JPanel selectedSpellsPanel = new JPanel(new BorderLayout());
        selectedSpellsPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        selectedSpellsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JPanel selectedSpellsHeader = new JPanel(new BorderLayout());
        selectedSpellsHeader.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel selectedSpellsLabel = new JLabel("Selected spells:");
        selectedSpellsLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        selectedSpellsHeader.add(selectedSpellsLabel, BorderLayout.WEST);

        clearButton.setText("x");
        clearButton.addActionListener(e ->
        {
            calc.clearSelectedSpells();
            refreshUI();
        });

        selectedSpellsHeader.add(clearButton, BorderLayout.EAST);

        selectedSpellsPanel.add(selectedSpellsHeader, BorderLayout.NORTH);

        JScrollPane selectedSpellsScroll = new JScrollPane(selectedSpellIconsPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        selectedSpellsScroll.setBorder(null);
        selectedSpellsScroll.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        selectedSpellsScroll.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
        selectedSpellsScroll.setPreferredSize(new Dimension(0, BASE_SCROLL_HEIGHT));
        selectedSpellsScroll.addMouseWheelListener(this::forwardScrollWhenNoScrollbar);

        selectedSpellsPanel.add(selectedSpellsScroll, BorderLayout.CENTER);

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

        xButton.addMouseListener(new MouseAdapter()
        {
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

    private JPanel buildNeededRunesArea()
    {
        JPanel neededRunesPanel = new JPanel(new BorderLayout());
        neededRunesPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));
        neededRunesPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        JLabel neededRunesLabel = new JLabel("Runes needed:");
        neededRunesLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));

        neededRunesPanel.add(neededRunesLabel, BorderLayout.NORTH);

        neededRunesGroups.setBorder(new EmptyBorder(5, 5, 5, 5));
        neededRunesGroups.setBackground(ColorScheme.DARK_GRAY_COLOR);

        JScrollPane neededRunesScroll = new JScrollPane(neededRunesGroups, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
        neededRunesScroll.setBorder(null);
        neededRunesScroll.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        neededRunesScroll.getViewport().setBackground(ColorScheme.DARKER_GRAY_COLOR);
        neededRunesScroll.setPreferredSize(new Dimension(0, (int) (BASE_SCROLL_HEIGHT * 1.5)));
        neededRunesScroll.addMouseWheelListener(this::forwardScrollWhenNoScrollbar);

        neededRunesPanel.add(neededRunesScroll, BorderLayout.CENTER);

        return neededRunesPanel;
    }

    private void forwardScrollWhenNoScrollbar(MouseWheelEvent e)
    {
        JScrollPane scrollPane = (JScrollPane) e.getSource();
        JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        boolean scrollable = scrollBar.isVisible() && (scrollBar.getMaximum() - scrollBar.getVisibleAmount() > 0);

        if (!scrollable)
        {
            // Let the parent container handle the scroll
            scrollPane.getParent().dispatchEvent(e);
        }
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

    private boolean spellSlotContainsText(UISpellSlot slot, String text)
    {
        return slot.getSpellData().getSpellName().toLowerCase().contains(text.toLowerCase());
    }

    private void buildSpellUIElements()
    {
        for (SpellData spell : SpellData.values())
        {
            UISpellSlot slot = new UISpellSlot(spell);

            // Maintain an ordered list of UISpellSlots for the search display
            uiSpellSlots.add(slot);

            uiSpellSlotMap.put(spell, slot);

            UISpellIcon spellIcon = new UISpellIcon(spell);
            uiSpellIconMap.put(spell, spellIcon);

            MouseListener adjustSpellSet = new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    SpellSource src = (SpellSource) e.getSource();
                    calc.toggleSpell(src.getSpellData());
                    refreshUI();
                }
            };

            slot.addMouseListener(adjustSpellSet);
            spellIcon.addMouseListener(adjustSpellSet);
        }
    }

    private void loadRuneSprites()
    {
        clientThread.invokeLater(() ->
        {
            Iterator<RuneTypes> it = pendingRuneSprites.iterator();

            while (it.hasNext())
            {
                RuneTypes rune = it.next();

                BufferedImage img = itemManager.getImage(rune.getId());

                if (img == null)
                {
                    // Ignore the failed sprite load for now
                    continue;
                }

                uiRuneIconMap.put(rune, img);

                it.remove();
            }
        });
    }

    private void loadAndSetSpellSprites()
    {
        clientThread.invokeLater(() ->
        {
            Iterator<SpellData> it = pendingSpellSprites.iterator();

            while (it.hasNext())
            {
                SpellData spell = it.next();

                BufferedImage img = spriteManager.getSprite(spell.getSpriteID(), 0);

                if (img == null)
                {
                    // Ignore the failed sprite load for now
                    continue;
                }

                UISpellSlot slot = uiSpellSlotMap.get(spell);
                UISpellIcon icon = uiSpellIconMap.get(spell);

                SwingUtilities.invokeLater(() ->
                {
                    if (slot != null)
                    {
                        slot.setIcon(img);
                    }

                    if (icon != null)
                    {
                        icon.setIcon(img);
                    }
                });

                it.remove();
            }
        });
    }


    private void updateSpellSlotsUI()
    {
        for (UISpellSlot slot : uiSpellSlots)
        {
            slot.setSelected(calc.isSpellSelected(slot.getSpellData()));
        }
    }

    private void updateSelectedSpellIconsUI()
    {
        selectedSpellIconsPanel.removeAll();

        EnumSet<SpellData> selectedSpells = calc.getSpellSet();
        for (SpellData spell : selectedSpells)
        {
            UISpellIcon icon = uiSpellIconMap.get(spell);
            selectedSpellIconsPanel.add(icon);
        }

        clearButton.setVisible(!selectedSpells.isEmpty());
    }

    private void updateRuneSetsUI()
    {
        neededRunesGroups.removeAll();

        List<EnumSet<RuneTypes>> options = calc.getRuneSets();

        if(options.isEmpty())
        {
            return;
        }

        int setNum = 1;
        for (EnumSet<RuneTypes> runeSet : options)
        {
            JPanel runeSetPanel = new JPanel(new BorderLayout());
            runeSetPanel.setBorder(new EmptyBorder(0, 0, 5, 0));

            if (options.size() != 1)
            {
                JLabel runeSetLabel = new JLabel("Option " + setNum);
                runeSetPanel.add(runeSetLabel, BorderLayout.NORTH);
            }

            JPanel runeGroupPanel = new JPanel(new GridLayout(0, GRID_COLUMNS-1, ICON_BUFFER_SIZE, ICON_BUFFER_SIZE));

            for (RuneTypes rune : runeSet)
            {
                JLabel runeIcon = new JLabel();
                BufferedImage icon = uiRuneIconMap.get(rune);
                if (icon != null)
                {
                    runeIcon.setIcon(new ImageIcon(uiRuneIconMap.get(rune)));
                }
                runeIcon.setToolTipText(rune.toString());
                runeGroupPanel.add(runeIcon);
            }

            runeSetPanel.add(runeGroupPanel, BorderLayout.CENTER);

            neededRunesGroups.add(runeSetPanel);

            setNum++;
        }
    }

    private void refreshUI()
    {
        if (!pendingSpellSprites.isEmpty())
        {
            loadAndSetSpellSprites();
        }

        if (!pendingRuneSprites.isEmpty())
        {
            loadRuneSprites();
        }

        updateSpellSlotsUI();
        updateSelectedSpellIconsUI();
        updateRuneSetsUI();
        revalidate();
        repaint();
    }
}
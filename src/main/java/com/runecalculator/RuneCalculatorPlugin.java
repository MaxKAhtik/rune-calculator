package com.runecalculator;

import com.google.inject.Provider;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;


@Slf4j
@PluginDescriptor(
	name = "Rune Calculator",
	description = "Calculates what rune types are necessary to cast the selected spells.",
	tags = {"runes", "magic", "spell", "rune pouch", "calculator"},
	enabledByDefault = false
)

public class RuneCalculatorPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Provider<RuneCalculatorPanel> uiPanel;

	private NavigationButton uiNavigationButton;

	@Override
	protected void startUp() throws Exception {
		final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		uiNavigationButton = NavigationButton.builder()
			.tooltip("Rune Calculator")
			.icon(icon)
			.priority(20)
			.panel(uiPanel.get())
			.build();

		clientToolbar.addNavigation(uiNavigationButton);
	}

	@Override
	protected void shutDown() throws Exception {
		clientToolbar.removeNavigation(uiNavigationButton);
	}
}

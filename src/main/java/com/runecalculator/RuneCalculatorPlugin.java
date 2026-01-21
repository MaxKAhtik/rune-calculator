package com.runecalculator;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


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

	@Override
	protected void startUp() throws Exception
	{
		log.debug("Rune Calculator started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Rune Calculator stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{

	}
}

package com.gmail.zahusek;

import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.zahusek.commands.CommandManager;

public class Main extends JavaPlugin {
	
	@Override
	public void onEnable() {
		CommandManager.registerCommand(new TestCommand(), this);
		super.onEnable();
	}
	
	@Override
	public void onDisable() {
		super.onDisable();
	}
}

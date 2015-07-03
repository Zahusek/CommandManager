package com.gmail.zahusek.commandlistener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import com.gmail.zahusek.commandlistener.CmdHandler;
import com.gmail.zahusek.commandlistener.CommandListener;
import com.gmail.zahusek.commandlistener.SubHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CommandManager extends VanillaCommand {

	protected CommandManager(String name) { super(name); }
	
	private final String unknown = ChatColor.translateAlternateColorCodes('&',
			"&cno support an argument !");
	private static final Map<CommandListener, List<Method>> COMMANDS = Maps
			.newHashMap();
	private static final Map<CommandListener, List<Method>> SUBCOMMANDS = Maps
			.newHashMap();

	public static void registerCommand(CommandListener handler,
			JavaPlugin plugin) {

		List<Method> mcmd = Lists.newArrayList();
		List<Method> msub = Lists.newArrayList();

		for (Method method : handler.getClass().getDeclaredMethods()) {

			CmdHandler cmd = method.getAnnotation(CmdHandler.class);
			SubHandler sub = method.getAnnotation(SubHandler.class);
			if (cmd == null && sub == null)
				continue;

			Class<?>[] value = method.getParameterTypes();
			if (value.length != 2
					|| !CommandSender.class.equals(value[0])
					|| !String[].class.equals(value[1]))
				continue;

			if (cmd != null) {
				if (getCommandMap().getCommand(cmd.name()) != null)
					continue;

				CommandManager command = new CommandManager(cmd.name());
				if (!cmd.aliases().equals(new String[] { "" }))
					command.setAliases(Lists.newArrayList(cmd.aliases()));

				if (!cmd.description().equals(""))
					command.setDescription(cmd.description());
				getCommandMap().register(cmd.name(), command);

				mcmd.add(method);

				continue;
			}

			if (sub != null) {
				msub.add(method);
				continue;
			}

		}

		if (!mcmd.isEmpty())
			COMMANDS.put(handler, mcmd);
		if (!msub.isEmpty())
			SUBCOMMANDS.put(handler, msub);
	}

	private static CommandMap getCommandMap() {
		CommandMap map = null;
		Field field = null;
		try {
			field = Bukkit.getServer().getClass()
					.getDeclaredField("commandMap");
			field.setAccessible(true);
			if (field != null)
				map = (CommandMap) field.get(Bukkit.getServer());
		} catch (Exception e) {
			System.out.println("== An error when returning a commandMap ! ==");
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public boolean execute(CommandSender commandsender, String label,
			String[] args) {
		if (args.length > 0) {
			for (Entry<CommandListener, List<Method>> listeners : SUBCOMMANDS.entrySet()) {
				for (Method methods : listeners.getValue()) {
					
					CommandListener key = listeners.getKey();
					Method value = methods;
					
					SubHandler cmd = value.getAnnotation(SubHandler.class);
					if (!cmd.parent().equalsIgnoreCase(this.getName()))
						continue;

					List<String> cargs = Lists.newArrayList(args);
					List<String> sargs = Lists.newArrayList(cmd.args());

					for (int i = 0; i < cargs.size() && cargs.size() <= sargs.size(); i++)
						if (sargs.get(i).trim().equals(""))
							cargs.set(i, "");

					if (cargs.size() >= sargs.size()
							&& cargs.containsAll(sargs)) {

						if (!commandsender.hasPermission(cmd.permission())) {
							commandsender.sendMessage(ChatColor
									.translateAlternateColorCodes('&',
											cmd.permissionMessage()));
							return true;
						}

						try {
							value.invoke(key, commandsender,
									args);
							return true;
						} catch (IllegalAccessException
								| IllegalArgumentException
								| InvocationTargetException e) {
							e.printStackTrace();
						}
					}
				}
			}
			commandsender.sendMessage(unknown);
			return true;
		}
		for (Entry<CommandListener, List<Method>> listeners : COMMANDS.entrySet()) {
			for (Method methods : listeners.getValue()) {
				
				CommandListener key = listeners.getKey();
				Method value = methods;

				CmdHandler cmd = value.getAnnotation(CmdHandler.class);

				if (!cmd.name().equalsIgnoreCase(this.getName()))
					continue;

				if (!commandsender.hasPermission(cmd.permission())) {
					commandsender.sendMessage(ChatColor.translateAlternateColorCodes('&', cmd.permissionMessage()));
					return true;
				}
				try {
					value.invoke(key, commandsender, args);
					return true;
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | SecurityException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}

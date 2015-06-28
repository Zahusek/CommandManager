package com.gmail.zahusek.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CommandManager extends BukkitCommand {

	private final String unknown = ChatColor.translateAlternateColorCodes('&',
			"&cNo support for that argument!");

	private static final Map<CommandListener, List<Method>> COMMANDS = Maps
			.newHashMap();
	private static final Map<CommandListener, List<Method>> SUBCOMMANDS = Maps
			.newHashMap();

	public static void registerCommand(CommandListener handler, JavaPlugin plugin) {

		List<Method> mcmd = Lists.newArrayList();
		List<Method> msub = Lists.newArrayList();

		for (Method method : handler.getClass().getDeclaredMethods()) {

			Cmd cmd = method.getAnnotation(Cmd.class);
			Sub sub = method.getAnnotation(Sub.class);
			if (cmd == null && sub == null)
				continue;

			Class<?>[] value = method.getParameterTypes();
			if (value.length != 2
					|| !CommandSender.class.isAssignableFrom(value[0])
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

	protected CommandManager(String name) {
		super(name);
	}

	@Override
	public boolean execute(CommandSender commandsender, String label,
			String[] args) {
		if (args.length > 0) {
			for (Entry<CommandListener, List<Method>> listener : SUBCOMMANDS
					.entrySet()) {
				for (Method method : listener.getValue()) {

					Sub cmd = method.getAnnotation(Sub.class);
					if (!cmd.parent().equalsIgnoreCase(this.getName()))
						continue;

					List<String> cargs = Lists.newArrayList(args);
					List<String> sargs = Lists.newArrayList(cmd.args());

					for (int i = 0; i < cargs.size(); i++)
						if (sargs.get(i).trim().equals(""))
							cargs.set(i, "");

					if (args.length >= cmd.args().length
							&& cargs.containsAll(sargs)) {

						if (!commandsender.hasPermission(cmd.permission())) {
							commandsender.sendMessage(ChatColor
									.translateAlternateColorCodes('&',
											cmd.permissionMessage()));
							return true;
						}

						try {
							method.invoke(listener.getKey(), commandsender,
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
		for (Entry<CommandListener, List<Method>> listener : COMMANDS
				.entrySet()) {
			for (Method method : listener.getValue()) {

				Cmd cmd = method.getAnnotation(Cmd.class);

				if (!commandsender.hasPermission(cmd.permission())) {
					commandsender.sendMessage(ChatColor
							.translateAlternateColorCodes('&',
									cmd.permissionMessage()));
					return true;
				}

				if (!cmd.name().equalsIgnoreCase(this.getName()))
					continue;

				try {
					method.invoke(listener.getKey(), commandsender, args);
					return true;
				} catch (IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Cmd {

		String name();

		String[] aliases() default { "" };

		String description() default "";

		String permission() default "";

		String permissionMessage() default "&cYou do not have permission to use that command";

	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Sub {

		String parent();

		String[] args();

		String permission() default "";

		String permissionMessage() default "&cYou do not have permission to use that subcommand";

	}

	public interface CommandListener { }
}

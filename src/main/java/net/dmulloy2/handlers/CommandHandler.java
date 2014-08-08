/**
 * (c) 2014 dmulloy2
 */
package net.dmulloy2.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.dmulloy2.SwornPlugin;
import net.dmulloy2.chat.ComponentBuilder;
import net.dmulloy2.commands.Command;
import net.dmulloy2.util.FormatUtil;

import org.apache.commons.lang.Validate;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

/**
 * Handles commands. This supports both prefixed and non-prefixed commands.
 *
 * @author dmulloy2
 */

public class CommandHandler implements CommandExecutor
{
	private String commandPrefix;
	private List<Command> registeredPrefixedCommands;
	private List<Command> registeredCommands;

	private final SwornPlugin plugin;
	public CommandHandler(SwornPlugin plugin)
	{
		this.plugin = plugin;
		this.registeredCommands = new ArrayList<>();
	}

	/**
	 * Registers a non-prefixed {@link Command}.
	 *
	 * @param command Non-prefixed {@link Command} to register.
	 */
	public void registerCommand(Command command)
	{
		Validate.notNull(command, "command cannot be null!");
		PluginCommand pluginCommand = plugin.getCommand(command.getName());
		if (pluginCommand != null)
		{
			pluginCommand.setExecutor(command);
			registeredCommands.add(command);
		}
		else
		{
			plugin.getLogHandler().log(Level.WARNING, "Entry for command {0} is missing in plugin.yml", command.getName());
		}
	}

	/**
	 * Registers a prefixed {@link Command}. The commandPrefix must be set for
	 * this method to work.
	 *
	 * @param command Prefixed {@link Command} to register.
	 */
	public void registerPrefixedCommand(Command command)
	{
		Validate.notNull(command, "command cannot be null!");
		if (commandPrefix != null)
			registeredPrefixedCommands.add(command);
	}

	/**
	 * @return A {@link List} of all registered non-prefixed commands.
	 */
	public List<Command> getRegisteredCommands()
	{
		return registeredCommands;
	}

	/**
	 * @return A {@link List} of all registered prefixed commands.
	 */
	public List<Command> getRegisteredPrefixedCommands()
	{
		return registeredPrefixedCommands;
	}

	/**
	 * @return The command prefix.
	 */
	public String getCommandPrefix()
	{
		return commandPrefix;
	}

	/**
	 * Sets the command prefix. This method must be called before any prefixed
	 * commands are registered.
	 *
	 * @param commandPrefix Command prefix
	 */
	public void setCommandPrefix(String commandPrefix)
	{
		Validate.notEmpty(commandPrefix, "prefix cannot be null or empty!");
		this.commandPrefix = commandPrefix;
		this.registeredPrefixedCommands = new ArrayList<Command>();

		plugin.getCommand(commandPrefix).setExecutor(this);
	}

	/**
	 * @return whether or not the command prefix is used.
	 */
	public boolean usesCommandPrefix()
	{
		return commandPrefix != null;
	}

	public final Command getCommand(String name)
	{
		Validate.notNull(name, "name cannot be null!");
		for (Command command : registeredPrefixedCommands)
		{
			if (name.equalsIgnoreCase(command.getName()) || command.getAliases().contains(name.toLowerCase()))
				return command;
		}

		for (Command command : registeredCommands)
		{
			if (name.equalsIgnoreCase(command.getName()) || command.getAliases().contains(name.toLowerCase()))
				return command;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args)
	{
		List<String> argsList = new ArrayList<String>();

		if (args.length > 0)
		{
			String commandName = args[0];
			for (int i = 1; i < args.length; i++)
				argsList.add(args[i]);

			Command command = getCommand(commandName);
			if (command != null)
			{
				command.execute(sender, args);
				return true;
			}

			new ComponentBuilder(FormatUtil.format("&cError: &4Unknown command \"&c{0}&4\". Try ", commandName))
				.addAll(getHelpCommand().getFancyUsageTemplate()).send(sender);
		}
		else
		{
			getHelpCommand().execute(sender, args);
		}

		return true;
	}

	private final Command getHelpCommand()
	{
		if (plugin.getHelpCommand() != null)
			return plugin.getHelpCommand();

		return getCommand("help");
	}
}
package com.gmail.zahusek;

import org.bukkit.command.CommandSender;

import com.gmail.zahusek.commands.CommandManager.*;

public class TestCommand implements CommandListener {
	
	@Cmd(name = "test1", aliases = {"test"}, permission = "test.hh")
	public void command(CommandSender cs, String[] args){
		cs.sendMessage("");
	}
	@Sub(parent = "test1", args = {"test", "", "message"}, permission = "test.message")
	public void subcommand(CommandSender cs, String[] args){
		cs.sendMessage("Look ! --> " + args[1]);
	}
	
	@Sub(parent = "test1", args = {"set", "", "del"}, permission = "test.del")
	public void subcommand1(CommandSender cs, String[] args){
		cs.sendMessage("Look now ! --> " + args[1]);
	}
}

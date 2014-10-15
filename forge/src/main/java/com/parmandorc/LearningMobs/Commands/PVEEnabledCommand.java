package com.parmandorc.LearningMobs.Commands;

import com.parmandorc.LearningMobs.EntityLearningMob;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

public class PVEEnabledCommand extends CommandBase 
{
	@Override
	public String getCommandName() 
	{
		return "PVEEnabled";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) 
	{
		return "/PVEEnabled [<true|false>] // Sets or prints wether Learning Mobs should attack players or other Learning Mobs";
	}

	@Override
	public void processCommand(ICommandSender commandsender, String[] args) 
	{
		if (args.length > 1)
		{
			commandsender.addChatMessage(new ChatComponentTranslation("msg.PVEnabledCommand_InvalidArguments.txt"));
			return;
		}
		
		if (args.length == 0)
		{
			if (EntityLearningMob.PVEEnabled)
				commandsender.addChatMessage(new ChatComponentTranslation("msg.PVEEnabledCommand_isTrue.txt"));
			else
				commandsender.addChatMessage(new ChatComponentTranslation("msg.PVEEnabledCommand_isFalse.txt"));

			return;
		}
		
		if (args[0].equals("true"))
		{
			commandsender.addChatMessage(new ChatComponentTranslation("msg.PVEEnabledCommand_setToTrue.txt"));
			EntityLearningMob.PVEEnabled = true;
		}
		else if (args[0].equals("false"))
		{
			commandsender.addChatMessage(new ChatComponentTranslation("msg.PVEEnabledCommand_setToFalse.txt"));
			EntityLearningMob.PVEEnabled = false;
		}
		else
		{
			commandsender.addChatMessage(new ChatComponentTranslation("msg.PVEEnabledCommand_InvalidArguments.txt"));
		}
		return;
	}

}

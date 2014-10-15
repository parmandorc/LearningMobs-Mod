package com.parmandorc.LearningMobs.Commands;

import com.parmandorc.LearningMobs.EntityLearningMob;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

public class AutoRespawningEnabledCommand extends CommandBase {

	@Override
	public String getCommandName() 
	{
		return "autoRespawningEnabled";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) 
	{
		return "/autoRespawningEnabled [<true|false>] // Makes Learning Mobs autorespawn when dying, enabling infinite iteration in the q-learning algorithm.";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] args) 
	{
		if (args.length > 1)
		{
			icommandsender.addChatMessage(new ChatComponentTranslation("msg.autoRespawningEnabledCommand_InvalidArguments.txt"));
			return;
		}
		if (args.length == 0)
		{
			if (EntityLearningMob.autoRespawningEnabled)
				icommandsender.addChatMessage(new ChatComponentTranslation("msg.autoRespawningEnabledCommand_isTrue.txt"));
			else
				icommandsender.addChatMessage(new ChatComponentTranslation("msg.autoRespawningEnabledCommand_isFalse.txt"));

			return;
		}
		
		if (args[0].equals("true"))
		{
			icommandsender.addChatMessage(new ChatComponentTranslation("msg.autoRespawningEnabledCommand_setToTrue.txt"));
			EntityLearningMob.autoRespawningEnabled = true;
		}
		else if (args[0].equals("false"))
		{
			icommandsender.addChatMessage(new ChatComponentTranslation("msg.autoRespawningEnabledCommand_setToFalse.txt"));
			EntityLearningMob.autoRespawningEnabled = false;
		}
		else
		{
			icommandsender.addChatMessage(new ChatComponentTranslation("msg.autoRespawningEnabledCommand_InvalidArguments.txt"));
		}
		return;
	}

}
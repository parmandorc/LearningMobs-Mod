package com.parmandorc.LearningMobs.Commands;

import java.util.HashMap;

import com.parmandorc.LearningMobs.Fighter.EntityLMFighter;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentTranslation;

public class ClearQValuesCommand extends CommandBase 
{

	@Override
	public String getCommandName() 
	{
		return "clearQValues";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) 
	{
		return "/clearQValues [LearningMobClassName] // Clears the q-values map of all Learning Mobs, or just of the specified class. Warning: information may be irretrievably lost! LearningMobClassName valid values: LMFighter";
	}

	@Override
	public void processCommand(ICommandSender commandsender, String[] args) 
	{
		if (args.length > 1)
		{
			commandsender.addChatMessage(new ChatComponentTranslation("msg.clearQValuesCommand_InvalidArguments.txt"));
			return;
		}
		
		if (args.length == 0)
		{
			//Clear all
			commandsender.addChatMessage(new ChatComponentTranslation("msg.clearQValuesCommand_ClearAll.txt"));
			
			//LMFighter
			if (EntityLMFighter.Q_values == null)
				EntityLMFighter.Q_values = new HashMap<Integer,Double>();
			else
				EntityLMFighter.Q_values.clear();
			EntityLMFighter.QRandomSelectionRate = 1;
			EntityLMFighter.QIterations = 0;
		}
		else
		{
			//Clear this only
			if (args[0].equals("LMFighter"))
			{
				commandsender.addChatMessage(new ChatComponentTranslation("msg.clearQValuesCommand_ClearLMFighter.txt"));
				if (EntityLMFighter.Q_values == null)
					EntityLMFighter.Q_values = new HashMap<Integer,Double>();
				else
					EntityLMFighter.Q_values.clear();
				EntityLMFighter.QRandomSelectionRate = 1;
				EntityLMFighter.QIterations = 0;
			}
			else
			{
				commandsender.addChatMessage(new ChatComponentTranslation("msg.clearQValuesCommand_InvalidLMClass.txt"));
				return;
			}
		}
	}

}

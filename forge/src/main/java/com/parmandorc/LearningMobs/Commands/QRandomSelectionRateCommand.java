/**
 * @author Pablo Rodríguez, parmandorc
 * If you use this code, please remember to give credit by linking to the mobs url:
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2241864-learningmobs-mod
 * 
 * This commands sets or prints the QRandomSelectionRate factor of specified class.
 * This value determines how probably will a task be chosen randomly instead of choosing the best task.
 */
package com.parmandorc.LearningMobs.Commands;

import com.parmandorc.LearningMobs.Fighter.EntityLMFighter;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

public class QRandomSelectionRateCommand extends CommandBase
{
	@Override
	public String getCommandName() 
	{
		return "QRandomSelectionRate";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) 
	{
		return "/QRandomSelectionRate <LearningMobClassName> [value] // Sets or prints the QRandomSelectionRate factor for the specified class. LearningMobClassName valid values: LMFighter";
	}

	@Override
	public void processCommand(ICommandSender commandsender, String[] args) 
	{
		if (args.length == 0 || args.length > 2)
		{
			commandsender.addChatMessage(new ChatComponentTranslation("msg.QRandomSelectionRateCommand_InvalidArguments.txt"));
			return;
		}
		
		if (args[0].equals("LMFighter"))
		{
			if (args.length == 1)
			{
				commandsender.addChatMessage(new ChatComponentTranslation("msg.QRandomSelectionRateCommand_printValue.txt"));
				commandsender.addChatMessage(new ChatComponentText("LMFighter: "+EntityLMFighter.QRandomSelectionRate));
				return;
			}
			
			double value = getDoubleValue(args[1]);
			if (value == -1)
			{
				commandsender.addChatMessage(new ChatComponentTranslation("msg.QRandomSelectionRateCommand_InvalidValue.txt"));
				return;
			}
			else
			{
				commandsender.addChatMessage(new ChatComponentTranslation("msg.QRandomSelectionRateCommand_setValue.txt"));
				commandsender.addChatMessage(new ChatComponentText("LMFighter: "+value));
				EntityLMFighter.QRandomSelectionRate = value;
			}
		}
		else
		{
			commandsender.addChatMessage(new ChatComponentTranslation("msg.QRandomSelectionRateCommand_InvalidClass.txt"));
			return;
		}
	}

	private double getDoubleValue(String string)
	{
		double value;
		try  
		{  
			value = Double.parseDouble(string);  
		}  
		catch(NumberFormatException nfe)  
		{  
			return -1;  
		}  
		
		if (value < 0 || value > 1)
			return -1;
		
		return value;  
	}
}
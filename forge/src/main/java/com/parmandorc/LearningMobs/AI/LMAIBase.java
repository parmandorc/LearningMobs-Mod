/**
 * @author Pablo Rodríguez, parmandorc
 * If you use this code, please remember to give credit by linking to the mobs url:
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2241864-learningmobs-mod
 * 
 * For being able to use the task system of the vanilla mobs, tasks had to be tweaked in order to be able to execute only
 * one of them at a time, and them having a finite duration (or in other worlds, being able to finish).
 * All tasks used by LearningMobs will have to extend from this to ensure meeting this requirements.
 */
package com.parmandorc.LearningMobs.AI;

import com.parmandorc.LearningMobs.EntityLearningMob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public abstract class LMAIBase extends EntityAIBase 
{
	/**The entity that is executing this task*/
	EntityLearningMob owner;
	/**The time the mob will have to wait when executing this task to be able to execute it again. Currently without use. Planned for the future*/
	protected int cooldown;
	/**The time it takes a mob to execute this task*/
	protected int duration;
	/**The tick the task's execution is currently in*/
	protected int currentTick;
	
	/**Base constructor*/
	public LMAIBase(EntityLearningMob owner, int duration, int cooldown) 
	{
		this.duration = duration;
		this.cooldown = cooldown;
		this.currentTick = 0;
		this.owner = owner;
	}
	
	/**
	 * @return whether this task's execution has ended
	 */
	public boolean hasFinished() 
	{
		return currentTick >= duration;
	}
	
	/**
	 * Sets this task's execution to its initial point.
	 */
	public void reset() 
	{
		currentTick = 0;
	}
	
	/**
	 * Called every tick of this task's execution.
	 */
	public void updateTask()
	{
		currentTick++;
	}
}

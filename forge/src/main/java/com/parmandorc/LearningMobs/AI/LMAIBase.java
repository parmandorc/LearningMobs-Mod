package com.parmandorc.LearningMobs.AI;

import com.parmandorc.LearningMobs.EntityLearningMob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;

public abstract class LMAIBase extends EntityAIBase 
{
	EntityLearningMob owner;
	protected int cooldown;
	protected int duration;
	protected int currentTick;
	
	public LMAIBase(EntityLearningMob owner, int duration, int cooldown) 
	{
		this.duration = duration;
		this.cooldown = cooldown;
		this.currentTick = 0;
		this.owner = owner;
	}
	
	public boolean hasFinished() 
	{
		return currentTick >= duration;
	}
	
	public void reset() 
	{
		currentTick = 0;
	}
	
	public void updateTask()
	{
		currentTick++;
	}
}

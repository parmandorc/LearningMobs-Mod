package com.parmandorc.LearningMobs.AI;

import com.parmandorc.LearningMobs.EntityLearningMob;
import com.parmandorc.LearningMobs.EntityLearningMob.State;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

public class MoveTowardsTarget extends LMAIBase 
{
	EntityLivingBase target;
	Vec3 vectorToTarget;
	double speed;
	
	public MoveTowardsTarget(EntityLearningMob owner, double speed) 
	{
		super(owner, 20, 20);
		
		this.speed = speed;
	}

	public boolean shouldExecute() 
	{
		if (this.hasFinished())
			return false;
		
		target = owner.getAttackTarget();
		
		if (target == null)
		{
			owner.getNavigator().clearPathEntity();
			return false;
		}
		
		return true;
	}
	
	public boolean continueExecuting()
	{
		return this.shouldExecute() && !owner.getNavigator().noPath();
	}
	
	public void startExecuting()
	{	
		owner.getNavigator().tryMoveToEntityLiving(target, speed);
	}
	
	@Override
	public void updateTask()
	{	
		super.updateTask();
		
		if (this.hasFinished())
		{
			owner.getNavigator().clearPathEntity();
		}
		else
		{
			State newState = owner.new State(owner, target);
			if(!owner.getCurState().distanceStateEquals(newState))
			{
				this.currentTick = this.duration;
				owner.getNavigator().clearPathEntity();
			}
		}
	}
}

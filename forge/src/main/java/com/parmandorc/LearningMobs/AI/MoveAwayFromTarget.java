package com.parmandorc.LearningMobs.AI;

import java.util.Random;

import com.parmandorc.LearningMobs.EntityLearningMob;
import com.parmandorc.LearningMobs.EntityLearningMob.State;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.Vec3;

public class MoveAwayFromTarget extends LMAIBase 
{
	EntityLivingBase target;
	Vec3 vectorToTarget;
	double speed;
	
	public MoveAwayFromTarget(EntityLearningMob owner, double speed) 
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
		Vec3 aux1 = Vec3.createVectorHelper(owner.posX, owner.posY, owner.posZ);
		Random rand = this.owner.getRNG();
		Vec3 aux4 = ((aux1.subtract(Vec3.createVectorHelper(target.posX, target.posY, target.posZ))).subtract(aux1)).addVector(rand.nextDouble(), rand.nextDouble()*3-1, rand.nextDouble());
		owner.getNavigator().tryMoveToXYZ(aux4.xCoord, aux4.yCoord, aux4.zCoord, speed);
	}
	
	@Override
	public void updateTask()
	{	
		super.updateTask();

		if (currentTick == duration || owner.getDistanceToEntity(target) > owner.getEntityAttribute(SharedMonsterAttributes.followRange).getBaseValue()*0.6) //To avoid issues when walking out the range)
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
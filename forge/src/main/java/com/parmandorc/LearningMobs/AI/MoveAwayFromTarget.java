/**
 * @author Pablo Rodríguez, parmandorc
 * If you use this code, please remember to give credit by linking to the mobs url:
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2241864-learningmobs-mod
 * 
 * Make the entity move away from its target for one second or until the distance to the target has effectively changed.
 */
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
		//Random is needed for special scenarios where the point the entity is trying to move is way up in the air or under ground.
		Vec3 aux2 = ((aux1.subtract(Vec3.createVectorHelper(target.posX, target.posY, target.posZ))).subtract(aux1)).addVector(rand.nextDouble()-0.5, rand.nextDouble()*3-1.5, rand.nextDouble()-0.5);
		owner.getNavigator().tryMoveToXYZ(aux2.xCoord, aux2.yCoord, aux2.zCoord, speed);
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
			//Finish the task if distance to target has effectively changed.
			State newState = owner.new State(owner, target);
			if(!owner.getCurState().distanceStateEquals(newState))
			{
				this.currentTick = this.duration;
				owner.getNavigator().clearPathEntity();
			}
		}
	}
}
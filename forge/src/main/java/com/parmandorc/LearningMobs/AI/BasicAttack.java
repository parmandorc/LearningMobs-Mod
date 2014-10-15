package com.parmandorc.LearningMobs.AI;

import com.parmandorc.LearningMobs.EntityLearningMob;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.world.WorldServer;

public class BasicAttack extends LMAIBase 
{
	EntityLivingBase target;
	double range;
	
	public BasicAttack(EntityLearningMob owner, double range) 
	{
		super(owner, 20, 20);
		this.range = range;
	}

	public boolean shouldExecute() 
	{
		if (this.hasFinished())
			return false;
		
		return true;
	}
	
	public boolean continueExecuting()
	{
		return this.shouldExecute();
	}
	
	@Override
	public void updateTask()
	{	
		super.updateTask();

		if (currentTick == 1)
		{			
			this.target = owner.getAttackTarget();
			
			if (target != null && owner.getDistanceToEntity(target) <= range)
			{
//				owner.faceEntity(target, 5.0F, 5.0F);
//				this.owner.getLookHelper().setLookPosition(this.target.posX, this.target.posY + (double)this.target.getEyeHeight(), this.target.posZ, 10.0F, (float)this.owner.getVerticalFaceSpeed());

				owner.attackEntityAsMob(target);
			}
			
	        if (this.owner.getHeldItem() != null)
	        {
	            this.owner.swingItem();
	        }
		}
		
	}
}

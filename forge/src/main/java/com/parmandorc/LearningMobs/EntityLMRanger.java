/**
 * @author Pablo Rodríguez, parmandorc
 * If you use this code, please remember to give credit by linking to the mobs url:
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2241864-learningmobs-mod
 * 
 * Class of the Ranger LM class. Currently without use. Planned for the future.
 */
package com.parmandorc.LearningMobs;

import java.util.HashMap;

import net.minecraft.world.World;

public class EntityLMRanger extends EntityLearningMob {

	public EntityLMRanger(World p_i1738_1_) {
		super(p_i1738_1_);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected HashMap<Integer, Double> getQ_Values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getTotalLMTasks() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void Q_values_init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected double getQRandomSelectionRate() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setQRandomSelectionRate(double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected int getQIterations() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void setQIterations(int value) {
		// TODO Auto-generated method stub
		
	}

}

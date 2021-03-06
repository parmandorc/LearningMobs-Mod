/**
 * @author Pablo Rodríguez, parmandorc
 * If you use this code, please remember to give credit by linking to the mobs url:
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2241864-learningmobs-mod
 * 
 * LMFighter class. This is a melee fighting mob, that attacks with its sword.
 */
package com.parmandorc.LearningMobs.Fighter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;

import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.parmandorc.LearningMobs.EntityLearningMob;
import com.parmandorc.LearningMobs.AI.BasicAttack;
import com.parmandorc.LearningMobs.AI.LMAIBase;
import com.parmandorc.LearningMobs.AI.MoveAwayFromTarget;
import com.parmandorc.LearningMobs.AI.MoveTowardsTarget;


public class EntityLMFighter extends EntityLearningMob 
{	
	/** Table of knowledge used in the q-learning algorithm. 
	 * Knowledge base is shared amongst all entities of same class in the world.
	 * The key is obtained by the method getQKey() */
	public static HashMap<Integer,Double> Q_values;

	/** This factor determines how probably will a task be chosen randomly instead of choosing the best task. */
	public static double QRandomSelectionRate = 1;

	/** Number of qiterations executed since the beginning of this game's execution. Currently with no proper use.*/
	public static int QIterations = 0;

	
	/**Total number of LM tasks this LMob is able to use*/
	final int totalLMTasks = 3;
	/** List of LM tasks this LMob is able to use. Each number will designate
	 * the position of each task in the tasks array LMTasks[] from super class. 
	 * Thus, values must be in the range 0 - totalLMTasks. */
	final int LMTask_moveTowards = 0;
	final int LMTask_moveAway = 1;
	final int LMTask_basicAttack = 2;

	/**Constructor*/
	public EntityLMFighter(World p_i1738_1_) 
	{
		super(p_i1738_1_);
		this.setSize(0.9F, 1.3F);
		
		//Creation of the LMTasks array and every single task.
		LMTasks = new LMAIBase[totalLMTasks];
		LMTasks[LMTask_moveTowards] = new MoveTowardsTarget(this, 1.4);
		LMTasks[LMTask_moveAway] = new MoveAwayFromTarget(this, 1.4);
		LMTasks[LMTask_basicAttack] = new BasicAttack(this, meleeRange);
	}
	
	@Override
	/**
	 * Sets equipment upon spawning
	 */
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData p_110161_1_)
	{
		Object p_110161_1_1 = super.onSpawnWithEgg(p_110161_1_);
		
		this.setCurrentItemOrArmor(0, new ItemStack(Items.iron_sword));
		
		return p_110161_1_;
	}
	
	@Override
	/** Sets this mobs AD */
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(PVEEnabled ? 0 : -4.0);
    }
	
	@Override
	/** Currently only handles changes in PVE mode, since this mob deals 2 damage to other LM, but 6 damage to players. */
	public void onUpdate()
	{
        this.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(PVEEnabled ? 0 : -4.0);

		super.onUpdate();
	}
	
	@Override
	/**This method takes care of obtaining the Q_values table from file if this is the first entity of this class spawning,
	 * or if this file doesn't exist, creates an empty map.*/
	protected void Q_values_init()
	{
		if (Q_values == null)
		{
			File file = new File("./saves/learningmobsmod/qvaluesfiles/LMFighter.ser");
			if (file.exists())
			{
				try {
					FileInputStream fis = new FileInputStream(file.getPath());
					ObjectInputStream ois = new ObjectInputStream(fis);
					Q_values = (HashMap<Integer,Double>) ois.readObject();
					ois.close();
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
			else
				Q_values = new HashMap<Integer,Double>();
		}
	}

	@Override
	protected HashMap<Integer,Double> getQ_Values() { return Q_values; }

	@Override
	protected int getTotalLMTasks() { return this.totalLMTasks; }

	@Override
	protected double getQRandomSelectionRate() { return this.QRandomSelectionRate; }

	@Override
	protected void setQRandomSelectionRate(double value) { this.QRandomSelectionRate = value; }

	@Override
	protected int getQIterations() { return this.QIterations; }

	@Override
	protected void setQIterations(int value) { this.QIterations = value; }
}

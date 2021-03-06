/**
 * @author Pablo Rodríguez, parmandorc
 * If you use this code, please remember to give credit by linking to the mobs url:
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2241864-learningmobs-mod
 * 
 * The base class that use all types of mobs from the mod. It controls the basic system that is shared amongst every mob class,
 * including the SARSA q-learning algorithm, autorespawning management, and basic shared attributes.
 */
package com.parmandorc.LearningMobs;

import java.util.HashMap;

import com.parmandorc.LearningMobs.AI.LMAIBase;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public abstract class EntityLearningMob extends EntityCreature
{
	/**Q-learning algorithm variables and constants*/
	/**Array of tasks a LMob is able to use. Mob class that extends from this will need to fill this array upon contruction.*/
	protected LMAIBase[] LMTasks;
	/**Pointers to the positions in LMTasks array of the currently executing task, and the next task that should execute, respectively.*/
	private int currentTask, nextTask;
	/**Battle states saved before and after the current task's execution, respectively.*/
	private State prevState, curState;
	/**Value used in the Q-learning algorithm. The higher this value, the higher the impact of instant rewards is.*/
	private static final double QLearningRate = 0.2;
	/**Value used in the Q-learning algorithm. The higher this value, the higher the impact of long term rewards is.*/
	private static final double QDiscountFactor = 0.15;
	/**Value used in the Q-learning algorithm. It determines the variation on the QRandomSelectionRate factor for every iteration, which will be defined in the mob class*/
	private static final double QRandomDiscountFactor = 0.99999;
	
	//Enables autoRespawning mode
	public static boolean autoRespawningEnabled = false;
	//Used in autoRespawning handling
	private boolean needsRespawning = false;
	
	//When a mob kills is LearningMob target, this sets to true, enabling a max reward for immediately next iteration.
	private boolean justKilledTarget = false;

	//Constant, melee combat distance
	protected static final double meleeRange = 3.5;
	//Constant, ranged combat distance
	protected static final double rangedRange = 15;
	
	//Enabled PVE mode, or if false, enables LearningMob vs LearningMob
	public static boolean PVEEnabled;
	//Targeting task for targeting players
	private EntityAINearestAttackableTarget targetPlayers;
	//Targeting task for targeting LearningMobs
	private EntityAINearestAttackableTarget targetOtherLM;
	//Pointer to one of the two tasks before
	private EntityAINearestAttackableTarget currentTargeting;

	/**Group of parameters used for comparison of battle values between one iteration and the next. Includes the mobs health, its targets health and the distance between the two entities.*/
	public class State{
		EntityLearningMob owner;
		float targetsHealth = 0;
		float targetsMaxHealth = 10;
		float ownersHealth;
		float distanceToTarget;
		
		/**Constructor*/
		public State(EntityLearningMob owner, EntityLivingBase target)
		{
			this.owner = owner;
			this.ownersHealth = owner.getHealth();
			if (target != null)
			{
				this.targetsHealth = target.getHealth();
				this.targetsMaxHealth = target.getMaxHealth();
				this.distanceToTarget = owner.getDistanceToEntity(target);
			}
		}
		
		//Getters
		public float getOwnersHealth(){	return ownersHealth; }
		
		public float getTargetsHealth(){ return targetsHealth; }
		
		public float getTargetsMaxHealth(){ return targetsMaxHealth; }
		
		public float getDistanceToTarget(){ return distanceToTarget; }
		
		/**
		 * Used to determine if the distance between entities has effectively changed (from the algorithms point of view)
		 * @param other The state to compare
		 * @return true if and only if the distance value is effectively different
		 */
		public boolean distanceStateEquals(State other)
		{
			return getDistanceState(this.distanceToTarget) == getDistanceState(other.distanceToTarget);
		}
	}
	
	/**Constructor
	 * 
	 * Adds common tasks to all LearningMobs, including targeting mode.
	 * Initializes the Qvalues map of the entity (actually calls the method that does that)
	 */
	public EntityLearningMob(World p_i1738_1_) 
	{
		super(p_i1738_1_);

		//Tasks common to all LMobs, independently of their class.
		this.tasks.addTask(1, new EntityAISwimming(this));
		this.targetPlayers = new EntityAINearestAttackableTarget(this, EntityPlayer.class, 0, false);
		this.targetOtherLM = new EntityAINearestAttackableTarget(this, EntityLearningMob.class, 0, false);
		currentTargeting = PVEEnabled ? targetPlayers : targetOtherLM;
		this.targetTasks.addTask(1, currentTargeting);

		//Initialization of pointers for correct management of LMTasks array. This means no task has been executed or chosen yet.
		currentTask = nextTask = -1;
		
		Q_values_init();
		
		//Prevents entity from naturally despawning
		this.func_110163_bv();
	}

	/**
	 * Makes the entity invulnerable while its autoRespawning is being handled.
	 * Needed to avoid issues with entity not completely respawning if hit in the autorespawn process.
	 */
	@Override
    public boolean isEntityInvulnerable()
    {
        return this.needsRespawning;
    }
    
	/**
	 * @return The currently executing task, or -1 if none has executed yet.
	 */
	private LMAIBase getCurrentTask()
	{
		return (currentTask == -1) ? null : LMTasks[currentTask];
	}
	
	/**
	 * @return The currents combat state of the mob
	 */
	public State getCurState() { return curState; }

	/**
	 * Applies basic common attributes to all LearningMobs, like followRange, maxHealth or movementSpeed.
	 */
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.attackDamage);
        this.getEntityAttribute(SharedMonsterAttributes.followRange).setBaseValue(50.0D);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20.0D);
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.2D);
    }
    
    /**
     * Used for persistence of PVE mode and QRandomSelectionRate factor between game executions.
     */
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);

        nbt.setDouble("QRandomSelectionRate", this.getQRandomSelectionRate());
        nbt.setBoolean("PVEEnabled", PVEEnabled);
    }
    
    /**
     * Used for persistence of PVE mode and QRandomSelectionRate factor between game executions.
     */
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);

        double qrsr = nbt.getDouble("QRandomSelectionRate");
        this.setQRandomSelectionRate(qrsr == 0 ? 1 : qrsr);
        this.PVEEnabled = nbt.getBoolean("PVEEnabled");
    }
  
    /**
     * Called when attacking another entity. Adaptation of the original method from the vanilla EntityMob class.
     * Plans: enabling knockback attacks and other combat effects (commented code).
     */
	public boolean attackEntityAsMob(Entity p_70652_1_)
	{
        float f = (float)this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
        
//        int i = 0;
//
//        if (p_70652_1_ instanceof EntityLivingBase)
//        {
//            f += EnchantmentHelper.getEnchantmentModifierLiving(this, (EntityLivingBase)p_70652_1_);
//            i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)p_70652_1_);
//        }

        boolean flag = p_70652_1_.attackEntityFrom(DamageSource.causeMobDamage(this), f);

//        if (flag)
//        {
//            if (i > 0)
//            {
//                p_70652_1_.addVelocity((double)(-MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F) * (float)i * 0.5F));
//                this.motionX *= 0.6D;
//                this.motionZ *= 0.6D;
//            }
//
//            int j = EnchantmentHelper.getFireAspectModifier(this);
//
//            if (j > 0)
//            {
//                p_70652_1_.setFire(j * 4);
//            }
//
//            if (p_70652_1_ instanceof EntityLivingBase)
//            {
//                EnchantmentHelper.func_151384_a((EntityLivingBase)p_70652_1_, this);
//            }
//
//            EnchantmentHelper.func_151385_b(this, p_70652_1_);
//        }

        return flag;
	}
	
	@Override
	/**
	 * Currently just handles the targeting mode changes.
	 */
	public void onUpdate()
	{
		//PVE mode handling
		if ((PVEEnabled && currentTargeting == targetOtherLM) || (!PVEEnabled && currentTargeting == targetPlayers))
		{
			this.targetTasks.removeTask(currentTargeting);
			currentTargeting = PVEEnabled ? targetPlayers : targetOtherLM;
			this.targetTasks.addTask(1, currentTargeting);
		}
		
		super.onUpdate();
	}
	
	protected boolean isAIEnabled()
	{
		return true;
	}
	
	/** 
	 * This is where the q-learning algorithm iterates.
	 * Every iteration, and only once the current executing task has finished, compares the states before and after executing the task,
	 * and obtains a reward based on this, in order to update the QValues map accordingly. Then it chooses a new task, either randomly
	 * or checking in the qvalues map again for the best possible task from current state.
	 * 
	 * Special cases:
	 * -First iteration: if no task was performed before, and thus no state was saved, just chooses a task to execute.
	 * -Autorespawning handling
	 * -Fight won: if the target was killed, there's no need to choose a new task.
	 */
	protected void updateAITick()
	{	
		//Handles respawn
		if (needsRespawning)
		{
			this.setHealth(0);
			return;
		}
		
		if (this.getAttackTarget() == null && !justKilledTarget)
			return;
		
		if (currentTask == -1) //LM just spawned and has no LMTask started.
		{
			curState = new State(this, this.getAttackTarget());
			obtainNextTask();
			currentTask = nextTask;
			this.tasks.addTask(5, getCurrentTask());
			prevState = curState;
		}
		else if (getCurrentTask().hasFinished()) //Q-learning algorithm iterates once the current task has finished.
		{
			//Obtain this entities state after finished task
			this.curState = new State(this, this.getAttackTarget());
			
			//Obtain reward based on the states after and before executing the task.
			double reward = obtainReward();
			
			//Update Q_values based on reward
			updateQ_Values(reward);
			
			if (justKilledTarget) //If justKilledTarget, it will have to select a new target in next iteration. This one has thus finished.
			{
				nextTask = -1;
				currentTask = -1;
				justKilledTarget = false;
				return;
			}
			
			//Obtain task for new iteration based on current state and Q_values table.
			obtainNextTask();
			
			//Replacement of the task that should execute.
			if (currentTask == nextTask) //Optimization in case it is not necessary to replace the current task.
			{
				getCurrentTask().reset();
			}
			else
			{
				this.tasks.removeTask(getCurrentTask());
				currentTask = nextTask;
				getCurrentTask().reset();
				this.tasks.addTask(5, getCurrentTask());
			}
			
			//Save the state for comparison in next iteration.
			prevState = curState;
		}
	}
	
	
	/** Obtains a reward based on the states before and after executing a task.
	 * Takes into account the variation in this entity's life and the enemy's life. */
	private double obtainReward()
	{
		return justKilledTarget ? 10 : curState.getOwnersHealth() - prevState.getOwnersHealth() +
				prevState.getTargetsHealth() - curState.getTargetsHealth();
	}
	
	/**Updates the value for the current state and the just finished task in the Q_values table, based on the obtained reward.*/
	private void updateQ_Values(double reward)
	{
		int key = getQKey(getQState(prevState),currentTask);
		double qvalue = getQ_Values().containsKey(key) ? getQ_Values().get(key) : 0;
		int maxEstimateKey = getQKey(getQState(curState),getTaskWithMaxQValue(curState));
		double maxEstimate = getQ_Values().containsKey(maxEstimateKey) ? getQ_Values().get(maxEstimateKey) : 0;
		
		qvalue += QLearningRate * (reward + QDiscountFactor * maxEstimate - qvalue);
		
		getQ_Values().put(key, qvalue);
		setQIterations(getQIterations() + 1);
	}
	
	/**Selects a new task for the next iteration.
	 * It can return a random task or the task with the highest expected reward for the current state,
	 * based on the QRandomSelectionRate factor's value (the higher this value, the higher the possibility of using this task.*/
	private void obtainNextTask()
	{
		if (this.rand.nextDouble() < getQRandomSelectionRate())
			nextTask = this.rand.nextInt(getTotalLMTasks());
		else
		{
			int bestTask = getTaskWithMaxQValue(curState);
			int key = getQKey(getQState(curState), bestTask);
			double qvalue = getQ_Values().containsKey(key) ? getQ_Values().get(key) : 0;
			nextTask = bestTask;
		}

		setQRandomSelectionRate(getQRandomSelectionRate() * QRandomDiscountFactor);
	}
	
	/**Designates the state passed as parameter a unique id value, different from every state.
	 * Takes into account the distance to target, the life of this entity and the life of the enemy.
	 */
	private int getQState(State state)
	{
		return getDistanceState(state.getDistanceToTarget()) * 9 + getHPState(state.getOwnersHealth(),this.getMaxHealth()) * 3 + 
				getHPState(state.getTargetsHealth(),state.getTargetsMaxHealth());
	}
	
	/**Designates a unique id value to the combination of a state and a task. This will be the key in the Q_values table.*/
	private int getQKey(int state, int task)
	{
		return state * getTotalLMTasks() + task;
	}
	
	/**Returns the task with the highest expected reward (in the Q_values table) for passed state.*/
	private int getTaskWithMaxQValue(State state)
	{
		int task = 0;
		double maxvalue = 0;
		for (int i = 0, key = getQKey(getQState(state), 0); i < getTotalLMTasks(); i++, key++)
		{
			double qvalue = getQ_Values().containsKey(key) ? getQ_Values().get(key) : 0;
			if (maxvalue < qvalue || i == 0)
			{
				maxvalue = qvalue;
				task = i;
			}
		}
		return task;
	}
	
	/**Designates a unique id value to the distance passed.
	 * At a higher level, it divides the possibilities of distance between entity and enemy in three levels: out of range, ranged range or melee range.*/
	private int getDistanceState(float distance)
	{
		return (distance < meleeRange) ? 0 : (distance < rangedRange) ? 1 : 2;
	}
	
	/**Designates a unique id value to a certain HP value, based on the max HP value possible.
	 * At a higher level, it divides the HP bar of an entity in three levels: high, medium or low health. */
	private int getHPState(float health, float maxHealth)
	{
		float HPpercent = health / maxHealth;
		return (HPpercent == 1) ? 2 : (int)((HPpercent * 10 - 1) / 3);
	}
	
	/**Called when this entity kills its target. Enables max reward for next q-learning algorithm iteration*/
	public void onKillEntity(EntityLivingBase p_70074_1_)
	{
		super.onKillEntity(p_70074_1_);
		
		this.justKilledTarget = true;
	}
	
	/**
	 * AutoRespawningHandling. The way this is possible is by healing the target to its max health (in onDeath).
	 * In order to reset the death animation, the mob has to immediately be killed again (in updateAITick) and then 
	 * finally being healed to its max health (in onDeathUpdate), thus finishing the resurrection process.
	 */
	
	/**
	 * AutoRespawning handling. Also, sets the mob to a random position before resurrecting.
	 */
	protected void onDeathUpdate()
	{
		super.onDeathUpdate();

		if (this.autoRespawningEnabled)
		{
			if (deathTime == 10) //This must be done before tick 20, since the reposition has to be done will invulnerable to avoid issues
			{
				EntityLivingBase target = this.getAttackTarget();
				if (this.worldObj != null &&  target != null)
				{
					//Respawn in a near random position around target
					double maxDistance = this.getEntityAttribute(SharedMonsterAttributes.followRange).getBaseValue()*0.6;
					boolean positioned = false;
					int maxTries = 200;
					while(!positioned && maxTries > 0)
					{
						double newPosX = target.posX + this.rand.nextDouble() * maxDistance*2 - maxDistance;
						double newPosY = target.posY;
						double newPosZ = target.posZ + this.rand.nextDouble() * maxDistance*2 - maxDistance;
						do
						{
							this.setPosition(newPosX, newPosY, newPosZ);
							newPosY++;
						} 
						while (this.getDistanceToEntity(target) < maxDistance && !this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty());
						
						if (this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox).isEmpty() && canRespawnHere())
							positioned = true;
						
						maxTries--;
					} 
				}
			}
			else if (deathTime == 20)
				handleAutoRespawn();
		}
    }
	
	/**Checks if mob is not standing over a prohibited block when respawning. False for StaticLiquids and Fences.*/
	private boolean canRespawnHere()
	{
		int x = this.posX >= 0 ? (int)this.posX : (int)this.posX - 1;
		int z = this.posZ >= 0 ? (int)this.posZ : (int)this.posZ - 1;
		Block blockUnderThis = this.worldObj.getBlock(x, (int)this.posY, z);
		for (int i = (int)this.posY; i > -64 && blockUnderThis instanceof BlockAir; i--)
			blockUnderThis = this.worldObj.getBlock(x, i, z);
				
		return !(blockUnderThis instanceof BlockStaticLiquid) && !(blockUnderThis instanceof BlockFence);
	}
    
	/**
	 * Autorespawning handling. Final iteration in the q-learning algorithm for negative max reward.
	 */
	public void onDeath(DamageSource p_70645_1_)
	{
		super.onDeath(p_70645_1_);

		 //Will make a final iteration in the q-learning algorithm to earn a negative reward
		if (prevState != null)
		{
			this.curState = new State(this, this.getAttackTarget());
			if (!this.justKilledTarget) //If justKilledTarget, it will take max and min reward, which equals reward = 0, so there's no need to update qvalues
				updateQ_Values(-10);
			else
				justKilledTarget = false;
			
			//Resets the next and current task pointers to initial point (as if no task was ever performed)
			nextTask = -1;
			if (currentTask != -1) //Remove currentTask from the tasks list.
			{
				this.getCurrentTask().reset();
				this.tasks.removeTask(getCurrentTask());
			}
			currentTask = -1;
		}
		
		if (this.autoRespawningEnabled)
			handleAutoRespawn();
	}
	
	/** Autorespawning handling.*/
	private void handleAutoRespawn()
	{
		this.isDead = false;
		this.deathTime = 0;
		this.hurtTime = 0;
		this.setHealth(20);
		this.needsRespawning = !this.needsRespawning;
	}
	
	/**Called when player interacts with a mob. Currently for debugging purposes only.
	 * In the future, will probably open GUI for visualizing q-learning algorithm data.
	 */
	protected boolean interact(EntityPlayer p_70085_1_)
	{
		ItemStack itemstack = p_70085_1_.getCurrentEquippedItem();
        if (itemstack != null && itemstack.getItem() == Items.stick)
        {
        	System.out.println(this + " HP: "+this.getHealth()+"/"+this.getMaxHealth());
        	return true;
        }
        if (itemstack != null && itemstack.getItem() == Items.blaze_rod)
        {
        	System.out.println("QIterations = "+getQIterations() + ", QRandomSelectionRate = "+getQRandomSelectionRate());
        	System.out.println("PRINTING Q_VALUES BY TASKS");
        	String text = "";
        	for (int i = 0; i < getTotalLMTasks(); i++)
        	{
        		text = "Task #" + i + ": ";
        		for (int j = i; j < 81; j+=3)
        		{
        			if (getQ_Values().containsKey(j))
        				text += j + ":" + getQ_Values().get(j) + " ";
        		}
        		System.out.println(text);
        	}
        	return true;
        }
        if (itemstack != null && itemstack.getItem() == Items.arrow)
        {
        	System.out.println("QIterations = "+getQIterations() + ", QRandomSelectionRate = "+getQRandomSelectionRate());
        	System.out.println("PRINTING Q_VALUES");
        	System.out.println(getQ_Values().toString());
        	return true;
        }
		return false;
	}
	
	/**
	 * All LearningMob classes that extend from this will need to implement the following methods.
	 */
	
	/**
	 * Getter of the QValues map.
	 */
	protected abstract HashMap<Integer,Double> getQ_Values();
	/**
	 * Getter of the total number of tasks the mob is able to perform.
	 */
	protected abstract int getTotalLMTasks();
	/**
	 * This method is in charge of setting the QValues map upon construction of the mob. Will load the map from the correct file in the saves folder, or if inexistent, will create an empty map.
	 */
	protected abstract void Q_values_init();
	/**
	 * Getter of the QRandomSelectionRate factor. This determines how probably will a random task be chosen, instead of the best task.
	 */
	protected abstract double getQRandomSelectionRate();
	/**
	 * Setter of the QRandomSelectionRate factor.
	 */
	protected abstract void setQRandomSelectionRate(double value);
	/**
	 * Getter of the number of qiterations that have been executed since the beginning of executions. Currently without proper use.
	 */
	protected abstract int getQIterations();
	/**
	 * Setter of the number of qiterations that have been executed since the beginning of executions. Currently without proper use.
	 */
	protected abstract void setQIterations(int value);
}

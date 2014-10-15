package com.parmandorc.LearningMobs;

import com.parmandorc.LearningMobs.Commands.AutoRespawningEnabledCommand;
import com.parmandorc.LearningMobs.Commands.ClearQValuesCommand;
import com.parmandorc.LearningMobs.Commands.PVEEnabledCommand;
import com.parmandorc.LearningMobs.Commands.QRandomSelectionRateCommand;
import com.parmandorc.LearningMobs.Fighter.EntityLMFighter;
import com.parmandorc.LearningMobs.Fighter.RenderLMFighter;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityList.EntityEggInfo;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.server.FMLServerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = LearningMobs.modid, name = "LearningMobs Mod", version = LearningMobs.version)
public class LearningMobs 
{
	public static final String modid = "learningmobsmod";
	public static final String version = "1.0";
	public static int spawnEggUniqueID = 300;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		System.out.println("event.getModConfigurationDirectory().getAbsolutePath() - " + event.getModConfigurationDirectory().getAbsolutePath());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(new QValuesSavingHandler());
		
		int fighterColor = (180 << 16) + (100 << 8) + 100;
		int rangerColor = (100 << 16) + (100 << 8) + 100;
		int blackColor = 0;
		
		EntityRegistry.registerModEntity(EntityLMFighter.class, "LMFighter", 0, this, 80, 3, true);
//		EntityRegistry.registerModEntity(EntityLMRanger.class, "LMRanger", 1, this, 80, 3, true);
				
		LanguageRegistry.instance().addStringLocalization("entity."+this.modid+".LMFighter.name","LMFighter");
		
		registerEntityEgg(EntityLMFighter.class, blackColor, fighterColor);
		
		RenderingRegistry.registerEntityRenderingHandler(EntityLMFighter.class, new RenderLMFighter());
	}
	
	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		//Register commands
		event.registerServerCommand(new AutoRespawningEnabledCommand());
		event.registerServerCommand(new ClearQValuesCommand());
		event.registerServerCommand(new QRandomSelectionRateCommand());
		event.registerServerCommand(new PVEEnabledCommand());
	}
	
	public static int getSpawnEggUniqueID()
	{
		while (EntityList.getStringFromID(spawnEggUniqueID) != null)
		{
			spawnEggUniqueID++;
		}
		
		return spawnEggUniqueID;
	}
	
	//Registers a new egg type for specified class
	public static void registerEntityEgg(Class <? extends Entity> entity, int backgroundColor, int primaryColor)
	{
		int id = getSpawnEggUniqueID();
		EntityList.IDtoClassMapping.put(id, entity);
		EntityList.entityEggs.put(id, new EntityEggInfo(id, backgroundColor, primaryColor));
	}
}

/**
 * @author Pablo Rodríguez, parmandorc
 * If you use this code, please remember to give credit by linking to the mobs url:
 * http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/2241864-learningmobs-mod
 * 
 * This handler is in charge of saving the qvalues from all LearningMob classes to their respective files in the saves folder, whenever the world is unloaded (game is closed) or saved.
 */
package com.parmandorc.LearningMobs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.parmandorc.LearningMobs.Fighter.EntityLMFighter;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.server.FMLServerHandler;

public class QValuesSavingHandler 
{
	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event)
	{
		saveQValues();
	}
	
	@SubscribeEvent
	public void onWorldSave(WorldEvent.Save event)
	{
		saveQValues();
	}
	
	private static void saveQValues()
	{		
	    File dir = new File("./saves/learningmobsmod/qvaluesfiles");
	    dir.mkdirs();
	    if (!dir.exists())
	    	return;
	    
	    if (EntityLMFighter.Q_values != null)
	    {
			try {
		    	File file = new File(dir.getPath()+"/LMFighter.ser");
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file.getPath());
			    ObjectOutputStream oos = new ObjectOutputStream(fos);
			    oos.writeObject(EntityLMFighter.Q_values);
			    oos.close();
			    fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
}

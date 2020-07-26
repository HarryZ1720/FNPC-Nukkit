package net.FENGberd.Nukkit.FNPC;

import java.util.*;

import cn.nukkit.*;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.item.*;
import cn.nukkit.event.*;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.*;
import cn.nukkit.command.*;
import cn.nukkit.command.data.*;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.*;
import cn.nukkit.network.protocol.*;
import cn.nukkit.command.data.args.*;

import net.FENGberd.Nukkit.FNPC.npc.*;
import net.FENGberd.Nukkit.FNPC.utils.*;
import net.FENGberd.Nukkit.FNPC.tasks.*;
import net.FENGberd.Nukkit.FNPC.commands.*;
import net.FENGberd.Nukkit.FNPC.utils.Utils;

import com.google.gson.*;
import co.aikar.timings.Timings;

@SuppressWarnings("unused")
public class Main extends PluginBase implements Listener
{
	private static Main obj=null;
	private static HashMap<String,RegisteredNPC> registeredNPC=new HashMap<>();

	public static Main getInstance()
	{
		return Main.obj;
	}

	public static HashMap<String,RegisteredNPC> getRegisteredNpcs()
	{
		return registeredNPC;
	}

	public static RegisteredNPC getRegisteredNpcClass(String name)
	{
		RegisteredNPC npc=Main.registeredNPC.getOrDefault(name.toLowerCase(),null);
		if(npc==null)
		{
			return null;
		}
		return npc;
	}

	public static void unregisterNpc(String name)
	{
		Main.registeredNPC.remove(name.toLowerCase());
	}

	public static boolean registerNpc(String name,String description,Class npcClass)
	{
		return Main.registerNpc(name,description,npcClass,false);
	}

	public static boolean registerNpc(String name,String description,Class npcClass,boolean force)
	{
		name=name.toLowerCase();
		if(NPC.class.isAssignableFrom(npcClass) && ! npcClass.isInterface() && (Main.registeredNPC.getOrDefault(name,null)==null || force))
		{
			Main.registeredNPC.put(name,new RegisteredNPC(Utils.cast(npcClass),name,description));
			NPC.reloadUnknownNPC();
			return true;
		}
		return false;
	}

	/**
	 * 静态分割线********************************
	 */
	 
	 NpcCommand npcCommand=null;
	
	@Override
	public void onEnable()
	{
		if(Main.obj==null)
		{
			Main.obj=this;
			Main.registerNpc("normal","普通NPC(无实际功能)",NPC.class,true);
			Main.registerNpc("reply","回复型NPC(使用/fnpc chat)",ReplyNPC.class,true);
			Main.registerNpc("command","指令型NPC(使用/fnpc command)",CommandNPC.class,true);
			Main.registerNpc("teleport","传送型NPC(使用/fnpc teleport或/fnpc transfer)",TeleportNPC.class,true);
		}
		NPC.init();
		Utils.loadLang(this.getServer().getLanguage());
		QuickSystemTask quickSystemTask=new QuickSystemTask(this);
		npcCommand=new NpcCommand();
		this.getServer().getCommandMap().register("FNPC",npcCommand);
		
		this.getServer().getPluginManager().registerEvents(this,this);
		this.getServer().getScheduler().scheduleRepeatingTask(quickSystemTask,1);
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		NPC.playerMove(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDataPacketReceive(DataPacketReceiveEvent event)
	{
		/*if(event.getPacket() instanceof CommandRequestPacket)
		{
			CommandRequestPacket pk=Utils.cast(event.getPacket());
			if(pk.command.startsWith("/fnpc "))
			{
				String commandText=pk.command;
				if(pk.command.split(" ").length > 1)
				{
					CommandParameter[] pars=npcCommand.getCommandParameters(pk.command.split(" ")[1]);
					if(pars!=null)
					{
						for(CommandParameter par:pars)
						{

							if(par.type!=null)
							{
								switch(par.type)
								{
								case TARGET:
									CommandArg rules=new Gson().fromJson(arg,CommandArg.class);
									commandText+=" "+rules.getRules()[0].getValue();
									break;
								case POSITION:
									CommandArgBlockVector bv=new Gson().fromJson(arg,CommandArgBlockVector.class);
									commandText+=" "+bv.getX()+" "+bv.getY()+" " + bv.getZ();
									break;
								case STRING:
								case RAWTEXT:
									String string=new Gson().fromJson(arg, String.class);
									commandText+=" "+string;
									break;
								default:
									commandText+=" "+arg.toString();
									break;
								}
							}
						}
					}
					this.getLogger().warning(commandText);
					PlayerCommandPreprocessEvent playerCommandPreprocessEvent=new PlayerCommandPreprocessEvent(event.getPlayer(),"/"+commandText);
					this.getServer().getPluginManager().callEvent(playerCommandPreprocessEvent);
					if(!playerCommandPreprocessEvent.isCancelled())
					{
						Timings.playerCommandTimer.startTiming();
						this.getServer().dispatchCommand(playerCommandPreprocessEvent.getPlayer(),playerCommandPreprocessEvent.getMessage().substring(1));
						Timings.playerCommandTimer.stopTiming();
					}
				}
				event.setCancelled(true);
			}
		}
		else
		{*/
		NPC.packetReceive(event.getPlayer(),event.getPacket());
		//}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onDataPacketSend(DataPacketSendEvent event)
	{
		if(event.getPacket() instanceof AvailableCommandsPacket)
		{
			AvailableCommandsPacket pk=Utils.cast(event.getPacket());
			Map<String,CommandDataVersions> data=pk.commands;
			npcCommand.processCustomCommandData(data);
			pk.commands=data;
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		NPC.spawnAllTo(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void onEntityLevelChange(EntityLevelChangeEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			NPC.spawnAllTo(Utils.cast(event.getEntity()),event.getTarget());
		}
	}
}


package kookaburra.minecraft.kit.kits.technical;

import java.util.ArrayList;
import java.util.List;

import kookaburra.io.IO;
import kookaburra.io.Json;
import kookaburra.lang.Callback;
import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.Kit;
import kookaburra.minecraft.kit.KitPlayer;
import kookaburra.minecraft.kit.KitPlayerCollection;
import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.worlds.KitsWorld;
import kookaburra.minecraft.kit.worlds.main.MainWorld;
import kookaburra.minecraft.player.McpvpPlayerCollection;
import kookaburra.minecraft.player.YouTubeMode;
import kookaburra.minecraft.util.Util;
import kookaburra.web.API;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public abstract class KitBase implements Kit, Listener
{
	protected String name;
	protected Class<? extends KitBase> type;
	protected List<ItemStack> items;
	protected ItemStack helmet;
	protected ItemStack chestplate;
	protected ItemStack leggings;
	protected ItemStack boots;
	protected boolean fillWithSoup = true;
	protected KitMetadata meta;

	public KitBase()
	{
		Class<? extends KitBase> kitBase = this.getClass();
		name = kitBase.getSimpleName().replace(".class", "");
		type = kitBase;
		
		items = new ArrayList<ItemStack>();
	
		Bukkit.getPluginManager().registerEvents(this, KitPlugin.getInstance());
		
		System.out.println("Calling for " + name);
		
		loadMeta(1);
	}
	
	private void loadMeta(final int tries)
	{
		if(tries > 2)
		{
			return;
		}
	
		if(tries > 1)
		{
			System.out.println("Retrying...");
		}
		
		API.callAsync(KitPlugin.getInstance(), "https://direct.minecraftpvp.com/api/kitpvp/getkit/" + name.toLowerCase(), IO.STRING_DESERIALIZER, new Callback<String>()
		{
			@Override
			public void onFailure(Exception e)
			{
				System.out.println("Failed to load for " + name);
				e.printStackTrace();
				meta = null;
				
				loadMeta(tries + 1);
			}

			@Override
			public void onSuccess(String result)
			{
				System.out.println("Succesfully loaded " + name);
				meta = IO.parse(result, Json.deserializer(KitMetadata.class));
			}
		});
	}
	
	@Override
	public String getName()
	{
		return name;
	}
	
	public ItemStack getSingleSoup()
	{
		return new ItemStack(Material.MUSHROOM_SOUP);
	}
	
	public Class<? extends KitBase> getType()
	{
		return type;
	}
	
	public List<ItemStack> getItems()
	{
		return items;
	}
	
	public ItemStack getBoots()
	{
		return boots;
	}
	
	public ItemStack getLeggings()
	{
		return leggings;
	}
	
	public ItemStack getChestplate()
	{
		return chestplate;
	}
	
	public ItemStack getHelmet()
	{
		return helmet;
	}
	
	@Override
	public String getDescription()
	{
		return meta != null ? meta.getDescription() : "";
	}
	
	public KitMetadata getMetadata()
	{
		return meta;
	}
	
	public boolean hasMetadata()
	{
		return meta != null;
	}

	public ItemStack getKitIcon()
	{
		for (ItemStack item : items)
		{
			return item;
		}
		
		return new ItemStack(Material.DISPENSER);
	}

	public void onEquip(Player player)
	{
		player.getInventory().clear();
		EquipPlayer(player, true, fillWithSoup);
	}
	
	public void EquipPlayer(Player player, boolean reset, boolean fillWithSoup)
	{
		PlayerInventory inventory = player.getInventory();
		
		for(ItemStack item : player.getInventory().getContents())
		{
			if(item == null)
				continue;
			
			if(isKitItem(item))
			{
				player.getInventory().remove(item);
			}
		}
		
		player.updateInventory();
		
		if(reset)
		{
			for(PotionEffect e : player.getActivePotionEffects())
				player.removePotionEffect(e.getType());
			
			player.setMaxHealth(player.getMaxHealth());
			player.setHealth(player.getMaxHealth());
			player.setFoodLevel(20);
			player.setSaturation(1f);
		}
		
		for (ItemStack item : items)
		{
			inventory.addItem(item.clone());
		}
		
		player.updateInventory();
		
		inventory.setArmorContents(new ItemStack[4]);

		if(getBoots() != null)
			inventory.setBoots(getBoots().clone());

		if(getLeggings() != null)
			inventory.setLeggings(getLeggings().clone());

		if(getChestplate() != null)
			inventory.setChestplate(getChestplate().clone());

		if(getHelmet() != null)
			inventory.setHelmet(getHelmet().clone());

		player.updateInventory();
		
		if(fillWithSoup)
		{
			while(inventory.firstEmpty() != -1)
				inventory.addItem(getSingleSoup());
			
			player.updateInventory();
		}
	}
	
	public void sendCooldownMessage(Player player, long millis)
	{
		player.sendMessage(ChatColor.RED + "Still on cooldown for " + ChatColor.GOLD + Util.millisToRoundedTime(millis + 1000) + ChatColor.RED + "." );
	}
	
	protected boolean canUseKit(Player player)
	{			
		if (player == null)
			return false;
		
		if(player.getWorld() != KitPlugin.getMainWorld())
			return false;
		
		if(KitsWorld.getWorld(MainWorld.class).isInsideSpawn(player.getLocation()))
			return false;
		
		KitPlayer kp = KitPlayerCollection.players.get(player);
		
		if(kp == null)
			return false;
		
		if (kp.getCurrentKit() != type)
			return false;
		
		 if (McpvpPlayerCollection.players.get(player).isAdminMode())
			 return false;
		
		if (YouTubeMode.isInYouTubeMode(player))
			return false;
		
		return true;
	}
	
	public boolean isKitItem(ItemStack item)
	{
		ItemStack[] armor = { boots, leggings, chestplate, helmet };
		
		for(ItemStack i : items)
		{
			if(i == null)
				continue;
			
			if(i.getType() == item.getType())
				return true;
		}
		
		for(ItemStack i : armor)
		{
			if(i == null)
				continue;
			
			if(i.getType() == item.getType())
				return true;
		}
		
		return false;
	}
	
	@EventHandler
	public void onTickEvent(TickEvent event)
	{
		onTick(event);
	}
	
	public void onTick(TickEvent event)
	{
		
	}
}

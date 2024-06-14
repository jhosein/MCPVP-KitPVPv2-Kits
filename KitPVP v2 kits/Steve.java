package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.KitPlayer;
import kookaburra.minecraft.kit.KitPlayerCollection;
import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.player.McpvpPlayerCollection;
import kookaburra.minecraft.player.YouTubeMode;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Steve extends KitBase
{
	public Steve()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.IRON_HELMET);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.SKULL_ITEM);
		
		item.setDurability((short) 3);
		
		ItemUtil.addDescription(item, "A kit for beginners!", "Disallows you to drop FULL soup bowls.");
		
		return item;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onDropItem(PlayerDropItemEvent event)
	{
		if(canUseKit(event.getPlayer()) && event.getItemDrop().getItemStack().getType() == Material.MUSHROOM_SOUP)
		{
			event.getPlayer().sendMessage(ChatColor.RED + "Keep it!");
			event.setCancelled(true);
		}
	}
	
	protected boolean canUseKit(Player player)
	{			
		if (player == null)
			return false;
		
		if(player.getWorld() != KitPlugin.getMainWorld())
			return false;
		
		KitPlayer kp = KitPlayerCollection.players.get(player);
		
		if (kp == null)
			return false;
		
		if (kp.getCurrentKit() != type)
			return false;
		
		 if (McpvpPlayerCollection.players.get(player).isAdminMode())
			 return false;
		
		if (YouTubeMode.isInYouTubeMode(player))
			return false;
		
		return true;
	}
	
	@Override
	public void onTick(TickEvent event)
	{
		super.onTick(event);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(canUseKit(player))
				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
		}
	}
}

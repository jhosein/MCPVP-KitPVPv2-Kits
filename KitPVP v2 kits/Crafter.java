package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Crafter extends KitBase
{
	public Crafter()
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
		ItemStack item = new ItemStack(Material.WORKBENCH);
		
		ItemUtil.addDescription(item, "Start with a stack of both mushrooms", "and a stack of bowls.", "", "Allows you to make new soup!");
		
		return item;
	}
	
	@Override
	public void EquipPlayer(Player player, boolean clear, boolean fillWithSoup)
	{
		super.EquipPlayer(player, clear, fillWithSoup);
		
		if(fillWithSoup)
		{
			player.getInventory().setItem(9, new ItemStack(Material.RED_MUSHROOM, 64));
			player.getInventory().setItem(10, new ItemStack(Material.BROWN_MUSHROOM, 64));
			player.getInventory().setItem(11, new ItemStack(Material.BOWL, 64));
		}
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

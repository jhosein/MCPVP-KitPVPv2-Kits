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

public class PVP extends KitBase
{
	public PVP()
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
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
		
		ItemUtil.addDescription(item, "The most standard pvp kit.");
		
		return item;
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

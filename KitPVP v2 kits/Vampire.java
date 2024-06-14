package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class Vampire extends KitBase
{
	public Vampire()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.GOLD_CHESTPLATE);
		
		chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
		chestplate.addEnchantment(Enchantment.DURABILITY, 3);
		
		helmet = new ItemStack(Material.IRON_HELMET);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD, 1)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 1);
			}
		});
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.FERMENTED_SPIDER_EYE);
		
		ItemUtil.addDescription(item, "When you kill another player", "add half a heart to your HP bar.");
		
		return item;
	}
	
	@EventHandler
	public void onKill(PlayerDeathEvent event)
	{
		if(event.getEntity().getKiller() != null)
		{
			Player killer = event.getEntity().getKiller();
			
			if(canUseKit(killer))
			{
				if(killer.getMaxHealth() <= 40)
					killer.setMaxHealth(killer.getMaxHealth() + 1);
				
				killer.setHealth(Math.min(killer.getMaxHealth(), killer.getHealth() + 1));
			}
		}
	}
	
	@EventHandler
	public void onRespawn(final PlayerRespawnEvent event)
	{
		if(canUseKit(event.getPlayer()))
		{
			Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
			{
				@Override
				public void run()
				{
					event.getPlayer().setMaxHealth(20);
				}
			}, 1);
		}
	}
}

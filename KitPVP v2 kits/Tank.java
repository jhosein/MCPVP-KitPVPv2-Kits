package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Tank extends KitBase
{
	public Tank()
	{
		boots = new ItemStack(Material.DIAMOND_BOOTS);
		leggings = new ItemStack(Material.DIAMOND_LEGGINGS);
		chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
		helmet = new ItemStack(Material.TNT);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 1);
			}
		});
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.DIAMOND_CHESTPLATE);
		
		ItemUtil.addDescription(item, "When you kill someone, create a small explosion.");
		
		return item;
	}
	
	@Override
	public void onTick(TickEvent event)
	{
		super.onTick(event);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(canUseKit(player))
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0));
		}
	}
	
	@EventHandler
	public void onKill(PlayerDeathEvent event)
	{
		if(event.getEntity().getKiller() != null)
		{
			Player killer = event.getEntity().getKiller();
			
			if(canUseKit(killer))
			{
				TNTPrimed tnt = (TNTPrimed) killer.getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.PRIMED_TNT);

				tnt.setFuseTicks(0);
				tnt.setYield(3f);
				tnt.setIsIncendiary(true);
			}
		}
	}
}

package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PotionMaster extends KitBase
{
	public PotionMaster()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.IRON_HELMET);
		
		boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new org.bukkit.potion.Potion(PotionType.INSTANT_HEAL, 1).splash().toItemStack(1);
		
		ItemUtil.addDescription(item, "Get Instant Health 2 potions instead of soup", "You will get new potions from soup stations.");
		
		return item;
	}
	
	@Override
	public ItemStack getSingleSoup()
	{
		return new org.bukkit.potion.Potion(PotionType.INSTANT_HEAL, 2).splash().toItemStack(1);
	}
	
	@Override
	public void EquipPlayer(Player player, boolean clear, boolean fillWithSoup)
	{
		super.EquipPlayer(player, clear, fillWithSoup);
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
	
	@EventHandler
	public void onKill(PlayerDeathEvent event)
	{
		if(event.getEntity().getKiller() != null)
		{
			Player killer = event.getEntity().getKiller();
			
			if(canUseKit(killer))
			{
				killer.getInventory().addItem(new org.bukkit.potion.Potion(PotionType.INSTANT_HEAL, 2).splash().toItemStack(5));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsumeSoup(PlayerInteractEvent event)
	{
		if(event.hasItem() && event.getItem().getType() == Material.MUSHROOM_SOUP)
		{
			if(canUseKit(event.getPlayer()))
				event.setCancelled(true);
		}
	}
}

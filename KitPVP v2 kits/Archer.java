package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Archer extends KitBase
{
	public Archer()
	{
		boots = new ItemStack(Material.CHAINMAIL_BOOTS);
		leggings = new ItemStack(Material.CHAINMAIL_LEGGINGS);
		chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
		helmet = new ItemStack(Material.CHAINMAIL_HELMET);
		
		boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		
		ItemStack bow = new ItemStack(Material.BOW);
		bow.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 3);
		bow.addUnsafeEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
		bow.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
		
		items.add(new ItemStack(Material.WOOD_SWORD)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 2);
				addUnsafeEnchantment(Enchantment.DURABILITY, 10);
			}
		});
		items.add(bow);
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.BOW);
		
		ItemUtil.addDescription(item, "Start with a bow with: ", "Power II", "Punch III", "Infinite I");
		
		return item;
	}
	
	@Override
	public void EquipPlayer(Player player, boolean reset, boolean fillWithSoup)
	{
		super.EquipPlayer(player, reset, fillWithSoup);
		player.getInventory().setItem(9, new ItemStack(Material.ARROW, 1));
	}
	
	@Override
	public void onTick(TickEvent event)
	{
		super.onTick(event);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(canUseKit(player))
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0));
		}
	}
}

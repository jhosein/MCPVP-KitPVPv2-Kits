package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class Viper extends KitBase
{
	public Viper()
	{
		boots = new ItemStack(Material.LEATHER_BOOTS);
		leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
		helmet = new ItemStack(Material.LEATHER_HELMET);
		
		ItemStack[] armor = { boots, leggings, chestplate, helmet };
		
		// Make all armor the same.
		for(ItemStack i : armor)
		{
			LeatherArmorMeta meta = (LeatherArmorMeta)i.getItemMeta();
			meta.addEnchant(Enchantment.DURABILITY, 30, true);
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
			meta.setColor(Color.GREEN);
			
			i.setItemMeta(meta);
		}
		
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
		ItemStack item = new ItemStack(new org.bukkit.potion.Potion(PotionType.POISON, 1).toItemStack(1));
		
		ItemUtil.addDescription(item, "Have a 20% chance to poison a person", "with Poision 2 for 5 seconds.");
		
		return item;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onArrowHit(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player)
		{
			Player damager = (Player) event.getDamager();
			Player damaged = (Player) event.getEntity();
			
			if(canUseKit(damager))
			{
				if(Math.random() >= 0.8f)
				{
					damaged.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 5 * 20, 1), true);
				}
			}
		}
	}
}

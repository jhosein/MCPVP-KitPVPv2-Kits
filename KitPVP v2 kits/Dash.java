package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Dash extends SingleCooldownKitBase
{
	public Dash()
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
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
			meta.setColor(Color.RED);
			
			i.setItemMeta(meta);
		}
		
		ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
		sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		
		items.add(sword);
		
		ItemStack sugar = new ItemStack(Material.SUGAR);
		ItemUtil.setName(sugar, "ENERGY!!!");
		ItemUtil.addDescription(sugar, "Right click this item to gain a", ChatColor.BOLD + "BURSTTTTT" + ChatColor.RESET + " of speed!");
		
		items.add(sugar);
	}
	
	@Override
	public ItemStack getSingleSoup()
	{
		ItemStack soup = super.getSingleSoup();
		ItemUtil.setName(soup, "Mountain Stew"); // Mountain Dew, wonder if anybody will pick this up
		
		return soup;
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
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.SUGAR);
		
		ItemUtil.addDescription(item, "Get sugar that gives a big speed boost", "if you right click it (60s cooldown).");
		
		return item;
	}
	
	@Override
	public Object getID(Player player)
	{
		return "SUGAR_COOLDOWN";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 20 * 1000;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsumeSugar(PlayerInteractEvent event)
	{
		if(event.hasItem() && event.getItem().getType() == Material.SUGAR)
		{
			if(canUseKit(event.getPlayer()))
			{
				if(!cooldownGate(event.getPlayer())) // Check cooldown.
					return;
				
				final Player p = event.getPlayer();
				
				p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 100, 0));
				p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 7 * 20, 2), true);
			}
		}
	}
}

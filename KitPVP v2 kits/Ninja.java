package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Ninja extends SingleCooldownKitBase
{
	public Ninja()
	{
		items.add(new ItemStack(Material.DIAMOND_SWORD)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 3);
			}
		});
		items.add(new ItemStack(Material.REDSTONE));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.REDSTONE);
		
		ItemUtil.addDescription(item, "Use redstone dust to go invis for", "5 seconds, or until you damage someone.", "Also gain 1 second of resistance 4 on invis.");
		
		return item;
	}
	
	@Override
	public void onTick(TickEvent event)
	{
		super.onTick(event);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(canUseKit(player))
			{
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
				player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 1));
			}
		}
	}
	
	@Override
	public Object getID(Player player)
	{
		return "NINJA";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 10 * 1000;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsumeSugar(PlayerInteractEvent event)
	{
		if(event.hasItem() && event.getItem().getType() == Material.REDSTONE)
		{
			if(canUseKit(event.getPlayer()))
			{
				if(!cooldownGate(event.getPlayer()))
					return;
				
				final Player p = event.getPlayer();
				
				p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5 * 20, 0));
				p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1 * 20, 3), true);
			}
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Player)
		{
			Player damager = (Player) event.getDamager();
			
			if(canUseKit(damager) && damager.hasPotionEffect(PotionEffectType.INVISIBILITY))
			{
				damager.sendMessage(ChatColor.RED + "You lost your invis.");
				damager.removePotionEffect(PotionEffectType.INVISIBILITY);
				damager.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
			}
		}
	}
}

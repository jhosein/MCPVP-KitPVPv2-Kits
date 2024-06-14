package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.util.Cooldown;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Snowgolem extends SingleCooldownKitBase
{
	public Snowgolem()
	{
		boots = new ItemStack(Material.DIAMOND_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.JACK_O_LANTERN);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD, 1));
		items.add(new ItemStack(Material.DIAMOND_SPADE, 1));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.DIAMOND_SPADE);
		
		ItemUtil.addDescription(item, "Hit a person with your shovel to root them", "in place for 5 seconds (20 second cooldown).");
		
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
	
	public Object getID(Player player, Player rooted)
	{
		return "ROOT" + rooted.getName();
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 25 * 1000;
	}
	
	public void applyCooldown(Player player, Player rooted)
	{
		Cooldown.setCooldown(getValue(player), getID(player, rooted), getCooldownTime(player));
		Cooldown.setCooldown(getValue(player), getID(player), getCooldownTime(player));
	}
	
	public long getCooldown(Player player, Player rooted)
	{
		return Cooldown.getCooldown(getValue(player), getID(player, rooted));
	}

	public boolean isOnCooldown(Player player, Player rooted)
	{
		return (Cooldown.hasCooldown(getValue(player), getID(player, rooted)));
	}
	
	public boolean cooldownGate(Player player, Player rooted)
	{
		if(isOnCooldown(player, rooted))
		{
			Cooldown.setCooldown(getValue(player), getID(player), getCooldown(player, rooted));
			sendCooldownMessage(player, getCooldown(player, rooted));
			return false;
		}
		
		applyCooldown(player, rooted);
		
		return true;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onRoot(final EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player)
		{
			Player damager = (Player) event.getDamager();
			final Player damaged = (Player) event.getEntity();
			
			if(canUseKit(damager) && damager.getItemInHand() != null && damager.getItemInHand().getType() == Material.DIAMOND_SPADE)
			{
				if(!cooldownGate(damager, damaged))
					return;
				
				damaged.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 4 * 20, 250), true);
				damaged.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 4 * 20, 250), true);
			}
		}
	}

	@Override
	public Object getID(Player player)
	{
		return "LAST_ROOT";
	}
}

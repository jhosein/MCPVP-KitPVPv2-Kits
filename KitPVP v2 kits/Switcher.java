package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Switcher extends KitBase
{
	public Switcher()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.LEATHER_HELMET);
		
		LeatherArmorMeta meta = (LeatherArmorMeta)helmet.getItemMeta();
		meta.addEnchant(Enchantment.DURABILITY, 30, true);
		meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
		meta.setColor(Color.PURPLE);
		
		helmet.setItemMeta(meta);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD));
		items.add(new ItemStack(Material.SNOW_BALL, 16));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.SNOW_BALL);
		
		ItemUtil.addDescription(item, "Get 16 snowballs that will switch you", "with the person you hit with them.");
		
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
	
	@EventHandler
	public void onKill(PlayerDeathEvent event)
	{
		if(event.getEntity().getKiller() != null)
		{
			Player killer = event.getEntity().getKiller();
			
			if(canUseKit(killer))
			{
				killer.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 2));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSnowball(EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Projectile && event.getEntity() instanceof Player)
		{
			Projectile p = (Projectile) event.getDamager();
			
			if(!(p.getShooter() instanceof Player))
				return;
			
			Player shooter = (Player) p.getShooter();
			Player hit = (Player) event.getEntity();
			
			if(canUseKit(shooter))
			{
				Location hitPosition = hit.getLocation();
				
				hit.teleport(shooter.getLocation());
				hit.sendMessage(ChatColor.RED + "You got swapped by a switcher!");
				
				shooter.teleport(hitPosition);
			}
		}
	}
}

package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Crossbow extends KitBase
{
	public Crossbow()
	{
		boots = new ItemStack(Material.LEATHER_BOOTS);
		leggings = new ItemStack(Material.LEATHER_LEGGINGS);
		chestplate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
		helmet = new ItemStack(Material.LEATHER_HELMET);
		
		ItemStack[] armor = { boots, leggings, helmet };
		
		// Make all armor the same.
		for(ItemStack i : armor)
		{
			LeatherArmorMeta meta = (LeatherArmorMeta)i.getItemMeta();
			meta.addEnchant(Enchantment.DURABILITY, 30, true);
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
			meta.setColor(Color.RED);
			
			i.setItemMeta(meta);
		}

		chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
		
		ItemStack bow = new ItemStack(Material.BOW);
		bow.addEnchantment(Enchantment.ARROW_KNOCKBACK, 1);
		bow.addEnchantment(Enchantment.ARROW_DAMAGE, 2);
		
		items.add(new ItemStack(Material.STONE_SWORD)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 1);
				addEnchantment(Enchantment.DURABILITY, 3);
			}
		});
		items.add(bow);
	}
	
	@Override
	public void EquipPlayer(Player player, boolean reset, boolean fillWithSoup)
	{
		super.EquipPlayer(player, reset, fillWithSoup);
		
		player.getInventory().setItem(9, new ItemStack(Material.ARROW, 20));
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
		ItemStack item = new ItemStack(Material.ARROW);
		
		ItemUtil.addDescription(item, "Start with a bow and 20 arrow", "get back an arrow when you hit someone.", "If your bow was fully charge, explode on impact.");
		
		return item;
	}
	
	@EventHandler
	public void onEntityDamageByEnity(EntityDamageByEntityEvent event)
	{
		if(event.getEntity() instanceof Player && event.getDamager() instanceof Arrow)
		{
			Arrow a = (Arrow) event.getDamager();

			if(!(a.getShooter() instanceof Player))
				return;

			Player shooter = (Player) a.getShooter();
			Player hit = (Player) event.getEntity();

			if(canUseKit(shooter))
			{
				if(a.isCritical())
				{
					hit.getEyeLocation().getWorld().createExplosion(hit.getEyeLocation(), 0.5f);
				}

				shooter.getInventory().addItem(new ItemStack(Material.ARROW, 2));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onArrowHit(ProjectileHitEvent event)
	{
		Projectile p = (Projectile) event.getEntity();

		if(!(p instanceof Arrow))
			return;

		Arrow a = (Arrow) p;

		if(!(p.getShooter() instanceof Player))
			return;

		Player shooter = (Player) p.getShooter();

		if(canUseKit(shooter))
		{
			System.out.println(a.isCritical());
			
			if(a.isCritical())
			{
				a.getLocation().getWorld().playEffect(a.getLocation(), Effect.EXPLOSION_LARGE, 0);
				
				for(LivingEntity ent : a.getLocation().getWorld().getLivingEntities())
				{
					if(ent == shooter)
						continue;
					
					if(ent.getLocation().distance(a.getLocation()) < 5)
					{
						ent.damage(8, shooter);
					}
				}
			}
		}
	}
}

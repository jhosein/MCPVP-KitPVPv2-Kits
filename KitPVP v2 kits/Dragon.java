package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.util.Cooldown;
import kookaburra.minecraft.util.ItemUtil;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Dragon extends SingleCooldownKitBase
{
	public Dragon()
	{
		boots = new ItemStack(Material.LEATHER_BOOTS);
		
		LeatherArmorMeta meta = (LeatherArmorMeta)boots.getItemMeta();
		meta.addEnchant(Enchantment.DURABILITY, 30, true);
		meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 4, true);
		meta.setColor(Color.RED);
		
		boots.setItemMeta(meta);
		
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		
		helmet = new ItemStack(Material.DRAGON_EGG);
		helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD)
		{
			{
				ItemUtil.setName(this, "Dragon's Fang");
				ItemUtil.addDescription(this, "Deal half a damage more to players on fire.");
			}
		});
		
		items.add(new ItemStack(Material.BLAZE_POWDER)
		{
			{
				ItemUtil.setName(this, "Dragon's Breath");
				ItemUtil.addDescription(this, "Spit fire in a cone in front of you", "putting all enemies on fire for 4 seconds", "also deals 2 hearts of damage.");
			}
		});
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
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.BLAZE_POWDER);
		
		ItemUtil.addDescription(item, "The most standard pvp kit.");
		
		return item;
	}
	
	@Override
	public Object getID(Player player)
	{
		return "SPIT_FIRE";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 15 * 1000;
	}
	
	private final String DEBUFF = "HIT_BY_DRAGON_BREATH";
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onConsumeSugar(PlayerInteractEvent event)
	{
		if(event.hasItem() && event.getItem().getType() == Material.BLAZE_POWDER)
		{
			if(canUseKit(event.getPlayer()))
			{
				if(!cooldownGate(event.getPlayer())) // Check cooldown.
					return;
				
				final Player p = event.getPlayer();
				
				p.getWorld().playSound(p.getEyeLocation(), Sound.ENDERDRAGON_GROWL, 1f, 1f);
				
				final double yaw = p.getLocation().getYaw() + 90;
				
				for(float distance = 0; distance < 6; distance += 0.5f)
				{
					for(int angle = -20; angle <= 20; angle += 5)
					{
						final int finalAngle = angle;
						final float finalDistance = distance;
						final Location loc = p.getLocation().clone().add(0, 1f, 0);
						
						Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
						{
							public void run()
							{
								PacketPlayOutWorldParticles blood = new PacketPlayOutWorldParticles();
								
								double newAngle = yaw + finalAngle;
								double rad = Math.toRadians(newAngle);
								Vector dir = new Vector(Math.cos(rad), p.getLocation().getDirection().getY(), Math.sin(rad));

								Location newLoc = loc.clone();
								
								newLoc.add(dir.multiply(finalDistance));
								
								blood.a = EnumParticle.FLAME;
								blood.b = (float) (newLoc.getX());
								blood.c = (float) (newLoc.getY());
								blood.d = (float) (newLoc.getZ());
								blood.e = 0.1f;
								blood.f = -0.1f;
								blood.g = 0.1f;
								blood.h = 0F;
								blood.i = 5;
								
								for (Player other : p.getWorld().getPlayers())
								{
									if(other.getWorld() != p.getWorld())
										continue;
									
									double distance = other.getEyeLocation().distance(newLoc);
									
									if(other != p && distance < 2d && !Cooldown.hasCooldown(p.getName(), DEBUFF))
									{
										other.setFireTicks(4 * 20);
										other.damage(2, p);
										
										Cooldown.setCooldown(p.getName(), DEBUFF, 5 * 1000);
									}
									
									((CraftPlayer) other).getHandle().playerConnection.sendPacket(blood);
								}
							}
						}, (long) (distance * 1.5d));
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onFlameHit(final EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Player && event.getEntity() instanceof Player)
		{
			Player damager = (Player) event.getDamager();
			final Player damaged = (Player) event.getEntity();
			
			if(canUseKit(damager) && damager.getItemInHand() != null && damager.getItemInHand().getType() == Material.DIAMOND_SWORD)
			{
				if(damaged.getFireTicks() > 0)
				{
					event.setDamage(event.getDamage() + 1);
				}
			}
		}
	}
}

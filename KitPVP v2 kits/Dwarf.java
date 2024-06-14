package kookaburra.minecraft.kit.kits;

import java.util.HashMap;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayer;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayerCollection;
import kookaburra.minecraft.util.ItemUtil;
import kookaburra.minecraft.util.Util;
import kookaburra.minecraft.util.TitleUtil;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Dwarf extends SingleCooldownKitBase
{
	public Dwarf()
	{
		boots = new ItemStack(Material.LEATHER_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.LEATHER_HELMET);
		
		ItemStack[] armor = { boots, helmet };
		
		// Make all armor the same.
		for(ItemStack i : armor)
		{
			LeatherArmorMeta meta = (LeatherArmorMeta) i.getItemMeta();
			meta.addEnchant(Enchantment.DURABILITY, 30, true);
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
			meta.setColor(Color.BLACK);
			
			i.setItemMeta(meta);
		}
		
		ItemStack axe = new ItemStack(Material.DIAMOND_SWORD);
		
		items.add(axe);
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.DIAMOND_HELMET);
		
		ItemUtil.addDescription(item, "Crouch for 5 seconds to charge up a knockback", "then release your crouch and repel everyone away.", "(30 second cooldown)");
		
		return item;
	}
	
	public HashMap<String, Integer> chargeTime = new HashMap<String, Integer>();
	
	public final int SECONDS = 4;
	public final int STEP = 1;
	public final int MAX = (20 * SECONDS);
	public final int LAST_CHARGE = (20 * SECONDS) - 1;
	
	private boolean isDwarfing(Player player)
	{
		return player.isSneaking() && super.canUseKit(player);
	}

	@EventHandler
	public void onTick(TickEvent event)
	{
		super.onTick(event);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(canUseKit(player))
				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
		}
		
		if (event.getTick() % 1 == 0)
		{
			for (Player player : KitPlugin.getMainWorld().getPlayers())
			{
				if (isDwarfing(player))
				{
					if(isOnCooldown(player))
					{
						if(event.getTick() % 20 == 0)
						{
							sendCooldownMessage(player, getCooldown(player));
						}
						
						continue;
					}
					
					if (!chargeTime.containsKey(player.getName()))
					{
						chargeTime.put(player.getName(), STEP);
						continue;
					}

					int time = chargeTime.get(player.getName());
					
					int percent = (int) (((float)(time) / (float)MAX) * 100);
					
					if (time < LAST_CHARGE)
					{
						chargeTime.put(player.getName(), time + STEP);
						time += STEP;
						
						if(percent > 0 && percent % 20 == 0)
						{
							player.sendMessage(ChatColor.GREEN + "Charge for " + percent + "%");
							player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 1f, 1f);
						}
					}
					else if (time == LAST_CHARGE)
					{
						time = MAX;
						chargeTime.put(player.getName(), MAX);
						player.sendMessage(ChatColor.GREEN + "Fully charged!");
					}
					
					
					if(false)
					{
						PacketPlayOutWorldParticles blood = new PacketPlayOutWorldParticles();
						
						Location newLoc = player.getLocation().add(0, 0.1f, 0);
						
						blood.b = (float) (newLoc.getX());
						blood.c = (float) (newLoc.getY());
						blood.d = (float) (newLoc.getZ());
						blood.e = percent / 200f;
						blood.f = percent / 100f;
						blood.g = percent / 200f;
						blood.h = 0F;
						blood.i = percent / 5;
						
						for (Player other : player.getWorld().getPlayers())
						{
							if(other.getWorld() != player.getWorld())
								continue;
							
							blood.a = EnumParticle.BLOCK_DUST;
							
							((CraftPlayer) other).getHandle().playerConnection.sendPacket(blood);
						}
					}
				}
			}
		}
		
		for (Player player : KitPlugin.getMainWorld().getPlayers())
		{
			if (canUseKit(player) && !player.isSneaking())
			{
				if (!chargeTime.containsKey(player.getName()))
				{
					chargeTime.put(player.getName(), 0);
					continue;
				}
				
				int time = chargeTime.get(player.getName());
				
				if (time > 0)
				{
					knockBackExplosion(player, time);
					chargeTime.put(player.getName(), 0);
				}
			}
		}
	}
	
	@Override
	public Object getID(Player player)
	{
		return "DWARF_KNOCKBACK_EXPLOSION";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 30 * 1000;
	}

	private void knockBackExplosion(final Player player, int power)
	{
		if (isOnCooldown(player))
		{
			player.sendMessage(ChatColor.RED + "You are still on cooldown for " + ChatColor.GOLD + Util.millisToRoundedTime(getCooldown(player) + 1000) + ChatColor.RED + ".");
			return;
		}
		
		power = (int) ((power / 8) * 0.75);
		
		player.getWorld().playSound(player.getEyeLocation(), Sound.EXPLODE, 1f, 1f);
		
		for(float i = 0; i < 10; i += 0.4f)
		{
			final float finalI = i;
		
			Runnable run = new Runnable()
			{
				public void run()
				{
//					float distance = finalI;
//					
//					PacketPlayOutWorldParticles blood = new PacketPlayOutWorldParticles();
//					Location newLoc = player.getLocation().add(0, 0.1f, 0);
//					
//					blood.a = "blockdust_1";
//					blood.b = (float) (newLoc.getX());
//					blood.c = (float) (newLoc.getY());
//					blood.d = (float) (newLoc.getZ());
//					blood.e = distance;
//					blood.f = distance;
//					blood.g = distance;
//					blood.h = 0;
//					blood.i = 100;
//					
//					for (Player other : player.getWorld().getPlayers())
//					{
//						if (other.getWorld() != player.getWorld())
//							continue;
//						
//						if(!TitleUtil.canUse((CraftPlayer)other))
//						{
//							blood.a = "blockcrack_1";
//						}
//						else
//						{
//							blood.a = "blockdust_1";
//						}
//						
//						((CraftPlayer) other).getHandle().playerConnection.sendPacket(blood);
//					}
				}
			};
			
			Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), run, (long) i);
		}
		
		for (Player other : player.getWorld().getPlayers())
		{
			if (other == player)
				continue;
			
			if(other.getWorld() != player.getWorld())
				continue;
			
			if (player.getLocation().distance(other.getLocation()) < 4)
			{
				applyCooldown(player);
				
				Vector velocity = other.getEyeLocation().subtract(player.getLocation()).multiply((power)).toVector();
				
				other.damage(0.01d, player); // Set killer to player.
				
				NoHaxPlayer np = NoHaxPlayerCollection.players.get(other);
				
				if (velocity.getY() > 2)
					velocity.setY(2);
				if (velocity.getX() > 3)
					velocity.setX(3);
				if (velocity.getZ() > 3)
					velocity.setZ(3);
				if (velocity.getX() < -3)
					velocity.setX(-3);
				if (velocity.getZ() < -3)
					velocity.setZ(-3);
				
				np.invalidateAll((int) (20 * velocity.distance(new Vector(0, 0, 0))));
				
				other.setVelocity(velocity);
			}
		}
	}
}

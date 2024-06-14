package kookaburra.minecraft.kit.kits;

import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;

import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayer;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayerCollection;
import kookaburra.minecraft.nohax.plugin.detecting.movement.AntiKnockbackDetection;
import kookaburra.minecraft.nohax.plugin.detecting.movement.SpeedDetection;
import kookaburra.minecraft.nohax.plugin.detecting.movement.VelocityDetection;
import kookaburra.minecraft.util.ItemUtil;
import kookaburra.util.ReflectionUtil;
import net.minecraft.server.v1_8_R2.AxisAlignedBB;
import net.minecraft.server.v1_8_R2.BlockPosition;
import net.minecraft.server.v1_8_R2.Entity;
import net.minecraft.server.v1_8_R2.EntityArrow;
import net.minecraft.server.v1_8_R2.MovingObjectPosition;
import net.minecraft.server.v1_8_R2.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R2.Vec3D;
import net.minecraft.server.v1_8_R2.WorldServer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Glider extends KitBase
{
	public Glider()
	{
		boots = new ItemStack(Material.LEATHER_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.LEATHER_HELMET);
		
		ItemStack[] armor = { boots, helmet };
		
		// Make all armor the same.
		for(ItemStack i : armor)
		{
			LeatherArmorMeta meta = (LeatherArmorMeta)i.getItemMeta();
			meta.addEnchant(Enchantment.DURABILITY, 30, true);
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
			meta.setColor(Color.ORANGE);
			
			i.setItemMeta(meta);
		}
		
		items.add(new ItemStack(Material.DIAMOND_SWORD, 1)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 1);
			}
		});
		
		ItemStack bow = new ItemStack(Material.BOW);
		ItemUtil.setName(bow, "Glider Bow");
		ItemUtil.addDescription(bow, "Right click to shoot an arrow", "Then, you shall become the arrow.");
		items.add(bow);
		items.add(new ItemStack(Material.ARROW, 5));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.ENDER_PEARL);
		
		ItemUtil.addDescription(item, "Right click your bow to shoot an arrow", "Then, you shall become the arrow.");
		
		return item;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onGlide(final EntityShootBowEvent event)
	{
		Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				if(event.getEntity() instanceof Player)
				{
					final Player player = (Player) event.getEntity();
					
					if(canUseKit(player))
					{
						event.getProjectile().remove();
						
						WorldServer cw = ((CraftWorld)player.getWorld()).getHandle();
						
						EntityArrow e = new EntityArrow(cw, ((CraftPlayer)player).getHandle(), 1f)
						{
							@Override
							public void t_()
							{
								Field field = ReflectionUtil.getField("as", EntityArrow.class);
								
								/**
								 * Look at the part of the method that updates what entities to hit.
								 * Find the incrementing value that stops arrows from hitting the shooter.
								 */
								ReflectionUtil.setField(field, this, (int)0);
								
								super.t_();
							}
						};
						
						Field field = ReflectionUtil.getField("boundingBox", Entity.class);
						
						ReflectionUtil.setField(field, e, new AxisAlignedBB(0, 0, 0, 0, 0, 0)
						{
							@Override
							public AxisAlignedBB a(double name, double name2, double name3)
							{
								return this;
							}
							
							@Override
							public AxisAlignedBB grow(double name, double name2, double name3)
							{
								return this;
							}
						}); // Replace bounding box, disallowing you to change it.
						
						cw.addEntity(e);
						
						Projectile old = (Projectile) event.getProjectile();
						final Arrow arrow = (Arrow) e.getBukkitEntity();
						
						arrow.teleport(old.getLocation());
						arrow.setVelocity(old.getVelocity());
						arrow.setCritical(true);
						
						final NoHaxPlayer np = NoHaxPlayerCollection.players.get(player);
						
						if(np != null)
						{
							np.invalidateAll(5 * 20);
						}

						Timer timer = new Timer();
						
						timer.schedule(new TimerTask()
						{
							long timer = 0;
							
							@Override
							public void run()
							{
								timer += 10;
								
								if(timer % 1000 == 0)
								{
									new BukkitRunnable()
									{
										@Override
										public void run()
										{
											if(np != null)
												np.invalidateAll(5 * 20);
										}
									}.runTask(KitPlugin.getInstance());
								}
							
								player.setFallDistance(-5);
								
								CraftPlayer cp = ((CraftPlayer)player);
								
								boolean cancel = false;
								
								if(arrow.isOnGround() || arrow.isDead())
									cancel = true;
								
								if(player.isSneaking())
									cancel = true;
								
								cp.getHandle().locX = arrow.getLocation().getX();
								cp.getHandle().locY = arrow.getLocation().getY();
								cp.getHandle().locZ = arrow.getLocation().getZ();
								
								PacketPlayOutEntityTeleport tp = new PacketPlayOutEntityTeleport(cp.getHandle());
								
								tp.e = (byte) ((int) ((cp.getHandle().yaw) * 256.0F / 360.0F));
								tp.f = (byte) ((int) ((cp.getHandle().pitch) * 256.0F / 360.0F));
								
								cp.getHandle().playerConnection.sendPacket(tp);
								
								if(cancel)
								{
									float distance = (float) -(Math.max(0, player.getLocation().getY() - player.getWorld().getHighestBlockYAt(player.getLocation())));

									player.setFallDistance(distance / 2f);
									cancel();
								}
							}
						}, 10, 10); // Every 10 ms.
					}
				}
			}
		}, 1);
	}
}

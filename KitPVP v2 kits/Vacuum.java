package kookaburra.minecraft.kit.kits;

import java.util.ArrayList;
import java.util.Iterator;

import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayer;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayerCollection;
import kookaburra.minecraft.player.McpvpPlayerCollection;
import kookaburra.minecraft.util.ItemUtil;
import net.minecraft.server.v1_8_R2.EnumParticle;
import net.minecraft.server.v1_8_R2.PacketPlayOutWorldParticles;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class Vacuum extends SingleCooldownKitBase
{
	public Vacuum()
	{
		boots = new ItemStack(Material.GOLD_BOOTS)
		{
			{
				addUnsafeEnchantment(Enchantment.DURABILITY, 10);
			}
		};
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
		helmet = new ItemStack(Material.ENDER_PORTAL_FRAME);
		
		ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
		sword.addEnchantment(Enchantment.DAMAGE_ALL, 1);
		
		items.add(sword);
		
		ItemStack sugar = new ItemStack(Material.GOLD_PLATE);
		ItemUtil.setName(sugar, "Vacuum!!!");
		ItemUtil.addDescription(sugar, "Right click this item to suck", "all players in a 10 block radius towards you", "for 5 seconds (30s cooldown)");
		
		items.add(sugar);
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.GOLD_PLATE);
		
		ItemUtil.addDescription(item, "Vacuum the battlefield by pulling players", "towards you in a 10 block radius for 5 seconds", "(30s cooldown)");
		
		return item;
	}
	
	@Override
	public Object getID(Player player)
	{
		return "VACUUM";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 30 * 1000;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onExplode(final PlayerInteractEvent event)
	{
		final boolean left = event.getAction() == Action.LEFT_CLICK_BLOCK;
		final boolean right = event.getAction() == Action.RIGHT_CLICK_BLOCK;
		
		if(!left && !right)
			return;
		
		if(event.hasBlock() && event.hasItem() && event.getItem().getType() == Material.GOLD_PLATE)
		{
			if(canUseKit(event.getPlayer()))
			{
				event.setCancelled(true);

				if(!cooldownGate(event.getPlayer()))
					return;

				{
					final Player p = event.getPlayer();

					final Block block = event.getClickedBlock().getRelative(event.getBlockFace());

					p.getWorld().playSound(block.getLocation(), Sound.PORTAL_TRIGGER, 2f, 1f);
					
					final int vacuumSize = 5;

					final Location pullLocation = event.getClickedBlock().getLocation().add(0.5f, 0.5f, 0.5f);
					
					final ArrayList<String> affected = new ArrayList<String>();
					
					class ParticleData
					{
						boolean remove = false;
						
						float yaw;
						float pitch;
						float distance;
						Location loc;
						
						public void set(float yaw, float pitch, float distance)
						{
							this.yaw = yaw;
							this.pitch = pitch;
							this.distance = distance;
							
							Location location = p.getLocation().clone();
							location.setYaw(yaw);
							location.setPitch(pitch);
							
							loc = pullLocation.clone().add(location.getDirection().multiply(distance));
						}
						
						public void tick(float change)
						{
							float newDistance = distance + change;
							
							if(newDistance < 0 || newDistance > vacuumSize)
							{
								remove = true;
								return;
							}
							
							distance = newDistance;
							
							set(yaw, pitch, distance);
						}
					}
					
					final ArrayList<ParticleData> particles = new ArrayList<ParticleData>();

					int timeActive = 5 * 20;
					
					final float distanceChange = (left ? -1 : 1) * 0.2f * 4;
					
					Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
					{
						@Override
						public void run()
						{
							for(Player player : Bukkit.getOnlinePlayers())
							{
								player.sendBlockChange(block.getLocation(), Material.GOLD_PLATE, (byte) 0);
							}
						}
					}, 5);
					
					Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
					{
						@Override
						public void run()
						{
							for(Player player : Bukkit.getOnlinePlayers())
							{
								player.sendBlockChange(block.getLocation(), block.getType(), block.getData());
							}
						}
					}, timeActive);
					
					
					for(int i = 0; i < timeActive; i++)
					{
						final int finalI = i;
						
						Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
						{
							@Override
							public void run()
							{
								if(finalI % 40 == 0)
								{
									for(float yaw = 0; yaw < 360; yaw += 30)
									{
										for(float pitch = 180; pitch < 360; pitch += 30)
										{
											ParticleData data = new ParticleData();

											data.set(yaw, pitch, left ? vacuumSize : 0);

											particles.add(data);
										}
									}
								}
								
								if(finalI % 4 == 0)
								{
									Iterator<ParticleData> it = particles.iterator();
									
									while(it.hasNext())
									{
										ParticleData data = it.next();

										PacketPlayOutWorldParticles blood = new PacketPlayOutWorldParticles();

										Location newLoc = data.loc;
										
										data.tick(distanceChange);

										if(data.remove)
										{
											it.remove();
											continue;
										}

										blood.a = EnumParticle.SNOW_SHOVEL;
										blood.b = (float) (newLoc.getX());
										blood.c = (float) (newLoc.getY());
										blood.d = (float) (newLoc.getZ());
										blood.e = 0f;
										blood.f = 0f;
										blood.g = 0f;
										blood.h = 0;
										blood.i = 1;

										for (Player other : p.getWorld().getPlayers())
										{
											if (other.getWorld() != p.getWorld())
												continue;

											((CraftPlayer) other).getHandle().playerConnection.sendPacket(blood);
										}
									}
								}
								
								for(Entity e : event.getClickedBlock().getWorld().getEntities())
								{
									if(e == p)
										continue;

									if(e.getWorld() != p.getWorld()) // For some reason... this still happens... (Bukkit...)
									{
										continue;
									}

									double distance = e.getLocation().distance(block.getLocation());

									if(distance > 5)
										continue;
									
									if(e instanceof Player)
									{
										Player player = (Player) e;
										
										if(McpvpPlayerCollection.players.get(player).isAdminMode())
											continue;

										if(!affected.contains(player.getName()))
										{
											player.sendMessage(ChatColor.RED + "You're getting " + (left ? "pulled in" : "pushed away") + " by a Vacuum!");
											affected.add(player.getName());
										}
										
										NoHaxPlayer np = NoHaxPlayerCollection.players.get(player);
										
										np.invalidateAll(5);
									}

									Vector pull = pullLocation.clone().toVector().subtract(e.getLocation().toVector()).multiply(1/distance);
									Vector push = e.getLocation().toVector().subtract(pullLocation.clone().toVector()).multiply(1/distance);

									Vector vector = left ? pull : push;

									e.setVelocity(vector.multiply(0.2f));
								}
							}
						}, i);
					}
					
					Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
					{
						@Override
						public void run()
						{
							p.updateInventory();
						}
					}, 1);
				}
			}
		}
	}
}

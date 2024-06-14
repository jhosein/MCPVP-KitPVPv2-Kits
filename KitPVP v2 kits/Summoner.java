package kookaburra.minecraft.kit.kits;

import java.util.ArrayList;
import java.util.Map.Entry;

import kookaburra.lang.CIString;
import kookaburra.meta.Factory;
import kookaburra.meta.PlayerMeta;
import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Summoner extends SingleCooldownKitBase
{
	public PlayerMeta<ArrayList<Integer>> spawnedCreepers = new PlayerMeta<ArrayList<Integer>>(new Factory<ArrayList<Integer>>()
	{
		@Override
		public ArrayList<Integer> create()
		{
			return new ArrayList<Integer>();
		}
	});
	
	public Summoner()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.LEATHER_HELMET);
		
		LeatherArmorMeta meta = (LeatherArmorMeta)helmet.getItemMeta();
		meta.addEnchant(Enchantment.DURABILITY, 30, true);
		meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
		meta.setColor(Color.RED);
		
		helmet.setItemMeta(meta);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD));
		
		items.add(new ItemStack(Material.GOLD_HOE, 1)
		{
			{
				addUnsafeEnchantment(Enchantment.DURABILITY, 100);
			}
		});
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.GOLD_HOE);
		
		ItemUtil.addDescription(item, "Spawn creeper bombs from your summoner", "staff, they will spawn a creeper on impact.");
		
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
	
	@Override
	public Object getID(Player player)
	{
		return "SPAWN_BALL";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 20 * 1000;
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void reRouteCreeperDamage(EntityDamageByEntityEvent event)
	{
		for(Entry<CIString, ArrayList<Integer>> s : spawnedCreepers.getMap().entrySet())
		{
			if(s.getValue().contains(event.getDamager().getEntityId()))
			{
				// It's an owned creeper.
				
				double chargedCreeperDamage = 65;
				double normalCreeperDamage = 50; // Actually 22, but upped a bit to make more damage.
				double ratio = normalCreeperDamage / chargedCreeperDamage;
				
				event.setDamage(event.getDamage() * ratio); // Translate charged creeper damage to normal creeper damage

				Player player = Bukkit.getPlayerExact(s.getKey().toString());
				
				if(player != null)
				{
					if(player == event.getEntity())
						return; // Allow the damage
					
					event.setCancelled(true);
					((LivingEntity)event.getEntity()).damage(event.getDamage(), player);
					return;
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onSpawnBomb(PlayerInteractEvent event)
	{
		if(event.hasItem() && event.getItem().getType().equals(Material.GOLD_HOE))
		{
			if(canUseKit(event.getPlayer()))
			{
				event.setCancelled(true);
				
				if(!cooldownGate(event.getPlayer()))
					return;
				
				Snowball b = event.getPlayer().launchProjectile(Snowball.class);
				b.setVelocity(b.getVelocity().multiply(2f));
			}
		}
	}
	
	@EventHandler
	public void onLand(ProjectileHitEvent event)
	{
		if(event.getEntity().getShooter() instanceof Player)
		{
			Player p = (Player) event.getEntity().getShooter();
			
			if(canUseKit(p))
			{
				final org.bukkit.entity.Creeper creeper = (Creeper) event.getEntity().getWorld().spawnEntity(event.getEntity().getLocation(), EntityType.CREEPER);
				
				spawnedCreepers.get(p).add(creeper.getEntityId());
				
				creeper.setHealth(1);
				creeper.setPowered(true);
				creeper.setCanPickupItems(false);
				creeper.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10000, 2));
				
				Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
				{
					@Override
					public void run()
					{
						if(!creeper.isDead())
							creeper.remove();
					}
				}, 30 * 20);
			}
		}
	}
}

package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Zeus extends SingleCooldownKitBase
{
	public Zeus()
	{
		boots = new ItemStack(Material.GOLD_BOOTS);
		leggings = new ItemStack(Material.GOLD_LEGGINGS);
		chestplate = new ItemStack(Material.GOLD_CHESTPLATE);
		helmet = new ItemStack(Material.GOLD_HELMET);
		
		ItemStack[] armor = { boots, leggings, chestplate, helmet };
		
		// Make all armor the same.
		for(ItemStack i : armor)
		{
			ItemMeta meta = i.getItemMeta();
			meta.addEnchant(Enchantment.DURABILITY, 10, true);
			meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
			
			i.setItemMeta(meta);
		}
		
		ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
		
		items.add(sword);
		
		ItemStack rod = new ItemStack(Material.BLAZE_ROD);
		ItemUtil.setName(rod, "Lightning Rod");
		
		items.add(rod);
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.BLAZE_ROD);
		
		ItemUtil.addDescription(item, "Place a lightning box on the ground", "that detonates and catches lightning for a few seconds.", "This can hurt you, so be safe.");
		
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
		return "THUNDER_BOX";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 25 * 1000;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onUseBlazeRod(final PlayerInteractEvent event)
	{
		if(event.hasItem() && event.hasBlock() && event.getItem().getType() == Material.BLAZE_ROD)
		{
			if(canUseKit(event.getPlayer()))
			{
				event.setCancelled(true);
		
				if(!cooldownGate(event.getPlayer()))
					return;
				
				Block highestBlock = event.getClickedBlock().getRelative(event.getBlockFace());
				
				while(!highestBlock.isEmpty())
				{
					highestBlock = highestBlock.getRelative(BlockFace.UP);
				}
				
				final Block newBlock = highestBlock;
				
				newBlock.setType(Material.WOOL);
				
				newBlock.getWorld().playSound(newBlock.getLocation(), Sound.GHAST_SCREAM, 1f, 1f);
				
				int totalTime = 20;
				int time = (int) (1.5f * totalTime);
				
				for(int i = 0; i < time; i += 2)
				{
					Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
					{
						@SuppressWarnings("deprecation")
						@Override
						public void run()
						{
							newBlock.setData(DyeColor.values()[(int) (Math.random() * DyeColor.values().length)].getWoolData());
						}
					}, i);
				}
				
				for(float i = 0; i < totalTime; i += (totalTime / 5f))
				{
					Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
					{
						@SuppressWarnings("deprecation")
						@Override
						public void run()
						{
							Location newLocation = newBlock.getLocation().add((Math.random() * 4f) - 2f, 0, (Math.random() * 4f) - 2f);
							
							newLocation.setY(newLocation.getWorld().getHighestBlockYAt(newLocation));
							
							newBlock.getWorld().strikeLightningEffect(newLocation);
							
							double distance = 4;
							
							for(Entity e : event.getPlayer().getWorld().getEntities())
							{
								if(e instanceof Player)
								{
									Player enemy = (Player)e;
									
									if(event.getPlayer() == enemy)
										continue;
									
									if(enemy.getWorld() != newBlock.getWorld())
										continue;
									
									if(enemy.getLocation().distance(newBlock.getLocation()) < distance)
										enemy.damage(18d, event.getPlayer());
								}
							}
						}
					}, (long) (time) + Math.round(i));
				}
				
				Bukkit.getScheduler().runTaskLater(KitPlugin.getInstance(), new Runnable()
				{
					@SuppressWarnings("deprecation")
					@Override
					public void run()
					{
						if(!newBlock.isEmpty())
						{
							newBlock.setType(Material.AIR);
						}
					}
				}, (long) (totalTime + 5));
			}
		}
	}
}

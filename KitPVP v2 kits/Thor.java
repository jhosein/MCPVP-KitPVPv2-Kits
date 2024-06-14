package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Thor extends SingleCooldownKitBase
{
	public Thor()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.NETHERRACK);
		
		items.add(new ItemStack(Material.IRON_SWORD)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 2);
			}
		});
		
		items.add(new ItemStack(Material.WOOD_AXE));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.WOOD_AXE);
		
		ItemUtil.addDescription(item, "Call down lightning with your axe!");
		
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
		return "LIGHTNING_STRIKE";
	}

	@Override
	public long getCooldownTime(Player player)
	{
		return 6 * 1000;
	}
	
	@EventHandler
	public void onTakeDamage(EntityDamageEvent event)
	{
		if(event.getEntity() instanceof Player)
		{
			Player p = (Player) event.getEntity();
			
			if(event.getCause() != DamageCause.LIGHTNING)
				return;
			
			if(!canUseKit(p))
				return;
			
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onLightningStrike(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (!canUseKit(player))
			return;	
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasItem() && player.getItemInHand().getType() == Material.WOOD_AXE)
		{
			{
				if(!cooldownGate(player))
					return;
				
				event.getClickedBlock().getWorld().strikeLightningEffect(event.getClickedBlock().getLocation());
				
				double distance = 3;
				
				for(Entity e : event.getPlayer().getWorld().getEntities())
				{
					if(e instanceof Player)
					{
						Player enemy = (Player)e;
						
						if(event.getPlayer() == enemy)
							continue;
						
						if(enemy.getWorld() != event.getPlayer().getWorld())
							continue;
						
						if(enemy.getLocation().distance(event.getClickedBlock().getLocation()) < distance)
							enemy.damage(2.5d * 9d, event.getPlayer());
					}
				}
			}
		}
	}
}

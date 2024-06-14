
package kookaburra.minecraft.kit.kits.technical;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.KitPlayer;
import kookaburra.minecraft.kit.KitPlayerCollection;
import kookaburra.minecraft.util.Cooldown;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * KitBase that has a cooldown for each item.
 * Each item needs to have a name set with @ItemUtil
 * If no item name is set, the cooldown will be PER MATERIAL.
 * Will ping when the cooldown is over (when the item is in hand).
 * Fills the exp bar with the time left for the cooldown (when item is in hand).
 */
public abstract class PerItemCooldownKitBase extends KitBase
{
	public String getName(ItemStack item)
	{
		if(item == null)
		{
			return "";
		}
		
		String readName = ItemUtil.getName(item);
		
		if(readName == null || readName.isEmpty())
		{
			readName = item.getType().name();
		}
		
		return readName;
	}
	
	public Object getID(Player player, ItemStack item)
	{
		return getName(item);
	};
	
	public Object getValue(Player player, ItemStack item)
	{
		return player.getName();
	};
	
	public void applyCooldown(Player player, ItemStack item, int cooldown)
	{
		ItemUtil.setId(item, cooldown);
		Cooldown.setCooldown(getValue(player, item), getID(player, item), cooldown);
	}
	
	public long getCooldown(Player player, ItemStack item)
	{
		return Cooldown.getCooldown(getValue(player, item), getID(player, item));
	}

	public boolean isOnCooldown(Player player, ItemStack item)
	{
		return (Cooldown.hasCooldown(getValue(player, item), getID(player, item)));
	}
	
	/**
	 * If player has no cooldown, will return true.
	 * If the player has cooldown, it will display their cooldown to them.
	 * @param player
	 */
	public boolean cooldownGate(Player player, ItemStack item, int cooldown)
	{
		if(isOnCooldown(player, item))
		{
			sendCooldownMessage(player, cooldown);
			return false;
		}
		
		applyCooldown(player, item, cooldown);
		
		return true;
	}
	
	@Override
	public void onTick(TickEvent event)
	{
		super.onTick(event);
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			KitPlayer kp = KitPlayerCollection.players.get(player);
			
			if(kp == null)
				continue;
			
			if(kp.getCurrentKit() == null)
				continue;
			
			if(kp.getCurrentKit() == getType())
			{
				ItemStack inHand = player.getItemInHand();
				
				if(inHand == null)
				{
					player.setExp(0);
					continue;
				}
				
				long total = ItemUtil.getId(inHand);
				long value = getCooldown(player, inHand);
				
				double percentage = 1 - ((double)value / (double)total);
				
				percentage = Math.min(Math.max(percentage, 0), 1);
				
				if(player.getExp() < 1f && percentage >= 1f)
				{
					player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 1f, 1.4f);
				}
				
				player.setExp((float) percentage);
			}
		}
	}
}


package kookaburra.minecraft.kit.kits.technical;

import kookaburra.minecraft.event.TickEvent;
import kookaburra.minecraft.kit.KitPlayer;
import kookaburra.minecraft.kit.KitPlayerCollection;
import kookaburra.minecraft.util.Cooldown;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Kitbase that has a single cooldown tied to it.
 * Will ping when the cooldown is over.
 * Fills the exp bar with the time left for the cooldown.
 */
public abstract class SingleCooldownKitBase extends KitBase
{
	public abstract Object getID(Player player);
	
	public Object getValue(Player player)
	{
		return player.getName();
	};
	
	public abstract long getCooldownTime(Player player);
	
	public void applyCooldown(Player player)
	{
		Cooldown.setCooldown(getValue(player), getID(player), getCooldownTime(player));
	}
	
	public long getCooldown(Player player)
	{
		return Cooldown.getCooldown(getValue(player), getID(player));
	}

	public boolean isOnCooldown(Player player)
	{
		return (Cooldown.hasCooldown(getValue(player), getID(player)));
	}
	
	/**
	 * If player has no cooldown, will return true.
	 * If the player has cooldown, it will display their cooldown to them.
	 * @param player
	 */
	public boolean cooldownGate(Player player)
	{
		if(isOnCooldown(player))
		{
			sendCooldownMessage(player, getCooldown(player));
			return false;
		}
		
		applyCooldown(player);
		
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
				long total = getCooldownTime(player);
				long value = getCooldown(player);
				
				double percentage = 1 - ((double)value / (double)total);
				
				percentage = Math.min(Math.max(percentage, 0), 1);
				
				if(player.getExp() < 1f && percentage >= 1f)
				{
					player.playSound(player.getEyeLocation(), Sound.NOTE_PLING, 1f, 1.4f);
				}
				
				player.setLevel((int) ((value + ((1 - percentage) * 1000)) / 1000));
				player.setExp((float) percentage);
			}
		}
	}
}

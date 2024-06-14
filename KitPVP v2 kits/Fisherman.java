package kookaburra.minecraft.kit.kits;

import kookaburra.minecraft.kit.kits.technical.KitBase;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayer;
import kookaburra.minecraft.nohax.plugin.NoHaxPlayerCollection;
import kookaburra.minecraft.util.ItemUtil;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class Fisherman extends KitBase
{
	public Fisherman()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.GOLD_HELMET);
		helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		helmet.addEnchantment(Enchantment.DURABILITY, 3);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 1);
			}
		});
		
		items.add(new ItemStack(Material.FISHING_ROD));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(Material.FISHING_ROD);
		
		ItemUtil.addDescription(item, "Hook a player with your fishing rod", "and reel them in!");
		
		return item;
	}
	
	@EventHandler
	public void onPlayerFish(PlayerFishEvent event)
	{		
		Player player = event.getPlayer();
		
		if (!canUseKit(player))
			return;		
		
		// event.getCaught() is null if there's nothing caught
		if (event.getCaught() != null)
		{
			if (event.getCaught() instanceof LivingEntity)
			{
				float yaw = player.getLocation().getYaw() + 180;
				Location tp = player.getLocation();
				tp.setYaw(yaw);
				
				event.getCaught().teleport(tp);
				
				if(event.getCaught() instanceof Player)
				{
					NoHaxPlayer np = NoHaxPlayerCollection.players.get((Player)event.getCaught());

					np.invalidateAll(20);
				}
			}
		}
	}
}

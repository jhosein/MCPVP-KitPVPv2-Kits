package kookaburra.minecraft.kit.kits;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.kit.KitUtil;
import kookaburra.minecraft.kit.kits.technical.SingleCooldownKitBase;
import kookaburra.minecraft.player.PlayerUtil;
import kookaburra.minecraft.util.ItemUtil;
import net.minecraft.server.mcpvp.PacketSendEvent;
import net.minecraft.server.v1_8_R2.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R2.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class Witch extends SingleCooldownKitBase
{
	public Witch()
	{
		boots = new ItemStack(Material.IRON_BOOTS);
		leggings = new ItemStack(Material.IRON_LEGGINGS);
		chestplate = new ItemStack(Material.IRON_CHESTPLATE);
		helmet = new ItemStack(Material.LEATHER_HELMET);
		
		LeatherArmorMeta meta = (LeatherArmorMeta)helmet.getItemMeta();
		meta.addEnchant(Enchantment.DURABILITY, 30, true);
		meta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 3, true);
		meta.setColor(Color.BLACK);
		
		helmet.setItemMeta(meta);
		
		items.add(new ItemStack(Material.DIAMOND_SWORD, 1)
		{
			{
				addEnchantment(Enchantment.DAMAGE_ALL, 1);
			}
		});
		items.add(ItemUtil.setName(new ItemStack(ITEM_MATERIAL, 1), ITEM_NAME));
	}
	
	@Override
	public ItemStack getKitIcon()
	{
		ItemStack item = new ItemStack(ItemUtil.setName(new ItemStack(Material.STICK, 1), "Magic Wand"));
		
		item.addUnsafeEnchantment(Enchantment.LUCK, 69);
		
		ItemUtil.addDescription(item, "Use your hoe to spray", "instant damage splash potions in a cone!");
		
		return item;
	}
	
	@Override
	public Object getID(Player player)
	{
		return "WITCH";
	}
	
	private HashMap<String, Long> cooldowns = new HashMap<String, Long>();

	@Override
	public long getCooldownTime(Player player)
	{
		if(!cooldowns.containsKey(player.getName()))
			return 0;
		
		return cooldowns.get(player.getName());
	}
	
	public boolean cooldownGate(Player player, long cooldown)
	{
		if(isOnCooldown(player))
		{
			sendCooldownMessage(player, getCooldown(player));
			return false;
		}
		
		cooldowns.put(player.getName(), cooldown);
		applyCooldown(player);
		
		return true;
	}
	
	private final int DURATION = 5 * 20;
	
	private final static String ITEM_NAME = "Magic Wand";
	private final static Material ITEM_MATERIAL = Material.STICK;
	
	private Set<String> pigs = new HashSet<String>();
	
	@EventHandler
	private void cancelPlacingWool(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (!super.canUseKit(player))
			return;		
		
		ItemStack item = player.getItemInHand();
		
		if (item == null)
			return;
		
		if (item.getType() != ITEM_MATERIAL)
			return;
		
		String itemName = ItemUtil.getName(item);
		
		if (!itemName.equals(ITEM_NAME))
			return;
		
		if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			event.setCancelled(true);
			return;
		}
		
		if(!cooldownGate(player, 20 * 1000))
			return;
		
		LargeFireball ball = event.getPlayer().launchProjectile(LargeFireball.class);
		ball.setYield(ball.getYield() * 3);
	}
	
	@EventHandler
	private void turnToPig(EntityDamageByEntityEvent event)
	{	
		if(!(event.getDamager() instanceof Player))
			return;
		
		Player player = (Player) event.getDamager();
		
		if (!super.canUseKit(player))
			return;		
		
		ItemStack item = player.getItemInHand();
		
		if (item == null)
			return;
		
		if (item.getType() != ITEM_MATERIAL)
			return;
		
		String itemName = ItemUtil.getName(item);
		
		if (!itemName.equals(ITEM_NAME))
			return;
		
		Entity otherEntity = event.getEntity();
		
		if (!(otherEntity instanceof LivingEntity))
			return;
		
		if(!cooldownGate(player, 60 * 1000))
			return;
		
		LivingEntity livingEntity = (LivingEntity) otherEntity;					
		
		if (livingEntity.getType() == EntityType.PLAYER)
		{
			Player target = (Player) livingEntity;
			pigPlayer(target);
			player.sendMessage(ChatColor.GREEN + "You have turned " + target.getName() + " into a pig.");			
		}
		else
		{
			pigMob(livingEntity);
			player.sendMessage(ChatColor.GREEN + "You have turned " + livingEntity.getType() + " into a pig.");			
		}
			
	}
	
	private void pigMob(LivingEntity livingEntity)
	{
		Location location = livingEntity.getLocation();
		livingEntity.remove();
		
		location.getWorld().spawnEntity(location, EntityType.PIG);		
	}
	
	private void pigPlayer(final Player player)
	{
		player.sendMessage(ChatColor.RED + "Someone's turned you into a pig!");
		pigs.add(player.getName());
		
		KitUtil.forceNamePlateUpdate(player);
		
		player.getWorld().playSound(player.getLocation(), Sound.PIG_DEATH, 2f, 0.8f);
		
		new RemoveSheepTask(player.getName()).runTaskLater(KitPlugin.getInstance(), DURATION);
	}
	
	@EventHandler
	private void showSheepPlayers(PacketSendEvent event)
	{
		if (!(event.getPacket() instanceof PacketPlayOutNamedEntitySpawn))
			return;
		
		PacketPlayOutNamedEntitySpawn packet = (PacketPlayOutNamedEntitySpawn) event.getPacket();
		Player player = Bukkit.getPlayer(packet.b);
		
		if (player == null)
			return;
		
		if (!pigs.contains(player.getName()))
			return;
		
//		packet.b = PlayerUtil.copyProfile(packet.b, "Red", packet.b);
		
		PacketPlayOutSpawnEntityLiving spawnSheep = new PacketPlayOutSpawnEntityLiving(((CraftPlayer) player).getHandle());
		spawnSheep.b = EntityType.PIG.getTypeId();
		
		event.setPacket(spawnSheep);
	}
	
	@EventHandler
	private void stopPigInteraction(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();
		
		if (!pigs.contains(player.getName()))
			return;

		event.setCancelled(true);
		player.sendMessage(ChatColor.RED + "Oink.");		
	}
	
	@EventHandler
	private void stopPigDamage(EntityDamageByEntityEvent event)
	{
		Entity damager = event.getDamager();
		
		if (!(damager.getType() == EntityType.PLAYER))
			return;
		
		Player player = (Player) damager;
		
		if (!pigs.contains(player.getName()))
			return;
		
		event.setCancelled(true);
		player.sendMessage(ChatColor.RED + "Oink.");		
	}
	
	@EventHandler
	private void removePigOnDamage(EntityDamageEvent event)
	{
		Entity entity = event.getEntity();
		
		if (!(entity.getType() == EntityType.PLAYER))
			return;
		
		Player player = (Player) entity;
		
		if (pigs.contains(player.getName()))
		{
			event.setCancelled(true);
		}
	}
	
	private void removePig(final Player player)
	{
		pigs.remove(player.getName());
		player.sendMessage(ChatColor.GREEN + "You feel like a person again.");
		
		KitUtil.forceNamePlateUpdate(player);
	}
	
	private class RemoveSheepTask extends BukkitRunnable
	{
		private final String playerName;
		
		public RemoveSheepTask(String playerName)
		{
			this.playerName = playerName;
		}

		@Override
		public void run()
		{
			Player player = Bukkit.getPlayerExact(playerName);
			
			if (player == null)
				return;
			
			removePig(player);
		}
	}
}

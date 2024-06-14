package kookaburra.minecraft.kit.kits.technical;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import kookaburra.minecraft.inventory.gui.GuiFolder;
import kookaburra.minecraft.inventory.gui.GuiItem;
import kookaburra.minecraft.inventory.gui.GuiItemListener;
import kookaburra.minecraft.kit.KitPlayer;
import kookaburra.minecraft.kit.KitPlayerCollection;
import kookaburra.minecraft.kit.KitPlugin;
import kookaburra.minecraft.player.McpvpPlayer;
import kookaburra.minecraft.player.McpvpPlayerCollection;
import kookaburra.minecraft.player.Rank;
import kookaburra.minecraft.util.ItemUtil;
import kookaburra.minecraft.util.Util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class KitController
{
	public static Set<String> ownedKits(McpvpPlayer player)
	{
		return player.getKits().keySet();
	}
	
	public static void showKitGUI(Player player)
	{
		GuiFolder.BLANK = Material.getMaterial(102); // Glass pane
		GuiFolder folder = new GuiFolder("Pick your kit!");
		
		final McpvpPlayer mp = McpvpPlayerCollection.players.get(player);
		
		final Set<String> owned = ownedKits(mp);
		
		ArrayList<KitBase> kits = new ArrayList<KitBase>(KitFactory.kits.values());
		
		Collections.sort(kits, new Comparator<KitBase>()
		{
			@Override
			public int compare(KitBase o1, KitBase o2)
			{
				if(!o1.hasMetadata())
					return -1;
				
				if(!o2.hasMetadata())
					return 1;
				
				int o1value = 0;
				int o2value = 0;
				
				KitMetadata m1 = o1.getMetadata();
				KitMetadata m2 = o2.getMetadata();
				
				final boolean isFree1 = m1.getPremiumRank().ordinal() <= mp.getRank().ordinal();
				final boolean isOwned1 = owned.contains(o1.getName());

				o1value = (isOwned1 ? 1 : (isFree1 ? 2 : 3));
				
				final boolean isFree2 = m2.getPremiumRank().ordinal() <= mp.getRank().ordinal();
				final boolean isOwned2 = owned.contains(o2.getName());
				
				o2value = (isOwned2 ? 1 : (isFree2 ? 2 : 3));

				if(o1value - o2value != 0) // First order by either free or owned
					return o1value - o2value;
				
				o1value = m1.getPremiumRank().ordinal();
				o2value = m2.getPremiumRank().ordinal();
				
				if(o1value - o2value != 0) // Order by rank that it's free too
					return o1value - o2value;
				
				return o1.getName().compareToIgnoreCase(o2.getName()); // Order alphabetically
			}
		});
				
		for(final KitBase kit : kits)
		{
			String name = kit.getName();
			
			boolean free = false;
			
			String freeToRank = "";
			String freeForRank = null;
			
			if(kit.hasMetadata())
			{
				Rank kitRank = kit.getMetadata().getPremiumRank();
				
				free = kitRank.ordinal() <= mp.getRank().ordinal();
				
				freeToRank = kitRank.ordinal() > Rank.NORMAL.ordinal() ? "(" + kitRank.name() + ")" : "";
				freeForRank = ChatColor.GREEN + "Free for: " + kitRank.getColor() + kitRank.name().toUpperCase();
			}
			
			final boolean isFree = free;
			final boolean isOwned = owned.contains(name);
			final boolean isPremium = !isOwned && !isFree ? true : false;
			
			String status = (isOwned ? ChatColor.GREEN + "OWNED" : (isFree ? ChatColor.YELLOW + "FREE! " + freeToRank : ""));
			
			ItemStack icon = kit.getKitIcon();
			
			List<String> currentDesc = new ArrayList<String>();
			
			currentDesc.add(0, (!isPremium ? status : ChatColor.RED + "PREMIUM!" + ChatColor.WHITE + (freeForRank != null ? " - " + freeForRank : "")));
			
			int index = 1;
			
			if(isPremium)
			{
				if(kit.hasMetadata())
					currentDesc.add(index++, "Price: $" + kit.getMetadata().getPrice() + "");
				else
					currentDesc.add(index++, "Available to buy on mcpvp.com/kits/kitpvp");
			}
			
			if(kit.hasMetadata())
			{
				currentDesc.add(index++, "");

				ArrayList<String> desc = Util.cutStringAtLastWord(40, kit.getMetadata().getOverview());

				for(int i = 0; i < desc.size(); i++)
				{
					currentDesc.add(i + index, desc.get(i));
				}
			}
			
			ItemUtil.setLore(icon, currentDesc);
			
			GuiItem item = folder.addItem(kit.getKitIcon().getType(), kit.getName());
			
			item.setDurability(icon.getDurability());
			item.setMeta(icon.getItemMeta());
			
			item.setListener(new GuiItemListener()
			{
				@Override
				public void onClick(GuiItem item, Player player, InventoryClickEvent event)
				{
					if(isPremium)
					{
						player.closeInventory();
						player.sendMessage(ChatColor.GREEN + String.format("Click here to go to our kit page: %s", "mcpvp.com/kits/kitpvp"));
					}
					else
					{
						equipPlayer(kit, player);
						player.closeInventory();
					}
				}
			});
		}
	
//		for(page = 0; page <= folder.getLastPage(); page++)
//		{
//			GuiItem item = folder.addItem(Material.COOKIE, "Pick a RANDOM kit", new GuiItemPosition(page, 0, 4));
//			item.setListener(new GuiItemListener()
//			{
//				@Override
//				public void onClick(GuiItem item, Player player)
//				{
//					player.closeInventory();
//					player.performCommand("random");
//				}
//			});
//		}
//		
		folder.setPageHeight(5);
		folder.show(player, 0);
	}
	
	public static void equipPlayer(KitBase kit, Player player)
	{
		if(player.getWorld() != KitPlugin.getMainWorld())
		{
			player.sendMessage(ChatColor.GREEN + "Please go to the main world with /spawn to use kits!");
			return;
		}
		
		McpvpPlayer mp = McpvpPlayerCollection.players.get(player);
		
		if(mp.isAdminMode())
		{
			player.performCommand("admin");
			return;
		}
		
		KitPlayer kp = KitPlayerCollection.players.get(player);
		
		kp.setCurrentKit(kit.getType());
		
		kit.onEquip(player);
		
		player.sendMessage(ChatColor.GREEN + String.format("You are now a %s", kit.getName()));
	}
}

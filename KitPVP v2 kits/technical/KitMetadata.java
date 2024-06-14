
package kookaburra.minecraft.kit.kits.technical;

import java.util.Set;

import kookaburra.minecraft.player.Rank;

public class KitMetadata
{
	private String createDate;
	private String name;
	private String iconURL;
	private String description;
	private String overview;
	private double price;
	private boolean isBuyable;
	private String premiumLevel;
	private Rank premiumRank;
	private String youtubeID;
	private int weight;
	private Set<String> abilities;
	private Set<Strategy> strategies;
	private Set<String> tags;
	private Set<KitVideo> videos;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getIconUrl()
	{
		return iconURL;
	}

	public void setIconUrl(String iconUrl)
	{
		this.iconURL = iconUrl;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getOverview()
	{
		return overview;
	}

	public void setOverview(String overview)
	{
		this.overview = overview;
	}

	public boolean isBuyable()
	{
		return isBuyable;
	}

	public void setBuyable(boolean isBuyable)
	{
		this.isBuyable = isBuyable;
	}

	public String getPremiumLevel()
	{
		return premiumLevel;
	}

	public void setPremiumLevel(String premiumLevel)
	{
		this.premiumLevel = premiumLevel;
	}

	public Rank getPremiumRank()
	{
		if(premiumRank == null)
		{
			String premiumLevel = getPremiumLevel();
			
			if(premiumLevel == null || premiumLevel.isEmpty())
				return null;
			
			Rank rank = null;
			
			if(premiumLevel.equalsIgnoreCase("free"))
			{
				rank = Rank.NUB;
			}
			else
			{
				try
				{
					rank = Rank.valueOf(premiumLevel);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
			premiumRank = rank;
		}
		
		return premiumRank;
	}

	public void setPremiumRank(Rank premiumRank)
	{
		this.premiumRank = premiumRank;
	}

	public Set<String> getAbilities()
	{
		return abilities;
	}

	public void setAbilities(Set<String> abilities)
	{
		this.abilities = abilities;
	}

	public Set<Strategy> getStrategies()
	{
		return strategies;
	}

	public void setStrategies(Set<Strategy> strategies)
	{
		this.strategies = strategies;
	}

	public Set<String> getTags()
	{
		return tags;
	}

	public void setTags(Set<String> tags)
	{
		this.tags = tags;
	}

	public double getPrice()
	{
		return price;
	}

	public void setPrice(double price)
	{
		this.price = price;
	}

	public static class Strategy
	{
		private String name;
		private String description;
		private String counter;

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String getDescription()
		{
			return description;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public String getCounter()
		{
			return counter;
		}

		public void setCounter(String counter)
		{
			this.counter = counter;
		}
	}

	public static class KitVideo
	{
		private String youtubeID;
		private String title;
		private String author;
		private String authorUrl;
		private String videoUrl;
		private String description;
	}
}

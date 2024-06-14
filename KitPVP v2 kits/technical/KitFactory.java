
package kookaburra.minecraft.kit.kits.technical;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Reflection Kit Factory
 * Loads all classes extending @KitBase present in this jar.
 * @author ThaRedstoner
 */
public class KitFactory
{
	public static final String JAR = "plugins/kKitPVP2.jar";
	public static final String CLASSPATH = "kookaburra/minecraft/kit/kits";
	public static HashMap<Class<? extends KitBase>, KitBase> kits = new HashMap<Class<? extends KitBase>, KitBase>();

	@SuppressWarnings("unchecked")
	public static void load()
	{
		long start = System.currentTimeMillis();
		String kitString = "";
		int split = 0;
		HashMap<KitBase, Long> loadTime = new HashMap<KitBase, Long>();
		try
		{
			JarInputStream jarReader = new JarInputStream(new FileInputStream(JAR));
			JarEntry entry;
			
			while (true)
			{
				entry = jarReader.getNextJarEntry();
				long startKit = System.currentTimeMillis();
				
				if (entry == null)
				{
					break;
				}
				
				try
				{
					if (entry.getName().toLowerCase().startsWith(CLASSPATH + "/technical")) // No technical classes.
						continue;
					
					if ((entry.getName().toLowerCase().startsWith(CLASSPATH)) && entry.getName().toLowerCase().endsWith(".class"))
					{
						if (entry.getName().contains("$")) // Don't declare inner-classes.
							continue;
						
						System.out.println(entry.getName());

						Class<?> clazz = null;
						try
						{
							clazz = Class.forName(entry.getName().replace("/", ".").replace(".class", ""));
						}
						catch (Exception e)
						{

						}

						if (clazz == null)
							continue;
						
						Class kitBaseClass = clazz;

						while(kitBaseClass.getSuperclass() != KitBase.class)
						{
							kitBaseClass = kitBaseClass.getSuperclass();

							if(kitBaseClass == null)
							{
								break;
							}
						}

						if (kitBaseClass == null) // If no kitbase was found.
							continue;

						KitBase kit = (KitBase) clazz.newInstance();

						kits.put(kit.getType(), kit);

						long time = (System.currentTimeMillis() - startKit);
						loadTime.put(kit, time);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			jarReader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		long last = Long.MAX_VALUE;
		Iterator<Entry<KitBase, Long>> it = loadTime.entrySet().iterator();
		long highest = Long.MIN_VALUE;

		while (it.hasNext())
		{
			Entry<KitBase, Long> set = it.next();

			if (last == set.getValue())
			{
				if (split != 0 && !kitString.equals(""))
				{
					kitString += ", ";
				}
				kitString += set.getKey().getName() + "(" + set.getValue() + "ms)";
				split++;
				if (split >= 6)
				{
					kitString += "\n";
					split = 0;
				}
				it.remove();
			}

			if (set.getValue() > highest)
			{
				highest = set.getValue();
			}

			if (!it.hasNext())
			{
				last = highest;
				highest = Long.MIN_VALUE;
				it = loadTime.entrySet().iterator();
			}
		}

		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[Kit Factory] Loaded " + kits.size() + " kits. - " + (System.currentTimeMillis() - start) + "ms \n" + kitString);
	}

	public static KitBase GetByClass(Class<? extends KitBase> clazz)
	{
		return kits.containsKey(clazz) ? kits.get(clazz) : null;
	}
}

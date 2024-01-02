package csweetla.mob_follow;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.block.Block;
import net.minecraft.core.entity.Entity;
import net.minecraft.core.entity.EntityDispatcher;
import net.minecraft.core.item.Item;
import turniplabs.halplibe.util.TomlConfigHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import turniplabs.halplibe.util.toml.Toml;

import java.util.HashMap;
import java.util.Map;

public class MobFollow implements ModInitializer {
    public static final String MOD_ID = "mob_follow";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static TomlConfigHandler tconfig;
	static {
		tconfig = new TomlConfigHandler(MOD_ID,
		new Toml().addCategory("The item key of the item the player will hold to lead the mob","FollowItem")
			.addEntry("FollowItem.Pig","item.food.apple")
			.addEntry("FollowItem.Chicken","item.seeds.wheat")
			.addEntry("FollowItem.Sheep","item.wheat")
			.addEntry("FollowItem.Cow", "item.wheat")
		);
	}

	public static Map<Class<? extends Entity>, Item> lead_item_map = new HashMap<>();

	public void try_add_to_lead_item_map(String entity_string) {
		Class<? extends Entity> clazz = EntityDispatcher.keyToClassMap.get(entity_string);
		String config_entry = tconfig.getString("FollowItem." + entity_string);
		Item lead_item;
		if (config_entry.startsWith("item."))
			lead_item = Item.itemsList[Item.nameToIdMap.get(config_entry)];
		else if (config_entry.startsWith("tile."))
			lead_item = Block.getBlockByName(config_entry).asItem();
		else {
			LOGGER.info("Didn't recognize follow item '" + config_entry + "' for " + entity_string + ", so this animal won't be able to be lead!");
			lead_item = null;
		}
			lead_item_map.put(clazz,lead_item);
	}

    @Override
    public void onInitialize() {
		try_add_to_lead_item_map("Pig");
		try_add_to_lead_item_map("Chicken");
		try_add_to_lead_item_map("Sheep");
		try_add_to_lead_item_map("Cow");

		LOGGER.info(MOD_ID + " initialized");
    }
}

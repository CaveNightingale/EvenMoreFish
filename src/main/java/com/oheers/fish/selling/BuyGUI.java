package com.oheers.fish.selling;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.config.messages.Message;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuyGUI implements InventoryHolder {

	public static final int SECOND_STACK_AMOUNT = 10;
	private final Player player;
	private final Inventory menu;

	private final HashMap<Integer, Triple<Bait, Integer, Double>> baitsMap = new HashMap<>();

	private final ItemStack GLASS_PANE = new ItemStack(Material.GLASS_PANE);

	public int guiSize;

	public BuyGUI(Player player1) {
		this.guiSize = EvenMoreFish.baits.size() > 18 ? 72 : 36;
		this.player = player1;
		this.menu = Bukkit.createInventory(this, guiSize, new Message(ConfigMessage.BAIT_SHOP_GUI_NAME).getRawMessage(true, true));
		listAllBaits();
		player.openInventory(this.menu);
		ItemMeta meta = GLASS_PANE.getItemMeta();
		meta.setDisplayName(ChatColor.RESET.toString());
		GLASS_PANE.setItemMeta(meta);
	}

	public void playerBuy(Player player, int clickedSlot) {
		Triple<Bait, Integer, Double> buying = baitsMap.get(clickedSlot);
		if(buying == null)
			return;
		if(EvenMoreFish.econ.has(player, buying.getRight()) && EvenMoreFish.econ.withdrawPlayer(player, buying.getRight()).transactionSuccess()) {
			ItemStack toBuy = buying.getLeft().create(player);
			toBuy.setAmount(buying.getMiddle());
			player.getInventory().addItem(toBuy).forEach((i, it) -> player.getWorld().dropItem(player.getLocation(), it));
			this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.75f);
			listAllBaits();
			this.player.updateInventory();
		} else {
			this.player.playSound(this.player.getLocation(), Sound.BLOCK_NOTE_BLOCK_GUITAR, 1.0f, 0.0f);
		}
	}

	public void listAllBaits() {
		int i = 0;
		for(Map.Entry<String, Bait> bait : EvenMoreFish.baits.entrySet()) {
			ItemStack baitStack = bait.getValue().create(player);
			double price = EvenMoreFish.baitFile.getBaitPrice(bait.getKey());
			ItemMeta meta = baitStack.getItemMeta();
			List<String> lores = meta.getLore();
			Message priceMessage = new Message(EvenMoreFish.econ.has(player, price) ? ConfigMessage.BAIT_SHOP_GUI_PRICE : ConfigMessage.BAIT_SHOP_GUI_PRICE_CANT_AFFORD);
			priceMessage.setSellPrice(String.valueOf(price));
			lores.add(0, priceMessage.getRawMessage(true, true));
			meta.setLore(lores);
			baitStack.setItemMeta(meta);
			baitsMap.put(i, Triple.of(bait.getValue(), 1, price));

			menu.setItem(i, baitStack);
			baitStack = baitStack.clone();
			priceMessage.setSellPrice(String.valueOf(price * SECOND_STACK_AMOUNT));
			priceMessage = new Message(EvenMoreFish.econ.has(player, price * SECOND_STACK_AMOUNT) ? ConfigMessage.BAIT_SHOP_GUI_PRICE : ConfigMessage.BAIT_SHOP_GUI_PRICE_CANT_AFFORD);
			priceMessage.setSellPrice(String.valueOf(price * SECOND_STACK_AMOUNT));
			lores.set(0, priceMessage.getRawMessage(true, true));
			meta.setLore(lores);
			baitStack.setItemMeta(meta);
			baitStack.setAmount(SECOND_STACK_AMOUNT);
			baitsMap.put(i + 9, Triple.of(bait.getValue(), SECOND_STACK_AMOUNT, price * SECOND_STACK_AMOUNT));
			menu.setItem(i + 9, baitStack);

			i++;
			if(i > 72)
				return;
			if(i % 9 == 0)
				i += 9;
		}
		addFiller(GLASS_PANE);
	}

	public void addFiller(ItemStack fill) {
		for (int i = 0; i < guiSize; i++) {
			ItemStack item = menu.getItem(i);
			if (item == null) {
				menu.setItem(i, fill);
			}
		}
	}

	@NotNull
	@Override
	public Inventory getInventory() {
		return menu;
	}
}

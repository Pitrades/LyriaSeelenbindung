package org.silvius.lyriaseelenbindung;

import com.destroystokyo.paper.event.inventory.PrepareGrindstoneEvent;
import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.silvius.lyriaseelenbindung.LyriaSeelenbindung.econ;

public class Seelenbindung extends Enchantment implements Listener {
    double money = 1.05d;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getPlayer();
        Player killer = null;
        List<ItemStack> droppedItems = event.getDrops();
        Collection<ItemStack> itemsToRemove = new java.util.ArrayList<>(Collections.emptyList());
        if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
            if (dmgEvent.getDamager() instanceof Player) {
                killer = (Player) dmgEvent.getDamager();}
        }
        for (int i=0; i<droppedItems.size(); i++){
            ItemStack item = droppedItems.get(i);
            if(item!=null && item.getItemMeta().hasEnchant(this)){
                if(killer!=null){
                    econ.depositPlayer(player.getKiller(), money);
                    player.getKiller().sendMessage("Deinem Konto wurden Ð"+Double.toString(money)+" hinzugefügt");}

                event.getItemsToKeep().add(item);
                itemsToRemove.add(item);
                seelenbindungCommand.lowerSeelenbindungLevel(item);
            }
        }
        droppedItems.removeAll(itemsToRemove);
    }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event){
        //Wenn Item mit Verzauberung 2. Item ist, wird auf Result nicht Lore übertragen
        ItemStack result = event.getResult();
        ItemStack secondItem = event.getInventory().getSecondItem();

        if(result==null){return;}
        ItemMeta meta = result.getItemMeta();
        if(meta==null){return;}
        if(meta.hasLore() && meta.lore().toString().contains("Seelenbindung"))
        {event.getResult().addUnsafeEnchantment(LyriaSeelenbindung.seelenbindung, 1);}
        else if (secondItem!=null && secondItem.getItemMeta()!=null && secondItem.getItemMeta().hasLore() && secondItem.getItemMeta().lore().toString().contains("Seelenbindung")) {
            seelenbindungCommand.addSeelenbindung(event.getResult());
        }
    }

    @EventHandler
    public void onGrindstonePrepare(PrepareResultEvent event){
        if(event.getInventory().getType()!=InventoryType.GRINDSTONE){return;}
        ItemStack result = event.getResult();
        if(result==null){return;}
        ItemMeta meta = result.getItemMeta();
        if(meta==null){return;}
        if(meta.hasLore() && meta.lore().toString().contains("Seelenbindung"))
        {   System.out.println("Grindstone");
            seelenbindungCommand.removeSeelenbindung(result);}
    }
    @EventHandler
    public void onGrindstoneGrind(InventoryClickEvent event){
        if(event.getInventory().getType()== InventoryType.GRINDSTONE){
            ItemStack result = event.getCurrentItem();
            if(result==null){return;}
            ItemMeta meta = result.getItemMeta();
            if(meta==null){return;}
            PersistentDataContainer data = meta.getPersistentDataContainer();
            NamespacedKey namespacedKey = new NamespacedKey(LyriaSeelenbindung.getPlugin(), "hadSeelenbindung");
            if(data.has(namespacedKey) && data.get(namespacedKey, PersistentDataType.INTEGER)==1){
                data.remove(namespacedKey);
                result.setItemMeta(meta);
                System.out.println("SA");
                HumanEntity player = event.getWhoClicked();
                ExperienceOrb orb = player.getWorld().spawn(event.getInventory().getLocation(), ExperienceOrb.class);
                orb.setExperience(10);
            }

        }
    }
    public Seelenbindung(String namespace){
        super(new NamespacedKey(LyriaSeelenbindung.getPlugin(), namespace));

    }




    @Override
    public @NotNull String getName() {
        return "Seelenbindung";
    }


    @Override
    public @NotNull EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return super.getKey();
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean conflictsWith(@NotNull Enchantment other) {
        return false;
    }

    @Override
    public boolean canEnchantItem(@NotNull ItemStack item) {
        return true;
    }

    @Override
    public @NotNull Component displayName(int i) {
        return Component.text("Seelenbindung");
    }

    @Override
    public boolean isTradeable() {
        return false;
    }

    @Override
    public boolean isDiscoverable() {
        return false;
    }

    @Override
    public @NotNull EnchantmentRarity getRarity() {
        return null;
    }

    @Override
    public float getDamageIncrease(int i, @NotNull EntityCategory entityCategory) {
        return 0;
    }

    @Override
    public @NotNull Set<EquipmentSlot> getActiveSlots() {
        return null;
    }

    @Override
    public @NotNull String translationKey() {
        return "enchantment.custom.seelenbindung";
    }


}


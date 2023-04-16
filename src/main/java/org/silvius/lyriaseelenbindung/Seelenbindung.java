package org.silvius.lyriaseelenbindung;


import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import io.papermc.paper.enchantments.EnchantmentRarity;
import me.chocolf.moneyfrommobs.api.event.DropMoneyEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import me.chocolf.moneyfrommobs.MoneyFromMobs;
import me.chocolf.moneyfrommobs.manager.DropsManager;



import java.util.*;

public class Seelenbindung extends Enchantment implements Listener {


    final double money = 100d;

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        final Player player = event.getPlayer();
        Player killer = null;
        final List<ItemStack> droppedItems = event.getDrops();
        final Collection<ItemStack> itemsToRemove = new java.util.ArrayList<>(Collections.emptyList());
        boolean hadSeelenbindung = false;
        if (player.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent dmgEvent = (EntityDamageByEntityEvent) player.getLastDamageCause();
            if (dmgEvent.getDamager() instanceof Player) {
                killer = (Player) dmgEvent.getDamager();}
        }
        for (ItemStack item : droppedItems) {
            if (item != null && item.getItemMeta().hasEnchant(this)) {
                if (killer != null) {
                    hadSeelenbindung = true;
                }
                if (item.getItemMeta() instanceof Damageable) {
                    final ItemMeta meta = item.getItemMeta();
                    ((Damageable) meta).setDamage((int) Math.round(item.getType().getMaxDurability() * 0.2));
                    item.setItemMeta(meta);
                }
                event.getItemsToKeep().add(item);
                itemsToRemove.add(item);
                SeelenbindungCommand.lowerSeelenbindungLevel(item);
            }
        }
        droppedItems.removeAll(itemsToRemove);
        //Falls der Spieler Seelenbindung hatte und von einem Spieler getötet wurde: erhält Geld, xp
        if(hadSeelenbindung){
            ItemStack itemToDrop = new ItemStack(Material.EMERALD);
            double amount = money;
            Location location = event.getPlayer().getLocation();
            Entity entity = event.getEntity();
            int numberOfDrops = 1;
            DropMoneyEvent dropMoneyEvent = new DropMoneyEvent(itemToDrop,amount, location, killer, entity, numberOfDrops);
            Bukkit.getPluginManager().callEvent(dropMoneyEvent);
            if (dropMoneyEvent.isCancelled())
                return;
            itemToDrop = dropMoneyEvent.getItemToDrop();
            amount = dropMoneyEvent.getAmount();
            location = dropMoneyEvent.getLocation();
            numberOfDrops = dropMoneyEvent.getNumberOfDrops();
            DropsManager dropsManager = MoneyFromMobs.getInstance().getDropsManager();
            dropsManager.dropItem(itemToDrop, amount, location, numberOfDrops, killer);
            event.setDroppedExp(event.getDroppedExp()+300);
        }

        }

    @EventHandler
    public void onAnvilPrepare(PrepareAnvilEvent event){

        final ItemStack secondItem = event.getInventory().getSecondItem();
        final ItemStack firstItem = event.getInventory().getFirstItem();
        ItemStack result = event.getResult();
        Integer levelFirstItem = null;
        Integer levelSecondItem = null;

        if(secondItem==null || firstItem==null){return;}
        if(firstItem.getItemMeta().hasEnchant(LyriaSeelenbindung.seelenbindung)) {
            levelFirstItem = firstItem.getItemMeta().getEnchantLevel(LyriaSeelenbindung.seelenbindung);
        }

        if(secondItem.getItemMeta().hasEnchant(LyriaSeelenbindung.seelenbindung)) {
            levelSecondItem = secondItem.getItemMeta().getEnchantLevel(LyriaSeelenbindung.seelenbindung);
        }
        if(levelFirstItem==null && levelSecondItem==null){return;}
        if(firstItem.getType()==secondItem.getType() && result==null){
            event.setResult(firstItem.clone());
            result = event.getResult();
        }
        if(result==null){return;}
        if(Objects.equals(levelFirstItem, levelSecondItem)){
          SeelenbindungCommand.removeSeelenbindung(result);
          SeelenbindungCommand.addSeelenbindung(result, levelFirstItem+1);
        }
        SeelenbindungCommand.removeSeelenbindung(result);
        if(levelSecondItem==null){
            SeelenbindungCommand.addSeelenbindung(result, levelFirstItem);
        }
        else{
            SeelenbindungCommand.addSeelenbindung(result, Math.max(levelFirstItem, levelSecondItem));
        }
        event.setResult(result);

    }

    @EventHandler
    public void onGrindstonePrepare(PrepareResultEvent event){
        if(event.getInventory().getType()!=InventoryType.GRINDSTONE){return;}
        final ItemStack result = event.getResult();
        if(result==null){return;}
        final ItemMeta meta = result.getItemMeta();
        if(meta==null){return;}
        if(meta.hasLore() && meta.lore().toString().contains("Seelenbindung"))
        {   System.out.println("Grindstone");
            SeelenbindungCommand.removeSeelenbindung(result);}
    }
    @EventHandler
    public void onGrindstoneGrind(InventoryClickEvent event){
        if(event.getInventory().getType()== InventoryType.GRINDSTONE){
            final ItemStack result = event.getCurrentItem();
            if(result==null){return;}
            final ItemMeta meta = result.getItemMeta();
            if(meta==null){return;}
            final PersistentDataContainer data = meta.getPersistentDataContainer();
            final NamespacedKey namespacedKey = new NamespacedKey(LyriaSeelenbindung.getPlugin(), "hadSeelenbindung");
            if(data.has(namespacedKey) && data.get(namespacedKey, PersistentDataType.INTEGER)==1){
                data.remove(namespacedKey);
                result.setItemMeta(meta);
                System.out.println("SA");
                final HumanEntity player = event.getWhoClicked();
                final ExperienceOrb orb = player.getWorld().spawn(Objects.requireNonNull(event.getInventory().getLocation()), ExperienceOrb.class);
                orb.setExperience(10*meta.getEnchantLevel(LyriaSeelenbindung.seelenbindung));
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


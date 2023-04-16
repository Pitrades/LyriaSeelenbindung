package org.silvius.lyriaseelenbindung;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeelenbindungCommand implements CommandExecutor {
    public static void addSeelenbindung(ItemStack item, Integer level){
        item.addUnsafeEnchantment(LyriaSeelenbindung.seelenbindung, level);
        final ItemMeta meta = item.getItemMeta();
        final List<Component> lore = new ArrayList<>();
        lore.add(Component.text(ChatColor.GRAY + "Seelenbindung "+"I".repeat(level)));
        if (meta.hasLore()) {
            lore.addAll(meta.lore());
        }
        meta.lore(lore);
        item.setItemMeta(meta);
    }


    public static void lowerSeelenbindungLevel(ItemStack item){

        final ItemMeta meta = item.getItemMeta();
        final int enchantmentLevel = meta.getEnchantLevel(LyriaSeelenbindung.seelenbindung);
        meta.removeEnchant(LyriaSeelenbindung.seelenbindung);
        final List<Component> lore = new ArrayList<>();
        if (meta.hasLore()) {
            for (Component l : Objects.requireNonNull(meta.lore())){
                if(!l.toString().contains("Seelenbindung")){
                    lore.add(l);}
                else {
                    if(enchantmentLevel==2){
                        lore.add(Component.text(ChatColor.GRAY + "Seelenbindung I"));
                    } else if (enchantmentLevel==3) {
                        lore.add(Component.text(ChatColor.GRAY + "Seelenbindung II"));
                    }
                }
            }
        }
        if(enchantmentLevel>1){
            item.addUnsafeEnchantment(LyriaSeelenbindung.seelenbindung, enchantmentLevel-1);
            meta.addEnchant(LyriaSeelenbindung.seelenbindung, enchantmentLevel-1, true);}

        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public static void removeSeelenbindung(ItemStack item){
        final ItemMeta meta = item.getItemMeta();
        meta.removeEnchant(LyriaSeelenbindung.seelenbindung);
        final List<Component> lore = new ArrayList<>();
        if (meta.hasLore()) {
            for (Component l : Objects.requireNonNull(meta.lore())){
                if(!l.toString().contains("Seelenbindung")){
                    lore.add(l);}
            }
        }


        meta.lore(lore);

        final PersistentDataContainer data = meta.getPersistentDataContainer();
        final NamespacedKey namespacedKey = new NamespacedKey(LyriaSeelenbindung.getPlugin(), "hadSeelenbindung");
        data.set(namespacedKey, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
    }

    public static boolean isValidType(ItemStack item){
        return EnchantmentTarget.ALL.includes(item);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            final Player player = ((Player) commandSender).getPlayer();
            assert player != null;
            if(!player.hasPermission("lyriaseelenbindung.seelenbindung")){
                commandSender.sendMessage(ChatColor.RED+"Keine Berechtigung");
                return true;
            }
            final ItemStack item = player.getInventory().getItemInMainHand();
            if(!isValidType(item)){
                commandSender.sendMessage(ChatColor.RED+"Verzauberung kann nicht gesetzt werden");
                return true;
            }
            if(item.getItemMeta()!=null && item.getItemMeta().hasLore() && item.getItemMeta().lore().toString().contains("Seelenbindung")){
                commandSender.sendMessage(ChatColor.RED+"Seelenbindung schon vorhanden");
                return true;
            }
            if(strings.length==0){
                commandSender.sendMessage(ChatColor.RED+"Bitte Level angeben");
                return true;
            }
            if(strings.length==2){
                commandSender.sendMessage(ChatColor.RED+"Zu viele Argumente");
                return true;
            }
            ArrayList<String> validLevels= new ArrayList<>();
            validLevels.add("1");
            validLevels.add("2");
            validLevels.add("3");
            if(!validLevels.contains(strings[0])){
                return true;
            }

            addSeelenbindung(item, Integer.valueOf(strings[0]));

        }
        return true;

    }



}

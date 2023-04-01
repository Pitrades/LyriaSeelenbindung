package org.silvius.lyriaseelenbindung;

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

public class SeelenbindungCommand implements CommandExecutor {
    public static void addSeelenbindung(ItemStack item, Integer level){
        item.addUnsafeEnchantment(LyriaSeelenbindung.seelenbindung, level);
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Seelenbindung "+"I".repeat(level));
        if (meta.hasLore()) {
            for (String l : meta.getLore()){
                lore.add(l);
            }
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
    }


    public static void lowerSeelenbindungLevel(ItemStack item){

        ItemMeta meta = item.getItemMeta();
        Integer enchantmentLevel = meta.getEnchantLevel(LyriaSeelenbindung.seelenbindung);
        meta.removeEnchant(LyriaSeelenbindung.seelenbindung);
        List<String> lore = new ArrayList<String>();
        if (meta.hasLore()) {
            for (String l : meta.getLore()){
                if(!l.toString().contains("Seelenbindung")){
                    lore.add(l);}
                else {
                    if(enchantmentLevel==2){
                        lore.add(ChatColor.GRAY + "Seelenbindung I");
                    } else if (enchantmentLevel==3) {
                        lore.add(ChatColor.GRAY + "Seelenbindung II");
                    }
                }
            }
        }
        if(enchantmentLevel>1){
            item.addUnsafeEnchantment(LyriaSeelenbindung.seelenbindung, enchantmentLevel-1);
            meta.addEnchant(LyriaSeelenbindung.seelenbindung, enchantmentLevel-1, true);}

        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public static void removeSeelenbindung(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        meta.removeEnchant(LyriaSeelenbindung.seelenbindung);
        List<String> lore = new ArrayList<String>();
        if (meta.hasLore()) {
            for (String l : meta.getLore()){
                if(!l.toString().contains("Seelenbindung")){
                    lore.add(l);}
            }
        }


        meta.setLore(lore);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(LyriaSeelenbindung.getPlugin(), "hadSeelenbindung");
        data.set(namespacedKey, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
    }

    public static boolean isValidType(ItemStack item){
        if( EnchantmentTarget.ALL.includes(item)){return true;}
        return false;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = ((Player) commandSender).getPlayer();
            if(!player.hasPermission("lyriaseelenbindung.seelenbindung")){
                commandSender.sendMessage(ChatColor.RED+"Keine Berechtigung");
                return true;
            }
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item==null | !isValidType(item)){
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
            ArrayList<String> validLevels= new ArrayList<String>();
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

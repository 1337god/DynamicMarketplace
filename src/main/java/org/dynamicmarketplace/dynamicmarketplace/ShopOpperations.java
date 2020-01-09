package org.dynamicmarketplace.dynamicmarketplace;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ShopOpperations {

    private static Economy econ = DynamicMarketplace.economy;

    /* Utils */

    public static Material getItemMaterial (String name ){
        String converted = name.replaceAll("([A-Z])", "_$1").toUpperCase();
        return Material.getMaterial( converted );
    }

    public static void buy (Player player, String validItem, int validAmount ) {

        double price = SaveData.getFullPrice( validItem, validAmount, false );
        double bal = econ.getBalance( player );
        Material itemMaterial = getItemMaterial( validItem );

        /* make sure this buy is valid */

        if ( bal < price )
            PlayerInteractions.buyFailedCost( player, price, bal );

        else if ( price < 0)
            PlayerInteractions.buyFailedQuantity( player, validItem, validAmount);

        /* do buy */

        else {

            SaveData.updateQuantities( validItem, validAmount, false);
            econ.withdrawPlayer(player, price);
            int originalAmount = validAmount;
            int stackSize = itemMaterial.getMaxStackSize();

            while ( validAmount > 0 ){

                if ( player.getInventory().firstEmpty() == -1 ) {
                    int total = originalAmount - validAmount;
                    price = SaveData.getFullPrice( validItem, total, false );
                    PlayerInteractions.buyFailedSpace(player, validItem, total, price );
                    return;
                }

                int stack = validAmount > stackSize ? stackSize : validAmount;
                validAmount -= stack;


                ItemStack items = new ItemStack( itemMaterial );
                items.setAmount( stack );

                player.getInventory().addItem(items);
            }

            PlayerInteractions.buySuccess( player, validItem, originalAmount, price );

        }

    }

    public static void sell (Player player, String validItem, int validAmount ) {

        double bal = econ.getBalance( player );
        Material itemMat = getItemMaterial( validItem );

        int total = 0;

        for (ItemStack item : player.getInventory()) {
            if ( item == null ) continue;

            if ( item.getType() == itemMat && total < validAmount ) {

                if ( total + item.getAmount() > validAmount ) {
                    item.setAmount( item.getAmount() - ( validAmount - total ) );
                    total = validAmount;
                }
                else {
                    total += item.getAmount();
                    player.getInventory().removeItem( item );
                }

                if ( total >= validAmount )
                    break;

            }
        }

        double price = SaveData.getFullPrice( validItem, total, true );
        SaveData.updateQuantities( validItem, total, true );

        if ( total < validAmount )
            PlayerInteractions.sellFailedQuantity( player, validItem, total, price );
        else
            PlayerInteractions.sellSuccess( player, validItem, total, price );

        econ.depositPlayer(player, price);

    }

    public static void itemInfo ( Player player, String validItem ){

        double amount = SaveData.marketQuantity(validItem);
        double priceFor1_buy = SaveData.getFullPrice( validItem, 1, false);
        double priceFor1_sell = SaveData.getFullPrice( validItem, 1, true);
        double priceFor64_buy = SaveData.getFullPrice( validItem, 64, false);
        double priceFor64_sell = SaveData.getFullPrice( validItem, 64, true);

        PlayerInteractions.itemInfo(player, validItem, amount, new double[]{ priceFor1_buy, priceFor1_sell, priceFor64_buy, priceFor64_sell});

    }

    public static void sellAll ( Player player ){

        for (ItemStack item : player.getInventory()) {

            if ( item == null ) continue;
            if ( !item.getEnchantments().isEmpty()) continue;
            if ( item.getDurability()  != 0 ) continue;

            String name = item.getType().toString().toLowerCase() ;
            if ( SaveData.validItem( name ) )
                sell( player, name, item.getAmount());
        }
    }

    public static void sellHand ( Player player ){

        ItemStack item = player.getInventory().getItemInHand();
        String name = item.getType().toString().toLowerCase() ;

        if ( !item.getEnchantments().isEmpty())
            PlayerInteractions.invalidSellEnchant( player );
        else if ( item.getDurability()  != 0 )
            PlayerInteractions.invalidSellDamaged( player);
        else if ( !SaveData.validItem(  name ) )
            PlayerInteractions.itemInvalid( player, name);
        else
            sell ( player, name, item.getAmount() );
    }

    public static void infoHand ( Player player ){

        ItemStack item = player.getInventory().getItemInHand();
        String name = item.getType().toString().toLowerCase() ;
        if ( !SaveData.validItem(  name ) )
            PlayerInteractions.itemInvalid( player, name);
        else
            itemInfo ( player, name);
    }

    public static void cost (Player player, String validItem, int validAmount ) {
        double price = SaveData.getFullPrice( validItem, validAmount, false );
        PlayerInteractions.itemCost(player, validItem, validAmount, price );
    }

}

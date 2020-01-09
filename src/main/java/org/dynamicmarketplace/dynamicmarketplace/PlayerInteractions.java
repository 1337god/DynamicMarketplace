package org.dynamicmarketplace.dynamicmarketplace;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerInteractions {

    private static String prefix = ChatColor.GREEN + "[DynaMark] " + ChatColor.WHITE;

    private static void sendPlayer ( Player player, String message ){

        message = message.replaceAll( "\\[([^]]*)\\]", ChatColor.RED + "$1" + ChatColor.WHITE );
        message = message.replaceAll( "\\(([^)]*)\\)", ChatColor.GREEN + "$1" + ChatColor.WHITE );
        message = message.replaceAll( "\\{([^}]*)}", ChatColor.YELLOW + "$1" + ChatColor.WHITE );

        player.sendMessage( prefix + message );

    }

    public static void buyFailedCost (Player player, double cost, double balance) {
        sendPlayer ( player, String.format ("You cannot afford the purchase, cost: ($%.4f), your balance: ($%.2f)", cost, balance));
    }
    public static void buyFailedQuantity (Player player, String item, int amount) {
        sendPlayer ( player, String.format ("You cannot buy (%d) of (%s), there are not that many left in the shop", amount, item));
    }
    public static void buySuccess ( Player player, String item, int amount, double sale ){
        sendPlayer( player, String.format( "Bought (%d) (%s) for ($%.2f)", amount, item, sale));
    }
    public  static void buyFailedSpace ( Player player, String item, int amount, double sale ){
        sendPlayer( player, String.format( "Inventory space limited! Only bought (%d) (%s) for ($%.2f)", amount, item, sale));
    }

    public static void sellFailedQuantity ( Player player, String item, int amount, double sale ){
        sendPlayer ( player, String.format ("You dont have that much (%s), sold the (%d) you did have for ($%.2f)", item, amount, sale));
    }
    public static void sellSuccess ( Player player, String item, int amount, double sale ){
        sendPlayer ( player, String.format ("Sold (%d) (%s) for ($%.2f)", amount, item, sale));
    }

    public static void itemInfo ( Player player, String item, double amount, double[] costs ){
        sendPlayer( player, "-------------------------------");
        sendPlayer(player, String.format("{Info for item:} (%s) . . .", item));
        if ( amount > 0 ) {
            sendPlayer(player, String.format("This item is a basic material"));
            sendPlayer(player, String.format("There are (%.2f) in the shop", amount));
        }
        else
            sendPlayer(player, String.format("This item is a crafted material"));
        sendPlayer( player, String.format("For 1 item, Buy: (%.2f) Sell: (%.2f)", costs[0], costs[1]));
        sendPlayer( player, String.format("For a stack, Buy: (%.2f) Sell: (%.2f)", costs[2], costs[3]));
        sendPlayer( player, "-------------------------------");
    }

    public static void itemInvalid ( Player player, String item){
        sendPlayer( player, String.format("No item of name (%s) exists, ask an admin to add it to the market", item ));
    }

    public static void inputInvalid ( Player player, String input ){
        sendPlayer( player, String.format("Input number \'(%s)\' is not valid", input ));
    }

    public static void invalidSellEnchant ( Player player ){
        sendPlayer( player, "Cannot sell enchanted items");
    }
    public static void invalidSellDamaged ( Player player ){
        sendPlayer( player, "Cannot sell damaged items");
    }
    public static void itemCost ( Player player, String name, int amount, double price ){
        sendPlayer( player, String.format("(%d) (%s) currently costs (%.2f)", amount, name, price ));
    }

}

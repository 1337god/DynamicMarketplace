package org.dynamicmarketplace.dynamicmarketplace;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class DynamicMarketplace extends JavaPlugin {

    public static Economy economy = null;


    @Override
    public void onEnable() {

        if ( ! setupEconomy() ){ throwError (); }
        SaveData.init();


    }
    public static void throwError () {
        System.out.println("[*** Dynamic Market Error ***] Do you have an economy installed with vault? ");
        Bukkit.shutdown();
    }

    private boolean setupEconomy () {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration( net.milkbowl.vault.economy.Economy.class);
        if ( economyProvider != null )
            economy = economyProvider.getProvider();
        return ( economy != null );
    }

    private static int castInt ( String str, Player player ) {
        if ( str.matches("[0-9]+" ) ) {
            int number = Integer.parseInt(str);
            if ( number < 1000 )
                return  number;
        }
        PlayerInteractions.inputInvalid( player, str);
        return -1;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( sender instanceof Player ){

            // Get player who is asking
            Player player = (Player) sender;

            // Try to buy an item
            if ( command.getName().equals("buy")) {
                if (args.length == 0) return false;
                if (SaveData.validItem(args[0], player)) {
                    int amount = (args.length == 1) ? 1 : castInt(args[1], player);
                    if (amount > 0) {
                        ShopOpperations.buy(player, args[0], amount);
                        SaveData.saveMarket();
                    }
                }
            }

            // Try to sell an item
            if ( command.getName().equals("sell")) {
                if (args.length == 0) return false;
                if (SaveData.validItem(args[0], player)) {
                    int amount = (args.length == 1) ? 1 : castInt(args[1], player);
                    if (amount > 0) {
                        ShopOpperations.sell(player, args[0], amount);
                        SaveData.saveMarket();
                    }
                }
            }

            if ( command.getName().equals("itemInfo")) {
                if ( args.length == 0 ) return false;
                if (SaveData.validItem(args[0], player)) {
                    ShopOpperations.itemInfo( player, args[0] );
                }
                else {
                    PlayerInteractions.itemInvalid( player, args[0] );
                }
            }

            if ( command.getName().equals("sellAll") ){
                ShopOpperations.sellAll( player );
                SaveData.saveMarket();
            }

            if ( command.getName().equals("sellHand") ){
                ShopOpperations.sellHand( player );
                SaveData.saveMarket();
            }

            if ( command.getName().equals("infoHand")){
                ShopOpperations.infoHand( player );
            }

            if ( command.getName().equals("cost") ) {
                if ( args.length == 0 ) return false;
                if (SaveData.validItem(args[0], player)) {
                    int amount = (args.length == 1) ? 1 : castInt(args[1], player);
                    if (amount > 0) {
                        ShopOpperations.cost(player, args[0], amount);
                    }
                }
            }
        }

        return  true;
    }

}

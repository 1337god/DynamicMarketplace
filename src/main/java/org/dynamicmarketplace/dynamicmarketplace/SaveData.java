package org.dynamicmarketplace.dynamicmarketplace;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class SaveData {

    private static File dir = new File("plugins/DynamicMarket/" );
    private static File marketFile = new File("plugins/DynamicMarket/market.txt" );
    private static File settingsFile = new File("plugins/DynamicMarket/settings.txt" );
    private static File recipesFile = new File("plugins/DynamicMarket/recipes.txt" );

    private static HashMap< String, Double > marketQuantities = new HashMap < String, Double> ();
    private static HashMap< String, String > itemRecipes = new HashMap <String, String>();
    private static HashMap< String, Double > craftingCosts = new HashMap <String, Double>();

    public static void init () {

        if ( ! dir.exists() ) dir.mkdir();
        if ( ! marketFile.exists() ) createFile(marketFile);
        if ( ! settingsFile.exists() ) createFile(settingsFile);
        if ( ! recipesFile.exists() ) createFile(recipesFile);

        loadMarket();
        loadRecipes();
        loadSettings();

    }

    private static void createFile ( File file ){
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file.getAbsoluteFile(), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        writer.close();
    }

    private static String[] loadFile ( File file) {
        Scanner scanner = null;

        try { scanner = new Scanner( file ); }
        catch (FileNotFoundException e) { DynamicMarketplace.throwError(); }

        List<String> lines = new ArrayList<String>();
        while (scanner.hasNextLine()) {
            String next = scanner.nextLine();
            if (next.length() < 5 ) continue;
            if (next.charAt(0) == '#' ) continue;
            lines.add(next);
        }

        return lines.toArray(new String[0]);

    }

    public static void loadMarket ( ){

        String[] marketData = loadFile( marketFile );
        marketQuantities = new HashMap < String, Double> ();

        for ( int i = 0 ; i < marketData.length ; i ++ ){
            String[] parts = marketData[i].split(": " );
            String name = parts[0];
            Double items =  Double.parseDouble( parts[1] );
            marketQuantities.put( name, items );
        }
    }

    public static void loadSettings ( ) {

        String[] settings = loadFile( settingsFile );
        craftingCosts = new HashMap < String, Double> ();

        for ( int i = 0 ; i < settings.length ; i ++ ){
            String[] parts = settings[i].split(": " );
            String name = parts[0];
            Double percent = Double.parseDouble( parts[1] );
            craftingCosts.put( name, percent );
        }
    }

    public static void loadRecipes ( ) {

        String[] recipes = loadFile( recipesFile );
        itemRecipes = new HashMap < String, String> ();

        for ( int i = 0 ; i < recipes.length ; i ++ ){
            String[] parts = recipes[i].split(": " );
            itemRecipes.put( parts[0], parts[1] );
        }
    }

    public static boolean validItem ( String name, Player player) {
        if (  marketQuantities.containsKey( name ) )
            return  true;
        if ( itemRecipes.containsKey( name ))
            return true;
        return false;
    }

    public static boolean validItem ( String name ) {
        if (  marketQuantities.containsKey( name ) )
            return  true;
        if ( itemRecipes.containsKey( name ))
            return true;
        return false;
    }

    public static double getFullPrice ( String validItem, double quantity, boolean selling){
        double price = getPrice(validItem, quantity, selling);
        if ( ! selling )
            return 10 * price * getTax();
        else
            return 10 * price * ( 1 / getTax() );
    }

    private static Double getPrice ( String validItem, double quantity, boolean selling ){

        // Item is a base item
        if ( marketQuantities.containsKey( validItem ) ){
            double marketCount = marketQuantities.get( validItem );

            if ( selling ) {
                double nextCount = marketCount + quantity;
                return craftingCosts.get("scaleFactor") * (Math.log(nextCount) - Math.log(marketCount));
            }
            else {
                double nextCount = marketCount - quantity;
                if ( nextCount <= 0)
                    return -1.0;
                return craftingCosts.get("scaleFactor") * (Math.log(marketCount) - Math.log(nextCount));
            }
        }

        // Item is not a base item
        else {

            String[] recipe = itemRecipes.get( validItem ).split("(, | )");

            double cost = 0;
            double multiplier = craftingCosts.get( "crafting" );

            for ( int i = 0 ; i < recipe.length; i += 2) {
                if ( craftingCosts.containsKey( recipe[i] ) )
                    multiplier = craftingCosts.get( recipe[i] );
                else {
                    double sub_price = getPrice(recipe[i], quantity * Double.parseDouble(recipe[i + 1]), selling);
                    if ( sub_price < 0 )
                        return -1.0;
                    cost += sub_price;
                }
            }
            return cost * multiplier ;
        }
    }

    public static void updateQuantities ( String validItem, double amount, boolean sell ) {

        // Item is a base item
        if ( marketQuantities.containsKey( validItem ) ){
            double marketCount = marketQuantities.get( validItem );
            if ( sell )
                marketQuantities.replace( validItem, marketCount + amount );
            else
                marketQuantities.replace( validItem, marketCount - amount );
        }

        // Item is not a base item
        else {

            String[] recipe = itemRecipes.get( validItem ).split("(, | )");

            for ( int i = 0 ; i < recipe.length; i += 2)
                if ( !craftingCosts.containsKey( recipe[i] ) )
                    updateQuantities( recipe[i], amount * Double.parseDouble(recipe[i + 1]), sell );
        }
    }

    public static double marketQuantity ( String validItem ){
        if ( marketQuantities.containsKey( validItem ))
            return marketQuantities.get( validItem );
        else
            return -1;
    }

    public static double getTax (){
        if ( !craftingCosts.containsKey("tax"))
            return 1;
        return craftingCosts.get("tax");
    }

    public static void saveMarket () {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(marketFile.getAbsoluteFile(), "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        for (HashMap.Entry<String, Double> entry : marketQuantities.entrySet()){
            writer.write( entry.getKey() + ": " + entry.getValue() + "\n");
        }
        writer.close();

    }

}

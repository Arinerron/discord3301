package core;

import java.util.*;
import de.btobastian.javacord.*;
import de.btobastian.javacord.entities.*;
import de.btobastian.javacord.listener.*;
import de.btobastian.javacord.listener.server.*;
import de.btobastian.javacord.listener.message.*;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;

public abstract class Module {
    private DiscordBot bot = null;

    public Module(DiscordBot bot) {
        this.bot = bot;
    }

    /* returns the DiscordBot object */
    public final DiscordBot getDiscordBot() {
        return this.bot;
    }

    /* return the help menu for the given alias, or null */
    public String[] getHelp(String alias) {return null;}

    /* not required-- returns aliases of module */
    public String[] getAliases() { return new String[] {}; }

    /* return the name of the module */
    public abstract String getName();

    /* runs on initialization */
    public abstract void onInit();

    /* runs when a command is executed. return the response, or null. */
    public abstract String onCommand(String command, String[] args);
    public String onCommand(String command, String[] args, Message message) {return null;}

    /* check if strings are in a string */
    protected static boolean in(String main, String... stuff) {
        return Arrays.asList(stuff).contains(main.toLowerCase());
    }

    /* shows a help menu */
    protected String showHelp(String item) {
        String[] data = this.getHelp(item.toLowerCase());
        if(data != null)
            return data[1] + "\n\n    " + this.getDiscordBot().getPrefix() + " " + item + " " + data[0].trim();
        else
            return "Error: Failed to find help menu for alias \"" + item + "\" of module \"" + this.getName() + "\"";
    }

    /* combine strings to form one single string */
    public static String merge(String[] array) {
        return merge(array, 0);
    }

    /* do it again, but with an index */
    public static String merge(String[] array, int pos) {
        StringBuilder b = new StringBuilder();
        for(int i = pos; i < array.length; i++)
            b.append(array[i]).append((i == array.length - 1 ? "" : " "));
        return b.toString();
    }
}

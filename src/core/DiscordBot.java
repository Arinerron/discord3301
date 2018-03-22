package core;

import modules.*;
import org.json.*;
import com.google.common.util.concurrent.*;
import de.btobastian.javacord.*;
import de.btobastian.javacord.entities.*;
import de.btobastian.javacord.listener.*;
import de.btobastian.javacord.listener.server.*;
import de.btobastian.javacord.listener.message.*;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import java.util.*;
import java.io.*;

public class DiscordBot {
    private Module[] modules = {};
    private DiscordAPI api = null;
    private List<Channel> channels = new ArrayList<>();
    private String prefix = "!3301";

    public DiscordBot() {
        CryptoOps.init();

        /* If you want to include any other modules, add them here */
        this.modules = new Module[] {
            new ModuleCore(this),
            new ModuleOperations(this),
            new ModuleHash(this),
            new ModuleForensics(this),
            new ModuleUpdate(this)
        };

        Logger.report(Status.INFO, "Connecting to servers...");

        this.api = Javacord.getApi(this.getToken(), true);
        this.prefix = this.getConfig().getString("prefix").trim();

        api.connect(new FutureCallback<DiscordAPI>() {
            @Override
            public void onSuccess(DiscordAPI api) {
                /* parse channels file */
                try {
                    String file = "../../channels.txt";
                    try(BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
                        for(String line; (line = br.readLine()) != null; ) {
                            Channel c = api.getChannelById(line.trim());
                            if(c != null)
                                channels.add(c);
                            else
                                Logger.report(Status.WARNING, "Channel `" + line.trim() + "` is null");
                        }
                    }
                } catch(Exception e) {
                    Logger.report(Status.WARNING, "Failed to parse channels file");
                    e.printStackTrace();
                }

                /* register message handler event listener */
                api.registerListener(new MessageCreateListener() {
                    @Override
                    public void onMessageCreate(DiscordAPI api, Message message) {
                        try {
                            final String response = handle(message, message.getContent());
                            final int chunksize = 2000;

                            if(response != null) {
                                if(response.length() >= chunksize) {
                                    List<String> strings = new ArrayList<String>();
                                    int index = 0;

                                    while (index < response.length()) {
                                        strings.add(response.substring(index, Math.min(index + chunksize, response.length())));
                                        index += chunksize;
                                    }

                                    for(String s : strings)
                                        message.reply("```\n" + s + "\n```");
                                } else {
                                    message.reply("```\n" + response + "\n```");
                                }
                            }
                        } catch(Exception e) {
                            Logger.report(Status.ERROR, "Failed to handle command.");
                            e.printStackTrace();
                        }
                    }
                });

                /* register listener for joining new servers */
                api.registerListener(new ServerJoinListener() {
                    public void onServerJoin(DiscordAPI api2, Server server) {
                        Logger.report(Status.INFO, "Joined new server " + server.getName() + " [" + server.getMemberCount() + " members]");
                    }
                });

                /* initmods */
                Logger.report(Status.INFO, "Initializing modules...");
                for(Module module : modules) {
                    try {
                        module.onInit();
                    } catch(Exception e) {
                        Logger.report(Status.WARNING, "Failed to initialize module `" + module.getName() + "`:");
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });
    }

    protected String handle(String m) {
        return this.handle(null, m);
    }

    protected String handle(Message message, String m) {
        if(m.trim().toLowerCase().startsWith(this.getPrefix().toLowerCase())) {
            try {
                if(message != null)
                    message.getReceiver().type();
            } catch(Exception e) {
                e.printStackTrace();
            }

            String[] split = m.trim().split(" ");
            String main = split[0].toLowerCase();
            String ret = null;

            if(split.length == 1) {
                ret = "Displaying help menu...\n" + this.handle(this.getPrefix() + " help");
            } else {
                String subcommand = split[1].toLowerCase();

                for(Module mod : this.getModules()) {
                    if(Arrays.asList(mod.getAliases()).contains(subcommand)) {
                        int n = split.length - 2;
                        String[] truncated = new String[n];
                        System.arraycopy(split, 2, truncated, 0, n);

                        try {
                            ret = mod.onCommand(subcommand, truncated);
                            if(ret == null)
                                ret = mod.onCommand(subcommand, truncated, message);
                        } catch(Exception e) {
                            ret = "Fatal Error: " + e.getMessage() + "\nPlease contact the developer @arinerron#5687!";

                            Logger.report(Status.ERROR, "Failed to handle command.");
                            e.printStackTrace();
                        }

                        return ret;
                    }
                }

                ret = "Error: That subcommand does not exist. Try `" + this.getPrefix() + " help`.";
            }

            return ret; // might be able to remove?
        }

        return null;
    }

    public String getToken() {
        return this.getConfig().getString("token");
    }

    public String getPrefix() {
        return this.prefix;
    }

    public JSONObject getConfig() {
        return Config.getInstance().getData();
    }

    public DiscordAPI getDiscordAPI() {
        return this.api;
    }

    public Module[] getModules() {
        return this.modules;
    }
}

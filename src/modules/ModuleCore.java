package modules;

import core.*;

public class ModuleCore extends Module {
    public ModuleCore(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onInit() {}

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public String[] getHelp(String alias) {
        if(in(alias, "h", "help", "?")) {
            return new String[] {"", "Displays this help menu"};
        }

        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] {"help"};
    }

    @Override
    public String onCommand(String cmd, String[] args) {
        if(in(cmd, "help")) {
            StringBuilder b = new StringBuilder();

            for(Module m : this.getDiscordBot().getModules()) {
                for(String alias : m.getAliases()) {
                    String[] data = m.getHelp(alias.toLowerCase());

                    if(data != null && data.length == 2) {
                        b.append("`" + this.getDiscordBot().getPrefix() + " " + alias + " " + data[0].trim() + (data[0].trim().length() == 0 ? "" : " ") + "` - " + data[1] + "\n");
                    }
                }
            }

            return b.toString();
        }

        return null;
    }
}

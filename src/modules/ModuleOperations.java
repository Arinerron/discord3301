package modules;

import core.*;
import java.math.*;

public class ModuleOperations extends Module {
    public ModuleOperations(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onInit() {}

    @Override
    public String getName() {
        return "Operations";
    }

    @Override
    public String[] getHelp(String alias) {
        if(in(alias, "h", "help")) {
            return new String[] {"", "Displays this help menu"};
        } else if(in(alias, "subscribe")) {
            return new String[] {"", "Subscribe current channel to updates on websites hosted by cicada3301"};
        } else if(in(alias, "unsubscribe")) {
            return new String[] {"", "Unsubscribe current channel"};
        } else if(in(alias, "uppercase")) {
            return new String[] {"<string>", "Converts the string to uppercase"};
        } else if(in(alias, "lowercase")) {
            return new String[] {"<string>", "Converts the string to lowercase"};
        } else if(in(alias, "reverse")) {
            return new String[] {"<string>", "Reverses the characters in the string"};
        } else if(in(alias, "length")) {
            return new String[] {"<string>", "Counts the number of characters in the string"};
        } else if(in(alias, "base64")) {
            return new String[] {"<encode/decode> <string>", "Encodes/decodes the string with base64"};
        } else if(in(alias, "hex")) {
            return new String[] {"<encode/decode> <string>", "Encodes/decodes the string with hex"};
        } else if(in(alias, "binary")) {
            return new String[] {"<encode/decode> <string>", "Encodes/decodes the string with binary"};
        } else if(in(alias, "rot")) {
            return new String[] {"<amount/all> <string>", "Encodes/decodes the string with the Caesar cipher (aka: rot)"};
        } else if(in(alias, "atbash")) {
            return new String[] {"<string>", "Encodes/decodes the string with the Atbash cipher"};
        } else if(in(alias, "morse")) {
            return new String[] {"<encode/decode> <string>", "Encodes/decodes the string with Morse code"};
        } else if(in(alias, "phi")) {
            return new String[] {"<number>", "Calculates ϕ(number)"};
        } else if(in(alias, "prime")) {
            return new String[] {"<number>", "Check whether or not the given number is prime"};
        }

        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] {"uppercase", "lowercase", "reverse", "base64", "hex", "morse", "rot", "atbash", "phi", "prime"}; // removed: , "binary"
    }

    @Override
    public String onCommand(String cmd, String[] args) {
        if(in(cmd, "uppercase", "upper")) {
            if(args.length != 0) {
                return merge(args).toUpperCase();
            } else {
                return showHelp("uppercase");
            }
        } else if(in(cmd, "lowercase", "lower")) {
            if(args.length != 0) {
                return merge(args).toLowerCase();
            } else {
                return showHelp("lowercase");
            }
        } else if(in(cmd, "reverse")) {
            if(args.length != 0) {
                return CryptoOps.reverse(merge(args));
            } else {
                return showHelp("reverse");
            }
        } else if(in(cmd, "length", "bytes", "count")) {
            if(args.length != 0) {
                return "That string contains " + merge(args).length() + " characters";
            } else {
                return showHelp("length");
            }
        } else if(in(cmd, "base64")) {
            if(args.length > 1) {
                if(in(args[0], "encode", "encrypt", "en"))
                    return CryptoOps.base64_encode(merge(args, 1));
                else if(in(args[0], "decode", "decrypt", "de"))
                    return CryptoOps.base64_decode(merge(args, 1));
                else
                    return "Error: Unknown subcommand \"" + args[0].toLowerCase() + "\" of \"" + cmd.toLowerCase() + "\"";
            } else {
                return showHelp("base64");
            }
        } else if(in(cmd, "hex", "hexadecimal")) {
            if(args.length > 1) {
                if(in(args[0], "encode", "encrypt", "en"))
                    return CryptoOps.hex_encode(merge(args, 1));
                else if(in(args[0], "decode", "decrypt", "de"))
                    return CryptoOps.hex_decode(merge(args, 1));
                else
                    return "Error: Unknown subcommand \"" + args[0].toLowerCase() + "\" of \"" + cmd.toLowerCase() + "\"";
            } else {
                return showHelp("hex");
            }
        } else if(in(cmd, "binary")) {
            /*if(args.length > 1) {
                if(in(args[0], "encode", "encrypt", "en"))
                    return CryptoOps.binary_encode(merge(args));
                else if(in(args[0], "decode", "decrypt", "de"))
                    return CryptoOps.binary_decode(merge(args));
                else
                    return "Error: Unknown subcommand \"" + args[0].toLowerCase() + "\" of \"" + cmd.toLowerCase() + "\"";
            } else {
                return showHelp("binary");
            }*/

            return "Error: This feature is currently in development.";
        } else if(in(cmd, "morse")) {
            if(args.length > 1) {
                if(in(args[0], "encode", "encrypt", "en"))
                    return CryptoOps.morse_encode(merge(args, 1));
                else if(in(args[0], "decode", "decrypt", "de"))
                    return CryptoOps.morse_decode(merge(args, 1));
                else
                    return "Error: Unknown subcommand \"" + args[0].toLowerCase() + "\" of \"" + cmd.toLowerCase() + "\"";
            } else {
                return showHelp("morse");
            }
        } else if(in(cmd, "prime", "isprime", "is-prime", "checkprime", "check-prime")) {
            if(args.length != 0) {
                StringBuilder b = new StringBuilder();

                for(int i = 0; i < args.length; i++) {
                    try {
                        String parse = args[i];
                        BigInteger bigint = new BigInteger(parse);
                        if(parse.length() > 12)
                            b.append("Integer " + parse + " is " + ((bigint).isProbablePrime(5) ? "probably " : "not ") + "prime\n");
                        else
                            b.append("Integer " + parse + " is " + (CryptoOps.isPrime(bigint) ? "" : "not ") + "prime\n");
                    } catch(Exception e) {
                        e.printStackTrace();
                        if(args[i].toLowerCase().contains("rib")) {
                            return "...\n```\nhttp://i.imgur.com/YyhAfNC.gif\n```\nNo, it's not prime ribs :(";
                        } else {
                            b.append("Error: Failed to parse integer \"" + args[i] + "\"\n");
                        }
                    }
                }

                return b.toString();
            } else {
                return showHelp("prime");
            }
        } else if(in(cmd, "phi", "ϕ")) {
            if(args.length != 0) {
                try {
                    return "ϕ(" + args[0] + ") = " + CryptoOps.phi(Integer.parseInt(args[0]));
                } catch(Exception e) {
                    Logger.report(Status.ERROR, "Failed to calculate phi of " + args[0]);
                    e.printStackTrace();

                    return "Error: Failed to calculate phi of " + args[0];
                }
            } else {
                return showHelp("phi");
            }
        } else if(in(cmd, "rot", "shift", "caesar", "ceasar", "caeser", "ceaser", "caesarcipher")) { // some people don't know how to spell :p
            try {
                if(args.length > 1) {
                    if(args[0].equalsIgnoreCase("all")) {
                        String merged = merge(args, 1);
                        StringBuilder b = new StringBuilder();

                        for(int i = 0; i < 26; i++) {
                            b.append("rot-" + i + " = " + CryptoOps.rot(merged, i) + "\n");
                        }

                        return b.toString();
                    } else
                        return CryptoOps.rot(merge(args, 1), Integer.parseInt(args[0]));
                } else
                    return showHelp("rot");
            } catch(Exception e) {
                return "Error: Failed to rot encode.\n\n" + showHelp("rot");
            }
        } else if(in(cmd, "atbash")) {
            if(args.length != 0) {
                return CryptoOps.atbash(merge(args));
            } else
                showHelp("atbash");
        }

        return null;
    }
}

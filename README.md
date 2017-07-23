# About
Automated Discord notifications when cicada3301's websites are updated.

# How to set up
1. Clone this repository.
```
git clone https://github.com/Arinerron/discord3301/
```

2. Copy the file `Config.java.example` to `Config.java`
3. Paste your notification endpoint from Discord between the quotation marks in the variable `DISCORD_URL`.
4. Save the file. You're ready to run the bot now.
5. To run the bot, execute `sh run.sh` on any linux system with bash. If you don't have bash, just execute the following...
```
javac Main.java
java Main
```

# Debugging
Getting an error similar to this?

```
Main.java:125: error: cannot find symbol
            URL url = new URL(Config.DISCORD_URL);
                              ^
  symbol:   variable Config
  location: class Main
2 errors
```

That means you forgot to configure the file `Config.java`, or the file `Config.java` does not exist

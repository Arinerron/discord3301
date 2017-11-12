package modules;

import core.*;
import org.apache.commons.lang3.StringEscapeUtils;
import com.google.common.util.concurrent.FutureCallback;
import de.btobastian.javacord.*;
import de.btobastian.javacord.entities.message.Message;
import de.btobastian.javacord.entities.*;
import de.btobastian.javacord.listener.*;
import de.btobastian.javacord.listener.server.*;
import de.btobastian.javacord.listener.message.MessageCreateListener;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;
import java.security.MessageDigest;
import java.math.*;
import java.util.*;
import java.util.regex.*;
import org.apache.http.client.*;
import org.apache.http.client.entity.*;
import org.apache.http.util.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.message.BasicNameValuePair;
import java.net.*;
import java.io.*;
import java.util.stream.Collectors;
import org.json.*;
import org.apache.commons.io.FileUtils;
import java.nio.*;
import java.nio.file.*;
import java.nio.charset.*;
import org.apache.http.entity.StringEntity;
import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.io.*;
import java.nio.charset.*;
import java.text.*;

public class ModuleUpdate extends Module {
    public static List<Channel> channels = new ArrayList<>();

    public ModuleUpdate(DiscordBot bot) {
        super(bot);
    }

    public List<Channel> getChannels() {
        return this.channels;
    }

    @Override
    public void onInit() {
        try {
            String file = "../channels.txt";
            try(BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
                for(String line; (line = br.readLine()) != null; ) {
                    Channel c = this.getDiscordBot().getDiscordAPI().getChannelById(line.trim());
                    if(c != null)
                        channels.add(c);
                }
            }
        } catch(Exception e) {
            Logger.report(Status.ERROR, "Failed to parse sites list file!");
            e.printStackTrace();
        }

        try {
            String file = "../sites.txt";
            try(BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
                for(String line; (line = br.readLine()) != null; ) {
                    if(!line.trim().startsWith("#") && line.length() != 0) { // is it a comment?
                        String[] split = line.split(Pattern.quote(" "));
                        if(split.length < 2)
                            new Listener(this, split[0], 3);
                        else {
                            new Listener(this, split[0], Integer.parseInt(split[1]));
                        }
                    }
                }
            }
        } catch(Exception e) {
            Logger.report(Status.ERROR, "Failed to parse sites list file!");
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "Update";
    }

    @Override
    public String[] getHelp(String alias) {
        if(in(alias, "subscribe")) {
            return new String[] {"", "Subscribe current channel to updates on websites hosted by cicada3301"};
        } else if(in(alias, "unsubscribe")) {
            return new String[] {"", "Unsubscribe current channel"};
        }

        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] {"subscribe", "unsubscribe"};
    }

    @Override
    public String onCommand(String cmd, String[] args) {return null;}

    @Override
    public String onCommand(String cmd, String[] args, Message message) {
        if(in(cmd, "subscribe")) {
            if(message.isPrivateMessage()) {
                return "Sorry, this feature does not work for private messaging.";
            } else {
                try {
                    Channel c = message.getChannelReceiver();
                    boolean contains = false;
                    for(Channel c2 : this.getChannels())
                        if(c != null && c2.getId() == c.getId())
                            contains = true;
                    if(contains) {
                        return "It looks like this channel is already subscribed to updates.";
                    } else {
                        channels.add(c);
                        Logger.report(Status.INFO, "Subscribed channel #" + c.getName() + " in " + c.getServer().getName());
                        write();
                        c.sendMessage("Success! This channel has been subscribed to updates on cicada 3301 websites.");
                    }
                } catch(Exception e) {
                    Logger.report(Status.ERROR, "Failed to subscribe from channel.");
                    e.printStackTrace();
                    return "Uh oh, looks like something went wrong.";
                }
            }
        } else if(in(cmd, "unsubscribe")) {
            if(message.isPrivateMessage()) {
                return "Sorry, this feature does not work for private messaging.";
            } else {
                try {
                    Channel c = message.getChannelReceiver();
                    c.sendMessage("Success! This channel has been unsubscribed.");
                    channels.remove(c);
                    Logger.report(Status.INFO, "Unsubscribed channel #" + c.getName() + " in " + c.getServer().getName());
                    write();
                } catch(Exception e) {
                    Logger.report(Status.ERROR, "Failed to unsubscribe from channel.");
                    e.printStackTrace();
                    return "Uh oh, looks like something went wrong.";
                }
            }
        }

        return null;
    }

    /* write channels to file */
    public static void write() {
        try {
            StringBuilder b = new StringBuilder();
            List<String> ids = new ArrayList<>();
            for(Channel c : channels)
                if(c != null && !ids.contains(c.getId() + ""))
                    ids.add(c.getId() + "");

            for(String s : ids)
                    b.append(s + "\n");

            String str = b.toString();
            File newTextFile = new File("../channels.txt");

            FileWriter fw = new FileWriter(newTextFile);
            fw.write(str);
            fw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class Listener {
    protected static String REGEX_URL = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    protected List<String> URLS = new ArrayList<>();

    private ModuleUpdate update = null;
    private String SITE_HASH = "";
    private boolean ONLINE = true;
    private String url = "";
    private int minutes = 3;

    public String getURL() {
        return this.url;
    }

    public Listener(ModuleUpdate update, String url, int mins) {
        this.update = update;
        this.url = url;
        this.minutes = mins;

        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {

                String response = http2GET(getURL());

                if(SITE_HASH.length() != 0) {
                    if(!SITE_HASH.equals(MD5(response))) {
                        generatePayload("**Alert:** The website " + getURL() + " has been updated!");
                    }
                }

                SITE_HASH = MD5(response);

                List<String> newurls = new ArrayList<>();

                for(String s : scrapeURL(response)) {
                    if(!s.startsWith(getURL()) && !s.startsWith("http"))
                        s = getURL() + s;

                    newurls.add(s);
                }

                if(URLS.size() != 0) {
                    for(String s : newurls) {
                        if(!URLS.contains(s)) {
                            String ending = getType(s.substring(s.lastIndexOf('.') + 1).trim());
                            String msg = "**Alert:** New " + (ending.length() == 0 ? "" : ending + " ") + "file uploaded at " + s;
                            generatePayload(msg);
                            Logger.report(Status.WARNING, msg);
                        }
                    }
                }

                URLS = newurls;
            }
        };

        timer.schedule(hourlyTask, 0, 1000 * 60 * this.getMinutes());
    }

    /* generates the payload to send as message */
    public void generatePayload(String message) { // send a message to the discord server
        for(Channel c : this.getModuleUpdate().getChannels())
            if(c != null)
                try {
                    Logger.report(Status.INFO, message);
                    c.sendMessage(message);
                } catch(Exception e) {
                    Logger.report(Status.ERROR, "Message failed to send.");
                    e.printStackTrace();
                }
    }

    /* returns the domain name of a url */
    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public String http2GET(String urlToRead) { // perform a GET request
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            HttpURLConnection conn = (HttpURLConnection) con;
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line + "\n");
            }
            rd.close();

            if(!ONLINE)
                generatePayload("**Alert:** " + urlToRead + " is now online!");
            ONLINE = true;

            return result.toString();
        } catch(Exception e) {
            if(ONLINE)
                generatePayload("**Alert:** " + urlToRead + " is now offline!");
            ONLINE = false;
            e.printStackTrace();
            return "";
        }
    }

    /* gets a list of urls on the page */
    public static List<String> scrapeURL(String data) {
        List<String> urls = new ArrayList<>();

        Pattern pattern = Pattern.compile(REGEX_URL);
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            urls.add(matcher.group());
        }

        String[] data2 = data.split(Pattern.quote("src=\""));
        int i = 0;
        for(String s : data2) {
            i++;
            if(i != 1)
                urls.add(s.split(Pattern.quote("\""))[0]);
        }

        data2 = data.split(Pattern.quote("href=\""));
        i = 0;
        for(String s : data2) {
            i++;
            if(i != 1)
                urls.add(s.split(Pattern.quote("\""))[0]);
        }

        List<String> urls2 = urls.stream().distinct().collect(Collectors.toList());

        return urls2;
    }

    /* performs an http get request on the given url */
    public static String httpGET(String url) {
        url = url.trim();
        if(url.toLowerCase().startsWith("javascript:") || url.toLowerCase().startsWith("file:") || url.toLowerCase().startsWith("data:"))
            return "Error: Access denied";
        if(!url.startsWith("http"))
            url = "http://" + url;
        try {
            String host = getDomainName(url).toLowerCase();
            if(host.contains("localhost") || host.contains("192.168") || host.contains("127.0"))
                return "Error: Access denied";
        } catch(Exception e) {
            e.printStackTrace();
            return "Error: Access denied";
        }

        try {
            HttpGet request = new HttpGet(url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String entityContents = EntityUtils.toString(entity);
            return entityContents;
        } catch(Exception e) {
            e.printStackTrace();
            return "Error: Failed to fetch webpage";
        }
    }

    /* tries to identify the filetype */
    public static String getType(String s) {
        switch(s.toLowerCase()) {
            case "wav":
                return "audio";
            case "mp3":
                return "audio";
            case "jpg":
                return "image";
            case "png":
                return "image";
            case "mov":
                return "movie";
            case "mp4":
                return "movie";
            case "zip":
                return "archive";
            case "gz":
                return "archive";
            case "txt":
                return "text";
            case "mkv":
                return "movie";
            default:
                return "";
        }
    }

    public String MD5(String md5) { // collisions and security do not matter in this instance
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
              sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
           }

           return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return "";
    }

    public ModuleUpdate getModuleUpdate() {
        return this.update;
    }

    public int getMinutes() {
        return this.minutes;
    }
}

import java.util.*;
import java.util.regex.*;
import java.net.*;
import java.io.*;
import java.nio.charset.*;
import java.text.*;

public class Main {
    protected static String REGEX_URL = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    protected String SITE_URL = ""; // ignore this, this is not how you configure it
    protected String SITE_HASH = "";

    protected List<String> URLS = new ArrayList<>();
    protected boolean ONLINE = true;

    public static void main(String[] args) {
        interpret("sites.txt"); // read the list of sites
        log("Running."); // tell the user that the bot is running
    }

    public static void interpret(String file) { // read a the file sites.txt and run a new instance for each site to monitor
        try {
            try(BufferedReader br = new BufferedReader(new FileReader(new File(file)))) {
                for(String line; (line = br.readLine()) != null; ) {
                    if(!line.trim().startsWith("#") && line.length() != 0) { // is it a comment?
                        String[] split = line.split(Pattern.quote(" "));
                        if(split.length < 2)
                            new Main(split[0], 3);
                        else {
                            new Main(split[0], Integer.parseInt(split[1]));
                        }
                    }
                }
            }
        } catch(Exception e) {
            log("Failed to parse sites list file!");
            e.printStackTrace();
        }
    }

    public Main(String siteurl, int mins) {
        SITE_URL = siteurl;

        // generatePayload("Bot is now online. Checking for updates every 30 minutes...");

        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {

                String response = httpGET(SITE_URL);

                if(SITE_HASH.length() != 0) {
                    if(!SITE_HASH.equals(MD5(response))) {
                        generatePayload("**Alert:** The website " + SITE_URL + " has been updated!");
                    }
                }

                SITE_HASH = MD5(response);

                List<String> newurls = new ArrayList<>();

                for(String s : scrapeURL(response)) {
                    if(!s.startsWith(SITE_URL) && !s.startsWith("http"))
                        s = SITE_URL + s;

                    newurls.add(s);
                }

                if(URLS.size() != 0) {
                    for(String s : newurls) {
                        if(!URLS.contains(s)) {
                            String ending = getType(s.substring(s.lastIndexOf('.') + 1).trim());
                            String msg = "**Alert:** New " + (ending.length() == 0 ? "" : ending + " ") + "file uploaded at " + s;
                            generatePayload(msg);
                            log(msg);
                        }
                    }
                }

                URLS = newurls;
            }
        };

        timer.schedule(hourlyTask, 0, 1000 * 60 * mins);
    }

    public static String getType(String ending) { // figure out what type of file it is
        switch(ending.toLowerCase()) {
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
            default:
                return "";
        }
    }

    public String httpGET(String urlToRead) { // perform a GET request
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

    public static void log(String args) { // simply write a message to console
        System.out.println("[" + new SimpleDateFormat("MM/dd HH:mm").format(new Date()) + "] " + args);
    }

    public void generatePayload(String message) { // send a message to the discord server
        for(String url2 : Config.DISCORD_URL)
            try {
                URL url = new URL(url2);
                URLConnection con = url.openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
                HttpURLConnection http = (HttpURLConnection)con;
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                Map<String,String> arguments = new HashMap<>();
                arguments.put("content", message);
                StringJoiner sj = new StringJoiner("&");
                for(Map.Entry<String,String> entry : arguments.entrySet())
                    sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "="
                         + URLEncoder.encode(entry.getValue(), "UTF-8"));
                byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
                int length = out.length;
                http.setFixedLengthStreamingMode(length);
                http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                http.connect();
                try(OutputStream os = http.getOutputStream()) {
                    os.write(out);
                }
            } catch(Exception e) {
                e.printStackTrace();
                System.err.println("[ERROR] Message was: " + message);
            }
    }

    public List<String> scrapeURL(String data) { // scrape URLs from the page
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

        return urls;
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
}
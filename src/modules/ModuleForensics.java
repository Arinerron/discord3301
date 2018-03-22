package modules;

import core.*;
import java.util.*;
import org.apache.commons.lang3.StringEscapeUtils;
import java.math.*;
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

public class ModuleForensics extends Module {
    public ModuleForensics(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onInit() {}

    @Override
    public String getName() {
        return "Forensics";
    }

    @Override
    public String[] getHelp(String alias) {
        /*if(in(alias, "forensics", "analyze", "analyse")) {
            return new String[] {"<url>", "Displays information about an audio or image file"};
        } else */if(in(alias, "solve-cryptogram", "cryptogram")) {
            return new String[] {"<string>", "Attempts to solve cryptograms using the dictionary"};
        } else if(in(alias, "exif", "metadata")) {
            return new String[] {"<url>", "Displays EXIF data (metadata) in a JPEG image"};
        } else if(in(alias.replace("-", ""), "readqr", "qr", "qrcode", "readqrcode", "scanqr", "scanqrcode")) {
            return new String[] {"<url>", "Attempts to read the qr code image at the given url"};
        } else if(in(alias, "ascii-art", "asciiart", "art")) {
            return new String[] {"<string>", "Generates ASCII art out of the given string"};
        }

        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] {"exif", "read-qr", "solve-cryptogram", "ascii-art"};
    }

    @Override
    public String onCommand(String cmd, String[] args) {
        if(in(cmd, "exif", "metadata")) {
            if(args.length != 0) {
                return exif(merge(args));
            } else
                return showHelp("exif");
        } else if(in(cmd, "solve-cryptogram", "cryptogram")) {
            if(args.length != 0) {
                return "Best results:\n" + solve(merge(args));
            } else
                return showHelp("solve-cryptogram");
        } else if(in(cmd.replace("-", ""), "readqr", "qr", "qrcode", "readqrcode", "scanqr", "scanqrcode")) {
            if(args.length != 0) {
                String merged = merge(args);
                String content = httpGET("http://api.qrserver.com/v1/read-qr-code/?fileurl=" + URLEncoder.encode(merged));
                if(content.contains("en: malformed") || content.contains("Error: Failed to ") || content.contains("download error (could not establish connection)"))
                    return "Error: Failed to read QR code";
                else {
                    StringBuilder b = new StringBuilder();

                    JSONArray o = new JSONArray(content);
                    for (int i = 0; i < o.length(); i++) {
                        JSONObject obj = o.getJSONObject(i);
                        JSONArray name = obj.getJSONArray("symbol");
                        for (int ix = 0; ix < name.length(); ix++) {
                            JSONObject obj2 = name.getJSONObject(ix);
                            String data = obj2.getString("data");
                            b.append(data + "\n");
                        }
                    }

                    return b.toString();
                }
            } else
                return showHelp("read-qr");
        } else if(in(cmd, "ascii-art", "asciiart", "art")) {
            if(args.length != 0) {
                return getASCIIArt(merge(args));
            } else
                return showHelp("ascii-art");
        }

        return null;
    }

    /* do get request */
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

    /* returns the domain name as string */
    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    /* super messy code ik */
    public static String exif(String url) {
        try {
            url = url.trim();
            if(url.toLowerCase().startsWith("javascript:") || url.toLowerCase().startsWith("file:") || url.toLowerCase().startsWith("data:"))
                return null;
            if(!url.startsWith("http"))
                url = "http://" + url;

            String content = httpGET("http://www.verexif.com/en/ver.php?foto_file=&foto_url=" + URLEncoder.encode(url));
            if(content.contains("<div id=\"datos_foto\">")) {
                content = content.split(Pattern.quote("<div id=\"datos_foto\">"))[1].split(Pattern.quote("<strong id=\"thumbnail\">"))[0].replace("</dt><dd>", "  ").replace("</dd><dt>", "\n").replace("<dl style=\"min-height:200px\"><dt>", "").replace("</dl>", "").replace("</dd>", "").replace(" :   ", ": ");
                return content;
            } else {
                return "Error: Failed to fetch EXIF data";
            }
        } catch(Exception e) {
            e.printStackTrace();
            return "Error: Failed to fetch image";
        }
    }

    /* pointlessness */
    public static String getASCIIArt(String s) {
        final String font = "computer";
        try { // fonts: pebbles, computer
            return StringEscapeUtils.unescapeHtml4(httpGET("http://www.network-science.de/ascii/ascii.php?TEXT=" + URLEncoder.encode(s) + "&x=43&y=4&FONT=pebbles&RICH=no&FORM=left&STRE=no&WIDT=70").split(Pattern.quote("<TR><TD><PRE>"))[1].split(Pattern.quote("</PRE>"))[0]);
        } catch(Exception e) {
            e.printStackTrace();
            return "Error: Failed to generate ASCII art";
        }
    }

    /* tries to solve a cryptogram */
    public static String solve(String s) {
        String resp = "Error: Failed to solve cryptogram";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://rumkin.com/tools/cipher/cryptogram-solver.php");
        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("text", s));
        params.add(new BasicNameValuePair("dict", "american-english")); //american-english-huge
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();

            if (respEntity != null) {
                String content =  EntityUtils.toString(respEntity);

                String c = content.split(Pattern.quote("The Results</h2>"))[1].split(Pattern.quote("<div"))[0].trim();
                resp = c.replace("<br>", "").trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resp.replace("Sorry, no quotes found", "Error: Failed to solve cryptogram.");
    }
}

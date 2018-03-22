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

public class ModuleHash extends Module {
    public ModuleHash(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onInit() {}

    @Override
    public String getName() {
        return "Hash";
    }

    @Override
    public String[] getHelp(String alias) {
        if(in(alias, "hash", "encrypt")) {
            return new String[] {"<algorithm/all> <string>", "Hashes the given string using MD2, MD5, SHA-1, SHA-224, SHA-256, SHA384, or SHA-512"};
        } else if(in(alias, "hashid")) {
            return new String[] {"<string>", "Checks the given string against various hashing algorithms"};
        } else if(in(alias, "hashcrack")) {
            return new String[] {"<string>", "Attempts to crack the hash via rainbow tables"};
        }

        return null;
    }

    @Override
    public String[] getAliases() {
        return new String[] {"hash", "hashid", "hashcrack"};
    }

    @Override
    public String onCommand(String cmd, String[] args) {
        if(in(cmd, "hash", "encrypt")) {
            if(args.length >= 2) {
                String merged = merge(args, 1);
                List<String> algs = Arrays.asList(new String[] {"MD2", "MD5", "SHA-1", "SHA-224", "SHA-256", "SHA-384", "SHA-512"});

                if(args[0].equalsIgnoreCase("all")) {
                    StringBuilder b = new StringBuilder();

                    for(String s : algs) {
                        b.append(s + ": " + CryptoOps.hash(merged, s) + "\n");
                    }

                    return b.toString();
                } else {
                    String alg = args[0].toUpperCase().replace("-", "");

                    // help lazy people
                    if(alg.equals("SHA1"))
                        alg = "SHA-1";
                    else if(alg.equals("SHA224"))
                        alg = "SHA-224";
                    else if(alg.equals("SHA256"))
                        alg = "SHA-256";
                    else if(alg.equals("SHA384"))
                        alg = "SHA-384";
                    else if(alg.equals("SHA512"))
                        alg = "SHA-512";

                    if(algs.contains(alg)) {
                        return CryptoOps.hash(merged, alg);
                    } else {
                        return "Error: That algorithm does not exist.\n\n     Supported algorithms include MD2, MD5, SHA-1, SHA-224, SHA-256, SHA384, and SHA-512";
                    }
                }
            } else
                return showHelp("hash");
        } else if(in(cmd, "hashcrack", "crackhash", "hash-crack", "crack-hash")) {
            if(args.length != 0) {
                return crack(merge(args));
            } else
                return showHelp("hashcrack");
        } else if(in(cmd, "hashid", "idhash", "hashidentification", "hash-id", "id-hash", "hash-identification", "identify-hash")) {
            if(args.length != 0) {
                String data = getAlgorithms(merge(args));
                if(data.toLowerCase().contains("Sorry, we c"))
                    return "Error: Failed to identify hash.";
                else
                    return "That hash is most likely one of the following:\n" + data;
            } else
                return showHelp("hashid");
        }

        return null;
    }

    /* do some dirty work. This is the old code I used */
    public static String getAlgorithms(String s) {
        String resp = "Error: Failed to identify hash.";

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("https://www.onlinehashcrack.com/hash-identification.php#results");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("hash", s));
        params.add(new BasicNameValuePair("submit", "Submit"));
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

                String c = content.split(Pattern.quote("<h5>Your hash <i>may</i> be one of the following :</h5>"))[1].split(Pattern.quote("<h5>This identification"))[0].trim();
                resp = c.replace("<br />", "").replace("<p></p>", "").trim();

                StringBuilder builder = new StringBuilder();
                String[] args = resp.split(Pattern.quote("\n"));
                for(String hash : args)
                    if(!hash.contains("(") && !hash.contains(")"))
                        builder.append(hash + "\n");
                resp = builder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resp;
    }

    /* ik it's really messy */
    public static String crack(String s) {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL("http://crackhash.com/api.php?hash=" + URLEncoder.encode(s));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            String res = result.toString();
            if(res.equals("not found!"))
                res = "Error: Failed to crack hash.";
            return res;
        } catch(Exception e) {
            e.printStackTrace();
            return "Error: Failed to crack hash.";
        }
    }
}

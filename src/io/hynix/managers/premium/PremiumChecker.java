package io.hynix.managers.premium;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class PremiumChecker {

    public static boolean isPremium;
    public static String urlpastebin = "https://pastebin.com/raw/7wK91Muv";
    public static String uuidcheck = CheckingUUID.getUUID();

    static {
        isPremium = checkUUID(uuidcheck);
    }

    public static boolean checkUUID(String uuid) {
        try {
            URL url = new URL(urlpastebin);
            HttpsURLConnection connect = (HttpsURLConnection) url.openConnection();
            connect.setRequestMethod("GET");

            if (connect.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine).append("\n");
                }
                in.close();

                String[] lines = content.toString().split("\n");
                for (String line : lines) {
                    if (line.contains(uuid)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("UUID не найден");
        }
        return false;
    }
}

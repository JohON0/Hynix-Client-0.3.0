package io.hynix.managers.checking.version.check;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CheckVersion {
    private static final String VERSION_URL = "https://pastebin.com/raw/rDXXzeFz";

    public static String checkVersion() {
        String remoteVersion = "";
        try {
            remoteVersion = getRemoteVersion(VERSION_URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return remoteVersion;
    }

    private static String getRemoteVersion(String versionUrl) throws Exception {
        URL url = new URL(versionUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String remoteVersion = in.readLine();
        in.close();

        return remoteVersion.trim(); // Убираем лишние пробелы
    }
}

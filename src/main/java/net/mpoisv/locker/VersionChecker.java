package net.mpoisv.locker;

import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.System.Logger.Level;
import java.net.HttpURLConnection;
import java.net.URL;

public final class VersionChecker {
    private static String versionCode;
    public static String getVersionCode() {
        return versionCode;
    }

    public static boolean isLatestVersion(String currentVersion) {
        return versionCode == null || versionCode.equals(currentVersion);
    }

    public static void getLatestVersionFromServer() {
        versionCode = null;
        try {
            var connection = (HttpURLConnection) new URL("https://raw.githubusercontent.com/skyneton/minecraft-password-locker/main/build.gradle.kts").openConnection();
            connection.setReadTimeout(2000);
            connection.setUseCaches(false);

            try(var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while((line = reader.readLine()) != null) {
                    if(!(line.startsWith("version =") || line.startsWith("version="))) continue;
                    var versionPart = line.split("=")[1].trim();
                    versionCode = versionPart.substring(1, versionPart.length() - 1);
                    break;
                }
            }
        }catch(Exception e) {
            Main.instance.getLogger().warning(e.toString());
        }
    }
}

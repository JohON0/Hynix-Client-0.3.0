package io.hynix.managers.premium;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CheckingUUID {
    // Метод для получения системы
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    // метод получения UUID
    public static String getUUID() {
        String uuid = null;
        try {
            Process process;
            process = Runtime.getRuntime().exec("powershell -command \"Get-WmiObject -Class Win32_ComputerSystemProduct | Select-Object -ExpandProperty UUID\"");
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            uuid = reader.readLine(); // Читаем следующую строчку


            if (uuid != null) {
            uuid = uuid.trim();

        }
        reader.close();
        } catch (Exception e) {
            System.out.println("Ошибка получения UUID: " + e.getMessage());
        }
        return uuid;
    }
}

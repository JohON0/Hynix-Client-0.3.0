package io.hynix.managers.id;

import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class IDManager {
    private final File file;
    public static String threeDigitNumber;
    public static String iduser;

    public IDManager() {
        this.file = new File(new File(Minecraft.getInstance().gameDir, "saves/files/other"), "id.hynixid");
    }

    public static void createFile() {
        IDManager manager = new IDManager();

        if (!manager.file.exists()) {
            manager.generateAndSaveThreeDigitNumber();
        } else {
            System.out.println("Файл уже существует. Новый файл не будет создан.");
            manager.readFile();
        }
    }

    private void generateAndSaveThreeDigitNumber() {
        Random random = new Random();
        int number = random.nextInt(999) + 1;
        threeDigitNumber = Integer.toString(number);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(threeDigitNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            iduser = reader.readLine();
            System.out.println("id: " + iduser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

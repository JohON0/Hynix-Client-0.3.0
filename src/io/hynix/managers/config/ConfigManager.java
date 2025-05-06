package io.hynix.managers.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ConfigManager {
    public final Logger logger = Logger.getLogger(ConfigManager.class.getName());

    public static final File CONFIG_DIR = new File(Minecraft.getInstance().gameDir, "\\saves\\files\\configs");
    public final File AUTOCFG_DIR = new File(CONFIG_DIR, "backup.cfg");
    private static final JsonParser jsonParser = new JsonParser();

    public void init() throws IOException {
        setupFolder();
    }

    public void setupFolder() {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        } else if (AUTOCFG_DIR.exists()) {
            loadConfiguration("backup");
            logger.log(Level.SEVERE, "Load system configuration...");
        } else {
            logger.log(Level.SEVERE, "Creating system configuration...");
            try {
                AUTOCFG_DIR.createNewFile();
                logger.log(Level.SEVERE, "Created!");
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to create system configuration file", e);
            }
        }
    }

    public boolean isEmpty() {
        return getConfigs().isEmpty();
    }

    public List<Config> getConfigs() {
        List<Config> configs = new ArrayList<>();
        File[] configFiles = CONFIG_DIR.listFiles();

        if (configFiles != null) {
            for (File configFile : configFiles) {
                if (configFile.isFile() && configFile.getName().endsWith(".cfg")) {
                    String configName = configFile.getName().replace(".cfg", "");
                    Config config = findConfig(configName);
                    if (config != null) {
                        configs.add(config);
                    }
                }
            }
        }

        return configs;
    }
    public List<String> getAllConfigurations() {
        List<String> configurations = new ArrayList<>();
        File[] files = CONFIG_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".cfg")) {
                    String configName = file.getName().substring(0, file.getName().lastIndexOf(".cfg"));
                    configurations.add(configName);
                }
            }
        }
        return configurations;
    }
    private static byte[] compressBytes(byte[] byArray) throws DataFormatException, IOException {
        if (byArray.length == 0) {
            return byArray;
        }
        Inflater inflater = new Inflater();
        inflater.setInput(byArray);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(byArray.length);
        byte[] byArray2 = new byte[1024];
        while (true) {
            if (inflater.finished()) {
                byteArrayOutputStream.close();
                inflater.end();
                return byteArrayOutputStream.toByteArray();
            }
            int n = inflater.inflate(byArray2);
            byteArrayOutputStream.write(byArray2, 0, n);
        }
    }
    public static JsonElement compressAndWrite(File file) {
        try {
            byte[] byArray = java.nio.file.Files.readAllBytes(file.toPath());
            byte[] byArray2 = compressBytes(byArray);
            String string = new String(byArray2, 0, byArray2.length, StandardCharsets.UTF_8);
            return jsonParser.parse(string);
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }
    public void loadConfiguration(String configuration) {
        Config config = findConfig(configuration);
        try {
            FileReader reader = new FileReader(config.getFile());
            JsonParser parser = new JsonParser();
            JsonObject object = (JsonObject) parser.parse(reader);
            config.loadConfig(object);
        } catch (FileNotFoundException e) {
            logger.log(Level.WARNING, "Not Found Exception", e);
        } catch (NullPointerException pointerException) {
            logger.log(Level.WARNING, "Fatal error in Config!", pointerException);
        }
    }

    public void saveConfiguration(String configuration) {
        Config config = new Config(configuration);
        String contentPrettyPrint = new GsonBuilder().setPrettyPrinting().create().toJson(config.saveConfig());
        try {
            FileWriter writer = new FileWriter(config.getFile());
            writer.write(contentPrettyPrint);
            writer.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "File not found!", e);
        } catch (NullPointerException e) {
            logger.log(Level.WARNING, "Fatal Error in Config!", e);
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        if (new File(CONFIG_DIR, configName + ".cfg").exists())
            return new Config(configName);
        return null;
    }
}

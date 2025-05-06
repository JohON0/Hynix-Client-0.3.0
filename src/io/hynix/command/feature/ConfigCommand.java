package io.hynix.command.feature;

import io.hynix.command.interfaces.*;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitManager;
import io.hynix.ui.notifications.impl.WarningNotify;
import io.hynix.utils.client.SoundPlayer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.hynix.HynixMain;
import io.hynix.command.api.CommandException;
import io.hynix.managers.config.Config;
import io.hynix.managers.config.ConfigManager;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigCommand implements Command, CommandWithAdvice, MultiNamedCommand {

    final ConfigManager configManager;
    final Prefix prefix;
    final Logger logger;


    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "load" -> loadConfig(parameters);
            case "save" -> saveConfig(parameters);
            case "list" -> configList();
            case "dir" -> getDirectory();
            case "reset" -> resetConfig();
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " load, save, list, dir, reset");
        }
    }

    // ... (
    @Override
    public String name() {
        return "config";
    }

    @Override
    public String description() {
        return "Позволяет взаимодействовать с конфигами в чите";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        HynixMain.getInstance().getNotifyManager().add(0, new WarningNotify("Ошибка в выполнения команды!", 1000));
        return List.of(commandPrefix + name() + " load <config> - Загрузить конфиг",
                commandPrefix + name() + " save <config> - Сохранить конфиг",
                commandPrefix + name() + " list - Получить список конфигов",
                commandPrefix + name() + " dir - Открыть папку с конфигами",
                commandPrefix + name() + " reset - Создать пустой конфиг",
                "Пример: " + TextFormatting.RED + commandPrefix + "cfg save myConfig",
                "Пример: " + TextFormatting.RED + commandPrefix + "cfg load myConfig"

        );
    }

    @Override
    public List<String> aliases() {
        return List.of("cfg");
    }

    private void resetConfig() {
        UnitManager fr = HynixMain.getInstance().getModuleManager();
        for (Unit f : fr.getModules()) {
            if (f.isEnabled()) {
                f.setEnabled(false, true);
            }
        }

        fr.getModules().forEach(function -> {
            if (!function.getName().equals("ClickGui")) {
                function.setBind(0);
            }
        });

        logger.log(TextFormatting.GREEN + "Успешно.");
        SoundPlayer.playSound("r.wav");
    }

    private void loadConfig(Parameters parameters) {
        String configName = parameters.asString(1).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига!"));

        if (new File(configManager.CONFIG_DIR, configName + ".cfg").exists()) {
            configManager.loadConfiguration(configName);
            logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " загружена!");
            SoundPlayer.playSound("s.wav");
        } else {
            logger.log(TextFormatting.RED + "Конфигурация " + TextFormatting.GRAY + configName + TextFormatting.RED + " не найдена!");
        }
    }

    private void saveConfig(Parameters parameters) {
        String configName = parameters.asString(1).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига!"));
        configManager.saveConfiguration(configName);
        logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " сохранена!");
        SoundPlayer.playSound("s.wav");
    }

    private void configList() {
        if (configManager.isEmpty()) {
            logger.log(TextFormatting.RED + "Список конфигураций пустой");
            return;
        }
        logger.log(TextFormatting.GRAY + "Список конфигов:");

        for (Config config : configManager.getConfigs()) {
            logger.log(TextFormatting.GRAY + config.getName());
        }
    }

    private void getDirectory() {
        try {
            Runtime.getRuntime().exec("explorer " + configManager.CONFIG_DIR.getAbsolutePath());
        } catch (IOException e) {
            logger.log(TextFormatting.RED + "Папка с конфигурациями не найдена!" + e.getMessage());
        }
    }
}

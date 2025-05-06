package io.hynix.command.feature;

import io.hynix.HynixMain;
import io.hynix.command.interfaces.*;
import io.hynix.units.api.Unit;
import io.hynix.ui.notifications.impl.WarningNotify;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.glfw.GLFW;

import io.hynix.command.api.CommandException;
import io.hynix.utils.client.KeyStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BindCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "add" -> addBindToFunction(parameters, logger);
            case "remove" -> removeBindFromFunction(parameters, logger);
            case "clear" -> clearAllBindings(logger);
            case "list" -> listBoundKeys(logger);
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear, list");
        }
    }

    @Override
    public String name() {
        return "bind";
    }

    @Override
    public String description() {
        return "Позволяет забиндить функцию на определенную клавишу";
    }

    @Override
    public List<String> adviceMessage() {
        HynixMain.getInstance().getNotifyManager().add(0, new WarningNotify("Ошибка в выполнения команды!", 1000));
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "bind add <function> <key> - Добавить новый бинд",
                commandPrefix + "bind remove <function> <key> - Удалить бинд",
                commandPrefix + "bind list - Получить список биндов",
                commandPrefix + "bind clear - Очистить список биндов",
                "Пример: " + TextFormatting.RED + commandPrefix + "bind add HitAura R"
        );
    }
    private void addBindToFunction(Parameters parameters, Logger logger) {
        String functionName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название функции!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));

        Unit module = null;

        for (Unit func : HynixMain.getInstance().getModuleManager().getModules()) {
            if (func.getName().toLowerCase(Locale.ROOT).equals(functionName.toLowerCase(Locale.ROOT))) {
                module = func;
                break;
            }
        }

        Integer key = KeyStorage.getKey(keyName.toUpperCase());

        if (module == null) {
            logger.log(TextFormatting.RED + "Функция " + functionName + " не была найдена");
            return;
        }

        if (key == null) {
            logger.log(TextFormatting.RED + "Клавиша " + keyName + " не была найдена");
            return;
        }

        module.setBind(key);
        logger.log(TextFormatting.GREEN + "Бинд " + TextFormatting.RED
                + keyName.toUpperCase() + TextFormatting.GREEN
                + " был установлен для функции " + TextFormatting.RED + functionName);
    }

    private void removeBindFromFunction(Parameters parameters, Logger logger) {
        String functionName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название функции!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите кнопку!"));

        HynixMain.getInstance().getModuleManager().getModules().stream()
                .filter(f -> f.getName().equalsIgnoreCase(functionName))
                .forEach(f -> {
                    f.setBind(0);
                    logger.log(TextFormatting.GREEN + "Клавиша " + TextFormatting.RED + keyName.toUpperCase()
                            + TextFormatting.GREEN + " была отвязана от функции " + TextFormatting.RED + f.getName());
                });
    }

    private void clearAllBindings(Logger logger) {
        HynixMain.getInstance().getModuleManager().getModules().forEach(function -> function.setBind(0));
        logger.log(TextFormatting.GREEN + "Все клавиши были отвязаны от модулей");
    }

    private void listBoundKeys(Logger logger) {
        logger.log(TextFormatting.GRAY + "Список всех модулей с привязанными клавишами:");

        HynixMain.getInstance().getModuleManager().getModules().stream()
                .filter(f -> f.getBind() != 0)
                .map(f -> {
                    String keyName = GLFW.glfwGetKeyName(f.getBind(), -1);
                    keyName = keyName != null ? keyName : "";
                    return String.format("%s [%s%s%s]", f.getName(), TextFormatting.GRAY, keyName, TextFormatting.WHITE);
                })
                .forEach(logger::log);
    }

}

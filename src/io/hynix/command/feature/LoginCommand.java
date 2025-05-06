package io.hynix.command.feature;

import io.hynix.HynixMain;
import io.hynix.command.api.CommandException;
import io.hynix.command.interfaces.*;
import io.hynix.managers.config.AltConfig;
import io.hynix.ui.notifications.impl.WarningNotify;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginCommand implements Command, CommandWithAdvice, MultiNamedCommand {
    final Prefix prefix;
    final Logger logger;
    final Minecraft mc;



    @Override
    public void execute(Parameters parameters) {
        String random = HynixMain.getInstance().randomNickname();
        String nameArgument = parameters.asString(0).orElseThrow(() -> new CommandException(TextFormatting.RED + "Необходимо указать расстояние."));
        if (nameArgument.equals("random") || nameArgument.equals("r")) {
            nameArgument = random;
        }
        mc.session = new net.minecraft.util.Session(nameArgument, "", "", "mojang");
        AltConfig.updateFile();
        logger.log(TextFormatting.GREEN + "Ник изменён на - " + TextFormatting.GRAY + "[" + TextFormatting.WHITE + nameArgument + TextFormatting.GRAY + "]" + TextFormatting.RED + " (Требуется перезаход)");
    }


    @Override
    public String name() {
        return "login";
    }

    @Override
    public String description() {
        return "Меняет никнейм.";
    }

    @Override
    public List<String> adviceMessage() {
        HynixMain.getInstance().getNotifyManager().add(0, new WarningNotify("Ошибка в выполнения команды!", 1000));
        String commandPrefix = prefix.get();
        return List.of(
                TextFormatting.GRAY + commandPrefix + "login <nickname> - Меняет никнейм",
                "Пример: " + TextFormatting.RED + commandPrefix + "login EvaWareUser1337"
        );
    }

    @Override
    public List<String> aliases() {
        return List.of("l");
    }
}

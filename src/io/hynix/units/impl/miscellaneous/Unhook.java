package io.hynix.units.impl.miscellaneous;

import io.hynix.HynixMain;
import io.hynix.units.api.Unit;
import io.hynix.utils.client.SoundPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextFormatting;

import io.hynix.units.api.Category;
import io.hynix.units.api.UnitRegister;
import io.hynix.utils.client.ClientUtils;
import io.hynix.utils.johon0.math.TimerUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.ArrayList;
import java.util.List;

@UnitRegister(name = "Unhook", category = Category.Miscellaneous, desc = "Скрывает чит при проверке")
public class Unhook extends Unit {

    public static boolean unhooked = false;
    public String secret = "hynix";
    public TimerUtils timerUtils = new TimerUtils();

    @Override
    public void onEnable() {
        super.onEnable();
        process();
        SoundPlayer.playSound("proverka.wav");
        print("Чтобы вернуть чит напишите в чат " + TextFormatting.RED + secret);
        print("Все сообщения удалятся через 10 секунд");
        timerUtils.reset();

        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            mc.ingameGUI.getChatGUI().clearChatMessages(false);
            toggle();
        }).start();


        unhooked = true;

    }

    public List<Unit> saved = new ArrayList<>();

    public void process() {
        for (Unit module : HynixMain.getInstance().getModuleManager().getModules()) {
            if (module == this) continue;
            if (module.isEnabled()) {
                saved.add(module);
                module.setEnabled(false, false);
            }
        }
        ClientUtils.stopRPC();
        File folder = new File(Minecraft.getInstance().gameDir, "\\saves\\files");
        hiddenFolder(folder, true);
    }

    public void hook() {
        for (Unit module : saved) {
            if (module == this) continue;
            if (!module.isEnabled()) {
                module.setEnabled(true, false);
            }
        }
        File folder = new File(Minecraft.getInstance().gameDir, "\\saves\\files");
        hiddenFolder(folder, false);
        ClientUtils.startRPC();
        unhooked = false;
    }

    private void hiddenFolder(File folder, boolean hide) {
        if (folder.exists()) {
            try {
                Path folderPathObj = folder.toPath();
                DosFileAttributeView attributes = Files.getFileAttributeView(folderPathObj, DosFileAttributeView.class);
                attributes.setHidden(true);
            } catch (IOException e) {
                System.out.println("Не удалось скрыть папку: " + e.getMessage());
            }
        }
    }
}

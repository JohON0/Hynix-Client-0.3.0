package io.hynix.command.feature;

import io.hynix.command.interfaces.Command;
import io.hynix.command.interfaces.Logger;
import io.hynix.command.interfaces.Parameters;
import io.hynix.command.interfaces.Prefix;
import io.hynix.utils.client.IMinecraft;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.text.TextFormatting;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ParseCommand implements Command, IMinecraft {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        savePlayerData();
    }

    @Override
    public String name() {
        return "parse";
    }

    @Override
    public String description() {
        return "Парсит игроков из таба. (В папку 'saves/files/parse')";
    }

    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + name() + " - Сохранить ники игроков из таба в файл");
    }

    private void savePlayerData() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
        String formattedDateTime = now.format(formatter);
        File directory = new File("saves/files/parse");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        String getServerIP = mc.getCurrentServerData().serverIP;
        String fileName = (mc.isSingleplayer() ? "local" : getServerIP) + "_" + formattedDateTime;
        File file = new File(directory, fileName);
        Map<String, List<String>> prefixToPlayers = new LinkedHashMap<>();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            Minecraft minecraft = Minecraft.getInstance();
            ClientPlayNetHandler networkHandler = minecraft.getConnection();
            if (networkHandler != null) {
                networkHandler.getPlayerInfoMap().forEach(playerInfo -> {
                    String playerName = playerInfo.getGameProfile().getName();
                    String playerPrefix = getPlayerPrefix(playerName);

                    prefixToPlayers
                            .computeIfAbsent(playerPrefix, k -> new ArrayList<>())
                            .add(playerName);
                });

                for (Map.Entry<String, List<String>> entry : prefixToPlayers.entrySet()) {
                    String prefix = entry.getKey();
                    List<String> players = entry.getValue();

                    if (!prefix.isEmpty()) {
                        writer.write(prefix);
                        writer.newLine();
                    }
                    for (String player : players) {
                        writer.write("  " + player);
                        writer.newLine();
                    }
                    writer.newLine();
                }
            }

            String relativePath = "saves/files/parse/" + fileName + ".txt";
            logger.log(TextFormatting.GREEN + "Информация о игроках успешно сохранена в " + relativePath);
        } catch (IOException e) {
            logger.log(TextFormatting.RED + "Ошибка при сохранении информации о игроках: " + e.getMessage());
        }
    }

    private String getPlayerPrefix(String playerName) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayNetHandler networkHandler = minecraft.getConnection();
        if (networkHandler != null) {
            for (ScorePlayerTeam team : minecraft.world.getScoreboard().getTeams()) {
                if (team.getMembershipCollection().contains(playerName)) {
                    return team.getPrefix().getString();
                }
            }
        }
        return "";
    }
}

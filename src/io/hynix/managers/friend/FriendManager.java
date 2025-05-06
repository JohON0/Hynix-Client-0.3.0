package io.hynix.managers.friend;

import io.hynix.utils.client.SoundPlayer;
import lombok.Getter;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

public class FriendManager {
    @Getter
    private static final int color = new Color(128, 255, 128).getRGB();
    @Getter
    private static final List<Friend> friends = new ArrayList<Friend>();
    public static final File file = new File(Minecraft.getInstance().gameDir, "\\saves\\files\\other\\friend.cfg");

    public void init() throws IOException {
        if (file.exists()) {
            readFriends();
        } else {
            file.createNewFile();
        }
    }

    public static void add(String name) {
        friends.add(new Friend(name));
        updateFile();
    }

    public static boolean isFriend(String friend) {
        return friends.stream().anyMatch(isFriend -> isFriend.getName().equals(friend));
    }

    public static void remove(String name) {
        friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
        updateFile();
    }

    public static void clear() {
        friends.clear();
        updateFile();
    }

    public static void updateFile() {
        try {
            StringBuilder builder = new StringBuilder();
            friends.forEach(friend -> builder.append(friend.getName()).append("\n"));
            Files.write(file.toPath(), builder.toString().getBytes(), new OpenOption[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readFriends() {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file.getAbsolutePath()))));
            while ((line = reader.readLine()) != null) {
                friends.add(new Friend(line));
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package io.hynix.ui.mainmenu;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import lombok.AllArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@AllArgsConstructor
public class Alt {

    public String name;
    public ResourceLocation skin;

    @Override
    public String toString() {
        return name;
    }

    public Alt(String accountName) {
        this.name = accountName;
        UUID uuid = null;
        try {
            uuid = resolveUUID(accountName);
        } catch (IOException e) {
            uuid = UUID.randomUUID();
        }
        this.skin = DefaultPlayerSkin.getDefaultSkin(uuid);
        Minecraft.getInstance().getSkinManager().loadProfileTextures(new GameProfile(uuid, accountName), (type, loc, tex) -> {
            if (type == MinecraftProfileTexture.Type.SKIN) {
                skin = loc;
            }
        }, true);
    }

    public static UUID resolveUUID(String name) throws IOException {
        UUID uUID;
        InputStreamReader in = new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream(), StandardCharsets.UTF_8);
        try {
            uUID = UUID.fromString(new Gson().fromJson(in, JsonObject.class).get("id").getAsString().replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        }
        catch (Throwable uuid) {
            try {
                try {
                    in.close();
                }
                catch (Throwable throwable) {
                    uuid.addSuppressed(throwable);
                }
                throw uuid;
            }
            catch (Throwable ignored) {
                return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
            }
        }
        in.close();
        return uUID;
    }

}

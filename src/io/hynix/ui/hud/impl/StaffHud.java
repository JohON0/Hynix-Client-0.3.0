package io.hynix.ui.hud.impl;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;

import com.mojang.blaze3d.platform.GlStateManager;
import io.hynix.managers.staff.StaffManager;
import io.hynix.events.impl.EventRender2D;
import io.hynix.events.impl.EventUpdate;
import io.hynix.ui.clickgui.ClickGui;
import io.hynix.ui.hud.updater.ElementRenderer;
import io.hynix.ui.hud.updater.ElementUpdater;
import io.hynix.managers.theme.Theme;
import io.hynix.utils.johon0.animations.AnimationUtils;
import io.hynix.utils.johon0.animations.Direction;
import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import io.hynix.managers.drag.Dragging;
import io.hynix.utils.johon0.animations.impl.EaseBackIn;
import io.hynix.utils.johon0.render.color.ColorUtils;
import io.hynix.utils.johon0.render.render2d.RenderUtils;
import io.hynix.utils.johon0.render.other.Scissor;
import io.hynix.utils.text.font.ClientFonts;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class StaffHud implements ElementRenderer, ElementUpdater {

    final Dragging dragging;
    private final CompactAnimation widthAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final CompactAnimation heightAnimation = new CompactAnimation(Easing.EASE_OUT_QUART, 100);
    private final List<Staff> staffPlayers = new ArrayList<>();
    private final Pattern namePattern = Pattern.compile("^\\w{3,16}$");
    private final Pattern prefixMatches = Pattern.compile(".*(mod|der|adm|help|wne|хелп|адм|поддержка|кура|own|taf|curat|dev|supp|yt|сотруд).*");
    final AnimationUtils animation = new EaseBackIn(300, 1, 1);

    @Override
    public void update(EventUpdate e) {
        staffPlayers.clear();
        for (ScorePlayerTeam team : mc.world.getScoreboard().getTeams().stream().sorted(Comparator.comparing(Team::getName)).toList()) {
            String name = team.getMembershipCollection().toString().replaceAll("[\\[\\]]", "");
            boolean vanish = true;
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    vanish = false;
                }
            }
            NetworkPlayerInfo playerInfo = null; // переменная для хранения информации о игроке
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    playerInfo = info; // нашли игрока
                    break;
                }
            }
            if (playerInfo != null) {
                GameProfile gameProfile = playerInfo.getGameProfile();

                if (namePattern.matcher(name).matches() && !name.equals(mc.player.getName().getString())) {
                    if (!vanish) {
                        if (prefixMatches.matcher(team.getPrefix().getString().toLowerCase(Locale.ROOT)).matches() || StaffManager.isStaff(name)) {
                            Staff staff = new Staff(team.getPrefix(), name, false, Status.NONE, gameProfile);
                            staffPlayers.add(staff);
                        }
                    }
                    if (vanish && !team.getPrefix().getString().isEmpty()) {
                        Staff staff = new Staff(team.getPrefix(), name, true, Status.VANISHED, gameProfile);
                        staffPlayers.add(staff);
                    }
                }
            }

        }
    }

    float width;
    float height;

    @Override
    public void render(EventRender2D eventRender2D) {

        float posX = dragging.getX();
        float posY = dragging.getY();
        float padding = 5;
        float fontSize = 6.5f;
        MatrixStack ms = eventRender2D.getMatrixStack();
        String name = "Staff";
        float iconSize = 10;
        float nameWidth = ClientFonts.tenacity[16].getWidth(name);
        int textColor = Theme.rectColor;

        boolean isAnyStaffActive = false;

        if (!staffPlayers.isEmpty() || mc.currentScreen instanceof ChatScreen) {
            isAnyStaffActive = true;
        }

        animation.setDirection(isAnyStaffActive ? Direction.FORWARDS : Direction.BACKWARDS);
        animation.setDuration(isAnyStaffActive ? 300 : 200);

        GlStateManager.pushMatrix();
        RenderUtils.sizeAnimation(posX + (width / 2), (posY + height / 2), animation.getOutput());

        RenderUtils.drawShadow(posX,posY, (float) width, height, 5, ClickGui.backgroundColor);
        RenderUtils.drawRoundedRect(posX,posY, (float) width, height, 4, ClickGui.backgroundColor);
        ClientFonts.tenacity[16].drawString(ms, name, posX + iconSize , posY + padding + 4f, ClickGui.textcolor);
        ClientFonts.dev[25].drawString(ms, "E", posX + width - padding - 10, posY + 6f, textColor);
        posY += fontSize + padding + 2;

        float maxWidth = ClientFonts.tenacityBold[14].getWidth(name) + padding * 2;
        float localHeight = fontSize + padding * 2;

        posY += 7f;
        Scissor.push();
        Scissor.setFromComponentCoordinates(posX, posY, width, height);
        for (Staff f : staffPlayers) {
            // Получаем информацию о предмете
            ITextComponent prefix = f.getPrefix();
            float prefixWidth = ClientFonts.tenacityBold[14].getWidth(prefix.getString());
            String staff = (prefix.getString().isEmpty() ? "" : " ") + f.getName();
            float staffWidth = ClientFonts.tenacityBold[14].getWidth(staff);

            float localWidth = prefixWidth + staffWidth + 5 + padding * 3;

            // Отрисовка префикса и имени игрока
            ClientFonts.tenacityBold[14].drawString(ms, prefix, posX + padding + 7.5f, posY + 2f, -1);
            ClientFonts.tenacityBold[14].drawString(ms, staff, posX + padding + prefixWidth + 3.5f, posY + 2f, ClickGui.textcolor);
            RenderUtils.drawCircle(posX + width - padding - 1, posY + padding - 2, 4, f.getStatus().color);

            GameProfile gameProfile = f.getGameProfile();
            if (gameProfile != null) {
                NetworkPlayerInfo networkPlayerInfo = mc.getConnection().getPlayerInfo(gameProfile.getId());
                if (networkPlayerInfo != null) {
                    // Отрисовка головы
                    mc.getTextureManager().bindTexture(networkPlayerInfo.getLocationSkin());
                    int headX = (int) (posX + padding-2); // Позиция по X для головы
                    int headY = (int) (posY-1); // Позиция по Y для головы
                    AbstractGui.blit(ms, headX, headY, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64); // Рендер головы

                    // Если игрок носит шляпу, отображаем ее
                    PlayerEntity playerEntity = mc.world.getPlayerByUuid(gameProfile.getId());
                    if (playerEntity != null && playerEntity.isWearing(PlayerModelPart.HAT)) {
                        AbstractGui.blit(ms, headX, headY, 8, 8, 40.0F, 8.0F, 8, -8, 64, 64);
                    }
                }
            }

            // Обновление высоты для следующего игрока
            posY += fontSize + padding - 2;
            localHeight += fontSize + padding - 2;

            if (localWidth > maxWidth) {
                maxWidth = localWidth;
            }
        }

        Scissor.unset();
        Scissor.pop();

        GlStateManager.popMatrix();

        widthAnimation.run(Math.max(maxWidth, nameWidth + iconSize + 25));
        width = (float) widthAnimation.getValue();
        heightAnimation.run((localHeight + 5.5f));
        height = (float) heightAnimation.getValue();
        dragging.setWidth(width);
        dragging.setHeight(height);
    }

    @AllArgsConstructor
    @Data
    public static class Staff {
        ITextComponent prefix;
        String name;
        boolean isSpec;
        Status status;
        GameProfile gameProfile;
        public void updateStatus() {
            for (NetworkPlayerInfo info : mc.getConnection().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equals(name)) {
                    if (info.getGameType() == GameType.SPECTATOR) {
                        return;
                    }
                    status = Status.NONE;
                    return;
                }
            }
            status = Status.VANISHED;
        }
    }

    public enum Status {
        NONE(ColorUtils.rgb(111, 254, 68)),
        VANISHED(ColorUtils.rgb(254, 68, 68)),
        NEAR(ColorUtils.rgb(244, 221, 59));
        public final int color;

        Status(int color) {
            this.color = color;
        }
    }
}

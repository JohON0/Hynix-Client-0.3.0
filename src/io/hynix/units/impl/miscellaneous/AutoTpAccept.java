package io.hynix.units.impl.miscellaneous;

import com.google.common.eventbus.Subscribe;
import io.hynix.events.impl.EventPacket;
import io.hynix.managers.friend.Friend;
import io.hynix.managers.friend.FriendManager;
import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.Locale;

@UnitRegister(name = "AutoTPAccept", category = Category.Miscellaneous, desc = "Автоматически принимает телепортацию")
public class AutoTpAccept extends Unit {
    private final BooleanSetting onlyFriend = new BooleanSetting("Только друзья", true);
    private final String[] teleportMessages = new String[]{"has requested teleport", "просит телепортироваться", "хочет телепортироваться к вам", "просит к вам телепортироваться"};

    @Subscribe
    public void onPacket(EventPacket e) {
        if (mc.player == null || mc.world == null) return;
        if (e.getPacket() instanceof SChatPacket p) {
            String raw = p.getChatComponent().getString().toLowerCase(Locale.ROOT);
            String message = TextFormatting.getTextWithoutFormattingCodes(p.getChatComponent().getString());
            if (isTeleportMessage(message)) {
                if (onlyFriend.getValue()) {
                    boolean yes = false;

                    for (Friend friend : FriendManager.getFriends()) {
                        if (raw.contains(friend.getName().toLowerCase(Locale.ROOT))) {
                            yes = true;
                            break;
                        }
                    }

                    if (!yes) return;
                }

                mc.player.sendChatMessage("/tpaccept");
            }
        }
    }
    private boolean isTeleportMessage(String message) {
        return Arrays.stream(this.teleportMessages).map(String::toLowerCase).anyMatch(message::contains);
    }
}

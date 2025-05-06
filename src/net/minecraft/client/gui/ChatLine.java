package net.minecraft.client.gui;

import io.hynix.utils.johon0.animations.easing.CompactAnimation;
import io.hynix.utils.johon0.animations.easing.Easing;
import lombok.Getter;

public class ChatLine<T>
{
    private final int updateCounterCreated;
    private final T lineString;
    private final int chatLineID;
    @Getter
    private boolean isClient;
    private CompactAnimation slideAnimation = new CompactAnimation(Easing.EASE_OUT_EXPO, 450L);

    public ChatLine(int updatedCounterCreated, T lineString, int chatLineID, boolean isClient)
    {
        this.lineString = lineString;
        this.updateCounterCreated = updatedCounterCreated;
        this.chatLineID = chatLineID;
        this.isClient = isClient;
    }

    public T getLineString()
    {
        return this.lineString;
    }

    public int getUpdatedCounter()
    {
        return this.updateCounterCreated;
    }

    public int getChatLineID()
    {
        return this.chatLineID;
    }

    public CompactAnimation getSlideAnimation() {
        return this.slideAnimation;
    }
}

package io.hynix.events.impl;

import lombok.Data;
import net.minecraft.client.renderer.entity.PlayerRenderer;

@Data
public class EventModelRender extends CancelEvent {
    public PlayerRenderer renderer;
    private Runnable entityRenderer;

    public EventModelRender(PlayerRenderer renderer, Runnable entityRenderer) {
        this.renderer = renderer;
        this.entityRenderer = entityRenderer;
    }

    public void render() {
        entityRenderer.run();
    }
}

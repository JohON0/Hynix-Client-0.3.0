package io.hynix.ui.hud.updater;

import io.hynix.events.impl.EventRender2D;
import io.hynix.utils.client.IMinecraft;

public interface ElementRenderer extends IMinecraft {
    void render(EventRender2D eventRender2D);
}

package io.hynix.ui.hud.updater;

import io.hynix.events.impl.EventUpdate;
import io.hynix.utils.client.IMinecraft;

public interface ElementUpdater extends IMinecraft {

    void update(EventUpdate e);
}

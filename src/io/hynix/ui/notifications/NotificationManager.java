package io.hynix.ui.notifications;

import com.google.common.eventbus.Subscribe;
import io.hynix.HynixMain;
import io.hynix.events.impl.EventRender2D;
import io.hynix.units.impl.display.Notifications;
import io.hynix.ui.notifications.impl.NoNotify;
import io.hynix.ui.notifications.impl.SuccessNotify;
import io.hynix.ui.notifications.impl.WarningNotify;
import io.hynix.utils.client.IMinecraft;
import net.minecraft.client.gui.screen.ChatScreen;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

public class NotificationManager extends ArrayList<Notification> implements IMinecraft {

    public void init() {
        HynixMain.getInstance().getEventBus().register(this);
    }

    public void register(final String content, final NotificationType type, long delay) {
        final Notification notification = switch (type) {
            case YES -> new SuccessNotify(content, delay);
            case NO -> new NoNotify(content, delay);
            case WARN -> new WarningNotify(content, delay);
        };

        this.add(notification);
    }

    @Subscribe
    public void onRender(EventRender2D e) {
        // Получаем нужный элемент и проверяем его на null
        Notifications notifications = HynixMain.getInstance().getModuleManager().getNotifications();

        if (notifications.isEnabled()) {

            // Проверка на нулевые значения
            if (this.size() == 0 || mc.player == null || mc.world == null) return;

            int i = 0;
            Iterator<Notification> iterator = this.iterator();

            try {
                while (iterator.hasNext()) {
                    Notification notification = iterator.next();
                    if (notification != null) { // Проверка, чтобы избежать NullPointerException
                        if (!(mc.currentScreen instanceof ChatScreen)) {
                            notification.render(e.getMatrixStack(), i);
                        }
                        if (notification.hasExpired()) {
                            iterator.remove();
                        }
                        i++;
                    }
                }
            } catch (ConcurrentModificationException ignored) {
            }

            // Ограничение максимального количества уведомлений
            if (this.size() > 16) {
                this.clear();
            }
        }
    }
}

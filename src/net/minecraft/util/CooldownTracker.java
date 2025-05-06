package net.minecraft.util;

import com.google.common.collect.Maps;

import io.hynix.HynixMain;
import io.hynix.events.impl.EventCalculateCooldown;
import io.hynix.events.impl.EventCooldown;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.item.Item;
import net.minecraft.util.math.MathHelper;

public class CooldownTracker {
    private final Map<Item, Cooldown> cooldowns = Maps.newHashMap();
    private int ticks;

    public boolean hasCooldown(Item itemIn) {
        return this.getCooldown(itemIn, 0.0F) > 0.0F;
    }
    public long getCooldown1(Item itemIn, float partialTicks) {
        Cooldown cooldowntracker$cooldown = this.cooldowns.get(itemIn);

        EventCalculateCooldown cooldown = new EventCalculateCooldown(itemIn);
        HynixMain.getInstance().getEventBus().post(cooldown);

        if (cooldown.getCooldown() != 0) {
            return (long) (cooldown.getCooldown() * 1000); // Переводим в миллисекунды
        }

        if (cooldowntracker$cooldown != null) {
            float totalTicks = (float) (cooldowntracker$cooldown.expireTicks - cooldowntracker$cooldown.createTicks);
            float remainingTicks = (float) cooldowntracker$cooldown.expireTicks - ((float) this.ticks + partialTicks);
            float seconds = (remainingTicks / totalTicks) * (cooldowntracker$cooldown.expireTicks - cooldowntracker$cooldown.createTicks) * 0.05F; // Перевод в секунды
            return (long) (seconds * 1000); // Перевод в миллисекунды
        } else {
            return 0L;
        }
    }
    public float getCooldown(Item itemIn, float partialTicks) {
        Cooldown cooldowntracker$cooldown = this.cooldowns.get(itemIn);

        EventCalculateCooldown cooldown = new EventCalculateCooldown(itemIn);
        HynixMain.getInstance().getEventBus().post(cooldown);

        if (cooldown.getCooldown() != 0) {
            return cooldown.getCooldown();
        }

        if (cooldowntracker$cooldown != null) {
            float f = (float) (cooldowntracker$cooldown.expireTicks - cooldowntracker$cooldown.createTicks);
            float f1 = (float) cooldowntracker$cooldown.expireTicks - ((float) this.ticks + partialTicks);
            return MathHelper.clamp(f1 / f, 0.0F, 1.0F);
        } else {
            return 0.0F;
        }
    }

    public void tick() {
        ++this.ticks;

        if (!this.cooldowns.isEmpty()) {
            Iterator<Entry<Item, Cooldown>> iterator = this.cooldowns.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<Item, Cooldown> entry = iterator.next();

                if ((entry.getValue()).expireTicks <= this.ticks) {
                    eventCooldown.setItem(entry.getKey());
                    eventCooldown.setCooldownType(EventCooldown.CooldownType.REMOVED);
                    HynixMain.getInstance().getEventBus().post(eventCooldown);
                    iterator.remove();
                    this.notifyOnRemove(entry.getKey());
                }
            }
        }
    }

    EventCooldown eventCooldown = new EventCooldown(null, null);

    public void setCooldown(Item itemIn, int ticksIn) {
        eventCooldown.setItem(itemIn);
        eventCooldown.setCooldownType(EventCooldown.CooldownType.ADDED);
        HynixMain.getInstance().getEventBus().post(eventCooldown);
        this.cooldowns.put(itemIn, new Cooldown(this.ticks, this.ticks + ticksIn));
        this.notifyOnSet(itemIn, ticksIn);
    }

    public void removeCooldown(Item itemIn) {
        eventCooldown.setItem(itemIn);
        eventCooldown.setCooldownType(EventCooldown.CooldownType.REMOVED);
        HynixMain.getInstance().getEventBus().post(eventCooldown);
        this.cooldowns.remove(itemIn);
        this.notifyOnRemove(itemIn);
    }

    protected void notifyOnSet(Item itemIn, int ticksIn) {
    }

    protected void notifyOnRemove(Item itemIn) {
    }

    class Cooldown {
        private final int createTicks;
        private final int expireTicks;

        private Cooldown(int createTicksIn, int expireTicksIn) {
            this.createTicks = createTicksIn;
            this.expireTicks = expireTicksIn;
        }
    }
}

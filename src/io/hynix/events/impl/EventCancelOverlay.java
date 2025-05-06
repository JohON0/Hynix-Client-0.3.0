package io.hynix.events.impl;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventCancelOverlay extends CancelEvent {

    public final Overlays overlayType;

    public enum Overlays {
        FIRE_OVERLAY, BOSS_LINE, SCOREBOARD, TITLES, TOTEM, FOG, HURT, UNDER_WATER, CAMERA_CLIP, ARMOR
    }
    
}

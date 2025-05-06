package io.hynix.events.impl;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventInventoryClose extends CancelEvent {

    public int windowId;

}

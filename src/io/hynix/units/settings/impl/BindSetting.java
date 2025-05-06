package io.hynix.units.settings.impl;

import java.util.function.Supplier;

import io.hynix.units.settings.api.Setting;

public class BindSetting extends Setting<Integer> {
    public BindSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public BindSetting setVisible(Supplier<Boolean> bool) {
        return (BindSetting) super.setVisible(bool);
    }
}

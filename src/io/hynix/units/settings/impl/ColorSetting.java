package io.hynix.units.settings.impl;


import java.util.function.Supplier;

import io.hynix.units.settings.api.Setting;

public class ColorSetting extends Setting<Integer> {

    public ColorSetting(String name, Integer defaultVal) {
        super(name, defaultVal);
    }
    @Override
    public ColorSetting setVisible(Supplier<Boolean> bool) {
        return (ColorSetting) super.setVisible(bool);
    }
}
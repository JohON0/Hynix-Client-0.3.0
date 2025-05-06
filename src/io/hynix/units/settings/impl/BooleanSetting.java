package io.hynix.units.settings.impl;


import java.util.function.Supplier;

import io.hynix.units.settings.api.Setting;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, Boolean defaultVal) {
        super(name, defaultVal);
    }

    @Override
    public BooleanSetting setVisible(Supplier<Boolean> bool) {
        return (BooleanSetting) super.setVisible(bool);
    }

}
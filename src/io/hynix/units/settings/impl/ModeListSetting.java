package io.hynix.units.settings.impl;


import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import io.hynix.units.settings.api.Setting;

public class ModeListSetting extends Setting<List<BooleanSetting>> {
    public ModeListSetting(String name, BooleanSetting... strings) {
        super(name, Arrays.asList(strings));
    }

    public BooleanSetting is(String settingName) {
        return getValue().stream().filter(booleanSetting -> booleanSetting.getName().equalsIgnoreCase(settingName)).findFirst().orElse(null);
    }

    public BooleanSetting get(int index) {
        return getValue().get(index);
    }

    @Override
    public ModeListSetting setVisible(Supplier<Boolean> bool) {
        return (ModeListSetting) super.setVisible(bool);
    }
}
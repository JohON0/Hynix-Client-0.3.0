package io.hynix.units.settings.api;

import java.util.function.Supplier;

public class Setting<Value> implements ISetting {

    Value v;

    String settingName;
    public Supplier<Boolean> visible = () -> true;

    public Setting(String name, Value v) {
        this.settingName = name;
        this.v = v;
    }

    public String getName() {
        return settingName;
    }

    public void setValue(Value value) {
        v = value;
    }

    @Override
    public Setting<?> setVisible(Supplier<Boolean> bool) {
        visible = bool;
        return this;
    }

    public Value getValue() {
        return v;
    }
}
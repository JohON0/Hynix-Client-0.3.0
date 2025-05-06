package io.hynix.units.settings.api;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> setVisible(Supplier<Boolean> bool);
}
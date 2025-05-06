package io.hynix.units.impl.traversal;

import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;
import io.hynix.units.settings.impl.BooleanSetting;

@UnitRegister(name = "AutoSprint", category = Category.Traversal,desc = "Автоматически спринтится")
public class AutoSprint extends Unit {
    public BooleanSetting saveSprint = new BooleanSetting("Сохранять спринт", true);
    public AutoSprint() {
        addSettings(saveSprint);
    }
}

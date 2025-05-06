package io.hynix.units.impl.combat;

import io.hynix.units.api.Category;
import io.hynix.units.api.Unit;
import io.hynix.units.api.UnitRegister;

@UnitRegister(name = "NoEntityTrace", category = Category.Combat, desc = "Не дает мешать Entity при открытие сундука (полезно на Эвенте, когда возле сундука много людей)")
public class NoEntityTrace extends Unit {
}

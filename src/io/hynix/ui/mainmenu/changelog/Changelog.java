package io.hynix.ui.mainmenu.changelog;

import io.hynix.utils.johon0.render.color.ColorUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
public class Changelog {
    private static final int ADD_COLOR = ColorUtils.green; // Зеленый цвет для добавлений
    private static final int FIX_COLOR = ColorUtils.orange; // Оранжевый цвет для исправлений
    private static final int REMOVE_COLOR = ColorUtils.red; // Красный цвет для удалений
    private static final int OPTIMIZED_COLOR = ColorUtils.yellow; // Желтый цвет для оптимизаций

    private static List<Change> changes = new ArrayList<>();

    public static void addChange(String change, ChangeType type) {
        // Проверяем, существует ли уже это изменение в списке
        for (Change existingChange : changes) {
            if (existingChange.getMessage().equals(change) && existingChange.type == type) {
                return; // Изменение уже существует, выходим из метода
            }
        }
        // Если изменения нет в списке, добавляем его
        changes.add(new Change(change, type));
    }

    public static List<Change> getChanges() {
        return changes;
    }

    public enum ChangeType {
        ADD(ADD_COLOR),
        FIX(FIX_COLOR),
        REMOVE(REMOVE_COLOR),
        OPTIMIZED(OPTIMIZED_COLOR);

        private final int color;

        ChangeType(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    public static class Change {
        private final String message;
        private final ChangeType type;

        public Change(String message, ChangeType type) {
            this.message = message;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public int getColor() {
            return type.getColor();
        }
    }
}

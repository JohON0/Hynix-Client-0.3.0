package io.hynix.units.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {

    Combat("Combat", "E"),
    Traversal("Traversal", "D"),
    Display("Display", "F"),
    Miscellaneous("Miscellaneous", "C");
    private final String name;
    private final String icon;


}

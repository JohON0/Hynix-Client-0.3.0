package io.hynix.command.interfaces;

import java.util.ArrayList;
import java.util.Collection;

public interface Command {
    void execute(Parameters parameters);

    String name();

    String description();

    default public Collection<String> getSuggestions(String[] args, String s) {
        return new ArrayList<String>();
    }
}

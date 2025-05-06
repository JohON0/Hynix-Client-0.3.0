package io.hynix.command.interfaces;

public interface CommandProvider {
    Command command(String alias);
}

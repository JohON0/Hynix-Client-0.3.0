package io.hynix.command.api;

import io.hynix.command.interfaces.Logger;

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("message = " + message);
    }
}

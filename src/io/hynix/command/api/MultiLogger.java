package io.hynix.command.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

import io.hynix.command.interfaces.Logger;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultiLogger implements Logger {

    final List<Logger> loggers;

    @Override
    public void log(String message) {
        for (Logger logger : loggers) {
            logger.log(message);
        }
    }
}

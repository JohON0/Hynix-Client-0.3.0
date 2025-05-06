package io.hynix.command.api;

import io.hynix.command.interfaces.AdviceCommandFactory;
import io.hynix.command.interfaces.CommandProvider;
import io.hynix.command.interfaces.Logger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommandFactoryImpl implements AdviceCommandFactory {

    final Logger logger;
    @Override
    public AdviceCommand adviceCommand(CommandProvider commandProvider) {
        return new AdviceCommand(commandProvider, logger);
    }
}

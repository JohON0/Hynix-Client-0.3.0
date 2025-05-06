package io.hynix.command.interfaces;

import io.hynix.command.api.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}

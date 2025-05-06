package io.hynix.command.interfaces;

import io.hynix.command.api.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}

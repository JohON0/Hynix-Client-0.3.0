package io.hynix.command.api;

import io.hynix.command.interfaces.Parameters;
import io.hynix.command.interfaces.ParametersFactory;

public class ParametersFactoryImpl implements ParametersFactory {

    @Override
    public Parameters createParameters(String message, String delimiter) {
        return new ParametersImpl(message.split(delimiter));
    }
}

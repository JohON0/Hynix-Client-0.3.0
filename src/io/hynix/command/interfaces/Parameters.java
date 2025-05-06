package io.hynix.command.interfaces;

import java.util.Optional;

public interface Parameters {

    Optional<Integer> asInt(int index);

    Optional<Float> asFloat(int index);

    Optional<Double> asDouble(int index);


    Optional<String> asString(int index);

    String collectMessage(int startIndex);
}

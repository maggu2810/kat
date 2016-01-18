package de.maggu2810.kat.kce.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Constants {

    public static final String METHOD_PREFIX = "_";

    public static final Set<String> METHOD_NAMES_SKIP;

    static {
        final Set<String> methodsSkip = new HashSet<>();

        methodsSkip.add(String.format("%s%s", METHOD_PREFIX, "help"));

        METHOD_NAMES_SKIP = Collections.unmodifiableSet(methodsSkip);
    }

    public static final String COMMAND_HELP = "help";

    private Constants() {

    }

}

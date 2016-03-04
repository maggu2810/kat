/*
 * #%L
 * KAT :: Karaf Console Equinox Gateway
 * %%
 * Copyright (C) 2016 maggu2810
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

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

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

import java.lang.reflect.Method;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class CommandWrapperMethod extends CommandWrapper {

    private final Method method;

    /**
     * Constructor.
     *
     * @param commandProvider the command provider
     * @param method the method
     */
    public CommandWrapperMethod(final CommandProvider commandProvider, final Method method) {
        super(commandProvider);
        this.method = method;
    }

    @Override
    protected void execute(final CommandInterpreter interpreter) {
        try {
            method.invoke(commandProvider, interpreter);
        } catch (final Exception ex) {
            interpreter.printStackTrace(ex);
        }
    }

    @Override
    public String getDescription() {
        // return commandProvider.getHelp();
        return String.format("Call '%s' method of same scope.", Constants.COMMAND_HELP);
    }

    @Override
    public String getName() {
        // Remove first character (_) of method name.
        return method.getName().substring(Constants.METHOD_PREFIX.length());
    }

    @Override
    public String toString() {
        return "CommandWrapperMethod [method=" + method + ", toString()=" + super.toString() + "]";
    }

}

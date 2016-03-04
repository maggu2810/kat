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

import java.util.List;
import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Parser;
import org.apache.karaf.shell.api.console.Session;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public abstract class CommandWrapper implements Command {

    protected final CommandProvider commandProvider;

    /**
     * Constructor.
     *
     * @param commandProvider the command provider
     */
    public CommandWrapper(final CommandProvider commandProvider) {
        this.commandProvider = commandProvider;
    }

    @Override
    public final Object execute(final Session session, final List<Object> argList) {
        final CustomCommandInterpreter commandInterpreter = new CustomCommandInterpreter(session, argList);

        try {
            execute(commandInterpreter);
        } catch (final Exception ex) {
            commandInterpreter.printStackTrace(ex);
        }

        return null;
    }

    protected abstract void execute(final CommandInterpreter interpreter);

    @Override
    public final Completer getCompleter(final boolean arg0) {
        return null;
    }

    @Override
    public final Parser getParser() {
        return null;
    }

    @Override
    public final String getScope() {
        return "equinox." + commandProvider.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "CommandWrapper [scope=" + getScope() + ", name=" + getName() + ", commandProvider=" + commandProvider
                + "]";
    }

    @Override
    public abstract String getDescription();

    @Override
    public abstract String getName();

}

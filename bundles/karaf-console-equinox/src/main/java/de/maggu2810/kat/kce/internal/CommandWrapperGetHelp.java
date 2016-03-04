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

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class CommandWrapperGetHelp extends CommandWrapper {

    public CommandWrapperGetHelp(final CommandProvider commandProvider) {
        super(commandProvider);
    }

    @Override
    protected void execute(final CommandInterpreter interpreter) {
        try {
            interpreter.println(commandProvider.getHelp());
        } catch (final Exception ex) {
            interpreter.printStackTrace(ex);
        }
    }

    @Override
    public String getDescription() {
        // return commandProvider.getHelp();
        return "Call myself.";
    }

    @Override
    public String getName() {
        return Constants.COMMAND_HELP;
    }

}

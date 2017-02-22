/*
 * #%L
 * KAT :: Karaf Console Equinox Gateway
 * %%
 * Copyright (C) 2016 - 2017 maggu2810
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package de.maggu2810.kat.kce.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class CommandWrapperGetHelp extends CommandWrapper {

    /**
     * Constructor.
     *
     * @param commandProvider the command provider
     */
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

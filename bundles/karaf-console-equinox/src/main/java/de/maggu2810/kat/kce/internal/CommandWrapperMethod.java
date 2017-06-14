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

import java.lang.reflect.InvocationTargetException;
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
        } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
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

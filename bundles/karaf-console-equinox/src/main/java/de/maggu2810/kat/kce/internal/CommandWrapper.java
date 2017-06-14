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

import java.util.List;

import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.api.console.Completer;
import org.apache.karaf.shell.api.console.Parser;
import org.apache.karaf.shell.api.console.Session;
import org.checkerframework.checker.nullness.qual.Nullable;
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
    public final @Nullable Object execute(final Session session, final List<Object> argList) {
        execute(new CustomCommandInterpreter(session, argList));
        return null;
    }

    protected abstract void execute(final CommandInterpreter interpreter);

    @Override
    public final @Nullable Completer getCompleter(final boolean arg0) {
        return null;
    }

    @Override
    public final @Nullable Parser getParser() {
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

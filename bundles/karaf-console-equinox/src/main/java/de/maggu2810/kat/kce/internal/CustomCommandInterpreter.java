/*-
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.karaf.shell.api.console.Session;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;

public class CustomCommandInterpreter implements CommandInterpreter {

    /**
     * Let's write all to {@code System.out}.
     */
    private final PrintStream out = System.out;

    private final Iterator<Object> arguments;
    private final Session commandSession;

    /**
     * Constructor.
     *
     * @param commandSession the command session
     * @param args the arguments
     */
    public CustomCommandInterpreter(final Session commandSession, final List<Object> args) {
        this.commandSession = commandSession;
        this.arguments = args.iterator();
    }

    @Override
    public @Nullable Object execute(final String cmd) {
        try {
            return commandSession.execute(cmd);
        } catch (final Exception e) {
            // CHECKSTYLE - OK to catch Exception here
            throw new RuntimeException(e);
        }
    }

    @Override
    public @Nullable String nextArgument() {
        if (arguments.hasNext()) {
            final Object next = arguments.next();
            return next == null ? null : next.toString();
        }
        return null;
    }

    @Override
    public void print(final Object obj) {
        out.print(obj);
    }

    @Override
    public void println() {
        out.println();
    }

    @Override
    public void println(final Object obj) {
        out.println(obj);
    }

    @Override
    public void printStackTrace(final Throwable throwable) {
        throwable.printStackTrace(out);

        for (Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause()) {
            out.println("Nested Exception");
            cause.printStackTrace(out);
        }
    }

    @Override
    public void printDictionary(final Dictionary<?, ?> dic, final String title) {
        if (dic == null) {
            return;
        }

        final TreeMap<Object, @Nullable Object> map = new TreeMap<>();
        final Enumeration<?> keysEnum = dic.keys();
        while (keysEnum.hasMoreElements()) {
            final Object key = keysEnum.nextElement();
            if (key == null) {
                continue;
            }
            final Object value = dic.get(key);
            map.put(key, value);
        }

        if (title != null) {
            println(title);
        }
        for (final Entry<Object, @Nullable Object> entry : map.entrySet()) {
            println(" " + entry.getKey() + " = " + entry.getValue());
        }
        println();
    }

    @Override
    public void printBundleResource(final Bundle bundle, final String resource) {
        final URL entry = bundle.getEntry(resource);
        if (entry != null) {
            try {
                println(resource);

                try (final InputStream in = entry.openStream()) {
                    final byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = in.read(buffer)) != -1) {
                        print(new String(buffer, 0, read, Charset.defaultCharset()));
                    }
                }
            } catch (final IOException ex) {
                println();
                println(String.format("!! ERROR: reading resource (%s) from bundle (%s) failed.", resource, bundle));
                printStackTrace(ex);
            }
        } else {
            println(String.format("!! ERROR: the resource (%s) is not in bundle (%s).", resource, bundle));
        }
    }

}

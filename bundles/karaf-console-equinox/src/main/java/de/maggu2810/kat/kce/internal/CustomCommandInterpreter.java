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
/* ******************************************************************************
 * Copyright (c) 2003, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lazar Kirchev, SAP AG - derivative implementation from FrameworkCommandInterpreter
 *     Markus Rathgeb - fit the original implementation to the karaf-equinox-console project
 * *****************************************************************************/

/*
 * This is a modified version of the org.eclipse.equinox.console.command.adapter.CustomCommandInterpreter
 */

package de.maggu2810.kat.kce.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import org.apache.karaf.shell.api.console.Session;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;

public class CustomCommandInterpreter implements CommandInterpreter {

    private final PrintStream out = System.out;

    /**
     * Strings used to format other strings.
     */
    private static final String tab = "\t"; //$NON-NLS-1$
    private static final String newline = "\r\n"; //$NON-NLS-1$
    private final Iterator<Object> arguments;
    private final Session commandSession;

    /**
     * The maximum number of lines to print without user prompt.
     *
     * <p>
     * 0 means no user prompt is required, the window is scrollable.
     */
    private static final int maxLineCount = 0;

    /**
     * The number of lines printed without user prompt.
     */
    protected int currentLineCount;

    /**
     * Constructor.
     *
     * @param commandSession the command session
     * @param args the arguments
     */
    public CustomCommandInterpreter(final Session commandSession, final List<Object> args) {
        this.commandSession = commandSession;
        arguments = args.iterator();
    }

    @Override
    public Object execute(final String cmd) {
        try {
            return commandSession.execute(cmd);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String nextArgument() {
        if (arguments.hasNext()) {
            final Object next = arguments.next();
            return next == null ? null : next.toString();
        }
        return null;
    }

    /**
     * Prints an object to the output stream.
     *
     * @param obj the object to be printed
     */
    @Override
    public void print(final Object obj) {
        check4More();
        out.print(obj);
        out.flush();
    }

    /**
     * Prints a empty line to the output stream.
     */
    @Override
    public void println() {
        println(""); //$NON-NLS-1$
    }

    /**
     * Prints an object to the output medium (appended with newline character).
     *
     * <p>
     * If running on the target environment, the user is prompted with '--more' if more than the configured number of
     * lines have been printed without user prompt. This enables the user of the program to have control over scrolling.
     *
     * <p>
     * For this to work properly you should not embed "\n" etc. into the string.
     *
     * @param obj the object to be printed
     */
    @Override
    public void println(final Object obj) {
        if (obj == null) {
            return;
        }
        synchronized (out) {
            check4More();
            printline(obj);
            currentLineCount++;
            currentLineCount += obj.toString().length() / 80;
        }
    }

    /**
     * Print a stack trace including nested exceptions.
     *
     * @param throwable The offending exception
     */
    @Override
    public void printStackTrace(final Throwable throwable) {
        throwable.printStackTrace(out);

        final Method[] methods = throwable.getClass().getMethods();

        final int size = methods.length;
        final Class<Throwable> throwableCass = Throwable.class;

        for (int i = 0; i < size; i++) {
            final Method method = methods[i];

            if (Modifier.isPublic(method.getModifiers()) && method.getName().startsWith("get") //$NON-NLS-1$
                    && throwableCass.isAssignableFrom(method.getReturnType()) && method.getParameterTypes().length == 0) {
                try {
                    final Throwable nested = (Throwable) method.invoke(throwable, (Object) null);

                    if (nested != null && nested != throwable) {
                        out.println("Nested Exception");
                        printStackTrace(nested);
                    }
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    System.err.println(e);
                }
            }
        }
    }

    /**
     * Prints a string to the output medium (appended with newline character).
     *
     * <p>
     * This method does not increment the line counter for the 'more' prompt.
     *
     * @param str the string to be printed
     */
    private void printline(final Object str) {
        print(str + newline);
    }

    /**
     * Prints the given dictionary sorted by keys.
     *
     * @param dic the dictionary to print
     * @param title the header to print above the key/value pairs
     */
    @Override
    public void printDictionary(final Dictionary<?, ?> dic, final String title) {
        if (dic == null) {
            return;
        }

        final int count = dic.size();
        final String[] keys = new String[count];
        final Enumeration<?> keysEnum = dic.keys();
        int i = 0;
        while (keysEnum.hasMoreElements()) {
            keys[i++] = (String) keysEnum.nextElement();
        }
        Arrays.sort(keys);

        if (title != null) {
            println(title);
        }
        for (i = 0; i < count; i++) {
            println(" " + keys[i] + " = " + dic.get(keys[i])); //$NON-NLS-1$//$NON-NLS-2$
        }
        println();
    }

    /**
     * Prints the given bundle resource if it exists.
     *
     * @param bundle the bundle containing the resource
     * @param resource the resource to print
     */
    @Override
    public void printBundleResource(final Bundle bundle, final String resource) {
        URL entry = null;
        entry = bundle.getEntry(resource);
        if (entry != null) {
            try {
                println(resource);
                final InputStream in = entry.openStream();
                final byte[] buffer = new byte[1024];
                int read = 0;
                try {
                    while ((read = in.read(buffer)) != -1) {
                        print(new String(buffer, 0, read, Charset.defaultCharset()));
                    }
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (final IOException e) {
                            System.err.println(e);
                        }
                    }
                }
            } catch (final IOException e) {
                System.err.println(e);
            }
        } else {
            println("CONSOLE_RESOURCE [" + resource + "] NOT_IN_BUNDLE " + bundle.toString());
        }
    }

    /**
     * Answers the number of lines output to the console window should scroll without user interaction.
     *
     * @return The number of lines to scroll.
     */
    private int getMaximumLinesToScroll() {
        return maxLineCount;
    }

    /**
     * Displays the more... prompt if the max line count has been reached and waits for the operator to hit enter.
     *
     */
    private void check4More() {
        final int max = getMaximumLinesToScroll();
        if (max > 0 && currentLineCount >= max) {
            out.print("-- More...Press Enter to Continue...");
            out.flush();
            try {
                System.in.read();
            } catch (final IOException e) {
                e.printStackTrace();
            } // wait for user entry
            resetLineCount(); // Reset the line counter for the 'more' prompt
        }
    }

    /**
     * Resets the line counter for the 'more' prompt.
     */
    private void resetLineCount() {
        currentLineCount = 0;
    }

    /**
     * Answer a string (may be as many lines as you like) with help texts that explain the command.
     *
     * @return the help texts
     */
    public String getHelp() {
        final StringBuffer help = new StringBuffer(256);
        help.append("---Controlling the Console---");
        help.append(newline);
        help.append(tab);
        help.append("more - "); //$NON-NLS-1$
        help.append("More prompt for console output");
        help.append(newline);
        help.append(tab);
        help.append("disconnect - "); //$NON-NLS-1$
        help.append("isconnects from telnet session");
        help.append(newline);
        return help.toString();
    }
}

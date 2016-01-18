package de.maggu2810.kat.kce.internal;

import java.lang.reflect.Method;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

public class CommandWrapperMethod extends CommandWrapper {

    private final Method method;

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

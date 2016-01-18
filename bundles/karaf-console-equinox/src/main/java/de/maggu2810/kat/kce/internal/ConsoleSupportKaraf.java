package de.maggu2810.kat.kce.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.karaf.shell.api.console.Registry;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class ConsoleSupportKaraf {

    private final Logger logger = LoggerFactory.getLogger(ConsoleSupportKaraf.class);

    // This map contains all known session factories.
    private final Set<SessionFactory> sessionFactories = new HashSet<>();

    // This map contains all commands and that wrappers.
    private final Map<CommandProvider, List<CommandWrapper>> commands = new HashMap<>();

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeSessionFactory")
    protected void addSessionFactory(final SessionFactory sessionFactory) {
        sessionFactories.add(sessionFactory);
        register(sessionFactory.getRegistry());
    }

    protected void removeSessionFactory(final SessionFactory sessionFactory) {
        if (sessionFactories.remove(sessionFactory)) {
            unregister(sessionFactory.getRegistry());
        }
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, unbind = "removeCommandProvider")
    protected void addCommandProvider(final CommandProvider commandProvider) {
        final List<CommandWrapper> wrappersNew;
        final List<CommandWrapper> wrappersOld;

        wrappersNew = createCommandWrappers(commandProvider);
        wrappersOld = commands.put(commandProvider, wrappersNew);

        if (wrappersOld != null) {
            unregister(sessionFactories, wrappersOld);
        }

        register(sessionFactories, wrappersNew);
    }

    protected void removeCommandProvider(final CommandProvider commandProvider) {
        final List<CommandWrapper> wrappersOld = commands.remove(commandProvider);

        if (wrappersOld != null) {
            unregister(sessionFactories, wrappersOld);
        }
    }

    private Method[] getCommandMethods(final Object command) {
        final ArrayList<Method> names = new ArrayList<>();
        final Class<?> c = command.getClass();
        final Method[] methods = c.getDeclaredMethods();
        for (final Method method : methods) {
            final String methodName = method.getName();
            if (method.getModifiers() == Modifier.PUBLIC && methodName.startsWith(Constants.METHOD_PREFIX)
                    && !Constants.METHOD_NAMES_SKIP.contains(methodName)) {
                final Type[] types = method.getGenericParameterTypes();
                if (types.length == 1 && types[0].equals(CommandInterpreter.class)) {
                    names.add(method);
                }
            }
        }
        return names.toArray(new Method[names.size()]);
    }

    private List<CommandWrapper> createCommandWrappers(final CommandProvider commandProvider) {
        final List<CommandWrapper> wrappers = new LinkedList<>();

        wrappers.add(new CommandWrapperGetHelp(commandProvider));

        for (final Method method : getCommandMethods(commandProvider)) {
            final CommandWrapper commandWrapper = new CommandWrapperMethod(commandProvider, method);

            wrappers.add(commandWrapper);
        }

        return wrappers;
    }

    private void register(final Collection<SessionFactory> sessionFactories,
            final Collection<CommandWrapper> wrappers) {
        for (final SessionFactory sessionFactory : sessionFactories) {
            register(sessionFactory.getRegistry(), wrappers);
        }
    }

    private void register(final Registry registry) {
        register(registry, commands);
    }

    private void register(final Registry registry, final Map<CommandProvider, List<CommandWrapper>> commands) {
        for (final List<CommandWrapper> wrappers : commands.values()) {
            register(registry, wrappers);
        }
    }

    private void register(final Registry registry, final Collection<CommandWrapper> wrappers) {
        for (final CommandWrapper wrapper : wrappers) {
            register(registry, wrapper);
        }
    }

    private void register(final Registry registry, final CommandWrapper wrapper) {
        try {
            registry.register(wrapper);
        } catch (final Exception ex) {
            logger.error("Cannot register command '{}'.", wrapper, ex);
        }
    }

    private void unregister(final Collection<SessionFactory> sessionFactories,
            final Collection<CommandWrapper> wrappers) {
        for (final SessionFactory sessionFactory : sessionFactories) {
            unregister(sessionFactory.getRegistry(), wrappers);
        }
    }

    private void unregister(final Registry registry) {
        unregister(registry, commands);
    }

    private void unregister(final Registry registry, final Map<CommandProvider, List<CommandWrapper>> commands) {
        for (final List<CommandWrapper> wrappers : commands.values()) {
            unregister(registry, wrappers);
        }
    }

    private void unregister(final Registry registry, final Collection<CommandWrapper> wrappers) {
        for (final CommandWrapper wrapper : wrappers) {
            unregister(registry, wrapper);
        }
    }

    private void unregister(final Registry registry, final CommandWrapper wrapper) {
        try {
            registry.unregister(wrapper);
        } catch (final Exception ex) {
            logger.error("Cannot unregister command '{}'.", wrapper, ex);
        }
    }

}

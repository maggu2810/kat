# Short Description

If you are using Karaf you could extend your Karaf console with custom commands.

For example the Apache Felix SCR bundle supports this infrastructure to fetch information about the services of the current running OSGi container.

Also the Eclipse Equinox OSGi framework suffers a service, so you could implement a CommandProvider and add console commands to the Equinox console.

But if you are using Karaf and the Equinox OSGi framework you will only get access to the Karaf console commands extensions and not the extensions using the Equinox services.

Using this bundle you will be able to use the Equinox OSGi command providers on your Karaf console.

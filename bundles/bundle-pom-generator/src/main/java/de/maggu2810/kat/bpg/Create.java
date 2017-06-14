/*
 * #%L
 * KAT :: Bundle POM Generator
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

package de.maggu2810.kat.bpg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.apache.karaf.bundle.core.BundleInfo;
import org.apache.karaf.bundle.core.BundleService;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Karaf command to generate a POM file that contains the bundles of the current runtime as dependencies.
 *
 * @author Markus Rathgeb
 */
@Command(scope = "kat", name = "bpg", description = "bundle pom generator")
@Service
public class Create implements Action {

    private final Logger logger = LoggerFactory.getLogger(Create.class);

    @Option(name = "--context", aliases = { "-c" }, description = "Use the given bundle context")
    String context = "0";

    @Option(name = "-o", valueToShowInHelp = "",
            description = "Specified the location for the output project (directory will be created if necessary and a pom.xml is placed in), otherwise stdout is used.",
            required = false, multiValued = false)
    @Nullable
    String outPath = null;

    @Argument(index = 0, name = "ids",
            description = "The list of bundle (identified by IDs or name or name/version) separated by whitespaces",
            required = false, multiValued = true)
    @Nullable
    List<String> ids;

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized") // injected
    BundleContext bundleContext;

    @Reference
    @SuppressWarnings("initialization.fields.uninitialized") // injected
    BundleService bundleService;

    @Override
    public @Nullable Object execute() throws Exception {
        final Model model = new Model();

        // Add some hardcoded configuration
        model.setModelVersion("4.0.0");
        model.setGroupId("tmp");
        model.setArtifactId("tmp");
        model.setVersion("1.0.0-SNAPSHOT");

        // Collect bundles of interest
        final List<Bundle> bundles = bundleService.selectBundles(context, ids, true);

        for (final Bundle bundle : bundles) {
            final Dependency dep = new Dependency();
            if (handleBundle(bundle, dep)) {
                dep.setScope("provided");
                model.addDependency(dep);
            }
        }

        // Write model
        writeModel(model, Optional.ofNullable(outPath));

        return null;
    }

    /**
     * Write a Maven model.
     *
     * @param model the model that should be written
     * @param directory If present the directory a POM file should be created in otherwise the model is written to
     *            {@code System#out}
     * @throws IOException on error
     */
    private static void writeModel(final Model model, final Optional<String> directory) throws IOException {
        if (directory.isPresent()) {
            final File outDir = new File(directory.get());
            if (outDir.exists()) {
                if (!outDir.isDirectory()) {
                    throw new IOException(
                            String.format("The output path (%s) exists but is not a directory.", directory));
                }
            } else {
                if (!outDir.mkdirs()) {
                    throw new IOException(String.format("Cannot create directory (%s).", directory));
                }
            }
            try (final OutputStream out = new FileOutputStream(Paths.get(directory.get(), "pom.xml").toFile())) {
                writeModel(model, out);
            }
        } else {
            writeModel(model, System.out);
        }
    }

    /**
     * Write a Maven model to an output stream.
     *
     * @param model the model that should be written
     * @param out the output stream
     * @throws IOException on error
     */
    private static void writeModel(final Model model, final OutputStream out) throws IOException {
        final MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(out, model);
    }

    private boolean handleBundle(final Bundle bundle, final Dependency dep) {
        // Cannot use update location for system bundle, so it must be handled explicit.
        if (bundle.getBundleId() == 0) {
            return handleSystemBundle(bundle, dep);
        } else {
            return handleNonSystemBundle(bundle, dep);
        }
    }

    private boolean handleSystemBundle(final Bundle bundle, final Dependency dep) {
        final String bsn = bundle.getSymbolicName();
        final Version version = bundle.getVersion();

        switch (bsn) {
            case "org.apache.felix.framework":
                dep.setGroupId("org.apache.felix");
                dep.setArtifactId("org.apache.felix.framework");
                dep.setVersion(version.toString());
                return true;
            case "org.eclipse.osgi":
                dep.setGroupId("org.eclipse.birt.runtime");
                dep.setArtifactId("org.eclipse.osgi");
                dep.setVersion(version.toString());
                return true;
            default:
                logger.warn("Cannot handle system bundle: bsn={}, version={}, loc={}", bsn, version,
                        bundle.getLocation());
                return false;
        }
    }

    private boolean handleNonSystemBundle(final Bundle bundle, final Dependency dep) {
        final BundleInfo info = this.bundleService.getInfo(bundle);
        String updateLocation = info.getUpdateLocation();

        // wrap is used to generate OSGi bundles from another URL, so remove wrap...
        if (updateLocation.startsWith("wrap:")) {
            updateLocation = updateLocation.replaceFirst("wrap:", "");
            final int wrapInstructionIndex = updateLocation.indexOf("$");
            if (wrapInstructionIndex != -1) {
                updateLocation = updateLocation.substring(0, wrapInstructionIndex);
            }
        }

        if (updateLocation.startsWith("mvn:")) {
            return handleBundleLocMvn(dep, updateLocation);
        } else {
            logger.warn("Cannot handle update location {}", updateLocation);
            return false;
        }
    }

    private boolean handleBundleLocMvn(final Dependency dep, final String updateLocation) {
        final String gav = updateLocation.replaceFirst("mvn:", "");
        final String[] gavSplit = gav.split("/");
        if (gavSplit.length < 3) {
            logger.warn("Cannot handle mvn update location (not enough parts): {}", updateLocation);
            return false;
        }

        final String groupId = gavSplit[0];
        final String artifactId = gavSplit[1];
        final String version = gavSplit[2];

        dep.setGroupId(groupId);
        dep.setArtifactId(artifactId);
        dep.setVersion(version);

        switch (gavSplit.length) {
            case 3:
                return true;
            case 5:
                final String type = gavSplit[3];
                final String classifier = gavSplit[4];
                dep.setType(type);
                dep.setClassifier(classifier);
                return true;
            default:
                logger.warn("Cannot handle a length of: {} ({})", gavSplit.length, updateLocation);
                return false;
        }
    }
}

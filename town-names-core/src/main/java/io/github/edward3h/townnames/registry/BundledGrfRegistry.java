package io.github.edward3h.townnames.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Provides the set of NewGRF files bundled inside this JAR.
 *
 * <p>Populated at runtime by scanning the {@code /grf/} directory on the classpath. The stem of
 * each {@code .grf} filename (without extension) is the name exposed to callers.
 */
public final class BundledGrfRegistry {

    private static final BundledGrfRegistry INSTANCE = new BundledGrfRegistry();

    private final List<String> names;

    private BundledGrfRegistry() {
        names = List.copyOf(discoverNames());
    }

    public static BundledGrfRegistry getInstance() {
        return INSTANCE;
    }

    /** Returns the names of all bundled GRF files (without the .grf extension). */
    public List<String> availableNames() {
        return names;
    }

    /**
     * Opens an input stream for a bundled GRF by name.
     *
     * @throws IllegalArgumentException if the name is not in {@link #availableNames()}
     */
    public InputStream open(String name) {
        var stream = getClass().getResourceAsStream("/grf/" + name + ".grf");
        if (stream == null) {
            throw new IllegalArgumentException("No bundled GRF found for name: " + name);
        }
        return stream;
    }

    private static List<String> discoverNames() {
        var result = new ArrayList<String>();
        try {
            Enumeration<URL> resources =
                    BundledGrfRegistry.class.getClassLoader().getResources("grf");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                // For jar: URLs, list entries; for file: URLs, list directory
                if ("jar".equals(url.getProtocol())) {
                    result.addAll(listFromJar(url));
                } else {
                    result.addAll(listFromDirectory(url));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan bundled GRF resources", e);
        }
        Collections.sort(result);
        return result;
    }

    private static List<String> listFromJar(URL url) throws IOException {
        var names = new ArrayList<String>();
        String spec = url.toString();
        // e.g. jar:file:/path/to/jar.jar!/grf
        String jarPath = spec.substring("jar:".length(), spec.indexOf("!/"));
        try (var jar = new java.util.jar.JarFile(new java.io.File(new java.net.URI(jarPath)))) {
            jar.stream()
                    .filter(e -> e.getName().startsWith("grf/") && e.getName().endsWith(".grf"))
                    .map(e -> stem(e.getName().substring("grf/".length())))
                    .forEach(names::add);
        } catch (java.net.URISyntaxException e) {
            throw new IOException("Malformed jar URL: " + jarPath, e);
        }
        return names;
    }

    private static List<String> listFromDirectory(URL url) throws IOException {
        var dir = new java.io.File(url.getPath());
        if (!dir.isDirectory()) return List.of();
        var files = dir.listFiles((d, name) -> name.endsWith(".grf"));
        if (files == null) return List.of();
        return java.util.Arrays.stream(files).map(f -> stem(f.getName())).toList();
    }

    private static String stem(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }
}

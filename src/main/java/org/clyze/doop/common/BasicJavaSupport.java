package org.clyze.doop.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import org.objectweb.asm.ClassReader;

/**
 * This class gathers Java-specific code (such as JAR handling).
 */
public abstract class BasicJavaSupport {

    protected Set<String> classesInApplicationJars;
    protected Set<String> classesInLibraryJars;
    protected Set<String> classesInDependencyJars;
    private Map<String, Set<ArtifactEntry>> artifactToClassMap;
    private PropertyProvider propertyProvider;

    public BasicJavaSupport(Map<String, Set<ArtifactEntry>> artifactToClassMap, PropertyProvider propertyProvider) {
        this.classesInApplicationJars = new HashSet<>();
        this.classesInLibraryJars = new HashSet<>();
        this.classesInDependencyJars = new HashSet<>();
        this.artifactToClassMap = artifactToClassMap;
        this.propertyProvider = propertyProvider;
    }

    /**
     * Process a JAR input.
     * @param classSet   appropriate set to add class names
     * @param filename   the JAR filename
     */
    protected void processJar(Set<String> classSet, String filename) throws IOException {
        JarEntry entry;
        try (JarInputStream jin = new JarInputStream(new FileInputStream(filename));
             JarFile jarFile = new JarFile(filename)) {
            /* List all JAR entries */
            while ((entry = jin.getNextJarEntry()) != null) {
                /* Skip directories */
                if (entry.isDirectory())
                    continue;

                String entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    try {
                        ClassReader reader = new ClassReader(jarFile.getInputStream(entry));
                        String className = reader.getClassName().replace("/", ".");
                        classSet.add(className);
                        String artifact = (new File(jarFile.getName())).getName();
                        registerArtifactClass(artifact, className, "-");
                    } catch (IllegalArgumentException e) {
                        System.err.println("-- Problematic .class file \"" + entryName + "\"");
                    }
                } else if (entryName.endsWith(".properties")) {
                    propertyProvider.addProperties(jarFile.getInputStream(entry), filename);
                } /* Skip non-class files and non-property files */
            }
        }
    }

    /**
     * Registers a class with its container artifact.
     * @param artifact     the file name of the artifact containing the class
     * @param className    the name of the class
     * @param subArtifact  the sub-artifact (such as "classes.dex" for APKs)
     */
    protected void registerArtifactClass(String artifact, String className, String subArtifact) {
        ArtifactEntry ae = new ArtifactEntry(className, subArtifact);
        if (!artifactToClassMap.containsKey(artifact)) {
            Set<ArtifactEntry> artifactClasses = new HashSet<>();
            artifactClasses.add(ae);
            artifactToClassMap.put(artifact, artifactClasses);
        } else
            artifactToClassMap.get(artifact).add(ae);
    }

}

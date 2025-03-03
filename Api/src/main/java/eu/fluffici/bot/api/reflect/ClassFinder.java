package eu.fluffici.bot.api.reflect;

/*
---------------------------------------------------------------------------------
File Name : ClassFinder.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/



import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ClassFinder {
    private final List<Class<?>> classes = new ArrayList<>();

    /**
     * Finds all classes in the specified package and its sub-packages.
     * @param packageName the name of the package
     * @throws ClassNotFoundException if the package or any of its sub-packages is not found
     */
    public void find(String packageName) throws ClassNotFoundException {
        String path = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        File dir = new File(classLoader.getResource(path).getFile());
        if (dir.exists()) {
            findClasses(dir, packageName);
        } else {
            throw new ClassNotFoundException("Package " + packageName + " not found");
        }
    }

    /**
     * Recursively finds all classes in the specified directory and its sub-directories,
     * using the specified package name as the package prefix.
     *
     * @param directory   the directory to search for classes
     * @param packageName the name of the package
     * @throws ClassNotFoundException if the package or any of its sub-packages is not found
     */
    private void findClasses(File directory, String packageName) throws ClassNotFoundException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findClasses(file, packageName + "." + file.getName());
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                }
            }
        }
    }
}

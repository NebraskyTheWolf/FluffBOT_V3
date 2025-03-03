package eu.fluffici.bot.api.reflect;

/*
---------------------------------------------------------------------------------
File Name : ReflectionUtil.java

Developer : vakea
Email     : vakea@fluffici.eu
Real Name : Alex Guy Yann Le Roy

Date Created  : 02/06/2024
Last Modified : 02/06/2024

---------------------------------------------------------------------------------
*/

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReflectionUtil {
    /**
     * Instantiate classes that are annotated with a specific annotation.
     *
     * @param instance        the instance to pass as an argument to the classes' constructors
     * @param classLoader     the class loader to use for scanning classes
     * @param packageName     the name of the package to scan for classes
     * @param annotationClass the class representing the annotation to look for
     * @return a list of instantiated objects that are annotated with the specified annotation
     * @throws RuntimeException if a class could not be instantiated
     */
    public static List<Object> instantiateClassesWithCommandAnnotation(Object instance, ClassLoader classLoader, String packageName, Class<? extends Annotation> annotationClass) {
        ConfigurationBuilder config = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(packageName, classLoader))
                .addScanners(Scanners.TypesAnnotated);

        Reflections reflections = new Reflections(config);

        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(annotationClass);

        return annotatedClasses.stream().map(clazz -> {
            try {
                return clazz.getDeclaredConstructor(instance.getClass()).newInstance(instance);
            } catch (NoSuchMethodException e) {
                try {
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to instantiate class", ex);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to instantiate class", e);
            }
        }).collect(Collectors.toList());
    }
}

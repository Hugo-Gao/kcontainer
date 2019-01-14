package com.codemacro.container;

import com.codemacro.container.util.UnzipJar;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

// bundle-name/classes
// bundle-name/lib/*.jar
// bundle-name/lib/*.jar (with inner jars)
public class BundleClassLoader extends URLClassLoader {
    private static Logger logger = LoggerFactory.getLogger(BundleClassLoader.class);
    private static final String TMP_PATH = new File("./tmp").getAbsolutePath();
    private SharedClassList sharedClasses;

    public BundleClassLoader(File home, SharedClassList sharedClasses) throws MalformedURLException {
        super(getClassPath(home));
        this.sharedClasses = sharedClasses;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        logger.debug("try find class {}", name);
        Class<?> claz = null;
        try {
            claz = super.findClass(name);
        } catch (ClassNotFoundException e) {
            claz = null;
        }
        if (claz != null) {
            logger.debug("load from class path for {}", name);
            return claz;
        }
        claz = sharedClasses.get(name);
        if (claz != null) {
            logger.debug("load from shared class for {}", name);
            return claz;
        }
        logger.error("not found class {}", name);
        throw new ClassNotFoundException(name);
    }

    private static URL[] getClassPath(File home) throws MalformedURLException {
        File lib = new File(home.getAbsoluteFile() + "/lib");
        File[] jars = lib.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".jar");
            }
        });
        List<URL> col_urls = Lists.newArrayList();
        if (jars != null) {
            for (File jar : jars) {
                col_urls.add(jar.toURL());
                // add inner jar to class path
                if (UnzipJar.hasEntry(jar.getAbsolutePath(), ".jar")) {
                    List<File> inner_jars = extractInnerJars(home.getName(), jar.getAbsolutePath());
                    if (inner_jars != null) {
                        for (File inner_jar : inner_jars) {
                            col_urls.add(inner_jar.toURL());
                        }
                    }
                }
            }
        }
        String classes_path = home.getAbsolutePath() + "/classes/";

        File classes = new File(classes_path);
        if (classes.exists()) {
            col_urls.add(classes.toURL());
        }
        URL[] urls = new URL[col_urls.size()];
        col_urls.toArray(urls);
        return urls;

//        return Lists.newArrayList(Collections2.filter(col_urls, new Predicate<URL>() {
//            public boolean apply(URL url) {
//                return url != null;
//            }
//        })).toArray(urls);
    }

    private static List<File> extractInnerJars(String name, String jarFile) {
        logger.debug("extract inner jars {}", jarFile);
        String dst = TMP_PATH + File.separator + name;
        try {
            return UnzipJar.unzipJar(dst, jarFile, ".jar");
        } catch (IOException e) {
            logger.warn("extract jar file {} failed", jarFile);
        }
        return null;
    }

//    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {
//        File jcm = new File("bundle/jcm");
//        for (URL u : BundleClassLoader.getClassPath(jcm)) {
//            System.err.println(u);
//        }
//        BundleClassLoader loader = new BundleClassLoader(jcm, null);
//        System.out.println(loader.loadClass("com.codemacro.jcm.util.Hash"));
//        System.out.println(loader.loadClass("com.codemacro.jcm.JCMMain"));
//        loader.close();
//    }
}

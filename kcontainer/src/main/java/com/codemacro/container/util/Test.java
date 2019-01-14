package com.codemacro.container.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author yunfan.gyf
 **/
public class Test {
    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException {
        File file = new File("/Users/gaoyunfan/code/kcontainer/kcontainer/bundle/test1/classes");
        URL url = file.toURL();
        URLClassLoader loader = new URLClassLoader(new URL[]{url});
        Class<?> aClass = loader.loadClass("com.codemacro.test.A");
        System.out.println(aClass);
    }
}

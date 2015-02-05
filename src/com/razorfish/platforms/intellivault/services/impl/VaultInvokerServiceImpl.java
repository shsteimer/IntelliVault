package com.razorfish.platforms.intellivault.services.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.razorfish.platforms.intellivault.exceptions.IntelliVaultException;
import com.razorfish.platforms.intellivault.services.VaultInvokerService;
import com.intellij.openapi.diagnostic.Logger;
import com.razorfish.platforms.intellivault.exceptions.IntelliVaultException;

/**
 * Created with IntelliJ IDEA. User: sean.steimer Date: 3/17/13 Time: 9:07 AM To
 * change this template use File | Settings | File Templates.
 */
public class VaultInvokerServiceImpl implements VaultInvokerService {
    private static final String VAULT_CLASS = "com.day.jcr.vault.cli.VaultFsApp";
    private static final String VAULT_METHOD = "main";
    public static final String LIB = "lib";
    public static final String BIN = "bin";

    private ClassLoader vaultClassLoader;
    private boolean init = false;

    private static final Logger log = Logger.getInstance(VaultInvokerServiceImpl.class);

    @Override
    public void invokeVault(String vaultDir, String[] args) throws IntelliVaultException {
        try {
            initVault(vaultDir);

            log.info("executing vlt with params: " + Arrays.toString(args));

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(vaultClassLoader);
                Class<?> vltClass = Class.forName(VAULT_CLASS, true, vaultClassLoader);
                Method vltMethod = vltClass.getMethod(VAULT_METHOD, new Class[] { new String[0].getClass() });
                vltMethod.invoke(null, new Object[] { args });
            }finally {
                Thread.currentThread().setContextClassLoader(cl);
            }


        } catch (ClassNotFoundException e) {
            throw new IntelliVaultException(e);
        } catch (NoSuchMethodException e) {
            throw new IntelliVaultException(e);
        } catch (InvocationTargetException e) {
            throw new IntelliVaultException(e);
        } catch (IllegalAccessException e) {
            throw new IntelliVaultException(e);
        } catch (IOException e) {
            throw new IntelliVaultException(e);
        }

    }

    @Override
    public void forceReInit() {
        init=false;
    }

    /**
     * Initialize vault.  Basically finds all the jars in the vault folder and creates a custom class loader which
     * includes those jars.  All vault operations are then executed using that class loader.
     * @param vaultDir the vault home directory as specified in the settings diaog.
     *                 Could be the root directory, or potentially the bin or lib directory.
     * @throws IOException if an error occurs, sucha s the vault directory not being set.
     */
    private void initVault(String vaultDir) throws IOException {
        if (!init) {
            if (vaultDir == null || vaultDir.trim().length() == 0) {
                throw new IOException("Vault Directory not set");
            }

            if (vaultDir.endsWith(BIN)) {
                vaultDir = vaultDir.substring(0, vaultDir.lastIndexOf(File.separator) - 1);
            }

            if (!vaultDir.endsWith(LIB)) {
                vaultDir += File.separator + LIB;
            }

            List<URL> libList = new ArrayList<URL>();
            File libDir = new File(vaultDir.replace('/', File.separatorChar));
            File[] libs = libDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jar")) || (name.endsWith(".zip"));
                }
            });
            if (libs != null) {
                for (int i = 0; i < libs.length; i++) {
                    try {
                        libList.add(libs[i].toURI().toURL());
                    } catch (IOException e) {
                        log.error("error loading lib " + libs[i].getAbsolutePath() ,e);
                    }
                }
            }

            vaultClassLoader = new URLClassLoader(libList.toArray(new URL[libList.size()]));
            init = true;
        }
    }
}

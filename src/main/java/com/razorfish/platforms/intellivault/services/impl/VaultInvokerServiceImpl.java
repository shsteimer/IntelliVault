package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.razorfish.platforms.intellivault.exceptions.IntelliVaultException;
import com.razorfish.platforms.intellivault.services.VaultInvokerService;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Vault Invoker Service which handles actually calling vault to do import/export operations and dealing with the
 * class loaders to do so.
 */
public class VaultInvokerServiceImpl implements VaultInvokerService {

    public static final String LIB = "lib";
    public static final String BIN = "bin";
    private static final String VAULT3_CLASS = "org.apache.jackrabbit.vault.cli.VaultFsApp";
    private static final String VAULT_METHOD = "main";
    private static final Logger log = Logger.getInstance(VaultInvokerServiceImpl.class);
    private ClassLoader vaultClassLoader;
    private boolean init = false;

    @Override
    public void invokeVault(String vaultDir, String[] args) throws IntelliVaultException {
        try {
            initVault(vaultDir);

            log.info("executing vlt with params: " + filterCredentials(args));

            ClassLoader cl = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(vaultClassLoader);
/*
                VaultFsApp vltApp = new VaultFsApp();
                Parser parser = new Parser();

                Group appGroup = vltApp.getApplicationCLGroup();
                parser.setGroup(appGroup);

                CommandLine commandLine = parser.parse(args);

                vltApp.prepare(commandLine);
                vltApp.execute(commandLine);

 */

                Class<?> vltClass = Class.forName(VAULT3_CLASS, true, vaultClassLoader);
                Class<?> vltSuperClass = vltClass.getSuperclass();
                Class<?> parserClass = Class.forName("org.apache.commons.cli2.commandline.Parser", true, vaultClassLoader);
                Class<?> groupClass = Class.forName("org.apache.commons.cli2.Group", true, vaultClassLoader);
                Class<?> commandLineClass = Class.forName("org.apache.commons.cli2.CommandLine", true, vaultClassLoader);


                Constructor<?> vltAppCons = vltClass.getConstructor();
                Object vltApp = vltAppCons.newInstance();

                Constructor<?> parserCons = parserClass.getConstructor();
                Object parser = parserCons.newInstance();

                Method getApplicationCLGroup = vltSuperClass.getMethod("getApplicationCLGroup");
                Object appGroup = getApplicationCLGroup.invoke(vltApp);

                Method setGroup = parserClass.getMethod("setGroup", groupClass);
                setGroup.invoke(parser, appGroup);

                Method parse = parserClass.getMethod("parse", args.getClass());
                Object commandLine = parse.invoke(parser, new Object[]{args});

                Method init = vltClass.getDeclaredMethod("init");
                init.setAccessible(true);
                init.invoke(vltApp);

                Method prepare = vltClass.getMethod("prepare", commandLineClass);
                Method execute = vltClass.getMethod("execute", commandLineClass);

                prepare.invoke(vltApp, commandLine);
                execute.invoke(vltApp, commandLine);


            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }

        } catch (Exception e) {
            log.error("reflective exception invoking vlt.", e);
            throw new IntelliVaultException("reflective exception invoking vlt.", e);
        }
    }

    private String filterCredentials(String[] args) {
        StringBuilder builder = new StringBuilder();

        boolean credsNext=false;
        for(String arg: args) {
            if(credsNext) {
                String[] split = arg.split(":");
                String user = split[0];
                String pass = "*****";

                arg = user + ":" + pass;
                credsNext=false;
            } else if(arg.equals("--credentials")) {
                credsNext=true;
            }

            builder.append(arg).append(" ");
        }

        return builder.toString();
    }

    @Override
    public void forceReInit() {
        init = false;
    }

    /**
     * Initialize vault.  Basically finds all the jars in the vault folder and creates a custom class loader which
     * includes those jars.  All vault operations are then executed using that class loader.
     *
     * @param vaultDir the vault home directory as specified in the settings dialog. Could be the root directory, or
     *                 potentially the bin or lib directory.
     * @throws IOException if an error occurs, such as the vault directory not being set.
     */
    private void initVault(String vaultDir) throws IOException, IntelliVaultException {
        if (!init) {
            if (vaultDir == null || vaultDir.trim().length() == 0) {
                throw new IOException("Vault Directory not set");
            }

            if (vaultDir.endsWith(BIN)) {
                vaultDir = vaultDir.substring(0, vaultDir.lastIndexOf(File.separator));
            }

            if (!vaultDir.endsWith(LIB)) {
                vaultDir += File.separator + LIB;
            }

            log.info("loading vault libraries from " + vaultDir);
            List<URL> libList = new ArrayList<URL>();
            File libDir = new File(vaultDir.replace('/', File.separatorChar));
            File[] libs = libDir.listFiles((dir, name) -> (name.endsWith(".jar")) || (name.endsWith(".zip")));
            if (libs != null) {
                for (File lib : libs) {
                    try {
                        log.debug("adding library: " + lib.getAbsolutePath());
                        libList.add(lib.toURI().toURL());

                        String libName = lib.getName();
                        Pattern pattern = Pattern.compile("vault-vlt-([0-9]{1,6})\\.([0-9]{1,6})\\.([0-9]{1,6})\\.jar");
                        Matcher matcher = pattern.matcher(libName);
                        if(matcher.matches()) {
                            String majorVersionStr = matcher.group(1);
                            int majorVersion = Integer.parseInt(majorVersionStr);

                            String minorVersionStr = matcher.group(2);
                            int minorVersion = Integer.parseInt(minorVersionStr);

                            if(majorVersion<3 || minorVersion<2) {
                                throw new IntelliVaultException("IntelliVault only supports VLT version 3.2+. Please select a supported version in IntelliJ IDEA->Preferences...->Tools->IntelliVault");
                            }

                        }

                    } catch (IOException e) {
                        log.error("error loading lib " + lib.getAbsolutePath(), e);
                    }
                }

                vaultClassLoader = new URLClassLoader(libList.toArray(new URL[libList.size()]));
                init = true;
            }

        }
    }
}

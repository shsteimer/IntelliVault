package com.razorfish.platforms.intellivault.config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/16/13
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntelliVaultOperationConfig {
    private static final String TEMP_DIR_PROP = "java.io.tmpdir";

    private String vaultPath;
    private String tempDirectory;
    private String rootFolderName;
    private boolean verbose;
    private boolean debug;
    private boolean logToConsole;
    private List<String> fileIgnorePatterns;
    private boolean showMessageDialogs;

    /**
     * Create a new instance using the default value for all fields of the operation config
     */
    public IntelliVaultOperationConfig() {
        this.vaultPath= IntelliVaultConfigDefaults.VAULT_PATH;
        this.tempDirectory=System.getProperty(TEMP_DIR_PROP);
        this.rootFolderName= IntelliVaultConfigDefaults.ROOT_FOLDER;
        this.verbose= IntelliVaultConfigDefaults.VERBOSE;
        this.debug= IntelliVaultConfigDefaults.DEBUG;
        this.logToConsole= IntelliVaultConfigDefaults.CONSOLE_LOG;
        this.fileIgnorePatterns = new LinkedList<String>(Arrays.asList(IntelliVaultConfigDefaults.IGNORE_PATTERNS.split(",")));
        this.showMessageDialogs = IntelliVaultConfigDefaults.SHOW_MESSAGE_DIALOG;
    }

    public String getVaultPath() {
        return vaultPath;
    }

    public void setVaultPath(String vaultPath) {
        this.vaultPath = vaultPath;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public String getRootFolderName() {
        return rootFolderName;
    }

    public void setRootFolderName(String rootFolderName) {
        this.rootFolderName = rootFolderName;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isLogToConsole() {
        return logToConsole;
    }

    public void setLogToConsole(boolean logToConsole) {
        this.logToConsole = logToConsole;
    }

    public List<String> getFileIgnorePatterns() {
        return fileIgnorePatterns;
    }

    public void setFileIgnorePatterns(List<String> fileIgnorePatterns) {
        this.fileIgnorePatterns = fileIgnorePatterns;
    }

    public boolean showMessageDialogs() {
        return showMessageDialogs;
    }

    public void setShowMessageDialogs(boolean showMessageDialogs) {
        this.showMessageDialogs = showMessageDialogs;
    }
}

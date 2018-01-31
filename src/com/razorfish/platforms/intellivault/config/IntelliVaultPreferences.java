package com.razorfish.platforms.intellivault.config;


import com.intellij.openapi.vcs.history.VcsRevisionNumber;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/25/13
 * Time: 10:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntelliVaultPreferences implements Serializable, Cloneable {

    private static final long serialVersionUID = -7299400926047258802L;

    public String vaultPath;
    public String tempDirectory;
    public String rootFolderName;
    public boolean verbose;
    public boolean showDialogs;
    public boolean debug;
    public boolean logToConsole;
    public List<String> fileIgnorePatterns;

    // Strictly typed to linked hashmap for serialization
    public List<IntelliVaultCRXRepository> repoConfigs;

    /**
     * Create a default preferences object.
     */
    public IntelliVaultPreferences() {
        IntelliVaultOperationConfig operationConfig = new IntelliVaultOperationConfig();
        this.vaultPath = operationConfig.getVaultPath();
        this.tempDirectory = operationConfig.getTempDirectory();
        this.rootFolderName = operationConfig.getRootFolderName();

        this.verbose = operationConfig.isVerbose();
        this.debug = operationConfig.isDebug();
        this.showDialogs = operationConfig.showMessageDialogs();
        this.logToConsole = operationConfig.isLogToConsole();

        this.fileIgnorePatterns = operationConfig.getFileIgnorePatterns();
        this.repoConfigs = new LinkedList<>();
    }

    /**
     * Get the applicable IntelliVaultOperationConfig from this IntelliVaultPreferences object.
     *
     * @return the operation config
     */
    public IntelliVaultOperationConfig getOperationConfig() {

        IntelliVaultOperationConfig operationConfig = new IntelliVaultOperationConfig();
        operationConfig.setVaultPath(this.vaultPath);
        operationConfig.setTempDirectory(this.tempDirectory);
        operationConfig.setRootFolderName(this.rootFolderName);

        operationConfig.setVerbose(this.verbose);
        operationConfig.setLogToConsole(this.logToConsole);
        operationConfig.setDebug(this.debug);
        operationConfig.setShowMessageDialogs(this.showDialogs);

        operationConfig.setFileIgnorePatterns(this.fileIgnorePatterns);

        return operationConfig;
    }

    public IntelliVaultCRXRepository putRepositoryConfiguration(final String repoName, final String url, final String username, final String password) {
        IntelliVaultCRXRepository repo = new IntelliVaultCRXRepository(repoName, url, username, password);
        putRepositoryConfiguration(repo);
        return repo;
    }

    public void putRepositoryConfiguration(IntelliVaultCRXRepository repo) {
        IntelliVaultCRXRepository existing = getRepositoryConfiguration(repo.getName());
        if (existing != null) {
            // Keep list order
            existing.replaceWith(repo);
        } else {
            repoConfigs.add(repo);
        }
    }

    public void removeRepositoryConfiguration(final String repoName) {
        IntelliVaultCRXRepository repository = getRepositoryConfiguration(repoName);
        this.repoConfigs.remove(repository);
    }

    public IntelliVaultCRXRepository getRepositoryConfiguration(final String repoName) {
        for (IntelliVaultCRXRepository repo : repoConfigs) {
            if (repo.getName().equals(repoName)) {
                return repo;
            }
        }
        return null;
    }

    public List<IntelliVaultCRXRepository> getRepoConfigs() {
        return repoConfigs;
    }

    public IntelliVaultCRXRepository getFirstRepositoryConfiguration() {
        return repoConfigs.size() > 0 ? repoConfigs.get(0) : null;
    }

    public boolean hasRepositoryConfigs() {
        return !getRepoConfigs().isEmpty();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        getRepoConfigs().sort(Comparator.naturalOrder());

        in.defaultReadObject();
    }

    public Object clone() {
        IntelliVaultPreferences prefs = new IntelliVaultPreferences();
        prefs.vaultPath = vaultPath;

        prefs.tempDirectory = tempDirectory;
        prefs.rootFolderName = rootFolderName;
        prefs.verbose = verbose;
        prefs.showDialogs = showDialogs;
        prefs.debug = debug;
        prefs.logToConsole = logToConsole;
        if(fileIgnorePatterns != null){
            prefs.fileIgnorePatterns.clear(); // Remove defaults
            prefs.fileIgnorePatterns.addAll(fileIgnorePatterns);
        }

        if(repoConfigs !=  null){
            for(IntelliVaultCRXRepository repo : repoConfigs){
                prefs.repoConfigs.add((IntelliVaultCRXRepository) repo.clone());
            }
        }

        return prefs;
    }

}

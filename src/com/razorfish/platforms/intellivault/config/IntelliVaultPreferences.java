package com.razorfish.platforms.intellivault.config;


import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.razorfish.platforms.intellivault.services.impl.IntelliVaultPreferencesService;

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
        this.repoConfigs = new LinkedList<IntelliVaultCRXRepository>();
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

    /**
     * Convenience method for putting a {@link IntelliVaultCRXRepository} on the list.
     *
     * @see IntelliVaultPreferences#putRepositoryConfiguration(String, String, String, String)
     *
     * @param repoName The name of the repository to put.
     * @param url The url of the repository to put.
     * @param username The username of the repository to put.
     * @param password The password of the repository to put.
     *
     * @return The newly put or updated {@link IntelliVaultCRXRepository}
     * */
    public IntelliVaultCRXRepository putRepositoryConfiguration(final String repoName, final String url, final String username, final String password) {
        IntelliVaultCRXRepository repo = new IntelliVaultCRXRepository(repoName, url, username, password);
        return putRepositoryConfiguration(repo);
    }

    /**
     * Puts a {@link IntelliVaultCRXRepository} on the list.
     * This method is called 'put' because like a map, it will replace an existing value (treating {@link IntelliVaultCRXRepository#getName()} like a key)
     *
     * @param repo The {@link IntelliVaultCRXRepository} to put on the repository list.
     *
     * @return The {@link IntelliVaultCRXRepository} that was just put on the list.
     * */
    public IntelliVaultCRXRepository putRepositoryConfiguration(IntelliVaultCRXRepository repo) {
        IntelliVaultCRXRepository existing = getRepositoryConfiguration(repo.getName());
        if (existing != null) {
            // Keep list order
            existing.replaceWith(repo);
            return existing;
        } else {
            repoConfigs.add(repo);
        }
        return repo;
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

    /**
     * Convenience method to return the first {@link IntelliVaultCRXRepository} or null if none are setup yet.
     *
     * @return The first {@link IntelliVaultCRXRepository} in the settings list (after alphabetical sorting)
     * */
    public IntelliVaultCRXRepository getFirstRepositoryConfiguration() {
        return repoConfigs.size() > 0 ? repoConfigs.get(0) : null;
    }

    /**
     * Convenience method for determining if there are any {@link IntelliVaultCRXRepository} configurations setup yet.
     * */
    public boolean hasRepositoryConfigs() {
        return !getRepoConfigs().isEmpty();
    }

    /**
     * After deserialization this will sort the repository list by name.
     *
     * @param in The deserialized preferences.
     * */
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

        Collections.sort(getRepoConfigs());

        in.defaultReadObject();
    }

    /**
     * Clones this preferences object. Need to do this when loading the settings so that changes can be cancelled,
     * otherwise updating this object will auto-save (since it likely references the deserialized object)
     *
     * @see IntelliVaultPreferencesService#getPreferences()
     *
     * @return A deep clone of this preferences object.
     * */
    @Override
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

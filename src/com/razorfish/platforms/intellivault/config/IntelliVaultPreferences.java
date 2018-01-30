package com.razorfish.platforms.intellivault.config;


import java.io.Serializable;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/25/13
 * Time: 10:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntelliVaultPreferences implements Serializable {

    private static final long serialVersionUID = -7299400926047258802L;
    public static final String REPO_USER_DELIMITER = "@";

    //CHECKSTYLE:OFF
    public String vaultPath;
    public String tempDirectory;
    public String rootFolderName;
    public boolean verbose;
    public boolean showDialogs;
    public boolean debug;
    public boolean logToConsole;
    public List<String> fileIgnorePatterns;
    //CHECKSTYLE:ON

    public Map<String, IntelliVaultCRXRepository> repoConfigs;

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

        IntelliVaultCRXRepository repo = new IntelliVaultCRXRepository();
        this.addRepositoryConfiguration("default",repo);
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

    public void addRepositoryConfiguration(final String repoName, final String url, final String username, final String password) {
        IntelliVaultCRXRepository repo = new IntelliVaultCRXRepository(repoName,url,username,password);
        addRepositoryConfiguration(repoName, repo);
    }

    public void addRepositoryConfiguration(final String repoName, IntelliVaultCRXRepository repo) {
        if (this.repoConfigs == null) {
            this.repoConfigs = new HashMap<>();
        }

        this.repoConfigs.put(repoName, repo);
    }

    public void removeRepositoryConfiguration(final String repoName){
        this.repoConfigs.remove(repoName);
    }

    public void getRepositoryConfiguration(final String repoName){
        this.repoConfigs.get(repoName);
    }

    public Map<String, IntelliVaultCRXRepository> getRepoConfigs(){
        return repoConfigs;
    }

    public IntelliVaultCRXRepository getFirstRepositoryConfiguration(){
        Collection<IntelliVaultCRXRepository> collRepo = repoConfigs.values();
        List<IntelliVaultCRXRepository> listRepos;
        if (collRepo instanceof List){
            listRepos = (List<IntelliVaultCRXRepository>)collRepo;
        } else {
            listRepos = new ArrayList<>(collRepo);
        }

        return listRepos.get(0);
    }
}

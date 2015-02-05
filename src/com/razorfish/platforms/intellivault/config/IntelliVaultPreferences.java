package com.razorfish.platforms.intellivault.config;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<String,String> repoConfigs;

    /**
     * Create a default preferences object.
     */
    public IntelliVaultPreferences() {
        IntelliVaultOperationConfig operationConfig=new IntelliVaultOperationConfig();
        this.vaultPath = operationConfig.getVaultPath();
        this.tempDirectory = operationConfig.getTempDirectory();
        this.rootFolderName = operationConfig.getRootFolderName();

        this.verbose = operationConfig.isVerbose();
        this.debug = operationConfig.isDebug();
        this.showDialogs = operationConfig.showMessageDialogs();
        this.logToConsole = operationConfig.isLogToConsole();

        this.fileIgnorePatterns = operationConfig.getFileIgnorePatterns();

        IntelliVaultCRXRepository repo = new IntelliVaultCRXRepository();
        this.addRepositoryConfiguration(repo.getRepoUrl(), repo.getUsername(), repo.getPassword());
    }

    /**
     * Get the applicable IntelliVaultOperationConfig from this IntelliVaultPreferences object.
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

    public List<IntelliVaultCRXRepository> getRepositoryList() {
        List<IntelliVaultCRXRepository> repositoryList = new ArrayList<IntelliVaultCRXRepository>();
        for (String userUrl : this.repoConfigs.keySet()) {
            String password = this.repoConfigs.get(userUrl);
            String user = userUrl.substring(0, userUrl.indexOf(REPO_USER_DELIMITER));
            String url = userUrl.substring(userUrl.indexOf(REPO_USER_DELIMITER) + 1);

            IntelliVaultCRXRepository repository = new IntelliVaultCRXRepository();
            repository.setPassword(password);
            repository.setUsername(user);
            repository.setRepoUrl(url);
            repositoryList.add(repository);
        }

        return repositoryList;
    }

    public void addRepositoryConfiguration(final String url, final String username, final String password) {
        if(this.repoConfigs==null) {
            this.repoConfigs=new HashMap<String, String>();
        }

        this.repoConfigs.put(username + REPO_USER_DELIMITER + url, password);
    }
}

package com.razorfish.platforms.intellivault.config;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/15/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntelliVaultCRXRepository implements Serializable{

    private static final long serialVersionUID = 8008135L;
    private String repoUrl;
    private String username;
    private String password;

    /**
     * Create a new instance, pre-populating the default values for url, username, and password.
     */
    public IntelliVaultCRXRepository() {
        this.repoUrl = IntelliVaultConfigDefaults.REPO_URL;
        this.password = IntelliVaultConfigDefaults.REPO_PASSWORD;
        this.username = IntelliVaultConfigDefaults.REPO_USER;
    }

    public IntelliVaultCRXRepository(String repoUrl, String username, String password){
        this.repoUrl = repoUrl;
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}

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
    private String name;
    private String repoUrl;
    private String username;
    private String password;

    /**
     * Create a new instance, pre-populating the default values for url, username, and password.
     */
    public IntelliVaultCRXRepository() {
        this.name= "new repository";
        this.repoUrl = IntelliVaultConfigDefaults.REPO_URL;
        this.password = IntelliVaultConfigDefaults.REPO_PASSWORD;
        this.username = IntelliVaultConfigDefaults.REPO_USER;
    }

    public IntelliVaultCRXRepository(String name, String repoUrl, String username, String password){
        this.name = name;
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

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }


    @Override
    public String toString(){
        return name;
    }
}

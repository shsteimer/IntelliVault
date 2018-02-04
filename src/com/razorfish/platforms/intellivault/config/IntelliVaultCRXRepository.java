package com.razorfish.platforms.intellivault.config;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/15/13
 * Time: 12:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntelliVaultCRXRepository implements Serializable,Comparable<IntelliVaultCRXRepository>,Cloneable{

    private static final long serialVersionUID = 8008135L;
    private String name;
    private String repoUrl;
    private String username;
    private String password;

    /**
     * Create a new instance, pre-populating the default values for name, url, username, and password.
     */
    public IntelliVaultCRXRepository() {
        this.name = IntelliVaultConfigDefaults.REPO_NAME;
        this.repoUrl = IntelliVaultConfigDefaults.REPO_URL;
        this.password = IntelliVaultConfigDefaults.REPO_PASSWORD;
        this.username = IntelliVaultConfigDefaults.REPO_USER;
    }

    /**
     * Create a new instance with the supplied name, url, username, password.
     * */
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

    public void replaceWith(IntelliVaultCRXRepository otherRepoConfig){
        this.name = otherRepoConfig.getName();
        this.repoUrl = otherRepoConfig.getRepoUrl();
        this.username = otherRepoConfig.getUsername();
        this.password = otherRepoConfig.getPassword();
    }

    @Override
    public String toString(){
        return name;
    }

    @Override
    public int compareTo(@NotNull IntelliVaultCRXRepository o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public Object clone(){
        IntelliVaultCRXRepository newRepo = new IntelliVaultCRXRepository();
        newRepo.replaceWith(this);
        return newRepo;
    }
}

package com.razorfish.platforms.intellivault.config;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Value object used to store a configured crx repository where vault will import/export.
 */
public class IntelliVaultCRXRepository implements Serializable, Comparable<IntelliVaultCRXRepository>, Cloneable {

    private static final long serialVersionUID = 8008135L;
    private String name;
    private String repoUrl;

    /**
     * Create a new instance, pre-populating the default values for name, url, username, and password.
     */
    public IntelliVaultCRXRepository() {
        this.name = IntelliVaultConfigDefaults.REPO_NAME;
        this.repoUrl = IntelliVaultConfigDefaults.REPO_URL;
    }

    /**
     * Create a new instance with the supplied name, url, username, password.
     */
    public IntelliVaultCRXRepository(String name, String repoUrl) {
        this.name = name;
        this.repoUrl = repoUrl;
    }

    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void replaceWith(IntelliVaultCRXRepository otherRepoConfig) {
        this.name = otherRepoConfig.getName();
        this.repoUrl = otherRepoConfig.getRepoUrl();
    }

    @Override
    public int compareTo(
            @NotNull
                    IntelliVaultCRXRepository o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public Object clone() {
        IntelliVaultCRXRepository newRepo = new IntelliVaultCRXRepository();
        newRepo.replaceWith(this);
        return newRepo;
    }

    @Override
    public String toString() {
        return name;
    }
}

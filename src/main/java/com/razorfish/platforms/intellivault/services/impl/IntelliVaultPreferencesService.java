package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.razorfish.platforms.intellivault.config.IntelliVaultConfigDefaults;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import org.jetbrains.annotations.Nullable;

/**
 * The preferences services handles storing and retrieving the configuration state of the vault plugin.
 */
@State(name = "IntelliVaultPreferencesService", storages = {
        @Storage("IntelliVaultPreferencesService.xml")})
public class IntelliVaultPreferencesService implements PersistentStateComponent<IntelliVaultPreferences> {

    private IntelliVaultPreferences preferences;

    public IntelliVaultPreferences getPreferences() {
        if (preferences == null) {
            preferences = new IntelliVaultPreferences();
        }

        if (preferences.repoConfigList == null || preferences.repoConfigList.size() == 0) {
            preferences.repoConfigList = preferences.getDefaultRepos();
        }

        return (IntelliVaultPreferences) preferences.clone();
    }

    public void setPreferences(IntelliVaultPreferences preferences) {
        this.preferences = preferences;
    }

    @Nullable
    @Override
    public IntelliVaultPreferences getState() {
        IntelliVaultPreferences preferences = getPreferences();

        return preferences;

    }

    @Override
    public void loadState(IntelliVaultPreferences preferences) {
        setPreferences(preferences);
    }

    public Credentials retrieveCredentials(String repositoryName) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(repositoryName);

        Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
        if(credentials==null) {
            return new Credentials(IntelliVaultConfigDefaults.REPO_USER, IntelliVaultConfigDefaults.REPO_PASSWORD);
        }

        return credentials;
    }

    public void storeCredentials(String repositoryName, String username, String password) {
        if (username.equals(IntelliVaultConfigDefaults.REPO_USER) && password.equals(IntelliVaultConfigDefaults.REPO_PASSWORD)) {
            return;
        }

        CredentialAttributes credentialAttributes = createCredentialAttributes(repositoryName);
        Credentials credentials = new Credentials(username, password);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public void removeCredentials(String repositoryName) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(repositoryName);
        PasswordSafe.getInstance().set(credentialAttributes, null);
    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("IntelliVault", key));
    }


}

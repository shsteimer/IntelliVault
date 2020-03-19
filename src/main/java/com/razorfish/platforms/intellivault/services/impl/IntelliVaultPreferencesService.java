package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

        List<IntelliVaultCRXRepository> repoConfigList = preferences.getRepoConfigList();
        repoConfigList.forEach(repo -> {
            CredentialAttributes credentialAttributes = createCredentialAttributes(repo.getName());
            Credentials credentials = new Credentials(repo.getUsername(), repo.getPassword());
            PasswordSafe.getInstance().set(credentialAttributes, credentials);

            repo.setPassword("dummy");
            repo.setUsername("dummy");
        });


        return preferences;

    }

    @Override
    public void loadState(IntelliVaultPreferences preferences) {

        List<IntelliVaultCRXRepository> repoConfigList = preferences.getRepoConfigList();
        repoConfigList.forEach(repo -> {
            CredentialAttributes credentialAttributes = createCredentialAttributes(repo.getName());

            Credentials credentials = PasswordSafe.getInstance().get(credentialAttributes);
            if (credentials != null) {
                String password = credentials.getPasswordAsString();
                String userName = credentials.getUserName();

                repo.setPassword(password);
                repo.setUsername(userName);
            }
        });

        setPreferences(preferences);
    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("IntelliVault", key));
    }
}

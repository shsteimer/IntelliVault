package com.razorfish.platforms.intellivault.services.impl;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import org.jetbrains.annotations.Nullable;

/**
 * The preferences services handles storing and retrieving the configuration state of the vault plugin.
 */
@State(
        name = "IntelliVaultPreferencesService",
        storages = {
                @Storage(id = "main", file = "$APP_CONFIG$/IntelliVaultPreferencesService.xml")
        }
)
public class IntelliVaultPreferencesService implements PersistentStateComponent<IntelliVaultPreferences> {

    private IntelliVaultPreferences preferences;

    public IntelliVaultPreferences getPreferences() {
        if (preferences == null) {
            preferences = new IntelliVaultPreferences();
        }

        return (IntelliVaultPreferences) preferences.clone();
    }

    public void setPreferences(IntelliVaultPreferences preferences) {
        this.preferences = preferences;
    }

    @Nullable
    @Override
    public IntelliVaultPreferences getState() {
        return getPreferences();
    }

    @Override
    public void loadState(IntelliVaultPreferences preferences) {
        setPreferences(preferences);
    }
}

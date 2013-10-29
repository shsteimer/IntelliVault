package com.crownpartners.intellivault.services.impl;

import com.crownpartners.intellivault.config.IntelliVaultPreferences;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/28/13
 * Time: 11:50 PM
 * To change this template use File | Settings | File Templates.
 */
@State(
        name = "IntelliVaultPreferencesService",
        storages = {
                @Storage(id = "main", file = "$APP_CONFIG$/IntelliVaultPreferencesService.xml")
        }
)
public class IntelliVaultPreferencesService implements PersistentStateComponent<IntelliVaultPreferences> {

    private IntelliVaultPreferences preferences;

    /**
     * get the preferences.
     * @return the preferences
     */
    public IntelliVaultPreferences getPreferences() {
        if(preferences==null) {
            preferences = new IntelliVaultPreferences();
        }

        return preferences;
    }

    public void setPreferences(IntelliVaultPreferences preferences) {
        this.preferences=preferences;
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

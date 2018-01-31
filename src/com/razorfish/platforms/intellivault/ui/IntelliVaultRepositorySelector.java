package com.razorfish.platforms.intellivault.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.razorfish.platforms.intellivault.actions.IntelliVaultAbstractAction;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import com.razorfish.platforms.intellivault.services.impl.IntelliVaultPreferencesService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IntelliVaultRepositorySelector extends DialogWrapper {
    private JPanel contentPane;
    private JComboBox comboRepositorySelector;

    private IntelliVaultAbstractAction firingAction;

    public IntelliVaultRepositorySelector(Project project, IntelliVaultAbstractAction myAction) {
        super(project);

        firingAction = myAction;

        setModal(true);

        // call onCancel() on ESCAPE;
        contentPane.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancelAction();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        IntelliVaultPreferencesService preferenceService = ServiceManager.getService(IntelliVaultPreferencesService.class);
        IntelliVaultPreferences preferences = preferenceService.getPreferences();
        List<IntelliVaultCRXRepository> repoList = preferences.getRepoConfigs();

        // Add all configured repositories to the selector.
        for (IntelliVaultCRXRepository repo : repoList) {
            comboRepositorySelector.addItem(repo);
        }

        setTitle("Choose a CRX Repository");

        init();

    }

    @Override
    public void doOKAction() {
        IntelliVaultCRXRepository selectedRepository = (IntelliVaultCRXRepository) comboRepositorySelector.getSelectedItem();
        firingAction.setSelectedIntelliVaultCRXRepository(selectedRepository);
        super.doOKAction();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}

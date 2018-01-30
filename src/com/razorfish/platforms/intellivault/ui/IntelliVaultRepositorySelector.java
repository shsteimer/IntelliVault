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
import java.awt.event.*;
import java.util.Collection;
import java.util.Map;

public class IntelliVaultRepositorySelector extends DialogWrapper {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JComboBox comboRepositorySelector;

    private IntelliVaultAbstractAction firingAction;

    public IntelliVaultRepositorySelector(Project project, IntelliVaultAbstractAction myAction) {
        super(project);

        firingAction = myAction;

        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        IntelliVaultPreferencesService preferenceService = ServiceManager.getService(IntelliVaultPreferencesService.class);
        IntelliVaultPreferences preferences = preferenceService.getPreferences();
        Map<String,IntelliVaultCRXRepository> repositoryMap = preferences.getRepoConfigs();

        Collection<IntelliVaultCRXRepository> rValues = repositoryMap.values();
        for(IntelliVaultCRXRepository repo : rValues){
            comboRepositorySelector.addItem(repo);
        }

        setTitle("Choose a CRX Repository");

        init();

    }

    private void onOK() {

        IntelliVaultCRXRepository selectedRepository = (IntelliVaultCRXRepository)comboRepositorySelector.getSelectedItem();

        firingAction.setSelectedIntelliVaultCRXRepository(selectedRepository);
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }
}

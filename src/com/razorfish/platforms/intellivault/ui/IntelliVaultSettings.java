package com.razorfish.platforms.intellivault.ui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import com.razorfish.platforms.intellivault.services.VaultInvokerService;
import com.razorfish.platforms.intellivault.services.impl.IntelliVaultPreferencesService;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/15/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class IntelliVaultSettings implements Configurable {

    private static final Logger log = Logger.getInstance(IntelliVaultSettings.class);

    public static final String FILE_IGNORE_PATTERN_SEPERATOR = ",";
    public static final String CURRENT_DIRECTORY_SYMBOL = ".";
    private JPanel jPanel;
    private JComboBox comboProfileSelect;
    private JTextField txtVaultDir;
    private JButton btnVaultDirBrowse;
    private JButton btnTempDirBrowse;
    private JTextField txtTempDir;
    private JCheckBox verboseOutputCheckBox;
    private JTextField txtRepoUrl;
    private JTextField txtUsername;
    private JTextField txtPassword;
    private JTextField txtIgnorePatterns;
    private JTextField txtJCRRootDirName;
    private JButton btnRestoreDefaults;
    private JCheckBox showDialogsCheckBox;
    private JButton btnDeleteProfile;

    @Nls
    @Override
    public String getDisplayName() {
        return "IntelliVault";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "IntelliVault.Plugin.Help";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        btnTempDirBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            String currentDirectory = txtTempDir.getText()!=null && txtTempDir.getText().length()>0 ?
                    txtTempDir.getText() : CURRENT_DIRECTORY_SYMBOL;
            chooser.setCurrentDirectory(new java.io.File(currentDirectory));
            chooser.setDialogTitle("Select Temp Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            // Demonstrate "Open" dialog:
            int rVal = chooser.showOpenDialog(jPanel);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                txtTempDir.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnVaultDirBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            String curDir=txtVaultDir.getText()!=null && txtVaultDir.getText().length()>0
                    ? txtVaultDir.getText() : CURRENT_DIRECTORY_SYMBOL;
            chooser.setCurrentDirectory(new java.io.File(curDir));
            chooser.setDialogTitle("Select Vault Directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            // Demonstrate "Open" dialog:
            int rVal = chooser.showOpenDialog(jPanel);
            if (rVal == JFileChooser.APPROVE_OPTION) {
                txtVaultDir.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        btnRestoreDefaults.addActionListener(e -> setDialogStateFromPreferences(new IntelliVaultPreferences()));


        btnDeleteProfile.addActionListener(e -> deleteCurrentlySelectedProfile());


        reset();
        return jPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        IntelliVaultPreferencesService preferencesService
                = ServiceManager.getService(IntelliVaultPreferencesService.class);
        preferencesService.setPreferences(getPreferencesFromDialogState());

        VaultInvokerService vltInvoker = ServiceManager.getService(VaultInvokerService.class);
        vltInvoker.forceReInit();
    }



    @Override
    public void reset() {
        IntelliVaultPreferencesService preferencesService
                = ServiceManager.getService(IntelliVaultPreferencesService.class);
        setDialogStateFromPreferences(preferencesService.getPreferences());
    }

    /**
     * Serialize the current dialog state to a preferences object which can be used by IntelliVault.
     * @return the currently selected IntelliVaultPreferences
     */
    private IntelliVaultPreferences getPreferencesFromDialogState() {

        IntelliVaultPreferences preferencesBean = new IntelliVaultPreferences();
        preferencesBean.vaultPath = txtVaultDir.getText();
        preferencesBean.tempDirectory = txtTempDir.getText();
        preferencesBean.rootFolderName = txtJCRRootDirName.getText();

        preferencesBean.verbose = verboseOutputCheckBox.isSelected();
        preferencesBean.showDialogs = showDialogsCheckBox.isSelected();

        String ignorePatterns = txtIgnorePatterns.getText();
        if (ignorePatterns != null) {
            String[] ignorePatternsArray = ignorePatterns.split(FILE_IGNORE_PATTERN_SEPERATOR);
            preferencesBean.fileIgnorePatterns = Arrays.asList(ignorePatternsArray);
        }

        preferencesBean.repoConfigs.clear();
        String profileName = comboProfileSelect.getSelectedItem().toString();
        preferencesBean.addRepositoryConfiguration(profileName,txtRepoUrl.getText(), txtUsername.getText(), txtPassword.getText());

        return preferencesBean;
    }

    private void deleteCurrentlySelectedProfile(){
        Object selectedItem = comboProfileSelect.getSelectedItem();
        if(selectedItem != null){
            int deleteChoice = Messages.showYesNoDialog("Are you sure you want to delete the profile " + selectedItem.toString() + "?","Delete profile",null);
            if(deleteChoice == Messages.YES){
                comboProfileSelect.removeItem(selectedItem);
            }
        } else{
            log.warn("No option selected");
        }
    }

    /**
     * De-serialize an IntelliVaultPreferences object to the dialog, setting all dialog fields as defined in the object
     * passed in.
     * @param preferences the preferences object to set the dialog fields from.
     */
    private void setDialogStateFromPreferences(final IntelliVaultPreferences preferences) {
        txtVaultDir.setText(preferences.vaultPath);
        txtTempDir.setText(preferences.tempDirectory);
        txtJCRRootDirName.setText(preferences.rootFolderName);

        verboseOutputCheckBox.setSelected(preferences.verbose);
        showDialogsCheckBox.setSelected(preferences.showDialogs);

        StringBuffer buf = new StringBuffer();
        for (String s : preferences.fileIgnorePatterns) {
            buf.append(s).append(FILE_IGNORE_PATTERN_SEPERATOR);
        }
        String ignorePatterns=buf.toString();
        ignorePatterns = ignorePatterns.substring(0, ignorePatterns.length()-1);
        txtIgnorePatterns.setText(ignorePatterns);

        IntelliVaultCRXRepository intelliVaultCRXRepository = preferences.getFirstRepositoryConfiguration();
        txtRepoUrl.setText(intelliVaultCRXRepository.getRepoUrl());
        txtPassword.setText(intelliVaultCRXRepository.getPassword());
        txtUsername.setText(intelliVaultCRXRepository.getUsername());

    }

    @Override
    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}

package com.razorfish.platforms.intellivault.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultConfigDefaults;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import com.razorfish.platforms.intellivault.services.VaultInvokerService;
import com.razorfish.platforms.intellivault.services.impl.IntelliVaultPreferencesService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    private JButton btnDeleteRepository;
    private JButton btnSaveRepository;
    private JTextField txtRepoName;

    private IntelliVaultPreferences userPreferences;

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
        IntelliVaultPreferencesService preferencesService = ServiceManager.getService(IntelliVaultPreferencesService.class);
        userPreferences = preferencesService.getPreferences();

        btnTempDirBrowse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            String currentDirectory = txtTempDir.getText() != null && txtTempDir.getText().length() > 0 ?
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
            String curDir = txtVaultDir.getText() != null && txtVaultDir.getText().length() > 0
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
        btnDeleteRepository.addActionListener(e -> deleteCurrentlySelectedRepository());
        btnSaveRepository.addActionListener(e -> saveCurrentRepository());
        comboProfileSelect.addActionListener(e -> loadSelectedRepository());

        return jPanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        save();
    }

    @Override
    public void reset() {
        setDialogStateFromPreferences(userPreferences);
    }

    private void save() {
        IntelliVaultPreferencesService preferencesService = ServiceManager.getService(IntelliVaultPreferencesService.class);
        injectPreferencesFromDialogState(userPreferences);
        preferencesService.setPreferences(userPreferences);

        VaultInvokerService vltInvoker = ServiceManager.getService(VaultInvokerService.class);
        vltInvoker.forceReInit();
    }

    /**
     * Serialize the current dialog state to a preferences object which can be used by IntelliVault.
     * Injects non repository preferences
     */
    private void injectPreferencesFromDialogState(IntelliVaultPreferences preferencesBean) {
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

    }

    private void deleteCurrentlySelectedRepository() {
        Object selectedItem = comboProfileSelect.getSelectedItem();
        if (selectedItem != null) {
            int deleteChoice = Messages.showYesNoDialog("Are you sure you want to delete the profile " + selectedItem.toString() + "?", "Delete Profile", null);
            if (deleteChoice == Messages.YES) {
                comboProfileSelect.removeItem(selectedItem);
                userPreferences.removeRepositoryConfiguration(((IntelliVaultCRXRepository) selectedItem).getName());
            }
        } else {
            log.warn("No option selected");
        }
    }

    private void saveCurrentRepository() {
        String repoName = txtRepoName.getText();

        IntelliVaultCRXRepository newRepo = new IntelliVaultCRXRepository(
            repoName,
            txtRepoUrl.getText(),
            txtUsername.getText(),
            txtPassword.getText()
        );

        // Check if this
        IntelliVaultCRXRepository oldRepo = null;
        if(!repoName.equals(lastLoadedRepo) && lastLoadedRepo != null){
            oldRepo = userPreferences.getRepositoryConfiguration(lastLoadedRepo);
        }

        if (oldRepo != null) {
            // Repo name changed.
            oldRepo.replaceWith(newRepo);
            lastLoadedRepo = repoName;
        } else {
            // Repo name didn't change
            // This creates a new or overwrites an old. The above steps ensure renaming happens properly.
            newRepo = userPreferences.putRepositoryConfiguration(
                repoName,
                txtRepoUrl.getText(),
                txtUsername.getText(),
                txtPassword.getText()
            );
        }

        rebuildRepositoryComboBox(newRepo);
    }

    private String lastLoadedRepo = null;

    private void loadSelectedRepository() {
        Object selectedItem = comboProfileSelect.getSelectedItem();
        if (selectedItem != null) {
            if (selectedItem instanceof IntelliVaultCRXRepository) {
                setRepositoryStateFromRepository((IntelliVaultCRXRepository) selectedItem);
                lastLoadedRepo = ((IntelliVaultCRXRepository) selectedItem).getName();
            } else {
                // New repo was selected
                IntelliVaultCRXRepository repo = new IntelliVaultCRXRepository();
                setRepositoryStateFromRepository(repo);
                lastLoadedRepo = null;
            }
        } else {
            log.warn("No option selected");
        }
    }

    /**
     * De-serialize an IntelliVaultPreferences object to the dialog, setting all dialog fields as defined in the object
     * passed in.
     *
     * @param preferences the preferences object to set the dialog fields from.
     */
    private void setDialogStateFromPreferences(final IntelliVaultPreferences preferences) {
        txtVaultDir.setText(preferences.vaultPath);
        txtTempDir.setText(preferences.tempDirectory);
        txtJCRRootDirName.setText(preferences.rootFolderName);

        verboseOutputCheckBox.setSelected(preferences.verbose);
        showDialogsCheckBox.setSelected(preferences.showDialogs);

        StringBuilder buf = new StringBuilder();
        for (String s : preferences.fileIgnorePatterns) {
            buf.append(s).append(FILE_IGNORE_PATTERN_SEPERATOR);
        }
        String ignorePatterns = buf.toString();
        ignorePatterns = ignorePatterns.substring(0, ignorePatterns.length() - 1);
        txtIgnorePatterns.setText(ignorePatterns);

        rebuildRepositoryComboBox(null);
    }

    private void setRepositoryStateFromRepository(IntelliVaultCRXRepository repository) {
        if (repository != null) {
            txtRepoName.setText(repository.getName());
            txtRepoUrl.setText(repository.getRepoUrl());
            txtPassword.setText(repository.getPassword());
            txtUsername.setText(repository.getUsername());
        } else {
            txtRepoName.setText(IntelliVaultConfigDefaults.REPO_NAME);
            txtRepoUrl.setText(IntelliVaultConfigDefaults.REPO_URL);
            txtPassword.setText(IntelliVaultConfigDefaults.REPO_PASSWORD);
            txtUsername.setText(IntelliVaultConfigDefaults.REPO_USER);
        }
    }

    private void rebuildRepositoryComboBox(IntelliVaultCRXRepository selectedItem){
        comboProfileSelect.removeAllItems();
        comboProfileSelect.addItem("Create New Repository...");
        List<IntelliVaultCRXRepository> rValues = userPreferences.getRepoConfigs();
        for (IntelliVaultCRXRepository repo : rValues) {
            comboProfileSelect.addItem(repo);
        }
        if(selectedItem == null){
            selectedItem = userPreferences.getFirstRepositoryConfiguration();
        }
        if(selectedItem != null){
            comboProfileSelect.setSelectedItem(selectedItem);
        }
        setRepositoryStateFromRepository(selectedItem);
    }

    @Override
    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}

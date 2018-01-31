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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

        btnTempDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        btnVaultDirBrowse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
            }
        });

        btnRestoreDefaults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDialogStateFromPreferences(new IntelliVaultPreferences());
            }
        });
        btnDeleteRepository.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteCurrentlySelectedRepository();
            }
        });
        btnSaveRepository.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCurrentRepository();
            }
        });
        comboProfileSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadSelectedRepository();
            }
        });

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

    /**
     * Saves the {@link IntelliVaultPreferences} back to the {@link IntelliVaultPreferencesService}
     */
    private void save() {
        IntelliVaultPreferencesService preferencesService = ServiceManager.getService(IntelliVaultPreferencesService.class);
        injectPreferencesFromDialogState(userPreferences);
        preferencesService.setPreferences(userPreferences);

        VaultInvokerService vltInvoker = ServiceManager.getService(VaultInvokerService.class);
        vltInvoker.forceReInit();
    }

    /**
     * Serialize the current dialog state to a {@link IntelliVaultPreferences} object which can be used by IntelliVault.
     * Injects non-repository preferences only, repository settings are handled outside of this.
     *
     * @param preferencesBean The bean to inject the dialog state into.
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

    /**
     * Removes the currently selected {@link IntelliVaultCRXRepository} from the current {@link IntelliVaultPreferences} state.
     */
    private void deleteCurrentlySelectedRepository() {
        Object selectedItem = comboProfileSelect.getSelectedItem();
        if (selectedItem != null) {
            int deleteChoice = Messages.showYesNoDialog("Are you sure you want to delete the repository configuration '" + selectedItem.toString() + "'?", "Delete Configuration", null);
            if (deleteChoice == Messages.YES) {
                comboProfileSelect.removeItem(selectedItem);
                userPreferences.removeRepositoryConfiguration(((IntelliVaultCRXRepository) selectedItem).getName());
            }
        } else {
            log.warn("No option selected");
        }
    }

    /**
     * Puts the current {@link IntelliVaultCRXRepository} state in the {@link IntelliVaultPreferences} repository list.
     */
    private void saveCurrentRepository() {
        String repoName = txtRepoName.getText();

        // Setup the new repository.
        IntelliVaultCRXRepository newRepo = new IntelliVaultCRXRepository(
                repoName,
                txtRepoUrl.getText(),
                txtUsername.getText(),
                txtPassword.getText()
        );

        // Check if this put request is replacing an old repository configuration.
        IntelliVaultCRXRepository oldRepo = null;
        if (!repoName.equals(lastLoadedRepo) && lastLoadedRepo != null) {
            oldRepo = userPreferences.getRepositoryConfiguration(lastLoadedRepo);
        }

        if (oldRepo != null) {
            // Repo name has changed, replace in the same slot.
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

    /**
     * Sets the dialog state to reflect the newly selected {@link IntelliVaultCRXRepository}
     * If the user chooses the "Create New Repository..." option a new repository will be added to the {@link IntelliVaultPreferences} state.
     */
    private void loadSelectedRepository() {
        Object selectedItem = comboProfileSelect.getSelectedItem();
        if (selectedItem != null) {
            if (selectedItem instanceof IntelliVaultCRXRepository) {
                setDialogStateFromRepository((IntelliVaultCRXRepository) selectedItem);
                lastLoadedRepo = ((IntelliVaultCRXRepository) selectedItem).getName();
            } else {
                // New repo was selected
                IntelliVaultCRXRepository repo = new IntelliVaultCRXRepository();
                setDialogStateFromRepository(repo);
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

    /**
     * Sets the dialog state to represent a {@link IntelliVaultCRXRepository}.
     *
     * @param repository The {@link IntelliVaultCRXRepository} to update the ui values with.
     */
    private void setDialogStateFromRepository(IntelliVaultCRXRepository repository) {
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

    /**
     * Rebuilds the combo box that represents the current {@link IntelliVaultPreferences} state's {@link IntelliVaultCRXRepository} list.
     *
     * @param selectedItem An optional item to set as the selected item after rebuild.
     */
    private void rebuildRepositoryComboBox(IntelliVaultCRXRepository selectedItem) {
        comboProfileSelect.removeAllItems();
        comboProfileSelect.addItem("Create New Repository...");
        List<IntelliVaultCRXRepository> rValues = userPreferences.getRepoConfigs();
        for (IntelliVaultCRXRepository repo : rValues) {
            comboProfileSelect.addItem(repo);
        }
        if (selectedItem == null) {
            selectedItem = userPreferences.getFirstRepositoryConfiguration();
        }
        if (selectedItem != null) {
            comboProfileSelect.setSelectedItem(selectedItem);
        }
        setDialogStateFromRepository(selectedItem);
    }

    @Override
    public void disposeUIResources() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}

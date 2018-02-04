package com.razorfish.platforms.intellivault.actions;

import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultOperationConfig;
import com.razorfish.platforms.intellivault.config.IntelliVaultPreferences;
import com.razorfish.platforms.intellivault.services.IntelliVaultService;
import com.razorfish.platforms.intellivault.services.impl.IntelliVaultPreferencesService;
import com.razorfish.platforms.intellivault.ui.IntelliVaultRepositorySelector;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/15/13
 * Time: 1:31 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class IntelliVaultAbstractAction extends AnAction {

    private static final Logger log = Logger.getInstance(IntelliVaultAbstractAction.class);

    @Override
    public void update(final AnActionEvent evt) {
        PsiDirectory directory = getCRXDirectory(evt);
        //if directory is null (no jcr directory selected) or the directory is the jcr root, don't let them proceed.
        if (directory == null || directory.getName().equals(getIntelliVaultConfig().getRootFolderName())) {
            evt.getPresentation().setEnabled(false);
        }

    }

    public void actionPerformed(final AnActionEvent evt) {

        final IntelliVaultOperationConfig conf = getIntelliVaultConfig();

        final IntelliVaultPreferences preferences = ServiceManager.getService(IntelliVaultPreferencesService.class).getPreferences();

        // A user must config their repositories before using the tool.
        if (preferences.hasRepositoryConfigs()) {
            final PsiDirectory psiDir = getCRXDirectory(evt);
            final VaultOperationDirectory vaultOpDir = new VaultOperationDirectory(psiDir, conf.getRootFolderName());
            Project project = evt.getData(PlatformDataKeys.PROJECT);

            // Shows the dialog that lets the user select one of their configured CRX Repositories.
            final IntelliVaultRepositorySelector form = new IntelliVaultRepositorySelector(project, this);
            form.show();
            log.info("form exit code is " + form.getExitCode() + " we need " + DialogWrapper.OK_EXIT_CODE);
            if (form.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                IntelliVaultCRXRepository repository = getSelectedIntelliVaultCRXRepository();
                if (repository != null) {
                    boolean proceed = !conf.showMessageDialogs() ||
                            (
                                    Messages.showYesNoDialog(
                                            String.format(
                                                    getDialogMessage(),
                                                    repository.getRepoUrl() + vaultOpDir.getJcrPath(),
                                                    vaultOpDir.getPsiDir().getVirtualFile().getCanonicalPath()
                                            ),
                                            "Run IntelliVault?",
                                            Messages.getQuestionIcon()
                                    ) == Messages.YES
                            );
                    if (proceed) {
                        ProgressManager.getInstance().run(getTask(vaultOpDir, conf, repository, project));
                    } else {
                        log.debug("User canceled action after selecting a repository");
                    }
                } else {
                    log.warn("Cannot continue, selected repository is null");
                }
            } else {
                log.debug("User canceled action");
            }
        } else {
            Messages.showErrorDialog("You haven't set up any repositories yet. Go to File > Settings > IntelliVault to setup your repositories.", "Cannot Perform Action");
        }
    }

    protected abstract Task getTask(VaultOperationDirectory vaultOpDir, IntelliVaultOperationConfig conf, IntelliVaultCRXRepository repository, Project project);

    protected abstract String getDialogMessage();

    protected PsiDirectory getCRXDirectory(AnActionEvent evt) {
        IdeView ideView = evt.getData(LangDataKeys.IDE_VIEW);
        if (ideView != null) {
            PsiDirectory[] directories = ideView.getDirectories();
            if (directories != null && directories.length == 1) {
                PsiDirectory directory = directories[0];
                String rootFolderName = getIntelliVaultConfig().getRootFolderName();

                PsiDirectory curDir = directory;
                while (curDir != null) {
                    if (curDir.getName().equals(rootFolderName)) {
                        return directory;
                    }
                    curDir = curDir.getParentDirectory();
                }

            }
        }

        return null;
    }

    protected IntelliVaultService getVaultService() {
        return ServiceManager.getService(IntelliVaultService.class);
    }

    protected IntelliVaultOperationConfig getIntelliVaultConfig() {
        IntelliVaultPreferencesService preferences = ServiceManager.getService(IntelliVaultPreferencesService.class);
        return preferences.getPreferences().getOperationConfig();
    }

    /** The selected CRX repository from the select dialog. */
    private IntelliVaultCRXRepository crxRepository;

    /**
     * Gets the selected CRX repository from the {@link IntelliVaultRepositorySelector} that is shown when an action is executed.
     *
     * @return  The selected {@link IntelliVaultCRXRepository} or null if the selection was cancelled.
     * */
    public IntelliVaultCRXRepository getSelectedIntelliVaultCRXRepository() {
        return crxRepository;
    }


    /**
     * Sets the selected CRX repository from the {@link IntelliVaultRepositorySelector} that is shown when an action is executed.
     *
     * @param crxRepository The {@link IntelliVaultCRXRepository} to mark as selected.
     * */
    public void setSelectedIntelliVaultCRXRepository(IntelliVaultCRXRepository crxRepository) {
        this.crxRepository = crxRepository;
    }
}

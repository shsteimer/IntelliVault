package com.crownpartners.intellivault.actions;

import com.crownpartners.intellivault.config.IntelliVaultCRXRepository;
import com.crownpartners.intellivault.config.IntelliVaultOperationConfig;
import com.crownpartners.intellivault.services.IntelliVaultService;
import com.crownpartners.intellivault.services.impl.IntelliVaultPreferencesService;
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
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;

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
        if(directory==null || directory.getName().equals(getIntelliVaultConfig().getRootFolderName())) {
            evt.getPresentation().setEnabled(false);
            return;
        }

    }

    public void actionPerformed(final AnActionEvent evt) {
        final IntelliVaultCRXRepository repository = getSelectedIntelliVaultCRXRepository();
        final IntelliVaultOperationConfig conf = getIntelliVaultConfig();

        final PsiDirectory psiDir = getCRXDirectory(evt);
        final VaultOperationDirectory vaultOpDir = new VaultOperationDirectory(psiDir,conf.getRootFolderName());

        boolean proceed = !conf.showMessageDialogs() ||
                (
                Messages.showYesNoDialog(String.format(getDialogMessage(),new Object[]{
                        repository.getRepoUrl() + vaultOpDir.getJcrPath(),
                        vaultOpDir.getPsiDir().getVirtualFile().getCanonicalPath()}), "Run IntelliVault?",
                        Messages.getQuestionIcon()) == Messages.YES);
        if (proceed) {
            Project project = evt.getData(PlatformDataKeys.PROJECT);
            ProgressManager.getInstance().run(getTask(vaultOpDir, conf, repository, project));
        }
    }

    protected abstract Task getTask(VaultOperationDirectory vaultOpDir, IntelliVaultOperationConfig conf,
                                    IntelliVaultCRXRepository repository, Project project);

    protected abstract String getDialogMessage();

    protected PsiDirectory getCRXDirectory(AnActionEvent evt) {
        IdeView ideView = evt.getData(LangDataKeys.IDE_VIEW);
        if(ideView!=null){
            PsiDirectory[] directories = ideView.getDirectories();
            if(directories!=null && directories.length==1) {
                PsiDirectory directory = directories[0];
                String rootFolderName = getIntelliVaultConfig().getRootFolderName();

                PsiDirectory curDir=directory;
                while(curDir!=null) {
                    if(curDir.getName().equals(rootFolderName))     {
                        return directory;
                    }
                    curDir=curDir.getParentDirectory();
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

    protected IntelliVaultCRXRepository getSelectedIntelliVaultCRXRepository() {
        IntelliVaultPreferencesService preferences = ServiceManager.getService(IntelliVaultPreferencesService.class);

        return preferences.getPreferences().getRepositoryList().get(0);
    }
}

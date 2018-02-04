package com.razorfish.platforms.intellivault.actions;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.razorfish.platforms.intellivault.config.IntelliVaultCRXRepository;
import com.razorfish.platforms.intellivault.config.IntelliVaultOperationConfig;
import com.razorfish.platforms.intellivault.exceptions.IntelliVaultException;
import com.razorfish.platforms.intellivault.services.IntelliVaultService;
import org.jetbrains.annotations.NotNull;

/**
 * Created with IntelliJ IDEA. User: sean.steimer Date: 3/13/13 Time: 8:54 PM To
 * change this template use File | Settings | File Templates.
 */
public class IntelliVaultExportAction extends IntelliVaultAbstractAction {

    @Override
    protected Task getTask(VaultOperationDirectory vaultOpDir, IntelliVaultOperationConfig conf,
                           IntelliVaultCRXRepository repository, Project project) {
        return new IntelliVaultExportTask(vaultOpDir, conf, repository, project);
    }

    protected String getDialogMessage() {
        return "Export from %s to %s?";
    }

    private class IntelliVaultExportTask extends Task.Backgroundable {

        private VaultOperationDirectory vaultOpDir;
        private IntelliVaultOperationConfig conf;
        private IntelliVaultCRXRepository repository;
        private ConsoleView console;

        public IntelliVaultExportTask(final VaultOperationDirectory vaultOpDir, final IntelliVaultOperationConfig conf,
                                      final IntelliVaultCRXRepository repository, final Project project) {

            super(project, "Running IntelliVault Export Action");

            this.conf = conf;
            this.repository = repository;
            this.vaultOpDir = vaultOpDir;

            TextConsoleBuilderFactory factory = TextConsoleBuilderFactory.getInstance();
            this.console = factory.createBuilder(project).getConsole();

            createToolWindow(project);

        }

        private void createToolWindow(final Project project){
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            String twId = "IntelliVault";
            ToolWindow toolWindow = toolWindowManager.getToolWindow(twId);
            if (toolWindow == null) {
                toolWindow = toolWindowManager.registerToolWindow(twId, true, ToolWindowAnchor.BOTTOM);
            }

            ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
            Content content = contentFactory.createContent(console.getComponent(), "", false);

            toolWindow.getContentManager().addContent(content);
            toolWindow.getContentManager().setSelectedContent(content);
            //TODO toolWindow.setIcon();

            toolWindow.show(new Runnable() {
                @Override
                public void run() {
                    // Do nothing
                }
            });
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {
            final IntelliVaultService vaultService = getVaultService();
            try {
                vaultService.vaultExport(repository, conf, vaultOpDir, progressIndicator, console);

                if (conf.showMessageDialogs()) {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            Messages.showInfoMessage(String.format("Successfully Exported from %s.",
                                    repository.getRepoUrl() + vaultOpDir.getJcrPath()),
                                    "IntelliVault Export Completed Successfully!");
                        }
                    });
                }
            } catch (final IntelliVaultException e) {
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Messages.showErrorDialog(e.getLocalizedMessage(), "IntelliVault Error!");
                    }
                });

            }
        }
    }
}

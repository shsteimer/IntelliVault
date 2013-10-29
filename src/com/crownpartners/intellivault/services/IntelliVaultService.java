package com.crownpartners.intellivault.services;

import com.crownpartners.intellivault.actions.VaultOperationDirectory;
import com.crownpartners.intellivault.exceptions.IntelliVaultException;
import com.crownpartners.intellivault.config.IntelliVaultCRXRepository;
import com.crownpartners.intellivault.config.IntelliVaultOperationConfig;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.progress.ProgressIndicator;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/15/13
 * Time: 1:10 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IntelliVaultService {

    /**
     * Execute a vault export.
     *
     * @param repository the crx repository that will be exported from
     * @param opConf the configuration operations which will be used for the export
     * @param vaultOperationDirectory the directory that will be exported from crx
     * @param progressIndicator the progress indicator to be used to inform the user of the operations progress towards
     *                          completion
     * @param console the ConsoleView where IntelliVault can log it's output during execution of export
     * @throws IntelliVaultException if an error occurs preventing completion
     */
    void vaultExport(IntelliVaultCRXRepository repository, IntelliVaultOperationConfig opConf,
                     VaultOperationDirectory vaultOperationDirectory, ProgressIndicator progressIndicator,
                     ConsoleView console)
            throws IntelliVaultException;

    /**
     * Execute a vault import.
     *
     * @param repository the crx repository that will be imported to
     * @param opConf the configuration operations which will be used for the import
     * @param vaultOperationDirectory the directory that will be imported to crx
     * @param progressIndicator the progress indicator to be used to inform the user of the operations progress towards
     *                          completion
     * @param console the ConsoleView where IntelliVault can log it's output during execution of import
     * @throws IntelliVaultException if an error occurs preventing completion
     */
    void vaultImport(IntelliVaultCRXRepository repository, IntelliVaultOperationConfig opConf,
                     VaultOperationDirectory vaultOperationDirectory, ProgressIndicator progressIndicator,
                     ConsoleView console)
            throws IntelliVaultException;
}

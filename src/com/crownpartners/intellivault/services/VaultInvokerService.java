package com.crownpartners.intellivault.services;

import com.crownpartners.intellivault.exceptions.IntelliVaultException;

/**
 * Created with IntelliJ IDEA.
 * User: sean.steimer
 * Date: 3/17/13
 * Time: 9:06 AM
 * To change this template use File | Settings | File Templates.
 */
public interface VaultInvokerService {
    /**
     * Execute a vault command, as specified by the arguments passed in.
     * @param vaultPath the path to the vault folder on the file system.
     * @param args the arguments which will be used to invoke vault.
     * @throws IntelliVaultException if an error occurs during vault execution.
     */
    void invokeVault(String vaultPath, String[] args) throws IntelliVaultException;

    /**
     * Force vault to re-initialize before next invocation.  This should be used when a configuration changes which
     * would affect they way vault is called.
     */
    void forceReInit();
}

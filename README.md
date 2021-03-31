# IntelliVault

## Deprecation Notice

This plugin is deprecated and no longer updated or supported. It's functionality is largely redundant to that of the [repo tool](https://github.com/Adobe-Marketing-Cloud/tools/tree/master/repo), which can be easily bound to keyboard shortcuts in Intellij by following the steps [here](https://github.com/Adobe-Marketing-Cloud/tools/tree/master/repo#integration-into-intellij). 

You are strongly encouraged to stop using IntelliValut and use repo instead.

## About

A plugin for IntelliJ IDEA to interact with JCR repositories via the FileVault tool which is packaged with Adobe Experience Manager.

This plugin is largely based upon, and liberally borrows from, [VaultClipse](http://vaultclipse.sourceforge.net/) which is a plugin for the Eclipse IDE for interacting with FileVault.

The plugin can be found in the [JetBrains IDEA Plugin Repository](http://plugins.jetbrains.com/plugin/7328)

## Supported product versions

The *IntelliVault* plugin is currently supported on the following Intellij products:

* Intellij IDEA 2016.1 Community/Ultimate

## Installation

To install the plugin using the Intellij built-in plugin management dialog, go to **Preferences** > **Plugins** > **Browse Repositories**, type *Intellivault* and click the **Install** button.

NOTE: If after installing the plugin and restarting the IDE you don't see the **IntelliVault** option under **Tools** then your version is most likely not supported.

## Configuration

IntelliVault uses the [Filevault](https://helpx.adobe.com/experience-manager/6-3/sites/developing/using/ht-vlttool.html) tool under the covers to transfer content between IDEA and your AEM repository.  This is a hard dependency, and requires unpacking Filevault before you can configure the plugin.

Locate `filevault.zip` or `filevault.tgz` inside of your AEM directory at `crx-quickstart/opt/filevault` and unpack that to any directory.

Once you have unpacked **Filevault**, open the plugin configuration dialog accessible via **Preferences** > **Tools** > **IntelliVault** and set the following properties.

- **Vault Directory**: Set this to the directory where you unpacked Filevault, ie. `/Users/myuser/dev/tools/vault/vault-cli-3.1.38/bin`
- **Repository**: See `Multi-Repository Configuration` below
- **Show Operation Confirmation Dialogs**: If checked, IntelliVault will prompt you to comfirm each operation.  Uncheck this to remove those confirmations
- Other properties are optional and shouldn't require changes, but should be self-explanatory if/when changes are required

### Multi-Repository Configuration

IntelliVault now allows you to configure and manage multiple repository configurations. For each repo, you must set the following:

- **Repository Name**: Friendly name for this repo.
- **CRX Repository URL**: URL for the repo, i.e. http://localhost:4502
- **Username/Password**: Credentials used for transferring, ie. admin/admin

If more than one repo is configured, you will be prompted to select a repo for each operation.  If only one repo exists, that repo will be used without any prompt.

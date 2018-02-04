IntelliVault
============

A plugin for IntelliJ IDEA to interact with JCR repositories via the FileVault tool which is packaged with Adobe Experience Manager.

This plugin is largely based upon, and liberally borrows from, [VaultClipse](http://vaultclipse.sourceforge.net/) which is a plugin for the Eclipse IDE for interacting with FileVault.

The plugin can be found in the [JetBrains IDEA Plugin Repository](http://plugins.jetbrains.com/plugin/7328)

Supported product versions
---------------------------
The *IntelliVault* plugin is currently supported on the following Intellij products:

* Intellij IDEA 14 Community/Ultimate

Installation
-------------
To install the plugin using the Intellij built-in plugin management dialog, go to **Preferences** > **Plugins** > **Browse Repositories**, type *Intellivault* and click the **Install** button.

NOTE: If after installing the plugin and restarting the IDE you don't see the **IntelliVault** option under **Other Settings** then your version is most likely not supported.

Configuration
-------------
The plugin is using the [Vault](http://docs.adobe.com/docs/en/crx/2-3/how_to/how_to_use_the_vlttool.html) package that is supplied with Adobe CQ5/AEM6. 

The archived package for Vault command line tools is located under your CQ/AEM server folder in `crx-quickstart/opt/filevault/filevault.zip`. 

Unpack the file `filevault.zip` to any folder on your file system, and then in the **Intellivault** configuration dialog enter the path to the unpacked *Filevault* folder, i.e. `/Users/ccpizz/bin/vault-cli-3.1.6`.

Once you have unpacked the **filevault** files, open the plugin configuration dialog accessible via **Preferences** > **Other Settings** > **IntelliVault**.

In the dialog specify the repository:

**CRX Repository URL**: i.e. http://localhost:4502

**Username**: admin

**Password**: admin

Optionally untick the *Show Dialogs* checkbox if you don't want the confirmation dialog to pop up every time you sync.


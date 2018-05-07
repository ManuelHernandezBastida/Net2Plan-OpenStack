package com.net2plan.gui.plugins.utils;

import com.net2plan.gui.utils.FileChooserConfirmOverwrite;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * @author Jorge San Emeterio
 * @date 9/19/17
 */
public class CatalogFileChooser extends FileChooserConfirmOverwrite
{
    public CatalogFileChooser()
    {
        this(null);
    }

    public CatalogFileChooser(File currentDirectory)
    {
        super(currentDirectory);

        FileFilter cn2pFilter = new FileNameExtensionFilter("Net2Plan catalogue file (*.cn2p)", "cn2p");
        this.addChoosableFileFilter(cn2pFilter);

        this.setAcceptAllFileFilterUsed(false);
    }

    protected void onAccept()
    {
        this.getSelectedFile().delete();
    }
}

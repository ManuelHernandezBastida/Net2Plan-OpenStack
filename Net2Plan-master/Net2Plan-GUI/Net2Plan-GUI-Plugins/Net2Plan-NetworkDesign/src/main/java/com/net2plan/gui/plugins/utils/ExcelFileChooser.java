package com.net2plan.gui.plugins.utils;

import com.net2plan.gui.utils.FileChooserConfirmOverwrite;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

/**
 * @author Jorge San Emeterio
 * @date 9/19/17
 */
public class ExcelFileChooser extends FileChooserConfirmOverwrite
{
    public ExcelFileChooser()
    {
        this(null);
    }

    public ExcelFileChooser(File currentDirectory)
    {
        super(currentDirectory);

        this.setAcceptAllFileFilterUsed(false);

        FileFilter xlsFilter = new FileNameExtensionFilter("Excel 2003 file (*.xls)", "xls");
        FileFilter xlsxFilter = new FileNameExtensionFilter("Excel 2007 file (*.xlsx)", "xlsx");

        this.addChoosableFileFilter(xlsxFilter);
        this.addChoosableFileFilter(xlsFilter);
    }

    protected void onAccept()
    {
        this.getSelectedFile().delete();
    }
}

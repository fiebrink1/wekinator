/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wekimini.util;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author rebecca
 */
public class FileChooserWithExtension extends JFileChooser {

    protected final String extension;
    protected final String descriptionOfType;
    protected final boolean isSave;
    //  private final String defaultName;
    // private final String defaultDir;

    //defFile: should be fully qualified
    // defDir: also qualified
    // priority is File, if not null
    public FileChooserWithExtension(String ext, String description, File defFile, File defDir, boolean isSave) {
        super();
        this.extension = ext;
        this.descriptionOfType = description;
        this.isSave = isSave;
        //this.defaultName = defFname;
        //this.defaultDir = defDir;

        if (defDir != null) {
            setCurrentDirectory(defDir);
        }
        if (defFile != null) {
            setSelectedFile(defFile);
        }

        // this.set
        setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (isSave) {
            setDialogType(JFileChooser.SAVE_DIALOG);
        } else {
            setDialogType(JFileChooser.OPEN_DIALOG);
        }
        
        FileFilter filt = new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                String extt = Util.getExtension(f);
                if (extt != null && extt.equals(extension)) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public String getDescription() {
                if (descriptionOfType != null) {
                    return descriptionOfType + " (." + extension + ")";
                } else {
                    return "Supported types";
                }
            }
        };

        addChoosableFileFilter(filt);
        setFileFilter(filt);
    }

    private static String rebuildName(String[] parts, int lastIndex, String newExt) {
        String s = "";
        for (int i = 0; i <= lastIndex; i++) {
            s += parts[i];
        }
        s += "." + newExt;
        return s;
    }

    @Override
    public void approveSelection() {
        File selected = getSelectedFile();
        if (selected == null) {
            return;
        }

        if (!isSave) {
            //TODO
            if (selected == null || !selected.exists()) {
                return;
            }
            
            String extt = Util.getExtension(selected);
            if (extt == null || !extt.equals(extension)) {
                     FileFilter[] myFilters = this.getChoosableFileFilters();
                     String acceptables = "";
                     for (int i = 1; i < myFilters.length; i++) {
                         if (myFilters[i].accept(selected)) {
                             super.approveSelection();
                             return;
                         }
                       
                     }

                    int lResponse = JOptionPane.showConfirmDialog(this,
                            "This file does not match the recommended " +
                            ((descriptionOfType != null) ? descriptionOfType : "") +
                            " extension of ." + extension +
                            ". \nTry to open it anyway?", "",
                            JOptionPane.YES_NO_OPTION);

                    if (lResponse != JOptionPane.YES_OPTION) {
                        return;
                    }
               
            }


        } else {
           if (!selected.exists()) {
                String name = selected.getName();
                String parts[] = name.split("\\.");

                if (parts.length < 2) {
                    //there's no extension.
                    int lResponse = JOptionPane.showConfirmDialog(this,
                            "You have not provided a file extension. " +
                            "\nWould you like to add the recommended " +
                            ((descriptionOfType != null) ? descriptionOfType : "") +
                            "extension of ." + extension + "?", "",
                            JOptionPane.YES_NO_OPTION);
                    if (lResponse == JOptionPane.YES_OPTION) {
                        String newName = rebuildName(parts, parts.length - 1, extension);
                        setSelectedFile(new File(selected.getParent(), newName));
                    }

                } else if (!parts[parts.length - 1].equals(extension)) {
                    //Different extension
                    int lResponse = JOptionPane.showConfirmDialog(this,
                            "The file extension of ." + parts[parts.length - 1] +
                            " does not match the recommended " +
                            ((descriptionOfType != null) ? descriptionOfType : "") +
                            " extension of ." + extension +
                            ". \nWould you like to change the extension to ." + extension + "?", "",
                            JOptionPane.YES_NO_OPTION);

                    if (lResponse == JOptionPane.YES_OPTION) {
                        String newName = rebuildName(parts, parts.length - 2, extension);
                        setSelectedFile(new File(selected.getParent(), newName));
                        
                    }
                }
            }

           //Now selected might exist (and might have been renamed to exist now, but not earlier)
           selected = getSelectedFile();
          // File t = new File(selected.getAbsolutePath());
           if (selected.exists()) {
            //check for overwrite
               int lResponse = JOptionPane.showConfirmDialog(this,
                    "The file " + selected.getName() + " already exists. Do you " +
                    "want to replace the existing file?", "",
                    JOptionPane.YES_NO_OPTION);

            if (lResponse != JOptionPane.YES_OPTION) {
                return;
            }

           }

        }
        //approves the selection
        super.approveSelection();
    }
}

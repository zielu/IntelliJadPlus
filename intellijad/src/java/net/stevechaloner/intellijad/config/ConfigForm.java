package net.stevechaloner.intellijad.config;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

public class ConfigForm {
    private JCheckBox useProjectSpecificIntelliJadCheckBox;
    private JComboBox navTriggeredDecomp;
    private JCheckBox clearAndCloseConsoleCheckBox;
    private JCheckBox cleanupSourceRootsCheckBox;
    private JCheckBox tempDirectoryCheckBox;
    private JTextField tempDirTextField;
    private JButton browseTempDirButton;
    private JTextField outputDirectoryTextField;
    private JButton outputDirBrowseButton;
    private JCheckBox createIfDirectoryDoesnCheckBox;
    private JCheckBox markDecompiledFilesAsCheckBox;
    private JTable exclusionTable;
    private JButton removeExclusionButton;
    private JTextField excludePackageTextField;
    private JButton packageChooserButton;
    private JButton addExclusionButton;
    private JCheckBox alwaysExcludePackagesRecursivelyCheckBox;
}

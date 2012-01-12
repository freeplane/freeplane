package org.freeplane.main.application;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.freeplane.main.application.CommandLineParser.Options;
import org.junit.Before;
import org.junit.Test;

public class CommandLineParserTest {
    private String[] emptyArray = new String[0];
    private String[] someFiles = args("file1", "file2");
    private String menuItem = "$SomeAction0$";
    private String[] menuItems = args(menuItem);
    private String[] menuItemsWithQuit = args(menuItem, CommandLineParser.QUIT_MENU_ITEM_KEY);

    @Before
    public void setUp() {
        System.setProperty("nonInteractive", "false");
    }
    
    @Test
    public void testFilesOnly() {
        final Options result = CommandLineParser.parse(someFiles);
        assertEquals(Arrays.asList(someFiles), result.getFilesToOpen());
        check(result, someFiles, emptyArray, false, false, false);
    }

    @Test
    public void testFilesAndMenuItemOneArg() {
        final String[] args = merge(args("-X" + menuItem), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItems, false, false, false);
    }
    
    @Test
    public void testFilesAndMenuItemTwoArgs() {
        final String[] args = merge(args("-X", menuItem), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItems, false, false, false);
    }
    
    // options must come first or they are interpreted as files
    @Test
    public void testWrongOrder() {
        final String[] args = merge(someFiles, args("-X" + menuItem));
        check(CommandLineParser.parse(args), args, emptyArray , false, false, false);
    }

    @Test
    public void testFilesAndMenuItemAndStop() {
        final String[] args = merge(args("-SX" + menuItem), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItemsWithQuit, true, false, false);
    }

    @Test
    public void testFilesAndMenuItemAndHelp1() {
        final String[] args = merge(args("-hX" + menuItem), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItems, false, false, true);
    }
    
    @Test
    public void testFilesAndMenuItemAndHelp2() {
        final String[] args = merge(args("--help", "-X" + menuItem), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItems, false, false, true);
    }
    
    @Test
    public void testFilesAndMenuItemAndStopAndNonInteractive() {
        final String[] args = merge(args("-NSX" + menuItem), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItemsWithQuit, true, true, false);
    }
    
    @Test
    public void testFilesAndMenuItemAndStopTwoArgs() {
        final String[] args = merge(args("-X" + menuItem, "-S"), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItemsWithQuit, true, false, false);
    }
    
    @Test
    public void testFilesAndMenuItemAndStopAndNonInteractiveThreeArgs() {
        final String[] args = merge(args("-X" + menuItem, "-N", "-S"), someFiles);
        check(CommandLineParser.parse(args), someFiles, menuItemsWithQuit, true, true, false);
    }

    private void check(Options result, String[] filesToOpen, final String[] menuItems, final boolean stopAfterLaunch,
                       boolean nonInteractive, boolean isHelpRequested) {
        assertArrayEquals(filesToOpen, result.getFilesToOpenAsArray());
        assertArrayEquals(menuItems, result.getMenuItemsToExecuteAsArray());
        assertEquals(stopAfterLaunch, result.isStopAfterLaunch());
        assertEquals(nonInteractive, result.isNonInteractive());
    }
    
    private String[] args(String... strings) {
        return strings;
    }
    
    private String[] merge(String[]... stringArrays) {
        ArrayList<String> result = new ArrayList<String>();
        for (String[] strings : stringArrays) {
            result.addAll(Arrays.asList(strings));
        }
        return result.toArray(new String[result.size()]);
    }
}

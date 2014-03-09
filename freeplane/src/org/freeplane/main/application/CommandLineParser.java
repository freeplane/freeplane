package org.freeplane.main.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.freeplane.core.util.Compat;

class CommandLineParser {
    static final String QUIT_MENU_ITEM_KEY = "MB_QuitAction";

    public static class Options {
        private static final String HELP_MESSAGE = "Use:\n\tfreeplane [options] [file1 [file2 ...]]" //
                + "\n -X<menukey>: execute menu item with key <menukey>. Use devtools add-on to find the menu keys" //
                + "\n -S: stop after executing menu items" //
                + "\n -N: set the 'nonInteractive' system property to 'true'" //
                + "\n -U<userdir>: set the freeplane user config directory (default: "
                + Compat.getApplicationUserDirectory() + ")" //
                + "\n -h|--help: print this help";
        private List<String> filesToOpen = new ArrayList<String>();
        private List<String> menuItemsToExecute = new ArrayList<String>();
        private boolean stopAfterLaunch;
        private boolean nonInteractive;
        private boolean helpRequested = false;

        public void setFilesToOpen(final String[] filesToOpen) {
            this.filesToOpen = Arrays.asList(filesToOpen);
        }

        public void setMenuItemsToExecute(final String[] menuItemsToExecute) {
            this.menuItemsToExecute = Arrays.asList(menuItemsToExecute);
        }

        public boolean isStopAfterLaunch() {
            return stopAfterLaunch;
        }

        public void setStopAfterLaunch(boolean stopAfterLaunch) {
            this.stopAfterLaunch = stopAfterLaunch;
        }

        public List<String> getFilesToOpen() {
            return filesToOpen;
        }

        public String[] getFilesToOpenAsArray() {
            return filesToOpen.toArray(new String[filesToOpen.size()]);
        }

        public List<String> getMenuItemsToExecute() {
            return menuItemsToExecute;
        }

        public String[] getMenuItemsToExecuteAsArray() {
            return menuItemsToExecute.toArray(new String[menuItemsToExecute.size()]);
        }

        public boolean hasMenuItemsToExecute() {
            return !menuItemsToExecute.isEmpty();
        }

        public void addFilesToOpen(String file) {
            filesToOpen.add(file);
        }

        public void addMenuItemToExecute(String item) {
            menuItemsToExecute.add(item);
        }

        /** leads to setting of system property 'nonInteractive' - check via
         * <pre>
         *   boolean nonInteractive = Boolean.parseBoolean(System.getProperty("nonInteractive"));
         * </pre>
         */
        public void setNonInteractive(boolean b) {
            nonInteractive = b;
        }

        public boolean isNonInteractive() {
            return nonInteractive;
        }

        public boolean isHelpRequested() {
            return helpRequested;
        }

        public void setHelpRequested(boolean helpRequested) {
            this.helpRequested = helpRequested;
        }

        @Override
        public String toString() {
            return "Options(files: " + filesToOpen + ", menuItems: " + menuItemsToExecute + ", stopAfterLaunch: "
                    + stopAfterLaunch + ", nonInteractive: " + nonInteractive + ")";
        }

        public String getHelpMessage() {
            return HELP_MESSAGE;
        }
    }

    public static CommandLineParser.Options parse(String[] args) {
for(String arg:args) System.out.println(arg);
        CommandLineParser.Options result = new CommandLineParser.Options();
        if (args == null || args.length == 0 || !args[0].startsWith("-")) {
            result.setFilesToOpen(args);
            return result;
        }
        int i = 0;
        String[] mutableArgs = new String[args.length];
        System.arraycopy(args, 0, mutableArgs, 0, args.length);
        args = mutableArgs;
        for (; i != args.length; ++i) {
            String arg = args[i];
            if (arg.startsWith("-S")) {
                result.setStopAfterLaunch(true);
                // -SX mymenuitem is allowed
                if (arg.length() > 2) {
                    args[i] = "-" + arg.substring(2);
                    --i;
                }
            }
            else if (arg.startsWith("-N")) {
                result.setNonInteractive(true);
                // -NX mymenuitem is allowed
                if (arg.length() > 2) {
                    args[i] = "-" + arg.substring(2);
                    --i;
                }
            }
            else if (arg.startsWith("-X")) {
                if (arg.length() > 2)
                    result.addMenuItemToExecute(arg.substring(2));
                else if (args.length >= i)
                    result.addMenuItemToExecute(args[++i]);
            }
            else if (arg.startsWith("-U")) {
                String userdir = null;
                if (arg.length() > 2)
                    userdir = arg.substring(2);
                else if (args.length >= i)
                    userdir = args[++i];
                else
                    System.err.println("option -U<userdir> misses its parameter");
                if (userdir != null)
                    System.setProperty("org.freeplane.userfpdir", userdir);
            }
            else if (arg.startsWith("-h")) {
                result.setHelpRequested(true);
                // -hX mymenuitem is allowed
                if (arg.length() > 2) {
                    args[i] = "-" + arg.substring(2);
                    --i;
                }
            }
            else if (arg.equals("--help")) {
                result.setHelpRequested(true);
            }
            else {
                break;
            }
        }
        for (; i != args.length; ++i)
            result.addFilesToOpen(args[i]);
        if (result.stopAfterLaunch && !result.menuItemsToExecute.contains(QUIT_MENU_ITEM_KEY))
            result.addMenuItemToExecute(QUIT_MENU_ITEM_KEY);
        if (result.isHelpRequested()) {
            System.out.println(result.getHelpMessage());
        }
        return result;
    }
}

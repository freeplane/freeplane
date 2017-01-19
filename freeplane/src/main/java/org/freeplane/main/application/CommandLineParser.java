package org.freeplane.main.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.freeplane.core.util.Compat;

class CommandLineParser {
    static class Options {
        private static final String HELP_MESSAGE = //
                "\nUsage:\n\tfreeplane.bat [options] [file1 [file2 ...]]\n" //
                + "\n -X<menukey>   : execute menu item with key <menukey>." //
                + "\n                 hint: use devtools add-on to find appropriate menu keys" //
                + "\n -S            : stop after executing menu items" //
                + "\n -N            : set the 'nonInteractive' system property to 'true'" //
                + "\n -U<userdir>   : set the freeplane user config directory (default: "
                + Compat.getDefaultFreeplaneUserDirectory() + ")" //
                + "\n -h , --help   : print this help text";
        private List<String> filesToOpen = new ArrayList<String>();
        private List<String> menuItemsToExecute = new ArrayList<String>();
        private boolean stopAfterLaunch;
        private boolean nonInteractive;
        private boolean helpRequested = false;

        private void setFilesToOpen(final String[] filesToOpen) {
            this.filesToOpen = Arrays.asList(filesToOpen);
        }

        boolean shouldStopAfterLaunch() {
            return stopAfterLaunch;
        }

        private void setStopAfterLaunch(boolean stopAfterLaunch) {
            this.stopAfterLaunch = stopAfterLaunch;
        }

        String[] getFilesToOpenAsArray() {
            return filesToOpen.toArray(new String[filesToOpen.size()]);
        }

        List<String> getMenuItemsToExecute() {
            return menuItemsToExecute;
        }

        boolean hasMenuItemsToExecute() {
            return !menuItemsToExecute.isEmpty();
        }

        private void addFilesToOpen(String file) {
            filesToOpen.add(file);
        }

        private void addMenuItemToExecute(String item) {
            menuItemsToExecute.add(item);
        }

        /** leads to setting of system property 'nonInteractive' - check via
         * <pre>
         *   boolean nonInteractive = Boolean.parseBoolean(System.getProperty("nonInteractive"));
         * </pre>
         */
        private void setNonInteractive(boolean b) {
            nonInteractive = b;
        }

        boolean isNonInteractive() {
            return nonInteractive;
        }

        private boolean isHelpRequested() {
            return helpRequested;
        }

        private void setHelpRequested(boolean helpRequested) {
            this.helpRequested = helpRequested;
        }

        @Override
        public String toString() {
            return "Options(files: " + filesToOpen + ", menuItems: " + menuItemsToExecute + ", stopAfterLaunch: "
                    + stopAfterLaunch + ", nonInteractive: " + nonInteractive + ")";
        }

        private String getHelpMessage() {
            return HELP_MESSAGE;
        }
    }

    static CommandLineParser.Options parse(String[] args, boolean firstRun) {
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
                else if (args.length > i + 1)
                    result.addMenuItemToExecute(args[++i]);
            }
            else if (arg.startsWith("-U")) {
                String userdir = null;
                if (arg.length() > 2)
                    userdir = arg.substring(2);
                else if (args.length > i + 1)
                    userdir = args[++i];
                else {
                    if (firstRun)
                        System.err.println("option -U<userdir> misses its parameter");
                }
                if (userdir != null) {
                    System.setProperty(Compat.FREEPLANE_USERDIR_PROPERTY, userdir);
                    // make sure that old settings aren't imported!
                    System.setProperty("org.freeplane.old_userfpdir", userdir);
                }
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
        if (result.isHelpRequested() && firstRun) {
            System.out.println(result.getHelpMessage());
            System.exit(0);
        }
        return result;
    }
}

package main.java;

import java.util.*;

public class Solution {

    static private final String DEPEND_CMD = "DEPEND";
    static private final String INSTALL_CMD = "INSTALL";
    static private final String REMOVE_CMD = "REMOVE";
    static private final String LIST_CMD = "LIST";
    static private final String END_CMD = "END";

    static private Map<String, Set<String>> dependsOnMap = new HashMap<>();
    static private Map<String, Set<String>> dependedByMap = new HashMap<>();
    static private Set<String> installedComponents = new HashSet<>();

    /**
     * Processes the set of user input commands
     *
     * @param input Set of instructions to be executed
     */
    private static void doIt(String[] input) {

        for (String line : input) {
            if (line != null && !line.isEmpty()) {
                processCmd(line);
            }
        }

    }

    /**
     * Process a single line of command from the input
     *
     * @param line A single instruction to be executed.
     *             Can contain one of the commands DEPEND, INSTALL, REMOVE, LIST or END.
     */
    private static void processCmd(String line) {
        System.out.println(line);
        String[] tokens = line.split("\\s+");
        String cmd = tokens[0];
        switch (cmd) {
            case DEPEND_CMD:
                processDependCmd(tokens);
                break;
            case INSTALL_CMD:
                processInstallCmd(tokens);
                break;
            case REMOVE_CMD:
                processRemoveCmd(tokens);
                break;
            case END_CMD:
                processEndCmd();
                break;
            case LIST_CMD:
                processListCmd();
                break;
            default:
                processUnknownCmd(cmd);
                break;
        }
    }

    /**
     * Method to process a DEPEND command.
     * This method populates two maps.
     * dependsOnMap : Maintains the relationship between a component and its dependencies.
     * dependedByMap : Maintains the relationship between a component and the list of components that depends on it.
     *
     * @param tokens Requires an array that has minimum three values. 1) command 2) parent component and 3) dependencies.
     *               Eg) DEPEND TELNET TCPIP NETCARD
     */
    private static void processDependCmd(String[] tokens) {
        if (tokens.length >= 3) {
            String parentComponent = tokens[1];

            Set<String> dependencySet = new HashSet<>();
            if (dependsOnMap.containsKey(parentComponent)) {
                dependencySet = dependsOnMap.get(parentComponent);
            }

            for (int i = 2; i < tokens.length; i++) {
                String dependency = tokens[i];

                if (hasCyclicDependency(parentComponent, dependency)) {
                    System.out.println(dependency + " depends on " + parentComponent + ", ignoring command");
                    return;
                }

                dependencySet.add(dependency);

                if (dependedByMap.containsKey(dependency)) {
                    dependedByMap.get(dependency).add(parentComponent);
                } else {
                    Set<String> dependedBySet = new HashSet<>();
                    dependedBySet.add(parentComponent);
                    dependedByMap.put(dependency, dependedBySet);
                }
            }

            dependsOnMap.put(parentComponent, dependencySet);
        } else {
            exitWithError("DEPEND command is incomplete. Requires a parent component and its dependency. Eg) DEPEND TELNET TCPIP");
        }
    }

    /**
     * Method to check for cyclic dependency. Returns true if a cyclic dependency is present.
     *
     * @param parent     The parent component to check for cyclic dependency
     * @param dependency The dependency component to check for cyclic dependency
     * @return boolean
     */
    private static boolean hasCyclicDependency(String parent, String dependency) {
        if (parent.equals(dependency)) {
            return true;
        }

        if (dependsOnMap.containsKey(dependency)) {
            return dependsOnMap.get(dependency).contains(parent);
        } else {
            return false;
        }
    }

    /**
     * Method to process INSTALL command.
     *
     * @param tokens Requires an array that has two values. 1) Command 2) Component to install
     *               Eg) INSTALL TELNET
     */
    private static void processInstallCmd(String[] tokens) {
        if (tokens.length == 2) {
            String compToInstall = tokens[1];
            installComponent(compToInstall);
        } else {
            exitWithError("Invalid syntax for INSTALL command. Eg) INSTALL TELNET");
        }
    }

    private static void installComponent(String compToInstall){
        if (dependsOnMap.containsKey(compToInstall)) {
            Set<String> dependencySet = dependsOnMap.get(compToInstall);
            for (String dependency : dependencySet) {
                if (!installedComponents.contains(dependency)) {
                    installComponent(dependency);
                }else{
                    System.out.println(dependency + " is already installed.");
                }
            }
        }
        if (!installedComponents.contains(compToInstall)) {
            System.out.println("Installing " + compToInstall);
            installedComponents.add(compToInstall);
        } else {
            System.out.println(compToInstall + " is already installed.");
        }
    }

    /**
     * Method to process REMOVE command.
     *
     * @param tokens Requires an array that has two values. 1) Command 2) Component to remove
     *               Eg) REMOVE TELNET
     */
    private static void processRemoveCmd(String[] tokens) {

        if (tokens.length == 2) {
            String compToRem = tokens[1];
            removeComponent(compToRem,new HashSet<>());
        } else {
            exitWithError("Invalid syntax for REMOVE command. Eg) REMOVE TELNET");
        }
    }

    private static void removeComponent(String compToRem, Set<String> toBeRemoved){
        if (installedComponents.contains(compToRem)) {
            toBeRemoved.add(compToRem);
            if (dependedByMap.containsKey(compToRem)) {
                Set<String> dependedBySet = dependedByMap.get(compToRem);
                if(dependedBySet != null){
                    for(String dependency: dependedBySet){
                        if(installedComponents.contains(dependency) && ! toBeRemoved.contains(dependency)){
                            System.out.println(compToRem + " is still needed.");
                            return;
                        }
                    }
                }
            }
            Set<String> dependsOnSet = dependsOnMap.get(compToRem);

            if(dependsOnSet != null){
                for(String dependency: dependsOnSet){
                    removeComponent(dependency,toBeRemoved);
                }
            }

            installedComponents.remove(compToRem);
            System.out.println("Removed " + compToRem);
        } else {
            System.out.println(compToRem + " is not installed.");
        }
    }

    /**
     * Method to process LIST command.
     * This lists down all of the installed components.
     */
    private static void processListCmd() {
        for (String component : installedComponents) {
            System.out.println(component);
        }
    }

    /**
     * Method to process END command. The program exits on END command.
     */
    private static void processEndCmd() {
        System.out.println("Encountered END command. Exiting program.");
        System.exit(0);
    }

    /**
     * Method to process UNKNOWN command. The program exits on END command.
     */
    private static void processUnknownCmd(String cmd) {
        System.out.println("Unable to process unknown command " + cmd);
    }

    /**
     * Method to print the error output and exit the program.
     *
     * @param output Output to be printed.
     */
    private static void exitWithError(String output) {
        System.out.println(output);
        System.exit(1);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        int _input_size = 0;
        _input_size = Integer.parseInt(in.nextLine().trim());
        String[] _input = new String[_input_size];
        String _input_item;
        for (int _input_i = 0; _input_i < _input_size; _input_i++) {
            try {
                _input_item = in.nextLine();
            } catch (Exception e) {
                _input_item = null;
            }
            _input[_input_i] = _input_item;
        }

        doIt(_input);

    }
}

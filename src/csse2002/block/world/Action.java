package csse2002.block.world;

import csse2002.block.world.Builder;
import csse2002.block.world.InvalidBlockException;
import csse2002.block.world.NoExitException;
import csse2002.block.world.Tile;
import csse2002.block.world.TooHighException;
import csse2002.block.world.TooLowException;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an Action which can be performed on the block world (also called world map). <br> An
 * action is something that a builder can do on a tile in the block world. The actions include,
 * moving the builder in a direction, moving a block in a direction,<br> digging on the current tile
 * the builder is standing on and dropping an item from a builder's inventory.
 */
public class Action {

    /**
     * MOVE_BUILDER action which is represented by integer 0<br>
     */
    public static final int MOVE_BUILDER = 0;
    /**
     * MOVE_BLOCK action which is represented by integer 1<br>
     */
    public static final int MOVE_BLOCK = 1;
    /**
     * DIG action which is represented by integer 2<br>
     */
    public static final int DIG = 2;
    /**
     * DROP action which is represented by integer 3<br>
     */
    public static final int DROP = 3;

    private static final String MOVE_BUILDERRxp = "^MOVE_BUILDER[\\s]{1}([^\\s]+)$";
    private static Pattern patternMOVE_BUILDER = Pattern.compile(MOVE_BUILDERRxp);

    private static final String MOVE_BLOCKRxp = "^MOVE_BLOCK[\\s]{1}([^\\s]+)$";
    private static Pattern patternMOVE_BLOCK = Pattern.compile(MOVE_BLOCKRxp);

    private static final String DIGRxp = "^DIG$";
    private static Pattern patternDIG = Pattern.compile(DIGRxp);

    private static final String DROPRxp = "^DROP[\\s]{1}([^\\s]+)$";
    private static Pattern patternDROP = Pattern.compile(DROPRxp);

    private static final String secondActionDirRxp = "^(north|south|west|east)$";
    private static Pattern pattern2ndActionDir = Pattern.compile(secondActionDirRxp);

    private static final String secondActionDigitRxp = "^(-?[0-9]+)$";
    private static Pattern pattern2ndActionDigit = Pattern.compile(secondActionDigitRxp);

    private static String INVALID_STRING = "INVALID";

    private int primaryActionSaved = 0;
    private String secondaryActionSaved = "";

    /**
     * Create an Action that represents a manipulation of the blockworld.<br>
     */
    public Action(int primaryAction,
        String secondaryAction) {
        if (secondaryAction == null) {
            return;
        }

        primaryActionSaved = primaryAction;
        secondaryActionSaved = secondaryAction;
    }

    /**
     * Get the integer representing the Action (e.g., return 0 if Action is MOVE_BUILDER)<br>
     *
     * @return the primary action
     */
    public int getPrimaryAction() {
        return primaryActionSaved;
    }

    /**
     * Gets the supplementary information associated with the Action
     *
     * @return the secondary action, or "" (empty string) if no secondary action exists
     */
    public String getSecondaryAction() {
        return secondaryActionSaved;
    }

    /**
     * Create a single Action if possible from the given reader. <br>
     *
     * Read a line from the given reader and load the Action on that line. Only load one Action
     * (<b>hint:</b> reader.readLine()) and return the created action. <br> Each line consists of a
     * primary action, and optionally a secondary action.<br>
     *
     * This function should do the following:
     * <ul>
     * <li> If any line consists of 2 or more spaces (i.e. more than 2 tokens) throws an
     * ActionFormatException. </li>
     * <li> If the primary action is not one of MOVE_BLOCK, MOVE_BUILDER, DROP
     * or DIG, throw an ActionFormatException. </li>
     * <li> If the primary action is MOVE_BLOCK, MOVE_BUILDER or DROP, and
     * the primary action is not followed by a secondary action, throws an
     * ActionFormatException.</li>
     * <li> If the primary action is DIG, and DIG is not on a line by itself, with
     * no trailing whitespace, throws an ActionFormatException. </li>
     * <li> If the primary action is MOVE_BLOCK, MOVE_BUILDER or DROP, then
     * creates and return a new Action with the primary action constant with the same name, and the
     * secondary action. This method does not check the secondary action. </li>
     * <li> If the primary action is DIG, returns a new Action with the primary
     * action constant DIG, and an empty string ("") for the secondary action. </li>
     * <li> If reader is at the end of the file, returns null. </li>
     * <li> If an IOException is thrown by the reader, then throw an
     * ActionFormatException. </li>
     * </ul>
     *
     * For details of the action format see Action.loadActions().
     *
     * @param reader the reader to read the action contents form
     * @return the created action, or null if the reader is at the end of the file.
     * @throws ActionFormatException if the line has invalid contents and the action cannot be
     * created
     */
    public static Action loadAction(BufferedReader reader)
        throws ActionFormatException {

        if (reader == null) {
            return null;
        }

        String line;
        try {
            line = reader.readLine();
            if (line == null) {
                return null;
            }

            if (line.length() == 0) {
                return null;
            }
        } catch (IOException ioe) {
            throw new ActionFormatException();
        }

        Matcher matcherMOVE_BUILDER = patternMOVE_BUILDER.matcher(line);
        if (matcherMOVE_BUILDER.find()) {
            return new Action(MOVE_BUILDER, matcherMOVE_BUILDER.group(1));
        }

        Matcher matcherMOVE_BLOCK = patternMOVE_BLOCK.matcher(line);
        if (matcherMOVE_BLOCK.find()) {
            return new Action(MOVE_BLOCK, matcherMOVE_BLOCK.group(1));
        }

        Matcher matcherDIG = patternDIG.matcher(line);
        if (matcherDIG.find()) {
            return new Action(DIG, "");
        }

        Matcher matcherDROP = patternDROP.matcher(line);
        if (matcherDROP.find()) {
            return new Action(DROP, matcherDROP.group(1));
        }
        throw new ActionFormatException();
    }

    /**
     * Perform the given action on a WorldMap, and print output to System.out. After this method
     * finishes, map should be updated. (e.g., If the action is DIG, the Tile on which the builder
     * is currently on should be updated to contain 1 less block (Builder.digOnCurrentTile()). The
     * builder to use for actions is that given by map.getBuilder().
     *
     * Do the following for these actions:
     * <ul>
     * <li> For DIG action: call Builder.digOnCurrentTile(), then print to console "Top block on
     * current tile removed".</li>
     *
     * <li> For DROP action: call Builder.dropFromInventory(), then print to console "Dropped a
     * block from inventory". The dropped item is given by action.getSecondaryAction(), that is
     * first converted to an int. If the action.getSecondaryAction() cannot be converted to an int,
     * print "Error: Invalid action" to the console. Valid integers (including negative integers and
     * large positive integers) should be passed to Builder.dropFromInventory(). </li>
     *
     * <li> For the MOVE_BLOCK action: call Tile.moveBlock() on the builder's current tile
     * (Builder.getCurrentTile()), then print to console "Moved block {direction}". The direction is
     * given by action.getSecondaryAction()</li>
     *
     * <li> For MOVE_BUILDER action: call Builder.moveTo(), then print to console "Moved
     * builder {direction}". The direction is given by action.getSecondaryAction()</li>
     *
     * <li> If action.getPrimaryAction() &lt; 0 or action.getPrimaryAction()
     * &gt; 3, or action.getSecondary() is not a direction (for MOVE_BLOCK or MOVE_BUILDER), or a
     * valid integer (for DROP) then print to console "Error: Invalid action" </li>
     * </ul>
     * "{direction}" is one of "north", "east", "south" or "west". <br>
     *
     * For handling exceptions do the following:
     * <ul>
     * <li> If a NoExitException is thrown, print to the console "No exit this way" </li>
     * <li> If a TooHighException is thrown, print to the console "Too high" </li>
     * <li> If a TooLowException is thrown, print to the console "Too low" </li>
     * <li> If an InvalidBlockException is thrown, print to the console "Cannot
     * use that block" </li>
     * </ul>
     *
     * Each line printed to the console should have a trailing newline (i.e., use
     * System.out.println()).
     *
     * @param action the action to be done on the map
     * @param map the map to perform the action ons
     */
    public static void processAction(Action action,
        WorldMap map) {
        if (action == null) {
            return;
        }

        if (map == null) {
            return;
        }

        Builder builder = map.getBuilder();
        Tile currentTile = builder.getCurrentTile();
        String secondAction = action.getSecondaryAction();
        Matcher matcher2ndActionDir = null;

        switch (action.getPrimaryAction()) {
            case MOVE_BUILDER:
                matcher2ndActionDir = pattern2ndActionDir.matcher(secondAction);
                if (!matcher2ndActionDir.find()) {
                    System.out.println("Error: Invalid action");
                    break;
                }

                if (currentTile.getExits().containsKey(secondAction)) {
                    Tile targetTile = currentTile.getExits().get(secondAction);
                    try {
                        builder.moveTo(targetTile);
                        System.out.println("Moved builder " + secondAction);
                    } catch (NoExitException e) {
                        System.out.println("No exit this way");
                    }
                } else {
                    System.out.println("No exit this way");
                }
                break;

            case MOVE_BLOCK:
                matcher2ndActionDir = pattern2ndActionDir.matcher(secondAction);
                if (!matcher2ndActionDir.find()) {
                    System.out.println("Error: Invalid action");
                    break;
                }

                try {
                    currentTile.moveBlock(secondAction);
                    System.out.println("Moved block " + secondAction);
                } catch (TooHighException e) {
                    System.out.println("Too high");
                } catch (InvalidBlockException e) {
                    System.out.println("Cannot use that block");
                } catch (NoExitException e) {
                    System.out.println("No exit this way");
                }
                break;

            case DIG:
                try {
                    builder.digOnCurrentTile();
                    System.out.println("Top block on current tile removed");
                } catch (TooLowException e) {
                    System.out.println("Too low");
                } catch (InvalidBlockException e) {
                    System.out.println("Cannot use that block");
                }
                break;

            case DROP:
                Matcher matcher2ndActionDigit = pattern2ndActionDigit.matcher(secondAction);
                int inventoryIndex = -1;
                if (!matcher2ndActionDigit.find()) {
                    System.out.println("Error: Invalid action");
                    break;
                }

                inventoryIndex = Integer.parseInt(matcher2ndActionDigit.group(1));
                try {
                    builder.dropFromInventory(inventoryIndex);
                    System.out.println("Dropped a block from inventory");
                } catch (TooHighException e) {
                    System.out.println("Too high");
                } catch (InvalidBlockException e) {
                    System.out.println("Cannot use that block");
                }
                break;

            default:
                System.out.println("Error: Invalid action");
                break;
        }
    }

    /**
     * Read all the actions from the given reader and perform them on the given block world. <br>
     *
     * All actions that can be performed should print an appropriate message (as outlined in
     * processAction()), any invalid actions that cannot be created or performed on the world map,
     * should also print an error message (also described in processAction()). <br>
     *
     * Each message should be printed on a new line (Use System.out.println()). <br>
     *
     * Each action is listed on a single line, and one file can contain multiple actions. <br>
     *
     * Each action must be processed after it is read (i.e. do not read the whole file first, read
     * and process each action one at a time).
     *
     * The file format is as follows:
     * <br>
     *
     * <pre> primaryAction1 secondaryAction1
     *  primaryAction2 secondaryAction2
     *  ...
     *  primaryActionN secondaryActionN
     *  </pre>
     *
     * There is a single space " " between each primaryAction and secondaryAction. <br> The
     * primaryAction should be one of the following values:
     * <ul>
     * <li> MOVE_BUILDER </li>
     * <li> MOVE_BLOCK </li>
     * <li> DIG </li>
     * <li> DROP </li>
     * </ul>
     *
     *
     * If the secondaryAction is present, it should be one of the following values:
     * <ul>
     * <li> north </li>
     * <li> east </li>
     * <li> south </li>
     * <li> west </li>
     * <li> (a number) for DROP action </li>
     * </ul>
     *
     * An example file may look like this:
     * <pre> MOVE_BUILDER north
     *  MOVE_BUILDER south
     *  MOVE_BUILDER west
     *  DROP 1
     *  DROP 3
     *  DROP text
     *  DIG
     *  MOVE_BUILDER south
     *  MOVE_BLOCK north
     *  RANDOM_ACTION
     *  </pre>
     *
     * If all actions can be performed on the map, the output from the above file is:
     * <pre> Moved builder north
     *  Moved builder south
     *  Moved builder west
     *  Dropped a block from inventory
     *  Dropped a block from inventory
     *  Error: Invalid action
     *  Top block on current tile removed
     *  Moved builder south
     *  Moved block north
     *
     *  (The line "RANDOM_ACTION" should then cause an ActionFormatException to be thrown)
     *  </pre>
     *
     *
     * Hint: Repeatedly call Action.loadAction() to get the next Action, and then
     * Action.processAction() to process the action.
     *
     * @param reader the reader to read actions from
     * @param startingMap the starting map that actions will be applied tos
     * @throws ActionFormatException if loadAction throws an ActionFormatException
     */
    public static void processActions(java.io.BufferedReader reader,
        WorldMap startingMap)
        throws ActionFormatException {
        Action action = null;

        do {
            try {
                action = Action.loadAction(reader);
                Action.processAction(action, startingMap);
            } catch (ActionFormatException e) {
                throw e;
            }
        } while (action != null);
    }
}

package csse2002.block.world;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.jetbrains.annotations.NotNull;

/**
 * Handles top-level interaction with performing actions on a WorldMap
 */
public class Main {

    private static final int MAIN_ARG_LENGTH_INCORRECT = 1;
    private static final int MAIN_INPUT_MAP_FILE_ERROR = 2;
    private static final int MAIN_CREATE_BUFFERED_READER_ERROR = 3;
    private static final int MAIN_PROCESS_ACTIONS_ERROR = 4;
    private static final int MAIN_WORLD_MAP_SAVE_ERROR = 5;
    private static final String INPUT_STREAM_READER_NAME = "System.in";

    /**
     * The entry point of the application.<br>
     *
     * Takes 3 parameters an input map file (args[0]), actions (args[1]), and an output map file
     * (args[2]). <br>
     *
     * The actions parameter can be either a filename, or the string "System.in". <br>
     *
     * This function does the following:
     * <ol>
     * <li> If there are not 3 parameters, (i.e. args.length != 3),
     * print "Usage: program inputMap actions outputMap" using System.err.println() and then exit
     * with status 1 (Hint: use System.exit()) </li>
     * <li> Create a new WorldMap using the input map file. If an
     * exception is thrown, print the exception to the console using System.err.println(), and then
     * exit with status 2. </li>
     * <li> Create a BufferedReader to read actions. If parameter 2 is
     * a filename, the BufferedReader should be initialised using a new FileReader. If parameter 2
     * is the string "System.in", the buffered reader should be initialised using System.in and a
     * new InputStreamReader. If an exception is thrown, print the exception to the console using
     * System.err.println, and then exit with status 3. </li>
     * <li> Call Action.processActions() using the created BufferedReader
     * and WorldMap. If an exception is thrown, print the exception to the console using
     * System.err.println, and then exit with status 4. </li>
     * <li> Call WorldMap.saveMap() using the 3rd parameter to save the map
     * to an output file. If an exception is thrown, print the exception to the console using
     * System.err.println() and then exit with status 5.
     * </li>
     * </ol>
     *
     * To print an exception to System.err, use System.err.println(e), where e is the caught
     * exception.
     *
     * @param args the input arguments to the program
     */
    public static void main(@NotNull java.lang.String[] args) {
        BufferedReader br = null;
        WorldMap instanceWorldMap = null;
        if (args.length != 3) {
            System.err.println("Usage: program inputMap actions outputMap");
            System.exit(MAIN_ARG_LENGTH_INCORRECT);
        }

        try {
            instanceWorldMap = new WorldMap(args[0]);
        } catch (Exception e) {
            System.err.println(e);
            System.exit(MAIN_INPUT_MAP_FILE_ERROR);
        }

        String bufferReaderSourceName = args[1];

        if (bufferReaderSourceName.equals(INPUT_STREAM_READER_NAME)) {
            try {
                br = new BufferedReader(new InputStreamReader(System.in));
            } catch (Exception e) {
                System.err.println(e);
                System.exit(MAIN_CREATE_BUFFERED_READER_ERROR);
            }
        } else {
            try {
                br = new BufferedReader(new FileReader(bufferReaderSourceName));
            } catch (Exception e) {
                System.err.println(e);
                System.exit(MAIN_CREATE_BUFFERED_READER_ERROR);
            }
        }

        try {
            Action.processActions(br, instanceWorldMap);
        } catch (ActionFormatException e) {
            System.err.println(e);
            System.exit(MAIN_PROCESS_ACTIONS_ERROR);
        }

        try {
            instanceWorldMap.saveMap(args[2]);
        } catch (IOException e) {
            System.err.println(e);
            System.exit(MAIN_WORLD_MAP_SAVE_ERROR);
        }
    }
}

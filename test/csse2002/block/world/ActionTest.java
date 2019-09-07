package csse2002.block.world;

import static org.junit.Assert.*;

import csse2002.block.world.Action;
import csse2002.block.world.ActionFormatException;
import csse2002.block.world.Block;
import csse2002.block.world.Builder;
import csse2002.block.world.GrassBlock;
import csse2002.block.world.InvalidBlockException;
import csse2002.block.world.NoExitException;
import csse2002.block.world.Position;
import csse2002.block.world.SoilBlock;
import csse2002.block.world.StoneBlock;
import csse2002.block.world.Tile;
import csse2002.block.world.TooHighException;
import csse2002.block.world.WoodBlock;
import csse2002.block.world.WorldMap;
import csse2002.block.world.WorldMapInconsistentException;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class ActionTest {
    private ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private final static int ACTION_DIG = 2;
    private final static int ACTION_DROP = 3;
    private final static int ACTION_MOVE_BLOCK = 1;
    private final static int ACTION_MOVE_BUILDER = 0;

    private final static String INVALID_ACTION_RESULT = "Error: Invalid action";
    private final static String INVALID_BLOCK_EXCEPTION_ACTION_RESULT = "Cannot use that block";
    private final static String TOO_HIGH_EXCEPTION_ACTION_RESULT = "Too high";
    private final static String TOO_LOW_EXCEPTION_ACTION_RESULT = "Too low";
    private final static String NO_EXIT_EXCEPTION_ACTION_RESULT = "No exit this way";

    private final static String SUCCESS_DIG_ACTION_RESULT = "Top block on current tile removed";
    private final static String SUCCESS_DROP_ACTION_RESULT = "Dropped a block from inventory";
    private final static String SUCCESS_MOVE_BLOCK_ACTION_RESULT_PREFIX = "Moved block ";
    private final static String SUCCESS_MOVE_BUILDER_ACTION_RESULT_PREFIX = "Moved builder ";

    // Test case 1
    // Two more spaces case
    private final String[] invalidStringTwoOrMoreSpaces = {"MOVE_BUILDER  north","MOVE_BLOCK  south","DIG  ","DROP  0"};

    // Test case 2
    // not specified action
    private final String[] invalidStringNotSpecifiedActions = {"REMOVE  north","ADD  south","DIG ","RUN  0"};

    // Test case 3
    // not followed by secondary action
    private final String[] invalidStringNotFollowedBySecondaryAction = {"MOVE_BUILDER ","MOVE_BLOCK ","DROP "};

    // Test case 4
    // DIG action with followed secondary action
    private final String invalidStringDigFollowedBySecondaryAction = "DIG south";

    // Test case 5
    // Followed by invalid secondary but throw exception , and check whether primary action is correct
    private final String[] invalidStringNotCheckInvalidSecondaryAction = {"MOVE_BUILDER center","MOVE_BLOCK back","DROP -1"};
    private final int [] invalidStringNotCheckInvalidSecondaryActionExpectedPrimaryAction = {ACTION_MOVE_BUILDER,ACTION_MOVE_BLOCK,ACTION_DROP};

    // Test case 6
    // DIG action with followed secondary action
    private final String validStringDigWithZeroeLengthSecondaryAction = "DIG";

    // Test case 7
    private final String invalidFileName = "testfile.txt";

    private BufferedReader simulateFileInput = null;
    private Builder sampleBuilder = null;
    private Tile startingTile = null;
    private List<Block> builderBlockList = null;
    private List<Block> testBlockList = null;
    private WorldMap sampleWorldMap = null;

    @Before
    public void setup(){
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void reset(){
        simulateFileInput = null;
        sampleBuilder = null;
        startingTile = null;
        builderBlockList = null;
        sampleWorldMap = null;
        testBlockList = null;

        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private void resetSimulateStream(){
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    private List<Block> getBlockList(List<String> inputBlockTypeStringList){
        List<Block> testBlockList = new ArrayList<Block>();
        if( inputBlockTypeStringList == null )
            return testBlockList;
        for (String blockType: inputBlockTypeStringList
        ) {
            if ( blockType.equals("soil")) {
                testBlockList.add(new SoilBlock());
            } else if (blockType.equals("grass")){
                testBlockList.add(new GrassBlock());
            } else if (blockType.equals("stone")){
                testBlockList.add(new StoneBlock());
            } else if (blockType.equals("wood")){
                testBlockList.add(new WoodBlock());
            }
        }
        return testBlockList;
    }

    @Test
    public void loadActionForTest(){

        // Test case 1
        for(String testString :invalidStringTwoOrMoreSpaces){
            simulateFileInput = new BufferedReader(new StringReader(testString));
            try{
                Action.loadAction(simulateFileInput);
                fail();
            }catch (ActionFormatException e){

            }
        }

        // Test case 2
        for(String testString :invalidStringNotSpecifiedActions){
            simulateFileInput = new BufferedReader(new StringReader(testString));
            try{
                Action.loadAction(simulateFileInput);
                fail();
            }catch (ActionFormatException e){

            }
        }

        // Test case 3
        for(String testString :invalidStringNotFollowedBySecondaryAction){
            simulateFileInput = new BufferedReader(new StringReader(testString));
            try{
                Action.loadAction(simulateFileInput);
                fail();
            }catch (ActionFormatException e){

            }
        }

        // Test case 4
            simulateFileInput = new BufferedReader(new StringReader(invalidStringDigFollowedBySecondaryAction));
            try{
                Action.loadAction(simulateFileInput);
                fail();
            }catch (ActionFormatException e){
            }

        // Test case 5
        for(int i = 0;i< invalidStringNotCheckInvalidSecondaryAction.length;i++){
            String testString  = invalidStringNotCheckInvalidSecondaryAction[i];
            simulateFileInput = new BufferedReader(new StringReader(testString));
            Action targetAction = null;
            try{
                targetAction = Action.loadAction(simulateFileInput);
            }catch (ActionFormatException e){
                fail("Not expected exception by string :" + testString);
            }

            Assert.assertNotNull(targetAction);
            Assert.assertEquals(targetAction.getPrimaryAction(),invalidStringNotCheckInvalidSecondaryActionExpectedPrimaryAction[i]);
        }

        // Test case 6
        simulateFileInput = new BufferedReader(new StringReader(validStringDigWithZeroeLengthSecondaryAction));
        try{
            Action targetAction = Action.loadAction(simulateFileInput);
            Assert.assertNotNull(targetAction);
            Assert.assertEquals(targetAction.getPrimaryAction(),ACTION_DIG);
            Assert.assertEquals(targetAction.getSecondaryAction(),"");
        }catch (ActionFormatException e){
            fail();
        }

        // Test case 7 : no line in a target file
        try {
            File tmpFile = new File(invalidFileName);
            tmpFile.deleteOnExit();
            tmpFile.createNewFile();

            BufferedReader br = new BufferedReader(new FileReader(invalidFileName));
            Action targetAction = Action.loadAction(br);

            Assert.assertNull(targetAction);
        }catch (ActionFormatException e){
            fail();
        }catch (IOException e){
            fail();
        }
    }

    @Test
    public void processActionDROPFailByInvalidBlockException_one(){
        // Construct Tile map
        startingTile = new Tile();

        // Construct Builder
        sampleBuilder = new Builder("Bob",startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        /*
                Test DROP fail case
          */
        simulateFileInput = new BufferedReader(new StringReader("DROP 0"));
        try{
            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);
            Assert.assertEquals(INVALID_BLOCK_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
    }

    @Test
    public void processActionDROPFailByInvalidBlockException_two(){
        // Construct Tile map
        startingTile = new Tile();

        // Construct Builder
        testBlockList = getBlockList(new ArrayList<>(Arrays.asList("wood")) );
        try {
            sampleBuilder = new Builder("Bob", startingTile, testBlockList);
        }catch (InvalidBlockException e){

        }

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        /*
                Test DROP fail case
          */
        simulateFileInput = new BufferedReader(new StringReader("DROP -1"));
        try{
            resetSimulateStream();
            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);
            Assert.assertEquals(INVALID_BLOCK_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
    }

    @Test
    public void processActionDROPFailByInvalidAction(){
        // Construct Tile map
        startingTile = new Tile();

        // Construct Builder
        sampleBuilder = new Builder("Bob",startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        /*
                Test DROP fail case
          */
        simulateFileInput = new BufferedReader(new StringReader("DROP test"));
        try{
            resetSimulateStream();
            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);
            Assert.assertEquals(INVALID_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
    }

    @Test
    public void processActionDROPFailByTooHighException(){
        //<test TooHighException output>
        // Construct Builder
        testBlockList = getBlockList(new ArrayList<>(Arrays.asList("stone","stone","stone","stone","stone","stone","stone","stone")) );
        try {
            startingTile = new Tile(testBlockList);
        } catch(TooHighException e) {

        }

        try {
            testBlockList = getBlockList(new ArrayList<>(Arrays.asList("wood")) );
            sampleBuilder = new Builder("Bob", startingTile, testBlockList);
        }catch (InvalidBlockException e){

        }

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        simulateFileInput = new BufferedReader(new StringReader("DROP 0"));
        try{
            resetSimulateStream();
            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);
            Assert.assertEquals(TOO_HIGH_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
        //End : <test TooHighException output>
    }

    /*
                Test DIG fail case
          */
    @Test
    public void processActionDIGFailByTooLowException(){
        // Construct Tile map
        /*
             north_tile
                  |
            starting_tile -- east_tile
         */
        startingTile = new Tile();
        Tile northTile = new Tile();
        Tile eastTile = new Tile();

        WorldMap sampleWorldMap = null;

        try {
            startingTile.addExit("north", northTile);
            startingTile.addExit("east", eastTile);
        }catch(NoExitException e){

        }
        // Construct Builder
        Builder sampleBuilder = new Builder("Bob",startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        simulateFileInput = new BufferedReader(new StringReader("DIG\nDIG\nDIG\nDIG\n"));
        try{
            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);

            targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);

            targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);

            resetSimulateStream();

            targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);

            Assert.assertEquals(TOO_LOW_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
    }

    @Test
    public void processActionDIGFailByInvalidBlockException(){
        //<test TooHighException output>
        // Construct Builder
        testBlockList = getBlockList(new ArrayList<>(Arrays.asList("stone","stone","stone","stone","stone","stone","stone","stone")) );
        try {
            startingTile = new Tile(testBlockList);
        } catch(TooHighException e) {

        }
        sampleBuilder = new Builder("Bob", startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        simulateFileInput = new BufferedReader(new StringReader("DIG"));
        try{
            resetSimulateStream();
            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);
            Assert.assertEquals(INVALID_BLOCK_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
        //End : <test TooHighException output>
    }

    /*
                Test MOVE_BLOCK fail case
          */
    @Test
    public void processActionMOVE_BLOCKFailByNoExitException(){
        // Construct Tile map
        /*
             north_tile
                  |
            starting_tile -- east_tile
         */
        startingTile = new Tile();
        Tile northTile = new Tile();
        Tile eastTile = new Tile();

        WorldMap sampleWorldMap = null;

        try {
            startingTile.addExit("north", northTile);
            startingTile.addExit("east", eastTile);
        }catch(NoExitException e){

        }
        // Construct Builder
        Builder sampleBuilder = new Builder("Bob",startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        simulateFileInput = new BufferedReader(new StringReader("MOVE_BLOCK south"));
        try{
            resetSimulateStream();

            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);

            Assert.assertEquals(NO_EXIT_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
    }

    @Test
    public void processActionMOVE_BLOCKFailByTooHighException(){
        // Construct Tile map
        /*
             north_tile
                  |
            starting_tile -- east_tile
         */
        startingTile = new Tile();
        Tile northTile = new Tile();
        Tile eastTile = new Tile();

        WorldMap sampleWorldMap = null;

        try {
            startingTile.addExit("north", northTile);
            startingTile.addExit("east", eastTile);
        }catch(NoExitException e){

        }
        // Construct Builder
        Builder sampleBuilder = new Builder("Bob",startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        simulateFileInput = new BufferedReader(new StringReader("MOVE_BLOCK north"));
        try{
            resetSimulateStream();

            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);

            Assert.assertEquals(TOO_HIGH_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
    }

    @Test
    public void processActionMOVE_BLOCKFailByInvalidBlockException(){
        testBlockList = getBlockList(new ArrayList<>(Arrays.asList("stone","stone","stone","stone","stone","stone","stone","stone")) );
        try {
            startingTile = new Tile(testBlockList);
        } catch(TooHighException e) {

        }
        Tile northTile = new Tile();
        Tile eastTile = new Tile();

        WorldMap sampleWorldMap = null;

        try {
            startingTile.addExit("north", northTile);
            startingTile.addExit("east", eastTile);
        }catch(NoExitException e){

        }

        sampleBuilder = new Builder("Bob", startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        simulateFileInput = new BufferedReader(new StringReader("MOVE_BLOCK north"));
        try{
            resetSimulateStream();
            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);
            Assert.assertEquals(INVALID_BLOCK_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }
        //End : <test TooHighException output>
    }


    /*
                Test MOVE_BUILDER fail case
          */
    @Test
    public void processActionMOVE_BUILDERFailByNoExitException(){
        // Construct Tile map
        /*
             north_tile
                  |
            starting_tile -- east_tile
         */
        startingTile = new Tile();
        Tile northTile = new Tile();
        Tile eastTile = new Tile();

        WorldMap sampleWorldMap = null;

        try {
            startingTile.addExit("north", northTile);
            startingTile.addExit("east", eastTile);
        }catch(NoExitException e){

        }
        // Construct Builder
        Builder sampleBuilder = new Builder("Bob",startingTile);

        //Construct World Map
        try {
            sampleWorldMap = new WorldMap(startingTile, new Position(0, 0), sampleBuilder);
        }catch (WorldMapInconsistentException e){

        }

        simulateFileInput = new BufferedReader(new StringReader("MOVE_BUILDER south"));
        try{
            resetSimulateStream();

            Action targetAction = Action.loadAction(simulateFileInput);
            Action.processAction(targetAction,sampleWorldMap);

            Assert.assertEquals(NO_EXIT_EXCEPTION_ACTION_RESULT,outContent.toString().trim());
        }catch (ActionFormatException e){
        }

    }

    //Success DIG => DROP => MOVE_BLOCK * 3 => MOVE_BUILDER => DIG => DROP

}

package csse2002.block.world;

import csse2002.block.world.Block;
import csse2002.block.world.Builder;
import csse2002.block.world.GrassBlock;
import csse2002.block.world.InvalidBlockException;
import csse2002.block.world.NoExitException;
import csse2002.block.world.SoilBlock;
import csse2002.block.world.StoneBlock;
import csse2002.block.world.Tile;
import csse2002.block.world.TooHighException;
import csse2002.block.world.WoodBlock;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *A class to store a world maps
 */
public class WorldMap {
    private static String WEST_EXIT_NAME	=	"west";
    private static String SOUTH_EXIT_NAME	=	"south";
    private static String EAST_EXIT_NAME	=	"east";
    private static String NORTH_EXIT_NAME	=	"north";

    private Tile startingTileSaved =	null;
    private	Position startPositionSaved			=	null;
    private Builder builderSaved				=	null;
    private Map<Integer,TileParserResult> tileParserResultHashMap	=	null;
    private SparseTileArray sparseTileArray = null;
    private BuilderParserResult builderParserResult	=	null;

    /**
     *sConstructs a new block world map from a startingTile, position and
     *  builder, such that getBuilder() == builder,
     *  getStartPosition() == startPosition, and getTiles() returns a list
     *  of tiles that are linked to startingTile. <br>
     *
     *  Hint: create a SparseTileArray as a member, and use the
     *  addLinkedTiles to populate it.
     * @param startingTile the tile which the builder starts on
     * @param startPosition the position of the starting tile
     * @param builder the builder who will traverse the block world
     * @throws WorldMapInconsistentException if there are inconsistencies in the positions of tiles (such as two tiles at a single position)
     */
    public WorldMap(Tile startingTile,
        Position startPosition,
        csse2002.block.world.Builder builder)
        throws WorldMapInconsistentException{
        sparseTileArray = new SparseTileArray();
        startingTileSaved = startingTile;
        startPositionSaved = startPosition;
        builderSaved = builder;
        sparseTileArray.addLinkedTiles(startingTileSaved,startPositionSaved.getX(),startPositionSaved.getY());
    }

    /**
     *Construct a block world map from the given filename.
     *
     *  The block world map format is as follows:
     *  <pre>&lt;startingX&gt;
     * &lt;startingY&gt;
     * &lt;builder's name&gt;
     * &lt;inventory1&gt;,&lt;inventory2&gt;, ... ,&lt;inventoryN&gt;
     *
     * total:&lt;number of tiles&gt;
     * &lt;tile0 id&gt; &lt;block1&gt;,&lt;block2&gt;, ... ,&lt;blockN&gt;
     * &lt;tile1 id&gt; &lt;block1&gt;,&lt;block2&gt;, ... ,&lt;blockN&gt;
     *     ...
     * &lt;tileN-1 id&gt; &lt;block1&gt;,&lt;block2&gt;, ... ,&lt;blockN&gt;
     *
     * exits
     * &lt;tile0 id&gt; &lt;name1&gt;:&lt;id1&gt;,&lt;name2&gt;:&lt;id2&gt;, ... ,&lt;nameN&gt;:&lt;idN&gt;
     * &lt;tile1 id&gt; &lt;name1&gt;:&lt;id1&gt;,&lt;name2&gt;:&lt;id2&gt;, ... ,&lt;nameN&gt;:&lt;idN&gt;
     *     ...
     * &lt;tileN-1 id&gt; &lt;name1&gt;:&lt;id1&gt;,&lt;name2&gt;:&lt;id2&gt;, ... ,&lt;nameN&gt;:&lt;idN&gt;
     * </pre>
     *
     *
     *  For example: <br>
     *  <pre>1
     * 2
     * Bob
     * wood,wood,wood,soil
     *
     * total:4
     * 0 soil,soil,grass,wood
     * 1 grass,grass,soil
     * 2 soil,soil,soil,wood
     * 3 grass,grass,grass,stone
     *
     * exits
     * 0 east:2,north:1,west:3
     * 1 south:0
     * 2 west:0
     * 3 east:0
     * </pre>
     *
     *  Note: Files may end with or without a single newline character, but
     *  there should not be any blank lines at the end of the file. <br>
     *
     *  Tile IDs are the ordering of tiles returned by getTiles()
     *  i.e. tile 0 is getTiles().get(0). <br>
     *
     *  Tiles must have IDs bewteen 0 and N-1, where N is the number of tiles. <br>
     *
     *  The ordering does not need to be checked when loading a map (but
     *  the saveMap function below does when saving). <br>
     *  Note: A blank line is required for an empty inventory, and lines with
     *  just an ID followed by a space are required for:
     *  <ul>
     *      <li> A tile entry below "total:N", if the tile has no blocks </li>
     *      <li> A tile entry below "exits", if the tile has no exits </li>
     *  </ul>
     *
     *  The function should do the following:
     *  <ol>
     *      <li> Open the filename and read a map in the format
     *           given above. </li>
     *      <li> Construct a new Builder with the name and inventory from the
     *           file (to be returned by getBuilder()), and a starting tile set
     *           to the tile with ID 0 </li>
     *      <li> Construct a new Position for the starting position from the
     *           file to be returned as getStartPosition() </li>
     *      <li> Construct a Tile for each tile entry in the file (to be
     *           returned by getTiles() and getTile()) </li>
     *      <li> Link each tile by the exits that are given. </li>
     *      <li> Throw a WorldMapFormatException if the format of the
     *           file is incorrect. This includes:
     *           <ul>
     *                   <li> Any lines are missing, including the blank lines before "total:N", and before exits </li>
     *                   <li> startingX or startingY (lines 1 and 2) are not valid integers </li>
     *                   <li> There are not N entries under the line that says "total:N" </li>
     *                   <li> There are not N entries under the "exits" line
     *                      (there should be exactly N entries and then the file should end.) </li>
     *                   <li> N is not a valid integer, or N is negative </li>
     *                   <li> The names of blocks in inventory and on tiles are not one of "grass", "soil", "wood", "stone" </li>
     *                   <li> The names of exits in the "exits" sections are not one of "north", "east", "south", "west" </li>
     *                   <li> The ids of tiles are not valid integers, are less than 0 or greater than N - 1 </li>
     *                   <li> The ids that the exits refer to do not exist in the list of tiles </li>
     *                   <li> loaded tiles contain too many blocks, or GroundBlocks that have an index that is too
     *                        high (i.e., if the Tile or constructors would throw exceptions). </li>
     *                   <li> A file operation throws an IOException that is not a FileNotFoundException </li>
     *           </ul></li>
     *      <li> Throw a WorldMapInconsistentException if the format is
     *           correct, but tiles would end up in geometrically impossible
     *           locations (see SparseTileArray.addLinkedTiles()). </li>
     *      <li> Throw a FileNotFoundException if the file does not exist. </li>
     *  </ol>
     *
     *  Hint: create a SparseTileArray as a member and call
     *  SparseTileArray.addLinkedTiles() to populate it.
     * @param filename the name to load the file from
     * @throws WorldMapFormatException if the file is incorrectly formatted
     * @throws WorldMapInconsistentException if the file is correctly formatted, but has inconsistencies (such as overlapping tiles)s
     * @throws java.io.FileNotFoundException if the file does not exist
     */
    public WorldMap(String filename)
        throws WorldMapFormatException,
        WorldMapInconsistentException,
        java.io.FileNotFoundException{
        FileReader fr = null;
        BufferedReader br;
        AbstractParser parser;
        AbstractParser	schemaSectionParser = new SchemaSectionParser();
        AbstractParser	totalSectionParser = new TotalSectionParser();
        AbstractParser	exitsSectionParser = new ExitsSectionParser();

        schemaSectionParser.setNextParser(totalSectionParser);
        totalSectionParser.setNextParser(exitsSectionParser);
        builderParserResult = new BuilderParserResult();
        tileParserResultHashMap	=	new HashMap<Integer,TileParserResult>();

        String currentLine = "";

        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);

            // Starting Parser
            parser = schemaSectionParser;

            try {

                while(true) {
                    if( (currentLine = br.readLine()) == null ){
                        throw new WorldMapFormatException();
                    }
                    parser.doParsing(currentLine);
                    if(  parser.isFormatWrong() )
                        throw new WorldMapFormatException(currentLine);

                    if( parser.isEndOfParsing() ) {
                        if( (currentLine = br.readLine()) != null ) {
                            throw new WorldMapFormatException();
                        }
                        break;
                    }
                    if( parser.isEndState() ) {
                        parser = parser.getNextParser();
                    }
                }

            } catch (IOException ioe) {
                throw new WorldMapFormatException();
            }

        } catch(FileNotFoundException e) {
            throw e;
        } finally {
            if( fr != null) {
                try {
                    fr.close();
                }catch(IOException e) {

                }
            }
        }

        // Check result and create instance
        // Generate tile first
        for(TileParserResult tileResult : tileParserResultHashMap.values() ) {
            if( !tileResult.isFormatCorrect() )
                throw new WorldMapFormatException("" + tileResult.getSelfTileId());
            tileResult.generateTile();
            if( !tileResult.isFormatCorrect() )
                throw new WorldMapFormatException("" + tileResult.getSelfTileId());
        }

        // Generate tile neighbor later
        for(TileParserResult tileResult : tileParserResultHashMap.values() ) {
            if( !tileResult.isFormatCorrect() )
                throw new WorldMapFormatException("" + tileResult.getSelfTileId());
            tileResult.generateNeighbor();
            if( !tileResult.isFormatCorrect() )
                throw new WorldMapFormatException("" + tileResult.getSelfTileId());
        }

        if( !builderParserResult.isFormatCorrect() )
            throw new WorldMapFormatException(currentLine);

        startingTileSaved = tileParserResultHashMap.get(new Integer(0)).getTile();
        builderParserResult.setTile(startingTileSaved);

        builderSaved = builderParserResult.generateBuilder();
        startPositionSaved = builderParserResult.generateStartinPosition();

        sparseTileArray = new SparseTileArray();

        sparseTileArray.addLinkedTiles(startingTileSaved,startPositionSaved.getX(),startPositionSaved.getY()) ;
    }

    /**
     * Gets the builder associated with this block world.
     * @return the builder object
     */
    public Builder getBuilder(){
        return builderSaved;
    }

    /**
     * Gets the starting position.
     * @return the starting position.
     */
    public Position getStartPosition(){
        return startPositionSaved;
    }

    /**
     *Get a tile by position. <br>
     *  Hint: call SparseTileArray.getTile()
     * @param position get the Tile at this position
     * @return the tile at that positions
     */
    public Tile getTile(Position position){
        return sparseTileArray.getTile(position);
    }

    /**
     *Get a list of tiles in a breadth-first-search
     *  order (see <a href="../../../csse2002/block/world/SparseTileArray.html" title="class in csse2002.block.world"><code>SparseTileArray.getTiles()</code></a>
     *  for details). <br>
     *  Hint: call SparseTileArray.getTiles().
     * @return a list of ordered tiless
     */
    public List<Tile>	getTiles(){
        return sparseTileArray.getTiles();
    }

    /**
     *Saves the given WorldMap to a file specified by the filename. <br>
     *  See the WorldMap(filename) constructor for the format of the map. <br>
     *  The Tile IDs need to relate to the ordering of tiles returned by getTiles()
     *  i.e. tile 0 is getTiles().get(0) <br>
     *  The function should do the following:
     *  <ol>
     *      <li> Open the filename and write a map in the format
     *      given in the WorldMap constructor. </li>
     *      <li> Write the starting position (given by getStartPosition())
     *      </li>
     *      <li> Write the current builder's (given by getBuilder()) name
     *      and inventory.</li>
     *      <li> Write the number of tiles </li>
     *      <li> Write the index, and then each tile as given by
     *      getTiles() (in the same order). </li>
     *      <li> Write each tile's exits, as given by
     *      getTiles().get(id).getExits() </li>
     *      <li> Throw an IOException if the file cannot be opened for
     *      writing, or if writing fails. </li>
     *
     *  </ol>
     *
     *  Hint: call getTiles()
     * @param filename the filename to be written to
     * @throws java.io.IOException if the file cannot be opened or written to.
     */
    public void saveMap(String filename)
        throws java.io.IOException{
        FileWriter fw = null;
        BufferedWriter bw;

        try {
            fw = new FileWriter(filename);
            bw = new BufferedWriter(fw);

            try {

                // StartinxX StartingY
                bw.write(String.valueOf(startPositionSaved.getX()));
                bw.newLine();

                bw.write(String.valueOf(startPositionSaved.getY()));
                bw.newLine();

                // Builder's name
                bw.write(builderSaved.getName());
                bw.newLine();

                //inventory
                StringBuilder stBsuilder = new StringBuilder();
                boolean isFirstEnter = true;

                for(Block block : builderSaved.getInventory()) {
                    if( isFirstEnter ) {
                        isFirstEnter = false;
                    } else {
                        stBsuilder.append(',');
                    }
                    stBsuilder.append(block.getBlockType());
                }
                bw.write(stBsuilder.toString());
                bw.newLine();

                //(blank)
                bw.newLine();

                List<Tile> tileList = sparseTileArray.getTiles();

                //total:<number of tiles>
                stBsuilder = new StringBuilder();
                stBsuilder.append("total:");
                stBsuilder.append(String.valueOf(tileList.size()));
                bw.write(stBsuilder.toString());
                bw.newLine();

                //<tile0 id> <block1>,<block2>, ... ,<blockN>
                Map<Tile,Integer> tile2IntegerMap = new HashMap<>();
                stBsuilder = new StringBuilder();
                int tileId  =   0;

                for(Tile tile : tileList) {
                    isFirstEnter = true;

                    tile2IntegerMap.put(tile,tileId);

                    // Tile id
                    stBsuilder.append(String.valueOf(tileId++));

                    // Space
                    stBsuilder.append(' ');

                    // Block lists
                    for(Block block : tile.getBlocks()) {
                        if( isFirstEnter ) {
                            isFirstEnter = false;
                        } else {
                            stBsuilder.append(',');
                        }
                        stBsuilder.append(block.getBlockType());
                    }

                    // Newline
                    stBsuilder.append(System.lineSeparator());
                }

                bw.write(stBsuilder.toString());

                //(blank)
                bw.newLine();

                //exits
                bw.write("exits");
                bw.newLine();

                //<tile0 id> <name1>:<id1>,<name2>:<id2>, ... ,<nameN>:<idN>
                stBsuilder = new StringBuilder();
                tileId  =   0;

                for(Tile tile : tileList) {
                    // Tile id
                    stBsuilder.append(String.valueOf(tileId++));

                    // Space
                    stBsuilder.append(' ');

                    Tile neighborTile = null;
                    Integer neighborTileId = null;
                    int commaChecker = 0;

                    // Exits
                    //  "north"
                    neighborTile    =   tile.getExits().get(NORTH_EXIT_NAME);
                    if( (neighborTileId = tile2IntegerMap.get(neighborTile)) != null ) {
                        stBsuilder.append(NORTH_EXIT_NAME);
                        stBsuilder.append(":");
                        stBsuilder.append(String.valueOf(neighborTileId.intValue()));
                        commaChecker++;
                    }

                    //  "east"
                    neighborTile    =   tile.getExits().get(EAST_EXIT_NAME);
                    if( (neighborTileId = tile2IntegerMap.get(neighborTile)) != null ) {
                        if( commaChecker > 0 ) {
                            stBsuilder.append(",");
                            commaChecker--;
                        }
                        stBsuilder.append(EAST_EXIT_NAME);
                        stBsuilder.append(":");
                        stBsuilder.append(String.valueOf(neighborTileId.intValue()));
                        commaChecker++;
                    }

                    //  "south"
                    neighborTile    =   tile.getExits().get(SOUTH_EXIT_NAME);
                    if( (neighborTileId = tile2IntegerMap.get(neighborTile)) != null ) {
                        if( commaChecker > 0 ) {
                            stBsuilder.append(",");
                            commaChecker--;
                        }
                        stBsuilder.append(SOUTH_EXIT_NAME);
                        stBsuilder.append(":");
                        stBsuilder.append(String.valueOf(neighborTileId.intValue()));
                        commaChecker++;
                    }

                    //  "west"
                    neighborTile    =   tile.getExits().get(WEST_EXIT_NAME);
                    if( (neighborTileId = tile2IntegerMap.get(neighborTile)) != null ) {
                        if( commaChecker > 0 ) {
                            stBsuilder.append(",");
                            commaChecker--;
                        }
                        stBsuilder.append(WEST_EXIT_NAME);
                        stBsuilder.append(":");
                        stBsuilder.append(String.valueOf(neighborTileId.intValue()));
                        commaChecker++;
                    }

                    // Newline
                    stBsuilder.append(System.lineSeparator());
                }

                bw.write(stBsuilder.toString());
                bw.flush();
                bw.close();
            } catch (IOException ioe) {
                throw ioe;
            }
        } catch(FileNotFoundException e) {
            throw e;
        } finally {
            if( fw != null) {
                try {
                    fw.close();
                }catch(IOException e) {

                }
            }
        }

    }

    private class BuilderParserResult{
        private	int	startingX	=	-1;
        private	int	startingY	=	-1;
        private	String	builderName	=	null;
        private	boolean	isTotalFormatCorrect	=	true;
        private List<Block> inventoryBlock	=	null;
        private	Tile	selfTile		=	null;
        private Builder	builderInstance		=	null;

        public BuilderParserResult(){
            inventoryBlock = new ArrayList<Block>();
        }

        public boolean	isFormatCorrect(){
            return isTotalFormatCorrect;
        }

        public void setStartingX(int startingXInput){
            startingX = startingXInput;
        }

        public void setStartingY(int startingYInput){
            startingY = startingYInput;
        }

        public void setBuilderName(String builderNameInput){
            builderName = builderNameInput;
        }

        public void setTile(Tile tileInput){
            selfTile = tileInput;
        }

        public void addOneStartingBlock(String blockType){
            if ( blockType.equals("soil")) {
                inventoryBlock.add(new SoilBlock());
            } else if (blockType.equals("grass")){
                inventoryBlock.add(new GrassBlock());
            } else if (blockType.equals("stone")){
                inventoryBlock.add(new StoneBlock());
            } else if (blockType.equals("wood")){
                inventoryBlock.add(new WoodBlock());
            }
        }

        public Builder generateBuilder(){
            if( inventoryBlock.size() == 0 ) {
                builderInstance = new Builder(builderName,selfTile);
                return builderInstance;
            }
            try {
                builderInstance = new Builder(builderName,selfTile,inventoryBlock);
            } catch(InvalidBlockException e) {
                isTotalFormatCorrect = false;
            }
            return builderInstance;
        }

        public Position generateStartinPosition(){
            return new Position(startingX,startingY);
        }
    }

    private class TileParserResult{
        private	int	selfTileId	=	-1;
        private	int	northId		=	-1;
        private	int	eastId		=	-1;
        private	int	southId		=	-1;
        private	int	westId		=	-1;

        private	boolean	isNorthFormatCorrect	=	true;
        private	boolean	isEastFormatCorrect	=	true;
        private	boolean	isSouthFormatCorrect	=	true;
        private	boolean	isWestFormatCorrect	=	true;
        private	boolean	isTotalFormatCorrect	=	true;

        private	List<Block>	startingBlock	=	null;
        private	Tile	selfTile		=	null;

        public TileParserResult(int selfTileIdInput){
            startingBlock = new ArrayList<Block>();
            selfTileId = selfTileIdInput;
        }

        public boolean	isFormatCorrect(){
            return ( isNorthFormatCorrect && isEastFormatCorrect && isSouthFormatCorrect && isWestFormatCorrect && isTotalFormatCorrect);
        }

        public int getSouthId(){
            return southId;
        }

        public int getNorthId(){
            return northId;
        }

        public int getEastId(){
            return eastId;
        }

        public int getWestId(){
            return westId;
        }

        public int getSelfTileId(){
            return selfTileId;
        }

        public Tile getTile(){
            return selfTile;
        }

        public void addOneStartingBlock(String blockType){
            if ( blockType.equals("soil")) {
                startingBlock.add(new SoilBlock());
            } else if (blockType.equals("grass")){
                startingBlock.add(new GrassBlock());
            } else if (blockType.equals("stone")){
                startingBlock.add(new StoneBlock());
            } else if (blockType.equals("wood")){
                startingBlock.add(new WoodBlock());
            }
        }

        public void setChildId(String childName,int childId){
            if( childName.equals(WEST_EXIT_NAME) ) {
                if( westId != -1 )	{
                    isWestFormatCorrect = false;
                    return;
                }
                westId	=	childId;
                return;
            }

            if( childName.equals(SOUTH_EXIT_NAME) ) {
                if( southId != -1 )	{
                    isSouthFormatCorrect = false;
                    return;
                }

                southId	=	childId;
                return;
            }

            if( childName.equals(EAST_EXIT_NAME) ) {
                if( eastId != -1 )	{
                    isEastFormatCorrect = false;
                    return;
                }
                eastId	=	childId;
                return;
            }

            if( childName.equals(NORTH_EXIT_NAME) ) {
                if( northId != -1 )	{
                    isNorthFormatCorrect = false;
                    return;
                }
                northId	=	childId;
                return;
            }
        }

        public Tile generateTile(){
            try {
                selfTile = new Tile(startingBlock);
            } catch(TooHighException e) {
                isTotalFormatCorrect = false;
            }

            return selfTile;
        }

        public void generateNeighbor(){
            if( isFormatCorrect() ) {
                // west
                TileParserResult westTileParserResult	= tileParserResultHashMap.get(new Integer(westId));
                if( westTileParserResult != null && westTileParserResult.isFormatCorrect() ) {
                    try {
                        selfTile.addExit(WEST_EXIT_NAME, westTileParserResult.getTile());
                    }catch (NoExitException e) {

                    }
                } else if( westId != -1 ) {
                    isWestFormatCorrect = false;
                }

                // south
                TileParserResult southTileParserResult	= tileParserResultHashMap.get(new Integer(southId));
                if( southTileParserResult != null && southTileParserResult.isFormatCorrect() ) {
                    try {
                        selfTile.addExit(SOUTH_EXIT_NAME,southTileParserResult.getTile());
                    }catch (NoExitException e) {
                    }
                } else if( southId != -1 ) {
                    isSouthFormatCorrect = false;
                }

                // east
                TileParserResult eastTileParserResult	= tileParserResultHashMap.get(new Integer(eastId));
                if( eastTileParserResult != null && eastTileParserResult.isFormatCorrect() ) {
                    try{
                        selfTile.addExit(EAST_EXIT_NAME,eastTileParserResult.getTile());
                    }catch (NoExitException e) {

                    }
                } else if( eastId != -1 ) {
                    isEastFormatCorrect = false;
                }

                // north
                TileParserResult northTileParserResult	= tileParserResultHashMap.get(new Integer(northId));
                if( northTileParserResult != null && northTileParserResult.isFormatCorrect() ) {
                    try{
                        selfTile.addExit(NORTH_EXIT_NAME,northTileParserResult.getTile());
                    }catch (NoExitException e) {

                    }
                } else if( northId != -1 ) {
                    isNorthFormatCorrect  = false;
                }
            }
        }
    }

    private abstract class AbstractParser{
        protected boolean isFormatWrong = false;
        protected byte currentState = 0;
        protected AbstractParser nextParser = null;

        public AbstractParser(){
            currentState = getStartState();
        }

        public boolean isEndOfParsing(){
            return isEndState() && ( nextParser == null );
        }

        public void setNextParser(AbstractParser nextParserInput){
            nextParser = nextParserInput;
        }

        public AbstractParser getNextParser(){
            return nextParser;
        }

        abstract void doParsing(String line);

        public void transfer2NextState(){
            currentState = (byte)(currentState << 1);
        }

        public boolean isFormatWrong(){
            return isFormatWrong;
        }

        abstract boolean isEndState();

        abstract byte getStartState();

        void setRecordedTileEntryNum(int tileNum){
            return;
        }
    }

    private class SchemaSectionParser
        extends AbstractParser{
        private	static final String StartingXRxp	=	"^(-?[0-9]+)$";
        private Pattern patternStartingX	=	Pattern.compile(StartingXRxp);

        private	static final String StartingYRxp	=	"^(-?[0-9]+)$";
        private Pattern patternStartingY	=	Pattern.compile(StartingYRxp);

        private	static final String BuilderNameRxp	=	"^([^\\s]+)$";
        private Pattern patternBuilderName	=	Pattern.compile(BuilderNameRxp);

        private	static final String BuilderInventoryRxp	=	"([^,]+)|[,]{2,}|,$";
        private Pattern patternBuilderInventory	=	Pattern.compile(BuilderInventoryRxp);

        private static final String InventoryTokenRxp		=	"^(wood|soil|stone|grass)$";
        private Pattern patternInventoryToken	=	Pattern.compile(InventoryTokenRxp);

        private	static	final byte	STATE_STARTING_X_INFO	=	0x1<<1;
        private	static	final byte	STATE_STARTING_Y_INFO	=	0x1<<2;
        private	static	final byte	STATE_BUILDER_NAME_INFO	=	0x1<<3;
        private	static	final byte	STATE_INVENTORY_INFO	=	0x1<<4;
        private	static	final byte	STATE_END		=	0x1<<5;

        public SchemaSectionParser(){
            super();
        }

        @Override
        public void doParsing(String line){
            switch(currentState)
            {
                case STATE_STARTING_X_INFO:
                    Matcher matcherStartingX = patternStartingX.matcher(line);
                    if( matcherStartingX.find() ) {
                        builderParserResult.setStartingX(Integer.parseInt(matcherStartingX.group(1)));
                        transfer2NextState();
                    } else {
                        isFormatWrong = true;
                    }

                    break;

                case STATE_STARTING_Y_INFO:
                    Matcher matcherStartingY = patternStartingY.matcher(line);
                    if( matcherStartingY.find() ) {
                        builderParserResult.setStartingY(Integer.parseInt(matcherStartingY.group(1)));
                        transfer2NextState();
                    } else {
                        isFormatWrong = true;
                    }

                    break;

                case STATE_BUILDER_NAME_INFO:
                    // Check is it empty
                    if( line.length() == 0 ) {
                        builderParserResult.setBuilderName("");
                        transfer2NextState();
                        break;
                    }

                    Matcher matcherBuilderName = patternBuilderName.matcher(line);
                    if( matcherBuilderName.find() ) {
                        builderParserResult.setBuilderName(matcherBuilderName.group(1));
                        transfer2NextState();
                    } else {
                        isFormatWrong = true;
                    }

                    break;

                case STATE_INVENTORY_INFO:
                    // Check is it empty
                    if( line.length() == 0 ) {
                        transfer2NextState();
                        break;
                    }

                    Matcher matcherBuilderInventory = patternBuilderInventory.matcher(line);
                    int findCount = 0;

                    while( matcherBuilderInventory.find() )
                    {
                        findCount++;
                        Matcher matcherInventoryToken = patternInventoryToken.matcher(matcherBuilderInventory.group(1));
                        if( matcherInventoryToken.find() ) {
                            builderParserResult.addOneStartingBlock(matcherInventoryToken.group(1));
                        } else {
                            isFormatWrong = true;
                        }
                    }
                    if( findCount == 0 ) {
                        isFormatWrong = true;
                    }
                    transfer2NextState();
                    break;

            }
        }

        @Override
        public boolean isEndState(){
            return currentState == STATE_END;
        }

        @Override
        byte getStartState(){
            return STATE_STARTING_X_INFO;
        }
    }

    private class TotalSectionParser
        extends AbstractParser{
        private	static final String TotalTitleRxp	=	"^total:([0-9]+)$";
        private Pattern patternTotalTitle	=	Pattern.compile(TotalTitleRxp);
        private static final String TotalEntryCommaSepRxp	=	"([^,]+)|[,]{2,}|,$";
        private Pattern patternTotalEntryCommaSep	=	Pattern.compile(TotalEntryCommaSepRxp);
        private static final String TotalFirstTokenRxp	=	"^([0-9]+)[\\s]{1}(wood|soil|stone|grass|$)";
        private Pattern patternTotalFirstToken	=	Pattern.compile(TotalFirstTokenRxp);
        private static final String TotalOtherTokenRxp	=	"^(wood|soil|stone|grass)$";
        private Pattern patternTotalOtherToken	=	Pattern.compile(TotalOtherTokenRxp);

        private	static	final byte	STATE_TOTAL_SECTION_NEW_LINE		=	0x1<<1;
        private	static	final byte	STATE_TOTAL_SECTION_TITLE		=	0x1<<2;
        private	static	final byte	STATE_TOTAL_SECTION_PARSING_PER_TILE	=	0x1<<3;
        private	static	final byte	STATE_END				=	0x1<<4;

        private int recordedTileEntryCount = 0;
        private int parsedTileEntryCount = 0;

        public TotalSectionParser(){
            recordedTileEntryCount = 0;
            parsedTileEntryCount = 0;
        }

        @Override
        public void doParsing(String line){
            switch(currentState)
            {
                case STATE_TOTAL_SECTION_NEW_LINE:
                    if( line.length() ==0 ) {
                        transfer2NextState();
                    } else {
                        isFormatWrong = true;
                    }
                    break;

                case STATE_TOTAL_SECTION_TITLE:
                    Matcher matcherTotalTitle = patternTotalTitle.matcher(line);
                    if( matcherTotalTitle.find() ) {
                        recordedTileEntryCount = Integer.parseInt(matcherTotalTitle.group(1));
                        ExitsSectionParser nextParserInstance = (ExitsSectionParser)nextParser;
                        nextParserInstance.setRecordedTileEntryNum(recordedTileEntryCount);

                        transfer2NextState();
                    } else {
                        isFormatWrong = true;
                    }
                    break;

                case STATE_TOTAL_SECTION_PARSING_PER_TILE:
                    Matcher matcherTotalEntryCommaSep = patternTotalEntryCommaSep.matcher(line);
                    boolean isEmptyBlock = false;
                    int tileId = -1;
                    TileParserResult tileParserResult   =   null;

                    if( matcherTotalEntryCommaSep.find() ) {
                        Matcher matcherTotalFirstToken = patternTotalFirstToken.matcher(matcherTotalEntryCommaSep.group(1));
                        if( matcherTotalFirstToken.find() ) {
                            tileId = Integer.parseInt(matcherTotalFirstToken.group(1));

                            if( tileId < 0 || tileId >= recordedTileEntryCount ){
                                isFormatWrong = true;
                                transfer2NextState();
                                break;
                            }

                            TileParserResult tileParseResult = tileParserResultHashMap.get(
                                new Integer(tileId));
                            if( tileParseResult != null ) {
                                isFormatWrong = true;
                                transfer2NextState();
                                break;
                            }

                            tileParserResult = new TileParserResult(tileId);
                            tileParserResultHashMap.put(new Integer(tileId),tileParserResult);
                            if( matcherTotalFirstToken.group(2) == null || matcherTotalFirstToken.group(2).length() == 0 )
                            {
                                isEmptyBlock = true;
                            }else {
                                tileParserResult.addOneStartingBlock(matcherTotalFirstToken.group(2));
                            }
                        } else {
                            isFormatWrong = true;
                            transfer2NextState();
                            break;
                        }

                        while( matcherTotalEntryCommaSep.find() )
                        {
                            if( isEmptyBlock == true ) {
                                isFormatWrong = true;
                                transfer2NextState();
                                break;
                            }
                            Matcher matcherTotalOtherToken = patternTotalOtherToken.matcher(matcherTotalEntryCommaSep.group(1));
                            if( matcherTotalOtherToken.find() ) {
                                tileParserResult.addOneStartingBlock(matcherTotalOtherToken.group(1));
                            } else {
                                isFormatWrong = true;
                                transfer2NextState();
                                break;
                            }
                        }
                        parsedTileEntryCount++;
                        if( parsedTileEntryCount == recordedTileEntryCount ) {
                            transfer2NextState();
                        }
                    } else {
                        isFormatWrong = true;
                        transfer2NextState();
                    }
                    break;
            }
        }

        @Override
        public boolean isEndState(){
            return currentState == STATE_END;
        }

        @Override
        public byte getStartState(){
            return STATE_TOTAL_SECTION_NEW_LINE;
        }
    }

    private class ExitsSectionParser
        extends AbstractParser{
        private	static final String ExitsTitleRxp	=	"^exits$";
        private Pattern patternExitsTitle	=	Pattern.compile(ExitsTitleRxp);
        private	static final String ExitsEntryRxp	=	"^([0-9]+)[\\s]{1}(((east|west|south|north):([0-9]+))|$)((,(east|west|south|north):([0-9]+))|$)((,(east|west|south|north):([0-9]+))|$)((,(east|west|south|north):([0-9]+))|$)";
        private Pattern patternExitsEntry	=	Pattern.compile(ExitsEntryRxp);

        private	static	final byte	STATE_EXIT_SECTION_NEW_LINE		=	0x1<<1;
        private	static	final byte	STATE_EXIT_SECTION_TITLE		=	0x1<<2;
        private	static	final byte	STATE_EXIT_SECTION_PARSING_PER_TILE	=	0x1<<3;
        private	static	final byte	STATE_END				=	0x1<<4;

        private int parsedTileEntryCount = 0;
        private int recordedTileEntryCount = 0;
        private Set<Integer> alreadyVisited = null;

        public ExitsSectionParser(){
            parsedTileEntryCount = 0;
            alreadyVisited = new HashSet<>();
        }

        @Override
        public void doParsing(String line){
            switch(currentState)
            {
                case STATE_EXIT_SECTION_NEW_LINE:
                    if( line.length() == 0 ) {
                        transfer2NextState();
                    } else {
                        isFormatWrong = true;
                    }
                    break;

                case STATE_EXIT_SECTION_TITLE:
                    Matcher matcherExitsTitle = patternExitsTitle.matcher(line);
                    if( matcherExitsTitle.find() ) {
                        transfer2NextState();
                    } else {
                        isFormatWrong = true;
                    }
                    break;

                case STATE_EXIT_SECTION_PARSING_PER_TILE:
                    Matcher matcherExitsEntry = patternExitsEntry.matcher(line);
                    if( matcherExitsEntry.find() ) {
                        int tileId = Integer.parseInt(matcherExitsEntry.group(1));

                        if( tileId < 0 || tileId >= recordedTileEntryCount ){
                            isFormatWrong = true;
                            transfer2NextState();
                            break;
                        }

                        if( alreadyVisited.contains(new Integer(tileId)) ){
                            isFormatWrong = true;
                            transfer2NextState();
                            break;
                        } else {
                            alreadyVisited.add(new Integer(tileId));
                        }

                        TileParserResult tileParseResult = tileParserResultHashMap.get(new Integer(tileId));

                        if( tileParseResult == null ){
                            isFormatWrong = true;
                            transfer2NextState();
                            break;
                        }

                        if( matcherExitsEntry.group(4) != null ) {
                            tileParseResult.setChildId(matcherExitsEntry.group(4),Integer.parseInt(matcherExitsEntry.group(5)));
                        }

                        if( matcherExitsEntry.group(8) != null ) {
                            tileParseResult.setChildId(matcherExitsEntry.group(8),Integer.parseInt(matcherExitsEntry.group(9)));
                        }

                        if( matcherExitsEntry.group(12) != null ) {
                            tileParseResult.setChildId(matcherExitsEntry.group(12),Integer.parseInt(matcherExitsEntry.group(13)));
                        }

                        if( matcherExitsEntry.group(16) != null ) {
                            tileParseResult.setChildId(matcherExitsEntry.group(16),Integer.parseInt(matcherExitsEntry.group(17)));
                        }
                        parsedTileEntryCount++;
                        if( parsedTileEntryCount == recordedTileEntryCount ) {
                            transfer2NextState();
                        }
                    } else {
                        isFormatWrong = true;
                        transfer2NextState();
                        break;
                    }
                    break;
            }
        }

        @Override
        public boolean isEndState(){
            return currentState == STATE_END;
        }

        @Override
        public byte getStartState(){
            return STATE_EXIT_SECTION_NEW_LINE ;
        }

        public void setRecordedTileEntryNum(int tileNum){
            recordedTileEntryCount = tileNum;
        }

    }

}

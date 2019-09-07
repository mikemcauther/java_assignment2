package csse2002.block.world;

import csse2002.block.world.Tile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * A sparse representation of tiles in an Array. <br> Contains Tiless stored with an associated
 * Position (x, y) in a map. <br>
 */
public class SparseTileArray {

    private static final byte WEST_DIR = 0x1;
    private static final byte SOUTH_DIR = 0x1 << 1;
    private static final byte EAST_DIR = 0x1 << 2;
    private static final byte NORTH_DIR = 0x1 << 3;

    private static final String WEST_EXIT_NAME = "west";
    private static final String SOUTH_EXIT_NAME = "south";
    private static final String EAST_EXIT_NAME = "east";
    private static final String NORTH_EXIT_NAME = "north";

    private static int TREE_SIGNATURE_LEN = 20;

    private Map<Position, TileAdapter> position2TileAdapterMap = null;

    /**
     * Constructor for a SparseTileArray. Initializes an empty SparseTileArray, such that
     * getTile(new Position(x, y)) returns null for any x and y and getTiles() returns an empty
     * list.
     */
    public SparseTileArray() {
        position2TileAdapterMap = new HashMap<Position, TileAdapter>();
    }

    /**
     * Get the tile at position at (x, y), given by position.getX() and position.getY(). Return null
     * if there is no tile at (x, y). <br> Hint: Construct a Map&lt;Position, Tile&gt; in
     * addLinkedTiles to allow looking up tiles by position.
     *
     * @param position the tile position
     * @return the tile at (x, y) or null if no such tile exists.
     */
    public Tile getTile(Position position) {
        if (position == null) {
            return null;
        }

        if( position2TileAdapterMap.get(position) == null )
            return null;

        return position2TileAdapterMap.get(position).getTile();
    }

    /**
     * Get a set of ordered tiles from SparseTileArray in breadth-first-search order. <br> The
     * startingTile (passed to addLinkTiles) should be the first tile in the list. The following
     * tiles should be the tiles at the "north", "east", "south" and "west" exits from the starting
     * tile, if they exist. <br> Then for each of those tiles, the next tiles will be their "north",
     * "east", "south" and "west" exits, if they exist. The order should continue in the same way
     * through all the tiles that are linked to startingTile. <br> The list returned by getTiles may
     * be immutable, and if not, changing the list (i.e., adding or removing elements) should not
     * change that returned by subsequent calls to getTiles().
     *
     * @return a list of tiles in breadth-first-search order.
     */
    public List<Tile> getTiles() {
        List<TileAdapter> tileAdapterValuesList = new ArrayList<TileAdapter>(
            position2TileAdapterMap.values());
        Collections.sort(tileAdapterValuesList);
        List<Tile> targetList = new ArrayList<>();

        for (TileAdapter tileAdapter : tileAdapterValuesList) {
            targetList.add(tileAdapter.getTile());
        }

        return targetList;
    }

    /**
     * Add a set of tiles to the sparse tilemap. <br> This function does the following:
     * <ol>
     * <li> Remove any tiles that are already existing in the sparse map. </li>
     * <li> Add startingTile at position (startingX, startingY), such
     * that getTile(new Position(startingX, startingY)) == startingTile. </li>
     * <li> For each pair of linked tiles (tile1 at (x1, y1) and tile2 at (x2, y2)
     * that are accessible from startingTile (i.e. there is a path through a series of exits
     * startingTile.getExits().get("north").getExits().get("east") ... between the two tiles), tile2
     * will get a new position based on tile1's position, and tile1's exit name.
     * <ul>
     * <li> tile2 at "north"  exit should get a new position of (x1, y1 - 1),
     * i.e. getTile(new Position(x1, y1 - 1)) == tile1.getExits().get("north")</li>
     * <li> tile2 at "east" exit should get a position of (x1 + 1, y1),
     * i.e. getTile(new Position(x1 + 1, y1))  == tile1.getExits().get("east")</li>
     * <li> tile2 at "south" exit should get a position of (x1, y1 + 1),
     * i.e. getTile(new Position(x1, y1 + 1)) == tile1.getExits().get("south")</li>
     * <li> tile2 at "west" exit should get a position of (x1 - 1, y1),
     * i.e. getTile(new Position(x1 - 1, y1))  == tile1.getExits().get("west")</li>
     * </ul>
     * </li>
     * <li> If there are tiles that are not geometrically consistent, i.e. Tiles
     * that would occupy the same position or require two different coordinates for getTile() method
     * to work, throw a WorldMapInconsistentException. <br> Two examples of inconsistent tiles are:
     * <ol>
     * <li> tile1.getExits().get("north").getExits().get("south) is non null and not
     * == to tile1, throw a WorldMapInconsistentException. Note: one way exits are allowed, so
     * tile1.getExits().get("north").getExits().get("south) == null would be acceptable, but
     * tile1.getExits().get("north").getExits().get("south) == tile2 for some other non-null tile2
     * is not.</li>
     * <li> tile1.getExits().get("north").getExits().get("north") == tile1.
     * tile1 exits in two different places in this case.
     * </li>
     * </ol>
     * </li>
     * <li> getTiles() should return a list of each accessible tile in a
     * breadth-first search order (see getTiles()) </li>
     *
     * <li> If an exception is thrown, reset the state of the SparseTileArray
     * such that getTile(new Position(x, y)) returns null for any x and y. </li>
     * </ol>
     *
     * @param startingTile the starting point in adding the linked tiles. All added tiles must have
     * a path (via multiple exits) to this tile.
     * @param startingX the x coordinate of startingTile in the array
     * @param startingY the y coordinate of startingTile in the array
     * @throws WorldMapInconsistentException if the tiles in the set are not Geometrically
     * consistent
     */
    public void addLinkedTiles(csse2002.block.world.Tile startingTile,
        int startingX,
        int startingY)
        throws WorldMapInconsistentException {
        Queue<TileAdapter> nodesToVisit = new LinkedList<>();
        Set<Tile> alreadyVisited = new HashSet<>();
        position2TileAdapterMap = new HashMap<Position, TileAdapter>();

        // Used to check whether this tile has multiple Positions
        Map<Tile, Position> tile2PositionMap = new HashMap<>();

        byte[] rootByteSignature = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        TileAdapter rootTileAdapter = new TileAdapter(0, NORTH_DIR, rootByteSignature,
            new Position(startingX, startingY), startingTile);
        nodesToVisit.add(rootTileAdapter);
        tile2PositionMap.put(startingTile, rootTileAdapter.getSelfPosition());
        position2TileAdapterMap
            .put(rootTileAdapter.getSelfPosition(), rootTileAdapter);

        while (nodesToVisit.size() != 0) {
            TileAdapter targetTileAdapter = nodesToVisit.remove();

            if (!alreadyVisited.contains(targetTileAdapter.getTile())) {

                Position childPosition = null;
                Tile childTile = null;
                TileAdapter childTileAdapter = null;

                alreadyVisited.add(targetTileAdapter.getTile());

                // Check is consistent
                if (!targetTileAdapter.isConsistent()) {
                    resetStateAndThrowException();
                }

                // West
                if ((childTile = targetTileAdapter.getTile().getExits().get(WEST_EXIT_NAME))
                    != null) {
                    childTileAdapter = new TileAdapter(childTile,
                        targetTileAdapter,
                        WEST_DIR);

                    // Check is the position match
                    if ((childPosition = tile2PositionMap.get(childTile)) != null) {
                        if (!childPosition.equals(childTileAdapter.getSelfPosition())) {
                            resetStateAndThrowException();
                        }
                    }

                    // Grasp west child into queues
                    if (!position2TileAdapterMap.containsKey(childTileAdapter.getSelfPosition())) {
                        position2TileAdapterMap
                            .put(childTileAdapter.getSelfPosition(), childTileAdapter);
                        nodesToVisit.add(childTileAdapter);
                    }
                }

                // South
                if ((childTile = targetTileAdapter.getTile().getExits().get(SOUTH_EXIT_NAME))
                    != null) {
                    childTileAdapter = new TileAdapter(childTile,
                        targetTileAdapter,
                        SOUTH_DIR);

                    // Check is the position match
                    if ((childPosition = tile2PositionMap.get(childTile)) != null) {
                        if (!childPosition.equals(childTileAdapter.getSelfPosition())) {
                            resetStateAndThrowException();
                        }
                    }

                    // Grasp south child into queues
                    if (!position2TileAdapterMap.containsKey(childTileAdapter.getSelfPosition())) {
                        position2TileAdapterMap
                            .put(childTileAdapter.getSelfPosition(), childTileAdapter);
                        nodesToVisit.add(childTileAdapter);
                        tile2PositionMap.put(childTile, childTileAdapter.getSelfPosition());
                    }
                }

                // East
                if ((childTile = targetTileAdapter.getTile().getExits().get(EAST_EXIT_NAME))
                    != null) {
                    childTileAdapter = new TileAdapter(childTile,
                        targetTileAdapter,
                        EAST_DIR);

                    // Check is the position match
                    if ((childPosition = tile2PositionMap.get(childTile)) != null) {
                        if (!childPosition.equals(childTileAdapter.getSelfPosition())) {
                            resetStateAndThrowException();
                        }
                    }

                    // Grasp east child into queues
                    if (!position2TileAdapterMap.containsKey(childTileAdapter.getSelfPosition())) {
                        position2TileAdapterMap
                            .put(childTileAdapter.getSelfPosition(), childTileAdapter);
                        nodesToVisit.add(childTileAdapter);
                        tile2PositionMap.put(childTile, childTileAdapter.getSelfPosition());
                    }
                }

                // North
                if ((childTile = targetTileAdapter.getTile().getExits().get(NORTH_EXIT_NAME))
                    != null) {
                    childTileAdapter = new TileAdapter(childTile,
                        targetTileAdapter,
                        NORTH_DIR);

                    // Check is the position match
                    if ((childPosition = tile2PositionMap.get(childTile)) != null) {
                        if (!childPosition.equals(childTileAdapter.getSelfPosition())) {
                            resetStateAndThrowException();
                        }
                    }

                    // Grasp north child into queues
                    if (!position2TileAdapterMap.containsKey(childTileAdapter.getSelfPosition())) {
                        position2TileAdapterMap
                            .put(childTileAdapter.getSelfPosition(), childTileAdapter);
                        nodesToVisit.add(childTileAdapter);
                        tile2PositionMap.put(childTile, childTileAdapter.getSelfPosition());
                    }
                }
            }
        }

    }

    private void resetStateAndThrowException()
        throws WorldMapInconsistentException {
        position2TileAdapterMap = new HashMap<Position, TileAdapter>();
        throw new WorldMapInconsistentException();
    }


    private class TileAdapter implements Comparable<TileAdapter> {

        private Position selfPosition = null;
        private Tile selfTile = null;
        private byte[] treeSignatureByteArray = new byte[TREE_SIGNATURE_LEN];  //	20 bytes
        private int selfLevel = 0;

        private Position northPosition = null;
        private Position eastPosition = null;
        private Position southPosition = null;
        private Position westPosition = null;

        private boolean isNorthConsistent = true;
        private boolean isEastConsistent = true;
        private boolean isSouthConsistent = true;
        private boolean isWestConsistent = true;

        public TileAdapter(Tile selfTileInput,
            TileAdapter parentTileAdapter,
            byte selfDirInput) {
            this(parentTileAdapter.getLevel() + 1,
                selfDirInput,
                parentTileAdapter.getTreeSignature(),
                parentTileAdapter.getChildPosition(selfDirInput),
                selfTileInput);
        }

        public TileAdapter(int selfLevelInput,      // root	: 0
            byte selfDirInput,      // root : NORTH_DIR
            byte[] parentTreeSignatureInput,  // root : {0x00,0x00....(x20)}
            Position selfPositionInput,
            Tile selfTileInput) {
            if (selfTileInput == null) {
                return;
            }

            if (selfPositionInput == null) {
                return;
            }

            selfLevel = selfLevelInput;
            selfPosition = selfPositionInput;
            selfTile = selfTileInput;
            treeSignatureByteArray = new byte[TREE_SIGNATURE_LEN];

            for (int i = 0; i < selfLevel; i++) {
                treeSignatureByteArray[i] = parentTreeSignatureInput[i];
            }

            treeSignatureByteArray[selfLevel] = selfDirInput;

            southPosition = new Position(selfPosition.getX(), selfPosition.getY() + 1);
            northPosition = new Position(selfPosition.getX(), selfPosition.getY() - 1);
            eastPosition = new Position(selfPosition.getX() + 1, selfPosition.getY());
            westPosition = new Position(selfPosition.getX() - 1, selfPosition.getY());

            // South
            TileAdapter southTileAdapter = position2TileAdapterMap.get(southPosition);
            if (southTileAdapter != null) {
                if (this.getTile().getExits().containsKey(SOUTH_EXIT_NAME)) {
                    Tile southOfThisTile = this.getTile().getExits().get(SOUTH_EXIT_NAME);
                    if (southOfThisTile != null && southOfThisTile != southTileAdapter.getTile()) {
                        isSouthConsistent = false;
                    }
                }
                if (southTileAdapter.getTile().getExits().containsKey(NORTH_EXIT_NAME)) {
                    Tile northOfSouthTile = southTileAdapter.getTile().getExits()
                        .get(NORTH_EXIT_NAME);
                    if (northOfSouthTile != null && this.getTile() != northOfSouthTile) {
                        isSouthConsistent = false;
                    }
                }
            }

            // North
            TileAdapter northTileAdapter = position2TileAdapterMap.get(northPosition);
            if (northTileAdapter != null) {
                if (this.getTile().getExits().containsKey(NORTH_EXIT_NAME)) {
                    Tile northOfThisTile = this.getTile().getExits().get(NORTH_EXIT_NAME);
                    if (northOfThisTile != null && northOfThisTile != northTileAdapter.getTile()) {
                        isSouthConsistent = false;
                    }
                }
                if (northTileAdapter.getTile().getExits().containsKey(SOUTH_EXIT_NAME)) {
                    Tile southOfNorthTile = northTileAdapter.getTile().getExits()
                        .get(SOUTH_EXIT_NAME);
                    if (southOfNorthTile != null && this.getTile() != southOfNorthTile) {
                        isNorthConsistent = false;
                    }
                }

            }

            // East
            TileAdapter eastTileAdapter = position2TileAdapterMap.get(eastPosition);
            if (eastTileAdapter != null) {
                if (this.getTile().getExits().containsKey(EAST_EXIT_NAME)) {
                    Tile eastOfThisTile = this.getTile().getExits().get(EAST_EXIT_NAME);
                    if (eastOfThisTile != null && eastOfThisTile != eastTileAdapter.getTile()) {
                        isEastConsistent = false;
                    }
                }
                if (eastTileAdapter.getTile().getExits().containsKey(WEST_EXIT_NAME)) {
                    Tile westOfEastTile = eastTileAdapter.getTile().getExits().get(WEST_EXIT_NAME);
                    if (westOfEastTile != null && this.getTile() != westOfEastTile) {
                        isEastConsistent = false;
                    }
                }
            }

            //West
            TileAdapter westTileAdapter = position2TileAdapterMap.get(westPosition);
            if (westTileAdapter != null) {
                if (this.getTile().getExits().containsKey(WEST_EXIT_NAME)) {
                    Tile westOfThisTile = this.getTile().getExits().get(WEST_EXIT_NAME);
                    if (westOfThisTile != null && westOfThisTile != westTileAdapter.getTile()) {
                        isWestConsistent = false;
                    }
                }
                if (westTileAdapter.getTile().getExits().containsKey(EAST_EXIT_NAME)) {
                    Tile eastOfWestTile = westTileAdapter.getTile().getExits().get(EAST_EXIT_NAME);
                    if (eastOfWestTile != null
                        && this.getTile() != eastOfWestTile) {
                        isWestConsistent = false;
                    }
                }
            }

        }

        public int getLevel() {
            return selfLevel;
        }

        public Tile getTile() {
            return selfTile;
        }

        @Override
        public int compareTo(TileAdapter targetTileAdapter) {
            if (this.getLevel() < targetTileAdapter.getLevel()) {
                return -1;
            }

            if (this.getLevel() > targetTileAdapter.getLevel()) {
                return 1;
            }

            BigInteger targetBig = new BigInteger(targetTileAdapter.getTreeSignature());
            BigInteger selfBig = new BigInteger(this.getTreeSignature());
            return selfBig.compareTo(targetBig) * (-1);
        }

        public boolean isConsistent() {
            return (isNorthConsistent && isEastConsistent && isSouthConsistent && isWestConsistent);
        }

        public byte[] getTreeSignature() {
            return treeSignatureByteArray;
        }

        public Position getChildPosition(byte childDir) {
            switch (childDir) {
                case SOUTH_DIR:
                    return southPosition;

                case NORTH_DIR:
                    return northPosition;

                case EAST_DIR:
                    return eastPosition;

                case WEST_DIR:
                    return westPosition;
            }
            return null;
        }

        public Position getSelfPosition() {
            return selfPosition;
        }
    }

}

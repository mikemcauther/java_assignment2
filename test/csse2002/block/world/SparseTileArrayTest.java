package csse2002.block.world;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SparseTileArrayTest {

    private Tile startingTile = null;
    private Position startPositionSaved = null;
    private SparseTileArray sparseTileArray = null;

    @Test
    public void getTiletesting() {
        // Construct Tile map
        /*

                                tile(0,-2)
                                   |
                                tile(0,-1)
                                   |
                                   |
tile(-2,0)  -- tile(-1,0)  --  starting_tile(0,0) -- tile(1,0)
         */
        startingTile = new Tile();

        Tile starting_northTile = new Tile();
        Tile starting_westTile = new Tile();
        Tile starting_eastTile = new Tile();
        Tile starting_west_westTile = new Tile();
        Tile starting_north_northTile = new Tile();

        List<Tile> expectedTileList = new ArrayList<>();

        expectedTileList.add(startingTile);
        expectedTileList.add(starting_northTile);
        expectedTileList.add(starting_eastTile);
        expectedTileList.add(starting_westTile);
        expectedTileList.add(starting_north_northTile);
        expectedTileList.add(starting_west_westTile);
        try {
            startingTile.addExit("north", starting_northTile);
            startingTile.addExit("east", starting_eastTile);
            startingTile.addExit("west", starting_westTile);

            starting_northTile.addExit("north", starting_north_northTile);
            starting_westTile.addExit("west", starting_west_westTile);
        } catch (NoExitException e) {
            fail();
        }

        try {
            startPositionSaved = new Position(0, 0);
            sparseTileArray = new SparseTileArray();
            sparseTileArray
                .addLinkedTiles(startingTile, startPositionSaved.getX(), startPositionSaved.getY());
            Assert.assertEquals(sparseTileArray.getTile(new Position(0, -2)),
                starting_north_northTile);
            Assert
                .assertEquals(sparseTileArray.getTile(new Position(-2, 0)), starting_west_westTile);
            Assert
                .assertEquals(sparseTileArray.getTile(new Position(0, 0)), startingTile);

            Tile[] expectedTileArray = new Tile[expectedTileList.size()];
            expectedTileArray = expectedTileList.toArray(expectedTileArray);

            List<Tile> actualTileList = sparseTileArray.getTiles();
            Tile[] actualTileArray = new Tile[actualTileList.size()];
            actualTileArray = actualTileList.toArray(actualTileArray);

            Assert.assertArrayEquals(expectedTileArray, actualTileArray);
        } catch (WorldMapInconsistentException e) {
            fail();
        }
    }

    @Test
    public void addLinkedTilesFailByWorldMapInconsistentExceptionCase1() {
        // Construct Tile map
        /*

                                 tile2
                                   |
                              north_tile1
                                   |
                                   |
      tile 0  -- tile3  --  starting_tile0 -- east_tile2
         */
        startingTile = new Tile();
        Tile starting_northTile = new Tile();
        Tile starting_westTile = new Tile();
        Tile starting_eastTile = new Tile();
        Tile starting_west_westTile = new Tile();
        Tile starting_north_northTile = new Tile();

        WorldMap sampleWorldMap = null;

        try {

            startingTile.addExit("north", starting_northTile);
            startingTile.addExit("east", starting_eastTile);
            startingTile.addExit("west", starting_westTile);

            starting_northTile.addExit("north", startingTile);
            starting_westTile.addExit("west", startingTile);
        } catch (NoExitException e) {

        }

        try {
            startPositionSaved = new Position(0, 0);
            sparseTileArray = new SparseTileArray();
            sparseTileArray
                .addLinkedTiles(startingTile, startPositionSaved.getX(), startPositionSaved.getY());

            fail();
        } catch (WorldMapInconsistentException e) {
        }
    }

    @Test
    public void addLinkedTilesFailByWorldMapInconsistentExceptionCase2() {
        // Construct Tile map
        /*
                              north_tile1  -- tile 3
                                   |            |
                                   |            |
                            starting_tile0 --  tile 4 (also tile2)
         */
        startingTile = new Tile();
        Tile starting_northTile = new Tile();
        Tile starting_north_eastTile = new Tile();
        Tile starting_north_east_southTile = new Tile();
        Tile starting_eastTile = new Tile();

        WorldMap sampleWorldMap = null;

        try {

            startingTile.addExit("north", starting_northTile);
            starting_northTile.addExit("east", starting_north_eastTile);
            starting_northTile.addExit("south", startingTile);
            starting_north_eastTile.addExit("south", starting_north_east_southTile);
            starting_north_eastTile.addExit("west", starting_northTile);
            starting_north_east_southTile.addExit("north", starting_north_eastTile);

            startingTile.addExit("east", starting_eastTile);
            starting_eastTile.addExit("west", startingTile);
        } catch (NoExitException e) {

        }

        try {
            startPositionSaved = new Position(0, 0);
            sparseTileArray = new SparseTileArray();
            sparseTileArray
                .addLinkedTiles(startingTile, startPositionSaved.getX(), startPositionSaved.getY());

            fail();
        } catch (WorldMapInconsistentException e) {
        }
    }
}

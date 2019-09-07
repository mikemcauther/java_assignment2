package csse2002.block.world;

import csse2002.block.world.BlockWorldException;

/**
 * A World Map file contains the wrong format.
 */
public class WorldMapFormatException extends BlockWorldException {

    /**
     *
     */
    public WorldMapFormatException() {
        super();
    }

    /**
     *
     * @param message
     */
    public WorldMapFormatException(String message) {
        super(message);
    }
}

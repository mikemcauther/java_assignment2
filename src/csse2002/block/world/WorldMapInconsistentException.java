package csse2002.block.world;

import csse2002.block.world.BlockWorldException;

/**
 *A World Map file is geometrically inconsistent.
 */
public class WorldMapInconsistentException
    extends BlockWorldException {

    /**
     *
     */
    public WorldMapInconsistentException(){
        super();
    }

    /**
     *
     * @param message
     */
    public WorldMapInconsistentException(String message){
        super(message);
    }
}

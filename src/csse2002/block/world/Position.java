package csse2002.block.world;

/**
 * Represents the position of a Tile in the <code>SparseTileArray</code>
 */
public class Position
    implements java.lang.Comparable<Position> {

    private int x_coordinate = 0;
    private int y_coordinate = 0;
    private int HASH_CODE_RANGE = 1000;


    /**
     * Construct a position for (x, y)
     *
     * @param x the x coordinates
     * @param y the y coordinate
     */
    public Position(int x, int y) {
        x_coordinate = x;
        y_coordinate = y;
    }

    /**
     * Compare this position to another position. <br> return
     * <ul>
     * <li> -1 if getX() &lt; other.getX() </li>
     * <li> -1 if getX() == other.getX() and getY() &lt; other.getY() </li>
     * <li> 0 if getX() == other.getX() and getY() == other.getY() </li>
     * <li> 1 if getX() &gt; other.getX() </li>
     * <li> 1 if getX() == other.getX() and getY() &gt; other.getY() </li>
     * </ul>
     *
     * @param other the other Position to compare to
     * @return -1, 0, or 1 depending on conditions aboves
     */
    public int compareTo(Position other) {
        if (this.getX() > other.getX()) {
            return 1;
        }
        if (this.getX() < other.getX()) {
            return -1;
        }
        if (this.getY() > other.getY()) {
            return 1;
        }
        if (this.getY() < other.getY()) {
            return -1;
        }
        return 0;
    }

    /**
     * Indicates whether some other object is "equal to" this one. (see <a
     * href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html">
     * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html</a>) <br> Two Positions are
     * equal if getX() == other.getX() &amp;&amp; getY() == other.getY()
     *
     * @param obj the object to compare to
     * @return true if obj is an instance of Position and if obj.x == x and obj.y == y.
     */
    public boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Position)) {
            return false;
        }

        Position targetPosition = (Position) obj;
        if (this.getX() != targetPosition.getX()) {
            return false;
        }

        if (this.getY() != targetPosition.getY()) {
            return false;
        }

        return true;
    }

    /**
     * Get the x coordinate
     *
     * @return the x coordinate
     */
    public int getX() {
        return x_coordinate;
    }

    /**
     * Get the y coordinate
     *
     * @return the y coordinate
     */
    public int getY() {
        return y_coordinate;
    }

    /**
     * Compute a hashCode that meets the contract of Object.hashCode <br> (see <a
     * href="https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html">
     * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html</a>)
     *
     * @return a suitable hashcode for the Position
     */
    public int hashCode() {
        return (this.getX() + this.getY()) % HASH_CODE_RANGE;
    }

    /**
     * Convert this position to a string. <br> String should be "(&lt;x&gt;, &lt;y&gt;)" where
     * &lt;x&gt; is the value returned by getX() and &lt;y&gt; is the value returned by getY(). <br>
     * Note the space following the comma.
     *
     * @return a string representation of the position "(<x>, <y>)"
     */
    public String toString() {
        return "" + "(<" + this.getX() + ">, <" + this.getY() + ">)";
    }
}

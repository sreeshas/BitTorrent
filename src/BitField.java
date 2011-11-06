import java.util.BitSet;


public class BitField extends AbstractMessage{
    /**
     * bitMap is the payload for BitField messages.
     */
	public BitSet _bitMap;
	/**
	 * Stores type number of Choke Message;
	 */
	public int type=5;
}

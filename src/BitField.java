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
	
	public BitField(BitSet _bitMap){
		super(5);
		this._bitMap = _bitMap;
		
	}
}

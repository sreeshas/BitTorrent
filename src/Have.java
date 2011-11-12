
public class Have extends AbstractMessage{
   /**
    * Piece Index is the payload for Have messages.
    */
	public int  _pieceIndex;
	/**
	 * Stores type number of Choke Message;
	 */
	public int type=4;
	
	public Have(int _peiceIndex) {
		super(4);
		this._pieceIndex = _peiceIndex;
	}
}

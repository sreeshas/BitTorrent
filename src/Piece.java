import java.io.Serializable;


public class Piece extends AbstractMessage implements Serializable{
 
  /**
   *  Represents the ID of piece which also serves
   *  as its filenumber.
   */
   public int pieceIndex;
   
   /**
    * Represents the byte content of Piece.
    */
   public byte[] pieceBytes;
   /**
	 * Stores type number of Choke Message;
	 */
	public int type=7;
}


public class Request extends AbstractMessage{
   /**
    * Piece Index is the payload for Request messages.
    */
   public int pieceIndex;
   /**
	 * Stores type number of Choke Message;
	 */
   public int type=6;
   
   
   public Request(int pieceIndex) {
	   super(6);
	   this.pieceIndex =pieceIndex;
   }
}

import java.util.BitSet;


public class MessageProcessor implements Runnable {
    
	public AbstractMessage message;
	
	public String peerID;
	
	public MessageProcessor(AbstractMessage message,String peerID) {
		this.message = message;
		this.peerID = peerID;
	}
	@Override
	public void run() {
		
		
		switch(message.type){
		case 0: //choke
				  choke();
				  break;
		case 1: //unchoke
				  unchoke();
				  break;
		case 2: //interested
			      interested();
			      break;
		case 3: //notinterested
			      notinterested();
			      break;
		case 4: //have
			      have();
			      break;
		case 5: //bitfield
			      bitfield();
			      break;
		case 6: //request
			      request();
			      break;
		case 7: //piece
			      piece();
			      break;
			 
	    }
	}
  
  /*
   * 	1) Print the choke message to log.
   *    
   */
  private void choke(){
	  peerProcess.logger.info("Peer "+peerProcess._ID+"is choked by "+"Peer "+peerID);
  }
  /*
   *    1) Print the unchoke message to log.
   *    
   */
  private void unchoke(){
	  peerProcess.logger.info("Peer "+peerProcess._ID+"is unchoked by "+"Peer "+peerID);
  }
  /*
   *    1)  Mark connection as interested.
   *    2)  Add connection object to interested list.
   */
  private void interested(){
	   peerProcess.logger.info("Peer "+peerProcess._ID+" recieved the 'interested' message from "+"Peer "+peerID);
	   Connection interestedConnection = peerProcess._connectionMap.get(peerID);
	   interestedConnection.setInterested(true);
	   if(!peerProcess._interestedList.contains(interestedConnection)) {
		   peerProcess._interestedList.add(interestedConnection);
	   }
  }
  /*
   *    1)	Mark connection as not interested.
   *    2)  Remove connection object from interested list if present.
   */
  private void notinterested(){
	  peerProcess.logger.info("Peer "+peerProcess._ID+" recieved 'not interested' message from "+"Peer "+peerID);
	  Connection notInterestedConnection = peerProcess._connectionMap.get(peerID);
	  notInterestedConnection.setInterested(false);
	  if(peerProcess._interestedList.contains(notInterestedConnection)) {
		  peerProcess._interestedList.remove(notInterestedConnection);
	  }
  }
  /*
   *	1) Set the corresponding Bit in bitmap
   *	
   */
  private void have(){
	  synchronized(peerProcess.bitfieldLock) {
		  BitSet connectionBitSet = peerProcess._bitMapper.get(peerID);
		  connectionBitSet.set(((Have)message)._pieceIndex, true);
	  }
  }
  private void bitfield(){
	  synchronized(peerProcess.bitfieldLock) {
		  BitSet connectionBitSet = peerProcess._bitMapper.get(peerID);
		  connectionBitSet = ((BitField)message)._bitMap;
	  }
  }
  /*
   *	1)Send Piece Message containing bytes corresponding to piece to
   *      requested host .
   */
  private void request(){
	  byte[] pieceByte = null;
	  synchronized(peerProcess.pieceLock){
		   pieceByte = peerProcess.getPiece(((Request)message).pieceIndex);
	  }
	  Piece msgPiece = new Piece(pieceByte,((Request)message).pieceIndex);
	  Connection pieceConnection = peerProcess._connectionMap.get(peerID);
	  peerProcess.write(pieceConnection.get_oos(),msgPiece);
	  
  }
  /*
   *      1) Put the piece in temporary directory.
   *      2) Update _bitmap and _bitmapper.
   *      3) Broadcast Have message to other connections.
   */
  private void piece(){
	  synchronized(peerProcess.pieceLock){
		  peerProcess.putPiece(((Piece)message).pieceBytes,((Piece)message).pieceIndex);
	  }
	  synchronized(peerProcess.bitfieldLock){
		  peerProcess._bitmap.set(((Piece)message).pieceIndex,true);
		  peerProcess._bitMapper.put(peerID,peerProcess._bitmap);
	  }
	  peerProcess.set_currentPieceCount();
	  peerProcess.logger.info("Peer "+peerProcess._ID+" has downloaded piece "+((Piece)message).pieceIndex+" from Peer "+peerID);
	  peerProcess.logger.info("Now the number of Pieces it has is "+peerProcess.get_currentPieceCount());
	  Have haveMsg = new Have(((Piece)message).pieceIndex);
	  for(Connection c:peerProcess._connections) {
		  peerProcess.write(c.get_oos(),haveMsg);
	  }
	  
  }

}

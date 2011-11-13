import java.util.BitSet;
import java.util.Vector;


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
   *    2) Remove Peer from valid Request List.
   *    3) Remove all Pieces requested from requestedMap.
   */
  private void choke(){
	  peerProcess.logger.info("Peer "+peerProcess._ID+"is choked by "+"Peer "+peerID);
	  peerProcess.validRequestList.remove(peerID);
	  Vector<Integer> requestedList =peerProcess.requestedMap.get(peerID);
	  requestedList.clear();
  }
  /*
   *    1) Print the unchoke message to log.
   *    2) Add peerId to validRequestList
   *    3) Get a piece which is not requested.
   *    4) Add piece to already requested list.
   *    5) Create Request Message and send it.
   *    
   */
  private void unchoke(){
	  peerProcess.logger.info("Peer "+peerProcess._ID+"is unchoked by "+"Peer "+peerID);
	  if(!peerProcess.validRequestList.contains(peerID)) {
		  peerProcess.validRequestList.add(peerID);
	  }
	  BitSet connectionBitSet = peerProcess._bitMapper.get(peerID);
	  while(peerProcess.validRequestList.contains(peerID)){
		  int pieceId = peerProcess.compareBitmap(connectionBitSet);
		  if(-1 == pieceId)
		  {
			  continue;
		  }
		  if(!peerProcess.isRequested(pieceId)){
			  if(peerProcess.requestedMap.get(peerID) == null){
				  Vector<Integer> pieceRequested = new Vector<Integer>();
				  pieceRequested.add(pieceId);
				  peerProcess.requestedMap.put(peerID, pieceRequested);
			  }
			  else {
			  Vector<Integer> pieceRequested=peerProcess.requestedMap.get(peerID);
			  pieceRequested.add(pieceId);
			  }
			  MessageSender ms = new MessageSender();
			  ms._peerID = peerID;
			  Request r1 = new Request(pieceId);
			  ms.message = r1;
			  Thread senderThread = new Thread(ms);
			  senderThread.start();
		  }
	  }
	  
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
		  if(connectionBitSet == null ){
			  peerProcess._bitMapper.put(peerID, ((BitField)message)._bitMap);
		  }
		  else {
		  connectionBitSet = ((BitField)message)._bitMap;
		  }
	  }
	  peerProcess.logger.info("recieved bitfield");
	  MessageSender ms = new MessageSender();
	  ms._peerID = peerID;
	  ms.message =message;
	  Thread senderThread = new Thread(ms);
	  senderThread.start();
	  
  }
  /*
   *	1)Send Piece Message containing bytes corresponding to piece to
   *      requested host .
   */
  private void request(){
	  peerProcess.logger.info("Recieved request from " + peerID);
	  if(!peerProcess.unchokedList.contains(peerID)) {
		  return ;
	  }
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
	  peerProcess.logger.info("Recieved piece from " + peerID);
	  synchronized(peerProcess.pieceLock){
		  peerProcess.putPiece(((Piece)message).pieceBytes,((Piece)message).pieceIndex);
	  
		  synchronized(peerProcess.bitfieldLock){
			  peerProcess._bitmap.set(((Piece)message).pieceIndex,true);
			  peerProcess._bitMapper.put(peerID,peerProcess._bitmap);
		  }
		  peerProcess.set_currentPieceCount();
	  }
	  
	  peerProcess.removeRequested(((Piece)message).pieceIndex);
	  peerProcess.logger.info("Peer "+peerProcess._ID+" has downloaded piece "+((Piece)message).pieceIndex+" from Peer "+peerID);
	  peerProcess.logger.info("Now the number of Pieces it has is "+peerProcess.get_currentPieceCount());
	  Have haveMsg = new Have(((Piece)message).pieceIndex);
	  for(Connection c:peerProcess._connections) {
		  peerProcess.write(c.get_oos(),haveMsg);
	  }
	  
  }

}

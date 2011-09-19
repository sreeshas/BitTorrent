import java.util.ArrayList;

/**
 * This is main class for the BitTorrent Project.
 * @author sreenidhi
 *
 */
public class peerProcess {
	
	/*
	 * Represents unique _ID of the Peer.
	 */
	private String _ID;
	
	/*
	 * Represents the maximum number of simultaneous connections
	 * a Peer can have with other peers
	 */
	private int _maxNoOfConnections;
	
	/*
	 * List of Connection objects which represent the connections
	 * to Other peers
	 */
	private ArrayList<Connection> _connections;
	
	/*
	 * Represents the only instance of Peer class
	 */
	private static peerProcess _instance = null;
	
	/*
	 * Represents uploadRate of Peer
	 */
	private int _uploadRate;
	
	/*
	 * Represents downloadRate of Peer
	 */
	private int _downloadRate;
	
	/*
	 * Represents optimistic Unchoking Interval
	 */
	private int _optimisticUnchokingInterval;
	
	/*
	 * Represents unchoking Interval
	 */
	private int _unchokingInterval;
	
	/*
	 * Represents the file Name which is distributed among the peers.
	 */
	private String _fileName;
	
	/*
	 * Represents size of File which is distributed among the peers.
	 */
	private String _fileSize;
	
	/*
	 * File is split into pieceSize number of pieces.
	 */
	private int _pieceSize;
	
	/*
	 * Represents the host of @Peer
	 */
	private String host;
	
	/*
	 * Represents the port number on which the @Peer is listening.
	 */
	private int port;
	
	/*
	 * Returns true if @Peer has complete File.
	 */
	private Boolean hasCompleteFile;
	/**
	 * Implements Singleton Design pattern for BitTorrent program
	 * @return Peer
	 */
	public static peerProcess getInstance(String peerID){
		if(_instance==null){
			peerProcess peerprocess = new peerProcess();
			peerprocess._ID=peerID;
			return peerprocess;
		}
		return _instance;
	}
	
	/*
	 * Constructor is made Private to enforce singleton Design pattern
	 */
	private peerProcess(){
		_connections = new ArrayList<Connection>(_maxNoOfConnections);
	}
	
   /**
	* @return the _maxNoOfConnections
	*/
	public int get_maxNoOfConnections() {
		return _maxNoOfConnections;
	}

   /**
	* @param maxNoOfConnections the _maxNoOfConnections to set
	*/
	public void set_maxNoOfConnections(int maxNoOfConnections) {
		_maxNoOfConnections = maxNoOfConnections;
	}

   /**
	* @return the uploadRate
	*/
	public int getUploadRate() {
		return _uploadRate;
	}

   /**
    * @param uploadRate the uploadRate to set
	*/
	public void setUploadRate(int uploadRate) {
		this._uploadRate = uploadRate;
	}

   /**
	* @return the downloadRate
    */
	public int getDownloadRate() {
		return _downloadRate;
	}

   /**
	* @param downloadRate the downloadRate to set
	*/
	public void setDownloadRate(int downloadRate) {
		this._downloadRate = downloadRate;
	}

    public static void main(String[] args){
	   peerProcess peerprocess = peerProcess.getInstance(args[0]);
    }
}

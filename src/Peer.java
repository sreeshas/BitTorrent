import java.util.ArrayList;

/**
 * This is main class for the BitTorrent Project.
 * @author sreenidhi
 *
 */
public class Peer {
	
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
	private Peer _instance = null;
	
	/*
	 * Represents uploadRate of Peer
	 */
	private int uploadRate;
	
	/*
	 * Represents downloadRate of Peer
	 */
	private int downloadRate;
	
	/**
	 * Implements Singleton Design pattern for BitTorrent program
	 * @return Peer
	 */
	public Peer getInstance(){
		if(_instance==null){
			return new Peer();
		}
		return _instance;
	}
	
	/*
	 * Constructor is made Private to enforce singleton Design pattern
	 */
	private Peer(){
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
		return uploadRate;
	}

   /**
    * @param uploadRate the uploadRate to set
	*/
	public void setUploadRate(int uploadRate) {
		this.uploadRate = uploadRate;
	}

   /**
	* @return the downloadRate
    */
	public int getDownloadRate() {
		return downloadRate;
	}

   /**
	* @param downloadRate the downloadRate to set
	*/
	public void setDownloadRate(int downloadRate) {
		this.downloadRate = downloadRate;
	}

   public static void main(String[] args){
	  
   }
}

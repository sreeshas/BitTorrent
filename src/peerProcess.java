import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * This is main class for the BitTorrent Project.
 * @author sreenidhi
 *
 */
public class peerProcess {
	/*
	 * Logger for peerProcess
	 */
	private static Logger logger = Logger.getLogger("peerProcess.class");
	/*
	 * Represents unique _ID of the Peer.
	 */
	private static String _ID;
	
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
	private int _fileSize;
	
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
	
	/*
	 * Represents current working directory of the peer
	 */
	private String _currentWorkingDirectory;
	
	/*
	 * Represents data directory where peer files are 
	 * stored.
	 */
	private static String _dataDirectory;
	
	/*
	 * bitmap stores information about pieces contained by
	 * Peer
	 */
	private byte[] bitmap;
	
	/*
	 * Contains actual length of data in final piece.
	 */
	
	private int _spillOver;
	
	/*
	 * Represents total number of pieces required 
	 * for having complete file. 
	 * 
	 */
	private int _totalPieceCount;
	
	/**
	 * Implements Singleton Design pattern for BitTorrent program
	 * @return Peer
	 */
	public static peerProcess getInstance(String peerID){
		
		if(_instance==null){
			/* Set Peer ID */
			_ID=peerID;
			peerProcess peerprocess = new peerProcess();
			
			return peerprocess;
		}
		return _instance;
	}
	
	/*
	 * Constructor is made Private to enforce singleton Design pattern
	 */
	private peerProcess(){
		
		/* Set maximum number of connections */
		_connections = new ArrayList<Connection>(_maxNoOfConnections);
		/* Set current working directory */
		_currentWorkingDirectory = System.getProperty("user.dir");
		/* Set Data Directory */
		 _dataDirectory =_currentWorkingDirectory+"/"+"peer_"+_ID;
		
		
	}
	/* 
	 * 1) Create data directory if it does not exist.
	 * 
	 * 2) If the Peer is a seed,split the file into
	 *    specified number of pieces in temp directory, 
	 *    which is placed under data directory.
	 * 
	 * 3) If the Peer is a Leecher, (fill later)
	 *  
	 * 4) Initialize the bitmap
	 */
	private void initialize(){
		/* Create Data Directory if it does not exist */
		createDirectory(_dataDirectory);

		/* Set totalPieceCount */ 
		if(_fileSize%_pieceSize == 0){

			_totalPieceCount=_fileSize/_pieceSize;
			_spillOver = 0;
		}
		else{

			_totalPieceCount=(_fileSize/_pieceSize)+1;
			_spillOver = _totalPieceCount%_pieceSize;
		}
		
		/* Set bitmap */
		bitmap = new byte[_totalPieceCount];

		if(hasCompleteFile) {
            
			/* Initialize array value to 1 */
			Arrays.fill(bitmap, new Byte("1"));
		}
		else{
			
			/* Initialize array value to 0 */
			Arrays.fill(bitmap, new Byte("0"));

		}
	}
	
	private void intializeLeech() {
		// TODO Auto-generated method stub
		
	}

	/*
	 * 1) Determine 
	 */
	private void initializeSeed(){
		
		
	}
	/*
	 * Creates a directory if it does not exist
	 */
   private void createDirectory(String dirName) {
	   
	   File f = new File(dirName);
	   if(f.exists()){
		   logger.info("Directory: " + dirName + " already exists"); 
		   return;
	   }
	   boolean success = (new File(dirName)).mkdir();
	   if (success) {
		   logger.info("Directory: " + dirName + " created successfully");
	   } 
	   else{
		   logger.info("Could not create" + dirName);   
	   }
	   
		
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
    	
    	/* Configure Log Properties */
    	PropertyConfigurator.configure("log4j.properties");
    	
    	peerProcess peerprocess = peerProcess.getInstance(args[0]);
    }
}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;

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
	 * List of Connection objects which represent the connections
	 * to Other peers
	 * @GuardedBy ("this")
	 */
	private static  Vector<Connection> _connections ;
	
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
	private static volatile Boolean _hasCompleteFile;
	
	/*
	 * Represents current working directory of the peer
	 */
	private static String _currentWorkingDirectory;
	
	/*
	 * Represents data directory where peer files are 
	 * stored.
	 */
	private static String _dataDirectory;
	/*
	 * temporary directory under @_dataDirectory where file pieces 
	 * are stored.
	 */
	private static String _tempDirectory;
	
	/*
	 * bitmap stores information about pieces contained by Peer
	 * @GuardedBy("this")
	 */
	
	private BitSet _bitmap;

	/*
	 * Contains actual length of data in final piece.
	 */
	
	private static int _spillOver;
	
	/*
	 *  Represents a list which contains Piece Index already requested
	 *  from other peers.
	 *  When making a request for a Piece, Piece Index is first checked 
	 *  with @requestList and only if the Piece is not requested,
	 *  it is added to requestList and a request is made.
	 */
	private Vector<Integer> requestList = new Vector<Integer>();
	
	/*
	 * Represents total number of pieces required 
	 * for having complete file. 
	 * 
	 */
	private static int _totalPieceCount;
	
	/*
	 * Stores <code>_bitmap</code> of each peer with its <code>_ID<code> as key and 
	 * its corresponding <code>_bitmap</code> as value.
	 */
	//private HashMap<String,BitSet> _bitMapper = new HashMap<String,BitSet>();
	private Map<String,BitSet> _bitMapper = new ConcurrentHashMap<String,BitSet>();
	
	/*
	 * Represents number of Preferred Neighbors.
	 */
	private static int _noOfPreferredNeighbors;
	
	/*
	 * Indicates total number of Pieces with the Peer.
	 * @GuardedBy("this")
	 */
	private static volatile int _currentPieceCount;
	
	
	/*
	 * Used to identify the next bit in the _bitmap 
	 * whose bit is not set.
	 * @GuardedBy("this")
	 */
	private static volatile int  _currentIndex;
	
	/*
	 * Represents preferred NeighborList.
	 */
	private ArrayList<Connection> _preferredNeighborList;
	/*
	 * List of peers which are not connection yet.populated by loadConfiguration()
	 */
    private ArrayList<Connection> _peerList = new ArrayList<Connection>();
 	/**
	 * @return the _currentPieceCount
	 */
	private static synchronized int get_currentPieceCount() {
		return _currentPieceCount;
	}
	
	
	
    
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
	private peerProcess() {
		
    }
	
	/*
	 * whenever a thread completely downloads a piece from a Peer,
	 * this method is called. It first checks if the Piece was initially not 
	 * present with this Peer and then increments the no of Pieces present with 
	 * this Peer.
	 * 
	 * when _noOfPieces equals _totalNoOfPieces download is complete.
	 * 
	 * Not Tested..
	 */
	private synchronized void incrementPieces(int PieceID){
		if( _bitmap.get(PieceID) == false ) {
			_currentPieceCount++;
			_bitmap.set(PieceID);
		}
		
	}
	
	/*
	 *  Returns the next Clear Bit from starting from _currentIndex.
	 *  it returns -1 if all the bits in _bitmap are set.
	 *  
	 *  if the _currentIndex reaches size of the _bitmap and
	 *  _noOfPiecesPresent is not equal to _totalNoOfPieces
	 *  it wraps over the _bitmap array and starts again.
	 *  
	 *  Not Tested.
	 */
	private synchronized int getNextClearBit(){
		int nextClearBit = _bitmap.nextClearBit(_currentIndex);
		_currentIndex = nextClearBit;
		if( (nextClearBit == -1) && (_currentPieceCount != _totalPieceCount)){
			_currentIndex = 0;
			nextClearBit = _bitmap.nextClearBit(_currentIndex);
			_currentIndex = nextClearBit;
		}
		return nextClearBit;
	}
	/*
	 * Loads configuration Data.
	 * There are 2 configuration files ( *.cfg) PeerInfo.cfg and Common.cfg
	 * 
	 * 1) Properties are initialized.
	 * 
	 * 2) Connection objects are created for all Peers and added to a list. 
	 *    (Sockets  of the connection are not established yet.)
	 */
	private void loadConfiguration(){
		/* Set current working directory */
		_currentWorkingDirectory = System.getProperty("user.dir");
		/* Set Data Directory */
		 _dataDirectory =_currentWorkingDirectory+"/"+"peer_"+_ID;
		/* Set Temporary Directory */
		 _tempDirectory=_dataDirectory+"/"+"temp";
		
		Properties prop = new Properties();
		/* Loading properties from Common.cfg */
	    String fileName = _currentWorkingDirectory+ "/" + "Common.cfg";
	    InputStream is;
		try {
			is = new FileInputStream(fileName);
			prop.load(is);
			_noOfPreferredNeighbors = Integer.parseInt(prop.getProperty("NumberOfPreferredNeighbors"));
			_unchokingInterval = Integer.parseInt(prop.getProperty("UnchokingInterval"));
			_optimisticUnchokingInterval = Integer.parseInt(prop.getProperty("OptimisticUnchokingInterval"));
			_fileName = prop.getProperty("FileName");
			_fileSize = Integer.parseInt(prop.getProperty("FileSize"));
			_pieceSize = Integer.parseInt(prop.getProperty("PieceSize"));
			//logger.info("_noOfPreferredNeighbors" +" "+"_unchokingInterval"+" "+" _optimisticUnchokingInterval"+" "+"_fileName"+" "+"_fileSize"+" "+"_pieceSize");
			//logger.info(_noOfPreferredNeighbors +" "+_unchokingInterval+" "+ _optimisticUnchokingInterval+" "+_fileName+" "+_fileSize+" "+_pieceSize);
		} catch (FileNotFoundException e) {
			logger.info(e);
			
		} catch (IOException e) {
			logger.info(e);
			
		}
		/* Set maximum number of connections */
		synchronized(this) {
		_connections = new Vector<Connection>(_noOfPreferredNeighbors);
		}
		/* Loading properties from PeerInfo.cfg */
		fileName = _currentWorkingDirectory+ "/" + "PeerInfo.cfg";
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			  
			while ((strLine = br.readLine()) != null)   {
			   
				StringTokenizer st  = new StringTokenizer(strLine);
				while(st.hasMoreTokens()){
					String _peerID = st.nextToken();
					String _host = st.nextToken();
					int _port = Integer.parseInt(st.nextToken());
					Boolean _hasCompleteFile;
					if(Integer.parseInt(st.nextToken())==1){
					  _hasCompleteFile = true;
					}
					else {
					  _hasCompleteFile = false;
					}
					//logger.info(_peerID+" "+_host+" "+ _port+" "+_hasCompleteFile);
					if(_peerID.equals(_ID)){
						host = _host;
						port = _port;
						peerProcess._hasCompleteFile = _hasCompleteFile;
					}
					else{
					
			      final Connection c1 = new Connection(_peerID,_host,_port,_hasCompleteFile);
					_peerList.add(c1);
					
					}
				}
			
			}
			in.close();
		} catch (FileNotFoundException e) {
			logger.info(e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.info(e);
			e.printStackTrace();
		}
		
	    
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
	private synchronized void initialize(){
		/* Create Data Directory and temp Directory if it does not exist */
		createDirectory(_dataDirectory);
		createDirectory(_tempDirectory);
		/* Set totalPieceCount */ 
		if(_fileSize%_pieceSize == 0){

			_totalPieceCount=_fileSize/_pieceSize;
			_spillOver = 0;
		}
		else{

			_totalPieceCount=(_fileSize/_pieceSize)+1;
			_spillOver = _fileSize%_pieceSize;

		}
		
		/* Set _bitmap */
		_bitmap = new BitSet(_totalPieceCount);
		
		

		if(_hasCompleteFile) {
            
			/* Set all bit values to true */
			_bitmap.set(0,_totalPieceCount-1,true);
			/* Add the _bitmap to _bitMapper */
			_bitMapper.put(_ID,_bitmap);
			seedFunction();
		}
		else{
			
			/* Set all bit values to false . BitSet by default is initialized to false*/
			/* Add the _bitmap to _bitMapper */
			_bitMapper.put(_ID,_bitmap);
			leechFunction();

		}
	}
	private void seedFunction(){
		initializeSeed();
		initiateConnection();
	}
	private void leechFunction(){
		intializeLeech();
	}
	private void intializeLeech() {
		
		initiateConnection();
	}
	private void initiateConnection(){
		Thread taskScheduler = new Thread(new Runnable(){
			public void run(){
				while(_connections.size()==0){
					try {
						Thread.sleep(2000);
					} 
					catch (InterruptedException e) {
						logger.debug("initiateConnection()",e);
					}
				
				}
				//schedule setoptimizedneighbor task.
				Timer optimizedNeighborScheduler = new Timer();
				optimizedNeighborScheduler.schedule(new TimerTask(){
					public void run(){
						selectOptimisticNeighbor();
					}
				}, 0, _optimisticUnchokingInterval);
				//schedule setpreferredneighbor task.
//				Timer preferredNeighborScheduler = new Timer();
//				preferredNeighborScheduler.schedule(new TimerTask(){
//					public void run(){
//					    selectPreferredNeighbors();
//					}
//				}, 0, _unchokingInterval);
				
			}
		});
		taskScheduler.start();
		ArrayList<Thread> threadList = new ArrayList<Thread>();
		try {
			final ServerSocket welcomeSocket = new ServerSocket(port);
			
			for(final Connection c: _peerList){
				Runnable r1 = new Runnable(){
					@Override
					public void run(){
						if(c.get_peerSocket()!=null){

						}
						Socket connection = null;
						if(Integer.parseInt(c.get_peerID()) > Integer.parseInt(_ID)){

							try {
								connection = welcomeSocket.accept();
								
								logger.info("Peer"+" "+_ID+" "+"is connected from"+" "+"Peer"+c.get_peerID());
								c.set_peerSocket(connection);
								c.set_oos(new ObjectOutputStream(connection.getOutputStream()));
								c.set_ois(new ObjectInputStream(connection.getInputStream()));
								synchronized(this){
								_connections.add(c);
								}
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
						else{

							try {
								connection = new Socket(c.get_host(),c.get_port());
								logger.info("Peer"+" "+_ID+" "+"makes connection to"+" "+"Peer"+c.get_peerID());
								c.set_peerSocket(connection);
								c.set_oos(new ObjectOutputStream(connection.getOutputStream()));
								c.set_ois(new ObjectInputStream(connection.getInputStream()));
								synchronized(this){
								_connections.add(c);
								}
							}
							catch(ConnectException cr){
								try {
									logger.info("Waiting for other peers to start");
									Thread.sleep(2000);
									run();
								} catch (InterruptedException e) {

									e.printStackTrace();
								}


							}
							catch (UnknownHostException e) {

								e.printStackTrace();
							} catch (IOException e) {

								e.printStackTrace();
							} 

						}
					}
				};

				Thread t1 = new Thread(r1);
				t1.start();
				threadList.add(t1);
				
				

			}
		} catch (IOException e) {
			logger.info(e);
			e.printStackTrace();
		}
		for(Thread t: threadList){
			try {
				t.join();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
		}

		logger.info("All peers are now connected.");

	}

	/*
	 * 1) Create a temp directory under data directory
	 * 2) Split the file into @_totalPieceCount  number of Pieces
	 */
	private void initializeSeed(){
		
		splitFile();
	}
	
	/*
	 * splits _fileName file into _totalPieceCount number of pieces
	 * and writes each piece to _tempDirectory under _dataDirectory.
	 * fileName of a piece is its respective piece number.
	 * 
	 * File _fileName should be present in _dataDirectory.
	 */
	private void splitFile()
	{
		/*Open source file  _fileName */
		File srcFile = new File(_fileName);
		
		/*Load the bytes of _fileName to fileBytes array */
		byte[] fileBytes = new byte[_fileSize];
		long bytesRead=0;
		DataInputStream dis = null;
		try {
		    dis = new DataInputStream(new FileInputStream(srcFile));
			bytesRead = dis.read(fileBytes,0,_fileSize);
		} catch (FileNotFoundException e) {
			logger.info(e);
			e.printStackTrace();
		} catch (IOException e) {
		    logger.info(e);
			e.printStackTrace();
		} 
		if(bytesRead<_fileSize){
			logger.info("Could not read "+_fileName+" Completely");
			return;
		}
		
		/* writing the file into pieces */
		for(int i=0;i<_totalPieceCount;i++){
			try {
				OutputStream out = new FileOutputStream(_tempDirectory+"/"+i);
				
				/* if last Piece and there is spillOver bytes,
				 * copy only spillOver bytes.
				 */
				if(i+1==_totalPieceCount && _spillOver >0 ){
					out.write(fileBytes,(i*_pieceSize),_spillOver);
					out.close();
					continue;
				}
				out.write(fileBytes,(i*_pieceSize),_pieceSize);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	/*
	 * Compares passed peerBitmap with @bitMap and returns index of first 
	 * occurrence in @bitmap whose byte value is 0 and peerBitmap value is one.
	 * 
	 * if there is no difference or difference which does not help, it returns -1
	 * 
	 * Example
	 * bitMap      111011
	 * peerBitmap  111111       compareBitmap(peerBitmap) returns  3
	 * 
	 * bitMap      111000       
	 * peerBitmap  000000       compareBitmap(peerBitmap) returns -1
	 * 
	 * bitMap      111100
	 * peerBitmap  000111       compareBitmap(peerBitmap) returns  4
	 * 
	 * tested for above test cases.
	 */
	private synchronized int  compareBitmap( BitSet peerBitmap){
		
		if(_hasCompleteFile){
			/* if peer has Complete File ,comparing bitmaps is not required.*/
			return -1;
		}
		int index = 0;
		while(index != -1){
			/* Find the index of next bit which is set to false starting from <code>index</code>*/
			
			index = _bitmap.nextClearBit(index);
		    
			if(index != -1) {
				if(peerBitmap.get(index)){
					/* return index, when _bitmap does not have the bit set whereas peerBitmap bit is set. */
					return index;
				}
			}
			
		}
		
		return -1;
	}
	
	/*
	 * combines pieces in the @_tempDirectory to form a complete file 
	 * which is place in @_dataDirectory
	 * 
	 */
	private void combineFiles(){
	
		FileOutputStream fos = null;
		try {
			  File f = new File(_fileName);
			  if(!f.exists()){
				  f.createNewFile();
			  }
			  fos = new FileOutputStream(_fileName);
			
		} catch (FileNotFoundException e) {
			logger.info(e);
			return;
		} catch (IOException e) {
			logger.info(e);
			e.printStackTrace();
			return;
		}
		
		
		for(int i=0;i<_totalPieceCount;i++){
			byte[] fileBytes=getPiece(i);
			try {
				fos.write(fileBytes,0,fileBytes.length);
			} catch (IOException e) {
				logger.info(e);
			}
		}
		
		try {
			fos.close();
		} catch (IOException e) {
			logger.info(e);
		}
	}
	/**
	 *  Returns list of connections who are "interested".
	 */
	public synchronized ArrayList<Connection> getInterested() {
		ArrayList<Connection> interestedList = new ArrayList<Connection>();
		for(Connection c: _connections) {
			if(c.getInterested()==true) {
				interestedList.add(c);
			}
		}
		return interestedList;
	}
	/**
	 * Returns list of Connections whose Peers are not choked.
	 */
	public synchronized ArrayList<Connection> getUnchokedList() {
		ArrayList<Connection> unchokedList = new ArrayList<Connection>();
		for(Connection c: _connections) {
			if(c.getChoked()==false) {
				unchokedList.add(c);
			}
		}
		return unchokedList;
	}
	/*
	 * Inserts "pieceNumber" file into _tempDirectory.
	 * 
	 */
	private void putPiece(byte[] fileBytes, int pieceNumber){
		
		try {
			OutputStream out = new FileOutputStream(_tempDirectory+"/"+pieceNumber);
			out.write(fileBytes,0,fileBytes.length);
			out.close();
		} catch (IOException e) {
			logger.info(e);
			
		}
		
	}
	/*
	 * Returns content as byte array of piece "pieceNumber" which is 
	 * present in _tempDirectory
	 * 
	 */
	private byte[] getPiece(int pieceNumber){
		File pieceFile = new File(_tempDirectory+"/"+pieceNumber);
		if(!pieceFile.exists()){
			logger.info("Piece requested does not exist. PieceNumber = "+pieceNumber);
			return null;
		}
		int pieceFileSize = (int) pieceFile.length();
		/* 
		 * TODO:sreenidhi 
		 * new byte[] parameter can only be int. it does not work for pieces
		 * whose size is greater than int limit.
		 */
		byte[] pieceFileBytes = new byte[(int) pieceFileSize];
		int bytesRead=0;
		DataInputStream dis = null;
		try {
		    dis = new DataInputStream(new FileInputStream(pieceFile));
			bytesRead = dis.read(pieceFileBytes,0,pieceFileSize);
		} catch (FileNotFoundException e) {
			logger.info(e);
		
		} catch (IOException e) {
		    logger.info(e);
		
		} 
		if(bytesRead<pieceFileSize){
			logger.info("Could not read piece"+pieceNumber+" Completely");
			return null;
		}
		return pieceFileBytes;
	}
   /**
    * selects preferred Neighbors for every <code>_unchokingInterval</code>
    * @return <code> _preferredNeighborList </code>
    */
   public ArrayList<Connection> selectPreferredNeighbors(){
	  
	   _preferredNeighborList = new ArrayList<Connection>(_noOfPreferredNeighbors);
	   
	   /* if peer has complete file, select preferred neighbors randomly
	    * from peers who are "interested".
	    */ 
	   if(_hasCompleteFile) {
		ArrayList<Connection> interestedList = getInterested();
		ArrayList<Integer> alreadySelected =  new ArrayList<Integer>();
		Random rand = new Random();
		int min=0, max=_noOfPreferredNeighbors;

		while(alreadySelected.size()!=max) {
			int randomNum = rand.nextInt(max - min + 1) + min;
			if(!alreadySelected.contains(randomNum)) {
				alreadySelected.add(randomNum);
			}
		}
	    for(Integer i: alreadySelected){
		   _preferredNeighborList.add(interestedList.get(i));
	    }
	   	   return _preferredNeighborList;
	   	}
	   	else {
		/*
		 * yet to be implemented..   
		 */
		   return _preferredNeighborList;   
	   	}
   }
   /**
    * Selects optimistically unchoked neighbor for every <code>_optimisticUnchokingInterval</code>
    */
   public void selectOptimisticNeighbor(){
	   logger.info("in SON");
   }
	/*
	 * Creates a directory if it does not exist
	 */
   private void createDirectory(String dirName) {
	   
	   File f = new File(dirName);
	   if(f.exists()){
		  // logger.info("Directory: " + dirName + " already exists"); 
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
	* @return the _noOfPreferredNeighbors
	*/
	public int get_noOfPreferredNeighbors() {
		return _noOfPreferredNeighbors;
	}

   

   /**
	* @return the uploadRate
	*/
	public synchronized int getUploadRate() {
		return _uploadRate;
	}

   /**
    * @param uploadRate the uploadRate to set
	*/
	public synchronized void setUploadRate(int uploadRate) {
		this._uploadRate = uploadRate;
	}

   /**
	* @return the downloadRate
    */
	public synchronized int getDownloadRate() {
		return _downloadRate;
	}

   /**
	* @param downloadRate the downloadRate to set
	*/
	public synchronized void setDownloadRate(int downloadRate) {
		this._downloadRate = downloadRate;
	}
    /**
     * @param ois
     * @return AbstractMessage
     * 
     * Used to read Message from other peers.
     */
	public AbstractMessage read(ObjectInputStream ois){
		AbstractMessage aM =null;
		try {
			aM  = (AbstractMessage) ois.readObject();
			return aM;
		} catch (IOException e) {

			logger.debug("Read Exception",e);
		} catch (ClassNotFoundException e) {

		    logger.debug("Read Exception",e);
		}
		return aM;
	}
	
	/**
	 * @param oos
	 * @param aM
	 * Used to write Messages to hosts
	 */
	public void write(ObjectOutputStream oos, AbstractMessage aM){
		try {
			oos.writeObject(aM);
			oos.flush();
			} 
		catch (IOException e) {
			logger.debug("Write Exception",e);
		}
	}
	
	/**
	 * This is a dummy method to test the proper functioning of 
	 * private methods. This is called from TestRunner.
	 * The method to be tested is called in this method.
	 */
	public void test(){
		 loadConfiguration();
		 initialize();

		
		
	}
    public static void main(String[] args){
    	

    	PropertyConfigurator.configure("log4j.properties");
    	/* Configuring log file */
    	SimpleLayout layout = new SimpleLayout();
    	FileAppender appender;
		try {
			appender = new FileAppender(layout,"log_peer_"+args[0]+".log",false);
			appender.activateOptions();
		    logger.addAppender(appender);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
        peerProcess peerprocess = peerProcess.getInstance(args[0]);
    	peerprocess.test();
    	
    	
    }
}

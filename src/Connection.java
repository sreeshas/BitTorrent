import java.net.Socket;

/**
 * Data Class to represent Connection of Peer with other Peers
 * @author sreenidhi
 *
 */
public class Connection {
    /*
     * Represents uploadRate of Peer 
     * associate with the Connection
     */
	private int _uploadRate;
	
	/*
	 * Represents the downloadRate of 
	 * Peer associated with the Connection
	 */
	private int _downloadRate;
	
	/*
	 * The Socket through which other Peers
	 * can communicate.
	 */
	private Socket _peerSocket;
}

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Data Class to represent Connection of Peer with other Peers
 * @author sreenidhi
 *
 */
public final class Connection {
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Connection [_peerID=" + _peerID + "]";
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_peerID == null) ? 0 : _peerID.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Connection other = (Connection) obj;
		if (_peerID == null) {
			if (other._peerID != null)
				return false;
		} else if (!_peerID.equals(other._peerID))
			return false;
		return true;
	}
	/*
	 * _ID of the peer to whom this connection belongs.
	 */
	private final String _peerID;
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
	
	/*
	 * To identify if the Peer represented by this Connection
	 * is choked or not
	 */
	private Boolean choked;
	/*
	 * Represents Host Address of the Connection.
	 */
	private final String _host;
	/*
	 * Listening port of the Host.
	 */
	private  final int  _port;
	/*
	 * Indicates whether this Connection has complete File or not.
	 */
	private Boolean _hasCompleteFile;
	
	/*
	 * To Read Messages sent from host representing this connection.
	 */
	private ObjectInputStream _ois;
	
	/*
	 * To Write Messages to the host represented by this connection.
	 */
	private ObjectOutputStream _oos;
	
	/*
	 * Represents if this connection is interested in receiving pieces 
	 * from this Peer.
	 */
	private Boolean interested;
	
	/**
	 * @return the interested
	 */
	synchronized Boolean getInterested() {
		return interested;
	}
	/**
	 * @param interested the interested to set
	 */
	synchronized void setInterested(Boolean interested) {
		this.interested = interested;
	}
	public Connection(String _peerID, String _host, int _port, Boolean _hasCompleteFile) {
		this._peerID = _peerID;
		this._host = _host;
		this._port = _port;
		this._hasCompleteFile = _hasCompleteFile;
		this._peerSocket = null;
	}
	/**
	 * @return the _peerID
	 */
	public String get_peerID() {
		return _peerID;
	}
	
	/**
	 * @return the _uploadRate
	 */
	public int get_uploadRate() {
		return _uploadRate;
	}
	/**
	 * @param uploadRate the _uploadRate to set
	 */
	public void set_uploadRate(int uploadRate) {
		_uploadRate = uploadRate;
	}
	/**
	 * @return the _downloadRate
	 */
	public int get_downloadRate() {
		return _downloadRate;
	}
	/**
	 * @param downloadRate the _downloadRate to set
	 */
	public void set_downloadRate(int downloadRate) {
		_downloadRate = downloadRate;
	}
	/**
	 * @return the _peerSocket
	 */
	public Socket get_peerSocket() {
		return _peerSocket;
	}
	/**
	 * @param peerSocket the _peerSocket to set
	 */
	public void set_peerSocket(Socket peerSocket) {
		_peerSocket = peerSocket;
	}
	/**
	 * @return the choked
	 */
	public Boolean getChoked() {
		return choked;
	}
	/**
	 * @param choked the choked to set
	 */
	public void setChoked(Boolean choked) {
		this.choked = choked;
	}
	/**
	 * @return the _host
	 */
	public String get_host() {
		return _host;
	}
	
	/**
	 * @return the _port
	 */
	public int get_port() {
		return _port;
	}
	
	/**
	 * @return the _hasCompleteFile
	 */
	public Boolean get_hasCompleteFile() {
		return _hasCompleteFile;
	}
	/**
	 * @param hasCompleteFile the _hasCompleteFile to set
	 */
	public void set_hasCompleteFile(Boolean hasCompleteFile) {
		_hasCompleteFile = hasCompleteFile;
	}
	/**
	 * @return the _ois
	 */
	 synchronized ObjectInputStream get_ois() {
		return _ois;
	}
	/**
	 * @param _ois the _ois to set
	 */
	 synchronized void set_ois(ObjectInputStream _ois) {
		this._ois = _ois;
	}
	/**
	 * @return the _oos
	 */
	 synchronized ObjectOutputStream get_oos() {
		return _oos;
	}
	/**
	 * @param _oos the _oos to set
	 */
	synchronized void set_oos(ObjectOutputStream _oos) {
		this._oos = _oos;
	}
	
	
	
}

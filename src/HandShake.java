import java.io.Serializable;

/**
 * Represents HandShake Message used by Peer right after
 * connection is established.
 * @author sreenidhi
 *
 */
public class HandShake implements Serializable{
    
	
	private String _header=Constants.HANDSHAKE_HEADER;
	private int _peerID;
	private byte[] zeroBits = new byte[10];
	
	public HandShake(int _peerID) {
		this._peerID = _peerID;
	}

	/**
	 * @return the _header
	 */
	public String get_header() {
		return _header;
	}

	public int get_peerID() {
		return _peerID;
	}
}

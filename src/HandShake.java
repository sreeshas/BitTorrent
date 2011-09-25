/**
 * Represents HandShake Message used by Peer right after
 * connection is established.
 * @author sreenidhi
 *
 */
public class HandShake {
    
	private String _header=Constants.HANDSHAKE_HEADER;
	private int _peerID;
	private byte[] zeroBits = new byte[10];
}

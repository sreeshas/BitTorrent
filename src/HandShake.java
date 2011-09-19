/**
 * Represents HandShake Message used by Peer right after
 * connection is established.
 * @author sreenidhi
 *
 */
public class HandShake extends AbstractMessage{
    
	private String _header=Constants.HANDSHAKE_HEADER;
	private int _peerID;
}

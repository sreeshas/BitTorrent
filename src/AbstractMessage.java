import java.io.Serializable;

/*
 * Represents the Abstract Message Class 
 * which provides the generic structure for
 * Messages exchanged between Peers
 */
public abstract class AbstractMessage implements Serializable{
    /**
     *  Represents length of the Message.
     */
	public int length;
	/**
	 *  Represents type of Message.
	 */
	public int type;
	
	AbstractMessage(int type){
		this.type = type;
	}
	
	
}

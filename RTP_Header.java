import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/*
 * 
 */
public class RTP_Header {

	private int source_ip;
	private int dest_ip;
	private int port_numbers;
	private int ack_number;
	private int sync_flags_window;
	private int checksum;

	public int[] header;
	
	public RTP_Header() {
		header = new int[6];
		for(int i = 0; i < 6; i++){
			header[i] = 0;
		}
	}
	
	public RTP_Header( int source_ip, 
		int dest_ip, 
		int source_port, 
		int dest_port, 
		int ack_number, 
		int sync,
		int flags,
		int window_size ) {
		header = new int[6];
		header[0] = source_ip;
		header[1] = dest_ip;
		header[2] = packPortNumbers(source_port, dest_port);
		header[3] = 0; //create packet number? where should that go?
		header[4] = createSyncFlagsWindowLine(sync, flags, window_size);
		header[5] = 0; //checksum filled in by another class.
	}
	
	public byte[] asByteArray(){
		ByteBuffer byteBuffer = ByteBuffer.allocate(header.length * 4);        
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(header);

        return byteBuffer.array();
	}

	private int packPortNumbers(int source, int dest){
		if (source > 32768 || dest > 32768) {
			return -1; //will not fit into a 16 bit slot;
		}
		else {
			dest = dest << 16;
			return (dest | source);
		}
	}
	
	public boolean setDestinationPort(int dest){
		if (dest > 32768) {
			return false; //will not fit into a 16 bit slot;
		}
		else {
			dest = dest << 16;
			header[2] = (dest | header[2]);
			return true;
		}
	}
	
	public boolean setSourcePort(int source){
		if (source > 32768) {
			return false; //will not fit into a 16 bit slot;
		}
		else {
			header[2] = ( header[2] | source);
			return true;
		}
	}
	
	
	

	/*
	 * I'm setting up some basic functionality in this class which I fully expect to get refactored
	 * and farmed out to other places for cleanliness
	 */
	private int createSyncFlagsWindowLine(int sync, int flags, int window_size) {
		if (sync > 1 || sync < 0 || flags > 1 || flags < 0 || window_size > 268435456 || window_size < 0) {
			return -1; //one of those is out of bounds of what will fit in the final int
		}
		else {
			if(sync != 0) {
				setSyncOn();
			}
			else {
				setSyncOff();
			}
			if(flags == 0) {
				setAckFlag(true);
			}
			else {
				setDataFlag(true);
			}
			if(flags > 1) {
				setConnectionFlag(true);
			}
			setWindowSize(window_size);
			return sync_flags_window;
		}
	}
	
	public void setSyncOn() {
		sync_flags_window = set(sync_flags_window, 0);
		
	}
	
	public void setSyncOff() {
		sync_flags_window = clear(sync_flags_window, 0);
	}
	
	
	/*
	 * The data flag is bit 1 of sync_flags_window set to 1.
	 */
	public void setDataFlag(boolean bool) {
		if(bool == true) {
			sync_flags_window = set(sync_flags_window, 1);
		}
		else {
			sync_flags_window = clear(sync_flags_window, 1);
		}
	}
	
	
	/*
	 * The ack flag is bit 1 of sync_flags_window set to 0.
	 */
	public void setAckFlag(boolean bool) {
		if(bool == true) {
			sync_flags_window = clear(sync_flags_window, 1);
		}
		else {
			sync_flags_window = set(sync_flags_window, 1);
		}
	}

	
	public void setConnectionFlag(boolean bool) {
		if(bool == true) {
			sync_flags_window = set(sync_flags_window, 2);
		}
		else {
			sync_flags_window = clear(sync_flags_window, 1);
		}
	}
	
	
	public boolean setWindowSize(int window_size) {
		if( window_size >  0x1FFFFFFF) {
			return false; //will not fit
		}
		window_size = window_size << 3;
		sync_flags_window = sync_flags_window | window_size;
		return true;
	}
	
	
	public void setSequenceNumber(int seqnum){
		header[3] = seqnum;
	}
	
	public void setSourceIP(int source){
		header[0] = source;
	}
	
	public void setDestIP(int dest) {
		header[1] = dest;
	}

	/*---------------------GETTERS---------------------------*/

	public int getSourceIP() {
		return header[0];
	}

	public int getDestIP() {
		return header[1];
	}

	public int getSourcePort() {
		int source_p = header[2];
		source_p = source_p ^ 0xFFFF;
		source_p = source_p >> 16;
		return source_p;
	}

	public int getDestPort() {
		int dest_p = header[2];
		dest_p = dest_p ^ 0xFFFF0000;
		return dest_p;
	}

	//ack number is same as packet number
	public int getPacketNumber() {
		return header[3];
	}

	public int getWindowSize() {
		int window = header[4];
		window = window ^ 0x7;
		return (window >> 3);
	}

	public boolean isData() {
		return isSet(header[4], 1);
	}

	public boolean isACK() {
		return (!isSet(header[4], 1));
	}

	public boolean isSync() {
		return isSet(header[4], 0);
	}

	public boolean isConnection() {
		return isSet(header[4], 2);
	}

	public int getChecksum() {
		return header[5];
	}

	public int[] getHeader() {
		return header;
	}



	/**
	  * Sets the bit (sets to 1) pointed to by index.
	  * @param index index of which bit to set.
	  *        0 for the least significant bit (right most bit).
	  *        31 for the most significant bit.
	  */
	private int set(int bits, int index)
	{
	    //Ex: bits = 1000, index = 1, mask = 0010
		//Existing zeroes fall through OR, mask adds 1
		int mask = 1;
		mask = mask << index;

		bits = bits | (mask);
		return bits;
	}

	/**
	* Clears the bit (sets to 0) pointed to by index.
	* @param index index of which bit to set.
	*        0 for the least significant bit (right most bit).
	*        31 for the most significant bit.
	*/
	private int clear(int bits, int index)
	{
	//Ex: bits = 1010, index = 1, mask = 0010
	//All other 1's fall through XOR, excepted masked bit
	//1010 XOR 0010 = 1000 
		int mask = 1;
		mask = mask << index;

		bits = bits ^ (mask);
		return bits;
	}

	 /**
	  * Returns true if the bit pointed to by index is currently set.
	  * @param index index of which bit to check.  
	  *        0 for the least significant bit (right-most bit).
	  *        31 for the most significant bit.
	  * @return true if the bit is set, false if the bit is clear.
	  *         If the index is out of range (index >= 32), then return false.
	  */
	public boolean isSet(int bits, int index)
	{
		if( index >= 32)
		{
			return false;
		}
		int mask = 1;
		mask = mask << index;
		bits = bits & mask;

    //if the bit is turned on, the whole number will NOT equal 0
		return (bits != 0);
	}


}
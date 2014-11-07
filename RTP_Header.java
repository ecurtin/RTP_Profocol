/*
 * 
 */
public class RTP_Header {
	public int source_ip;
	public int dest_ip;
	public int port_numbers;
	public int ack_number;
	public int sync_flags_window;
	public int checksum; 
	
	public RTP_Header() {
		source_ip = 0;
		dest_ip = 0;
		port_numbers = 0;
		ack_number = 0;
		sync_flags_window = 0;
		checksum = 0;
	}
	
	public RTP_Header( int source_ip, 
					   int dest_ip, 
					   int source_port, 
					   int dest_port, 
					   int ack_number, 
					   int sync,
					   int flags,
					   int window_size ) {
		
	}
	
	public void setSyncOn() {
		
	}
	
	public void setSyncOff() {
		
	}
	
	public void setDataFlag(boolean bool) {
		
	}
	
	public void setAckFlag(boolean bool) {
		
	}
	
	public void setWindowSize(int window_size) {
		
	}
	
	
}

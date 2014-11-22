import java.net.DatagramPacket;

public class Packet {

	private int[] rpt_packet;
	//private int[] header = new int[6]; //this'll integrate somehow with RTPHeader, not sure how yet
	public RTP_Header header;
	private int[] rtp_data;

	//RECEIVING END, you don't know what type of packet it is yet
	public Packet(DatagramPacket datagram) {
		processDatagram(datagram);
	}

	//SENDING SIDE. empty for now. I think sending should instantiate 
	//the type of packet that it needs
	public Packet() {

	}

	//Upon receipt
	public void processDatagram(DatagramPacket datagram){
		// get datagram data, use ByteBufer to to asInt or whatever



		// split the new Int buffer into header (frst 6 ints) 
		// and data(all the rest)
	}

	// This should probably be implemented by each class individually
	public DatagramPacket makeDatagramPacket() {
		return null;
	}


	/*------------------------GETTERS & SETTERS------------------------*/

	/*
		returns the int[] inside of RTP_Header
	*/
	public int[] getHeader() {
		return header.getHeader();
	}

	//
	public int[] getData() {
		return rtp_data;
	}

	public boolean isData() {
		if( header.isData() ){
			return true;
		}
		return false;
	}

	public boolean isConnection() {
		if( header.isSync() && header.isConnection()) {
			return true;
		}
		return false;
	}

	public boolean isACK() {
		if( header.isACK() ) {
			return true;
		}
		return false;
	}

	public boolean isDisconnection() {
		if( !header.isSync() && header.isConnection()) {
			return true;
		}
		return false;
	}
}

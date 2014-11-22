import java.net.DatagramPacket;

public class Packet {

	private int[] rpt_packet;
	private int[] header = new int[6]; //this'll integrate somehow with RTPHeader, not sure how yet
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

	//return the int header in full
	public int[] getHeader() {
		return header;
	}

	//
	public int[] getData() {
		return rtp_data;
	}

	public boolean isConnectionPacket() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isACKPacket() {
		// TODO Auto-generated method stub
		return false;
	}

	public int getACKNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

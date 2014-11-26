import java.net.DatagramPacket;
import java.net.InetAddress;

public class Packet {

	protected byte[] rtp_packet;
	//private int[] header = new int[6]; //this'll integrate somehow with RTPHeader, not sure how yet
	public RTP_Header header;
	protected byte[] rtp_data;
	protected InetAddress destinationInetAddress;

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
		byte [] byteArray = datagram.getData();
		
		byte[] headerAsBytes = new byte[header.header.length * 4];
		for(int i = 0; i < headerAsBytes.length; i++) {
			headerAsBytes[i] = byteArray[i];
		}
		header = new RTP_Header(headerAsBytes);
		
		//TODO: funnel all the other bytes, if any, into file reconstruction.
		// split the new Int buffer into header (frst 6 ints) 
		// and data(all the rest)
	}

	public DatagramPacket packInUDP() {
		byte[] headerInBytes = header.asByteArray();
	
		return new DatagramPacket(headerInBytes,
									0,
									rtp_packet.length, 
									this.destinationInetAddress, 
									this.header.getDestPort());
	}

	/*------------------------GETTERS & SETTERS------------------------*/



	/*
		returns the int[] inside of RTP_Header
	*/
	public int[] getHeader() {
		return header.getHeader();
	}

	//
	public byte[] getData() {
		return rtp_data;
	}
	
	public int getACK() {
		return header.getPacketNumber();
	}
	
	public int getSeqNumber() {
		return header.getPacketNumber();
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
	
	public void setSeqNumber(int i) {
		header.setSequenceNumber(i);
	}

	public void setSourceIPAddress(InetAddress sourceAddress) {
		header.setSourceIP(inetAddressToInt( sourceAddress ));
		
	}

	public void setSourcePort(int sourcePort) {
		header.setSourcePort(sourcePort);
	}

	public void setDestinationIPAddress(InetAddress destinationAddress) {
		destinationInetAddress = destinationAddress;
		header.setDestIP(inetAddressToInt( destinationAddress ));
		
	}

	public void setDestinationPort(int destinationPort) {
		header.setDestinationPort(destinationPort);
	}

	private int packBytesIntoInt(byte[] bytes) {
		int ret = 0;
		for (int i = 0; i < bytes.length; i++) {
		    ret <<= 8;
		    ret |= bytes[i] & 0xff;
		}
		return ret;
		
	}
	
	private int inetAddressToInt(InetAddress inet){
		return packBytesIntoInt( inet.getAddress() );
	}

	public void setWindowSize(int windowSize) {
		if(header.setWindowSize(windowSize)) {
			return;
		}
		return; //not catching the "window size too big" at the moment
		
	}
	
	public int computeChecksum() {
		return 0;
	}
	
	public boolean validateChecksum() {
		return true;
	}


	
}

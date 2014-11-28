package shared;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.zip.Adler32;

public class Packet {
	
	protected byte[] rtp_packet;
	//private int[] header = new int[6]; //this'll integrate somehow with RTPHeader, not sure how yet
	public RTP_Header header = new RTP_Header();
	protected byte[] rtp_data;
	protected InetAddress destinationInetAddress;
	protected Adler32 checksumValidator = new Adler32();

	//RECEIVING END, you don't know what type of packet it is yet
	public Packet(DatagramPacket datagram) {
		processDatagram(datagram);
	}

	//SENDING SIDE. empty for now. I think sending should instantiate 
	//the type of packet that it needs
	public Packet() {
		header = new RTP_Header();
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
		
		if (byteArray.length > headerAsBytes.length) {
			rtp_data = new byte[getDataSize()];
			
			for(int i = headerAsBytes.length; i < (headerAsBytes.length + rtp_data.length); i++) {
				this.rtp_data[i - headerAsBytes.length] = byteArray[i];
			}
		}
	}
	
	public void makeRTPPacket() {
		if( this.rtp_data == null ) {
			setDataSize(0);
			rtp_packet = header.asByteArray();
		}
		else {
			setDataSize(rtp_data.length);
			byte[] headerInBytes = header.asByteArray();

			rtp_packet = new byte[headerInBytes.length + this.rtp_data.length];
			
			for(int i = 0; i < headerInBytes.length; i++) {
				rtp_packet[i] = headerInBytes[i];
			}
			
			for(int i = 0; i < rtp_data.length; i++) {
				rtp_packet[i + headerInBytes.length] = rtp_data[i];
			}
		}
	}

	public DatagramPacket packInUDP() {
		setChecksum(computeChecksum());
		makeRTPPacket();
		return new DatagramPacket(this.rtp_packet,
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
	
	public int getDataSize() {
		return header.getDataSize();
	}
	
	public void setDataSize(int value) {
		header.setDataSize(value);
	}
	
	public int getACK() {
		return header.getPacketNumber();
	}
	
	public int getSeqNumber() {
		return header.getPacketNumber();
	}
	
	public int getWindowSize() {
		return header.getWindowSize();
	}

	public boolean isData() {
		if( header.isData() ){
			return true;
		}
		return false;
	}

	public boolean isConnection() {
		if( header.isSync() && header.isConnection() && !header.isACK()) {
			return true;
		}
		return false;
	}

	public boolean isACK() {
		if( header.isACK()) {
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
	
	public void setData(byte[] bytes) {
		this.rtp_data = bytes;
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
		makeRTPPacket();
		checksumValidator.reset();
		if(rtp_packet != null) {
			checksumValidator.update(rtp_packet, 4, (rtp_packet.length - 4));
		}
		
		//checksum returns a long, mod by int max value to make it fit into an int
		//this isn't part of the algorithm, but it is repeatable and should not effect
		//validation
		int checksumAsInt = (int) checksumValidator.getValue() % Integer.MAX_VALUE;
		
		return checksumAsInt;
		
		//return 0;
	}
	
	public void setChecksum(int value) {
		header.setChecksum(value);
		makeRTPPacket();
	}
	
	public int getChecksum() {
		return header.getChecksum();
	}

	public boolean validateChecksum() {
/*		checksumValidator.reset();
		// TODO: Figure out how to get the byte array of the received packet without it's checksum
		checksumValidator.update();
		long packetsCurrentChecksum = checksumValidator.getValue();
		checksumValidator.reset();
		
		if (packetsCurrentChecksum == header.getChecksum()) {
			return true;
		}
		return false;*/
		
		makeRTPPacket();
		int computedChecksum = computeChecksum();
		
		if(computedChecksum != header.getChecksum()) {
			return false;
		}
		else {
			return true;
		}
		
		//return true;
	}
	
	public void setFileName(String filename) {
		byte[] stringAsBytes = filename.getBytes();
		setData(stringAsBytes);
	}
	
	public String getFileName() {
		return new String(this.getData());
	}



	
}

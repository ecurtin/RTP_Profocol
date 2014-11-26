import java.net.DatagramPacket;

public class DataPacket extends Packet {
	
	public DataPacket(byte[] data) {
		header = new RTP_Header();
		header.setSyncOn();
		header.setConnectionFlag(false);
		header.setDataFlag(true);
		
		this.rtp_data = data;
		
	}
	
	private void makeRTPPacket() {
		byte[] headerInBytes = header.asByteArray();
		rtp_packet = new byte[headerInBytes.length + this.rtp_data.length];
		
		for(int i = 0; i < headerInBytes.length; i++) {
			rtp_packet[i] = headerInBytes[i];
		}
		
		for(int i = 0; i < headerInBytes.length; i++) {
			rtp_packet[i + headerInBytes.length] = rtp_data[i];
		}
	}
	
	//from oracle docs: DatagramPacket(byte[] buf, int offset, int length, InetAddress address, int port)
	public DatagramPacket packInUDP() {
		makeRTPPacket();
		return new DatagramPacket(this.rtp_packet,
									0,
									rtp_packet.length, 
									this.destinationInetAddress, 
									this.header.getDestPort());
	}
	
	

}

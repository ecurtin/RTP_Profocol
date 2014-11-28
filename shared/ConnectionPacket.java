package shared;

public class ConnectionPacket extends Packet {

	public ConnectionPacket() {
		header = new RTP_Header();
		header.setSyncOn();
		header.setConnectionFlag(true);
		header.setSequenceNumber(-1);
		header.setAckFlag(false);
	}
	
//	public void makeConnectionACK() {
//		header.setAckFlag(true);
//	}
	
//	public DatagramPacket makeDatagramPacket() {
//		return null;
//	}
	

}

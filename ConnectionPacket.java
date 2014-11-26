import java.net.DatagramPacket;

public class ConnectionPacket extends Packet {

	public ConnectionPacket() {
		header.setSyncOn();
		header.setConnectionFlag(true);
		header.setSequenceNumber(-1);
	}
	
	public void makeConnectionACK() {
		header.setAckFlag(true);
	}
	
	public DatagramPacket makeDatagramPacket() {
		return null;
	}
	

}

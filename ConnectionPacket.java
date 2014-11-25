import java.net.DatagramPacket;

public class ConnectionPacket extends Packet {

	public ConnectionPacket() {
		header.setSyncOn();
		header.setConnectionFlag(true);
	}
	
	public DatagramPacket makeDatagramPacket() {
		return null;
	}

}

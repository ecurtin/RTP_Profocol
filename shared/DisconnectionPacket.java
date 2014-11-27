package shared;
import java.net.DatagramPacket;

public class DisconnectionPacket extends Packet {

	public DisconnectionPacket() {
		header = new RTP_Header();
		header.setSyncOff();
		header.setConnectionFlag(true);
	}
	
//	public DatagramPacket makeDatagramPacket() {
//		return null;
//	}

}

import java.net.DatagramPacket;
import java.net.InetAddress;


public class DataPacket extends Packet {

	public DataPacket(byte[] data) {
		this.rtp_data = data;
		
	}


}

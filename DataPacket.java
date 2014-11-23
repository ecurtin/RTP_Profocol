import java.net.DatagramPacket;
import java.net.InetAddress;


public class DataPacket extends Packet {

	public DataPacket(byte[] data) {
		header.setSyncOn();
		header.setConnectionFlag(false);
		header.setDataFlag(true);
		
		this.rtp_data = data;
		
	}
	
	
	public DatagramPacket makeDatagramPacket() {
		DatagramPacket retPacket = new DatagramPacket()
	}


}

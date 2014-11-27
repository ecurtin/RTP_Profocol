package shared;
import java.net.DatagramSocket;


public class PacketReceiver {
	protected PacketCreator packetCreator;
	protected DatagramSocket socket;
	protected final int PACKET_SIZE = 1024;
	
	public PacketReceiver() {

	}

}

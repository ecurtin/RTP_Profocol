import java.io.File;
import java.net.DatagramPacket;
import java.util.Queue;


/**
 * PacketCreator is responsible for creating all packets sent.
 *
 */
public class PacketCreator {
	
	public PacketCreator() {
	}
	
	public Queue<DatagramPacket> createFilePackets(File file) {
		// Need to implement packet creation here
		Queue<DatagramPacket> packetsQueue = null;
		return packetsQueue;
	}
	
	public DatagramPacket[] createConnectPackets(String IPAddress, int windowSize) {
		//TODO: Implement packet connection creation
		return null;
	}
	
	public DatagramPacket[] createDisconnectPackets() {
		//TODO: Implement packet disconnection creation
		return null;
	}
	
	public DatagramPacket[] createRequestFilePackets(String filename) {
		//TODO: Implement request file packet creation
		return null;
	}
}

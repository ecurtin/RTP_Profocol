import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;


/**
 * PacketCreator is responsible for creating all packets sent.
 *
 */
public class PacketCreator {
	private File file;
	private PacketSender packetSender;
	
	public PacketCreator(DatagramSocket socket, int windowSize) throws SocketException {
		this.packetSender = new PacketSender(socket, windowSize);
	}
	
	public void sendFile() {
		// Create packets and put them in a queue to be sent
		Queue<DatagramPacket> packetsQueue = (Queue<DatagramPacket>) createFilePackets(file);
		
		while (!packetsQueue.isEmpty()) {
			// While we still have packets to send...
			while(sendMorePackets) {
				DatagramPacket[] packets = null;
				int numberOfPacketsToSend;
				int queueSize = packetsQueue.size();
				
				if (queueSize > windowSize) {
					numberOfPacketsToSend = windowSize;
				} else {
					numberOfPacketsToSend = queueSize;
				}
				
				for(int i = 0; i < numberOfPacketsToSend; i++) {
					packets[i] = packetsQueue.remove();
				};
				
				packetSender.sendPackets(packets);
			};
		};
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

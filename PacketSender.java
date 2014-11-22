import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class PacketSender {
	private DatagramSocket socket;
	private int windowSize;
	private DatagramPacket[] currentPackets;
	
	public PacketSender(DatagramSocket socket, int windowSize) throws SocketException {
		this.socket = socket;
		this.windowSize = windowSize;
	}
	
	public void sendPackets(DatagramPacket[] packets) throws IOException {
		currentPackets = packets;
		for (int i = 0; i < packets.length; i++) {
			socket.send(currentPackets[i]);
		};
	}
	
	/**
	 * Retransmit nonACK'd packets
	 */
	public void retransmitPackets() {
		
	}
}

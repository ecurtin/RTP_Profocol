import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

public class PacketSender {
	private DatagramSocket socket;
	private DatagramPacket[] currentPackets;
	
	public PacketSender(DatagramSocket socket) throws SocketException {
		this.socket = socket;
	}
	
	public void sendPacket(DatagramPacket packetToBeSent) throws IOException {
		socket.send(packetToBeSent);
	}
	
	public void sendPackets(DatagramPacket[] packets) throws IOException {
		currentPackets = packets;
		for (int i = 0; i < packets.length; i++) {
			socket.send(currentPackets[i]);
		};
	}

	public void setWindowSize(int windowSize) {
		// TODO Auto-generated method stub
		
	}
}

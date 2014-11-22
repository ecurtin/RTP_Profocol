import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Queue;

public class PacketSender {
	private DatagramSocket socket;
	private int windowSize;
	private DatagramPacket[] currentPackets;
	
	public PacketSender(DatagramSocket socket) throws SocketException {
		this.socket = socket;
	}
	
	public void sendPacket(DatagramPacket packetToBeSent) throws IOException {
		socket.send(packetToBeSent);
	}
	
	public void receiveAllDataToSend(Queue<DatagramPacket> packetsQueue) {
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

	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
		
	}
}

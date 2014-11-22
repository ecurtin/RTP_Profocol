import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Queue;


/**
 * PacketCreator is responsible for creating all packets sent.
 *
 */
public class PacketCreator {
	private File file;
	private PacketSender packetSender;
	private InetAddress sendingAddress;
	private int windowSize;
	private int sendingPort;
	private boolean isConnected = false;
	
	public PacketCreator(DatagramSocket socket) throws SocketException {
		this.packetSender = new PacketSender(socket);
	}
	
	public void sendConnectionPacket() {
		// TODO: Create ConnectionPacket class
		Packet connectionPacket = new ConnectionPacket();	
		connectionPacket.setIPAddress(sendingAddress);
		connectionPacket.setPort(sendingPort);
		DatagramPacket sendingPacket = connectionPacket.packInUDP();
		packetSender.sendPacket(sendingPacket);
	}
	
	public DatagramPacket[] createConnectPacket() {
		//TODO: Implement packet connection creation
		return null;
	}
	
	public void sendFile() {
		// Create packets and put them in a queue to be sent
		Queue<DatagramPacket> packetsQueue = (Queue<DatagramPacket>) createFilePackets(file);
		
	}
	
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
		packetSender.setWindowSize(windowSize);
	}
	
	public Queue<DatagramPacket> createFilePackets(File file) {
		// Need to implement packet creation here
		Queue<DatagramPacket> packetsQueue = null;
		return packetsQueue;
	}
	
	
	public DatagramPacket[] createDisconnectPackets() {
		//TODO: Implement packet disconnection creation
		return null;
	}
	
	public DatagramPacket[] createRequestFilePackets(String filename) {
		//TODO: Implement request file packet creation
		return null;
	}

	public void setAddress(InetAddress ipAddress) {
		this.sendingAddress = ipAddress;
		
	}

	public void receiveACK(int ackNumber) {
		// TODO Auto-generated method stub
		
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setPort(int port) {
		this.sendingPort = port;
		
	}
}

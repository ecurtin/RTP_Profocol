import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.Queue;


/**
 * PacketCreator is responsible for creating all packets sent.
 *
 */
public class PacketCreator {
	
	private int maxDataSizePerPacketInBytes = 832;
	
	private PacketSender packetSender;
	
	private String fileName;
	private byte[] fileData = new byte[maxDataSizePerPacketInBytes];
	private final int INITIALACK = -1;
	private int currentSeqNumber = -1;

	private InetAddress destinationAddress;
	private InetAddress sourceAddress;
	private int windowSize;
	private int destinationPort;
	private int sourcePort;
	
	private boolean isConnected = false;
	private Hashtable<Integer, DatagramPacket> sentPacketStore 
		= new Hashtable<Integer, DatagramPacket>();
	
	public PacketCreator(DatagramSocket socket, int sourcePort, InetAddress sourceAddress) throws SocketException {
		this.packetSender = new PacketSender(socket);
		this.sourcePort = sourcePort;
		this.sourceAddress = sourceAddress;
	}
	
	/**
	 * Creates and sends connection ACK
	 * @throws IOException 
	 */
	public void sendConnectionPacket() throws IOException {
		DatagramPacket sendingPacket = createConnectPacket();
		sentPacketStore.put(INITIALACK, sendingPacket);
		packetSender.sendPacket(sendingPacket);
	}
	
	// Creates connection packet
	private DatagramPacket createConnectPacket() {
		Packet connectionPacket = new ConnectionPacket();	
		connectionPacket.setDestinationIPAddress(destinationAddress);
		connectionPacket.setDestinationPort(destinationPort);
		DatagramPacket sendingPacket = connectionPacket.packInUDP();
		return sendingPacket;
	}
	
	public void receiveACK(int ackNumber) throws FileNotFoundException {
		// Check for packet that we received ACK for
		if (storageContainsPacket(ackNumber)) {
			removePacketFromStorage(ackNumber);

			// Begin sending file if ACK was for connection
			if (isConnectionACK(ackNumber)) {
				isConnected = true;
				sendFile();
			}
		}
	}
	
	private boolean storageContainsPacket(Integer key) {
		return sentPacketStore.containsKey(key);
	}
	
	private void removePacketFromStorage(Integer key) {
		sentPacketStore.remove(key);
	}
	
	private boolean isConnectionACK(int ackNumber) {
		return ackNumber == -1;
	}
	
	private void sendFile() throws FileNotFoundException {
		Queue<DatagramPacket> packetsQueue = (Queue<DatagramPacket>) createFilePackets();
		
		// TODO: THREAD THIS
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
		// Join back the thread
	}
	
	private Queue<DatagramPacket> createFilePackets() throws FileNotFoundException {
		FileInputStream fileStream = new FileInputStream(fileName);
		Queue<DatagramPacket> packetsQueue = null;
		
		while (moreFileDataToPacketize(fileStream)) {
			// Store as much data from the file as you can into fileData
			fileStream.read(fileData);
			DataPacket dataPacket = new DataPacket(fileData);
			
			dataPacket.setSeqNumber(currentSeqNumber++);
			dataPacket.setSourceIPAddress(sourceAddress);
			dataPacket.setSourcePort(sourcePort);
			dataPacket.setDestinationIPAddress(destinationAddress);
			dataPacket.setDestinationPort(destinationPort);
			DatagramPacket sendPacket = dataPacket.packInUDP();
			
			packetsQueue.add(sendPacket);
		}
		return packetsQueue;
	}
	
	private boolean moreFileDataToPacketize(FileInputStream fileStream) throws IOException {
		return fileStream.available() != 0;
	}
	
	
	public DatagramPacket[] createDisconnectPackets() {
		//TODO: Implement packet disconnection creation
		return null;
	}

	public void setDestinationAddress(InetAddress destinationAddress) {
		this.destinationAddress = destinationAddress;
		
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
		packetSender.setWindowSize(windowSize);
	}

	public void setDestinationPort(int port) {
		this.destinationPort = port;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
}

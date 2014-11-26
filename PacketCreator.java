import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


/**
 * PacketCreator is responsible for creating all packets sent.
 *
 */
public class PacketCreator {
	
	private int maxDataSizePerPacketInBytes = 832;
	
	private PacketSender packetSender;
	
	private String fileName;
	private FileInputStream fileStream;
	private byte[] fileData = new byte[maxDataSizePerPacketInBytes];
	private final int INITIAL_ACK = -1;
	private int currentSeqNumber = 0;
	private final int TIMEOUT_SIZE = 250; // ms

	private InetAddress destinationAddress;
	private InetAddress sourceAddress;
	private int windowSize;
	private int destinationPort;
	private int sourcePort;
	
	private Timer timeout = new Timer();
	private boolean moreDataPackets = true;
	private boolean disconnected = false;
	
	private boolean isConnected = false;
	private ConcurrentHashMap<Integer, DatagramPacket> sentPacketStore 
		= new ConcurrentHashMap<Integer, DatagramPacket>();
	
	private ArrayList<Integer> timeoutPackets = new ArrayList<Integer>();
	
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
		sentPacketStore.put(INITIAL_ACK, sendingPacket);
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
	
	/**
	 * Deals with all incoming ACKs, removing the associated buffered packages 
	 * from the store.
	 * @param ackNumber
	 * @throws IOException 
	 */
	public void receiveACK(int ackNumber) throws IOException {
		// Check for packet that we received ACK for
		if (storageContainsPacket(ackNumber)) {
			removePacketFromStorage(ackNumber);

			// Begin sending file if ACK was for connection
			if (isConnectionACK(ackNumber)) {
				isConnected = true;
				sendWindowOfPackets();
			}
		}
	}
	
	private boolean storageContainsPacket(Integer key) {
		return sentPacketStore.containsKey(key);
	}
	
	private void removePacketFromStorage(Integer key) {
		sentPacketStore.remove(key);
	}
	
	private DatagramPacket getPacketFromStorage(Integer key) {
		return sentPacketStore.get(key);
	}
	
	private boolean isConnectionACK(int ackNumber) {
		return ackNumber == -1;
	}
	
	// Create all file packets and begin to send them a window size at a time to the receiver.
	private void sendWindowOfPackets() throws IOException {
		Queue<DatagramPacket> packetsQueue = (Queue<DatagramPacket>) createFilePackets();
		int sizeOfQueue = packetsQueue.size();
		DatagramPacket[] packetsToBeSent = new DatagramPacket[sizeOfQueue];
		
		for(int i = 0; i < sizeOfQueue; i++) {
			packetsToBeSent[i] = packetsQueue.remove();
		};
		
		packetSender.sendPackets(packetsToBeSent);
		timeout.schedule(new TimeoutTask(), TIMEOUT_SIZE);
	}
	
	// Creates a window size of file packets and places them in the returning queue
	private Queue<DatagramPacket> createFilePackets() {
		Queue<DatagramPacket> packetsQueue = null;
		int numberOfPacketsCreated = 0;
		
		while (moreFileDataToPacketize(fileStream) && numberOfPacketsCreated != windowSize) {
			// Store as much data from the file as you can into fileData
			fileStream.read(fileData);
			DataPacket dataPacket = new DataPacket(fileData);
			
			// Set all of the header values
			dataPacket.setSeqNumber(currentSeqNumber);
			dataPacket.setSourceIPAddress(sourceAddress);
			dataPacket.setSourcePort(sourcePort);
			dataPacket.setDestinationIPAddress(destinationAddress);
			dataPacket.setDestinationPort(destinationPort);
			DatagramPacket sendPacket = dataPacket.packInUDP();
			
			// Add data packet to list of sent packets and storage
			timeoutPackets.add(currentSeqNumber);
			sentPacketStore.put(currentSeqNumber, sendPacket);
			
			numberOfPacketsCreated++;
			currentSeqNumber++;
			packetsQueue.add(sendPacket);
		}
		if (!moreFileDataToPacketize(fileStream)) {
			moreDataPackets = false;
		}
		return packetsQueue;
	}
	
	private boolean moreFileDataToPacketize(FileInputStream fileStream) throws IOException {
		return fileStream.available() != 0;
	}
	
	
	public void sendDisconnectPackets() {
		Packet disconnectPacket = new DisconnectPacket();
		
		// Add appropriate values to disconnect packet header
		disconnectPacket.setSourceIPAddress(sourceAddress);
		disconnectPacket.setSourcePort(sourcePort);
		disconnectPacket.setDestinationIPAddress(destinationAddress);
		disconnectPacket.setDestinationPort(destinationPort);
		disconnectPacket.setSeqNumber(currentSeqNumber);
		DatagramPacket udpDisconnectPacket = disconnectPacket.packInUDP();
		
		timeoutPackets.add(currentSeqNumber);
		sentPacketStore.put(currentSeqNumber, udpDisconnectPacket);
		currentSeqNumber++;
		
		packetSender.sendPacket(udpDisconnectPacket);
		timeout.schedule(new DisconnectTask(), TIMEOUT_SIZE);
	}

	public void setDestinationAddress(InetAddress destinationAddress) {
		this.destinationAddress = destinationAddress;
		
	}

	public void setFileName(String fileName) throws FileNotFoundException {
		this.fileName = fileName;
		fileStream = new FileInputStream(fileName);
	}
	
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	public void setDestinationPort(int port) {
		this.destinationPort = port;
	}
	
	public boolean isConnected() {
		return isConnected;
	}
	
	
	/**
	 * Responsible for performing all tasks after timeout is complete.
	 * @author Tommy
	 *
	 */
	class TimeoutTask extends TimerTask {
		@Override
		public void run() {
			try {
				while(!allACK()) {
					Thread.sleep(TIMEOUT_SIZE);
				}
				// If all are ACK'd send next window size of data packets
				timeoutPackets.clear();
				if (moreDataPackets) {
					sendWindowOfPackets();
				} else {
					sendDisconnectPackets();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// Determines which packets have not been ACK'd
		private boolean allACK() throws IOException {
			boolean isDone = true;
			int numberOfTimeoutPackets = timeoutPackets.size();
			DatagramPacket[] resentPackets = new DatagramPacket[numberOfTimeoutPackets];
			int missingPacketCounter = 0;
			
			for (int i = 0; i < numberOfTimeoutPackets; i++) {
				// Get the packet sequence numbers sent
				int seqNumber = timeoutPackets.get(i);
				
				// Packet has not been ack'd
				if (storageContainsPacket(seqNumber)) {
					resentPackets[missingPacketCounter] = getPacketFromStorage(seqNumber);
					missingPacketCounter++;
					isDone = false;
				}
			}
			// Resend all non-ack'd packets
			if (!isDone) {
				packetSender.sendPackets(resentPackets);
			}
			return isDone;
		}
	}
	
	/**
	 * Responsible for running the disconnect timeout task. It checks whether or 
	 * not the disconnect ACK has been received. If it has not, we resend
	 * @author Tommy
	 *
	 */
	class DisconnectTask extends TimerTask {

		@Override
		public void run() {
			try {
				while(!disconnectACK()) {
					Thread.sleep(TIMEOUT_SIZE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			timeoutPackets.clear();
		}
		
		// Determines if we received the disconnect ACK
		private boolean disconnectACK() throws IOException {
			boolean ackReceived = false;
			DatagramPacket resentPacket = null;
			int seqNumber = timeoutPackets.get(0);
			
			if (storageContainsPacket(seqNumber)) {
				ackReceived = true;
				resentPacket = getPacketFromStorage(seqNumber);
				packetSender.sendPacket(resentPacket);
			}
			
			return ackReceived;
			
		}
	}
	
}

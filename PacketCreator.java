import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


/**
 * PacketCreator is responsible for creating all packets sent.
 *
 */
public class PacketCreator {
	private PacketSender packetSender;
	
	private String fileName;
	private byte[] fileData = new byte[832];
	private final int INITIAL_ACK = -1;
	private int currentSeqNumber = -1;
	private final int TIMEOUT_SIZE = 250; // ms

	private InetAddress destinationAddress;
	private InetAddress sourceAddress;
	private int windowSize;
	private int destinationPort;
	private int sourcePort;
	
	private Timer timeout = new Timer();
	private boolean sendMorePackets = true;
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
		connectionPacket.setIPAddress(destinationAddress);
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
	
	// Create all file packets and begin to send them a window size at a time to the receiver.
	private void sendFile() throws IOException {
		Queue<DatagramPacket> packetsQueue = (Queue<DatagramPacket>) createFilePackets();
		
		// While we still have packets to send...
		while (!packetsQueue.isEmpty()) {

			// While we aren't waiting for ACK's to be received
			while(sendMorePackets) {
				sendMorePackets = false;
				
				int numberOfPacketsToSend;
				int numberOfTotalPacketsLeft = packetsQueue.size();
				
				if (numberOfTotalPacketsLeft > windowSize) {
					numberOfPacketsToSend = windowSize;
				} else {
					numberOfPacketsToSend = numberOfTotalPacketsLeft;
				}
				
				DatagramPacket[] packetsToBeSent = new DatagramPacket[numberOfPacketsToSend];
				
				for(int i = 0; i < numberOfPacketsToSend; i++) {
					packetsToBeSent[i] = packetsQueue.remove();
				};
				
				packetSender.sendPackets(packetsToBeSent);
				timeout.schedule(new TimeoutTask(), TIMEOUT_SIZE);
			};
		};
	}
	
	// Creates all file packets at once and places them in the returning queue
	private Queue<DatagramPacket> createFilePackets() throws FileNotFoundException {
		FileInputStream fileStream = new FileInputStream(fileName);
		Queue<DatagramPacket> packetsQueue = null;
		
		while (moreFileDataToPacketize(fileStream)) {
			// Store as much data from the file as you can into fileData
			fileStream.read(fileData);
			DataPacket dataPacket = new DataPacket(fileData);
			
			// Set all of the header values
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
				sendMorePackets = true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private boolean allACK() throws IOException {
			boolean isDone = true;
			int numberOfTimeoutPackets = timeoutPackets.size();
			DatagramPacket[] resentPackets = new DatagramPacket[numberOfTimeoutPackets];
			int missingPacketCounter = 0;
			
			for (int i = 0; i < numberOfTimeoutPackets; i++) {
				// Get the packet sequence numbers sent
				int ack = timeoutPackets.get(i);
				
				// Packet has not been ack'd
				if (sentPacketStore.containsKey(ack)) {
					resentPackets[missingPacketCounter] = sentPacketStore.get(ack);
					missingPacketCounter++;
					isDone = false;
				}
			}
			// Resend all non-ack'd packets
			packetSender.sendPackets(resentPackets);
			return isDone;
		}
		
	}
	
}

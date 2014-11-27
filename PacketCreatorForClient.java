import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Specialized packet creator for the client.
 *
 */
class PacketCreatorForClient extends PacketCreator {

	public PacketCreatorForClient(DatagramSocket socket, int sourcePort, InetAddress sourceAddress) throws SocketException {
		super(socket, sourcePort, sourceAddress);
	}
	
	public void sendACK(int ackNumber) throws IOException {
		DatagramPacket packetToBeSent = createACKPacket(ackNumber);
		packetSender.sendPacket(packetToBeSent);
	}
	
	public DatagramPacket createACKPacket(int ackNumber) {
		Packet createdPacket = new ACKPacket();
		
		createdPacket.setSeqNumber(ackNumber);
		createdPacket.setSourceIPAddress(sourceAddress);
		createdPacket.setSourcePort(sourcePort);
		createdPacket.setDestinationIPAddress(destinationAddress);
		createdPacket.setDestinationPort(destinationPort);
		DatagramPacket packetToBeSent = createdPacket.packInUDP();
		
		return packetToBeSent;
	}
}

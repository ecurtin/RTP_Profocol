
public class ACKPacket extends Packet {

	public ACKPacket() {
		System.out.println("Ack packet created");
		header.setSyncOn();
		header.setConnectionFlag(false);
		header.setAckFlag(true);
	}
	
//	public DatagramPacket makeDatagramPacket() {
//		return null;
//	}

}

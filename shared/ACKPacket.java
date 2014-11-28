package shared;

public class ACKPacket extends Packet {

	public ACKPacket() {
		header.setSyncOn();
		header.setConnectionFlag(false);
		header.setAckFlag(true);
	}
	
//	public DatagramPacket makeDatagramPacket() {
//		return null;
//	}

}

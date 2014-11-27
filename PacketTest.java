import static org.junit.Assert.*;

import org.junit.Test;


public class PacketTest {

	@Test
	public void testGetData() {
		Packet stuff = new Packet();
		stuff.header.setAckFlag(true);
		//assertTrue(stuff.isACK());
		assertTrue(stuff.isACK());
	}
	
	@Test
	public void hahaha() {
		Packet stuff = new ACKPacket();
		assertTrue(stuff.header.isACK());
		assertTrue(stuff.isACK());
	}
	
	@Test
	public void testChecksumValidation() {
		Packet pack1 = new Packet();
		pack1.setDestinationPort(359);
		pack1.header.setDataFlag(true);
		pack1.setData(new String("this is a string").getBytes());
		
		Packet pack2 = new Packet();
		pack2.setDestinationPort(359);
		pack2.header.setDataFlag(true);
		pack2.setData(new String("this isa string").getBytes());
		
		Packet pack3 = new Packet();
		pack3.setDestinationPort(358);
		pack3.header.setDataFlag(true);
		pack3.setData(new String("this is a string").getBytes());
		
		
		pack1.makeRTPPacket();
		pack2.makeRTPPacket();
		pack1.setChecksum(pack1.computeChecksum());
		
		assertNotEquals(pack1.computeChecksum(), pack2.computeChecksum());
		assertTrue(pack1.validateChecksum());
		assertNotEquals(pack1.computeChecksum(), pack3.computeChecksum());
	}

}

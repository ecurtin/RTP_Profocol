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

}

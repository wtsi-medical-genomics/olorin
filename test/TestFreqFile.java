import java.io.BufferedReader;

import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;


public class TestFreqFile extends TestCase {
	
	@Test public void testOpenFreqFile() throws Exception {
		FreqFile freq = new FreqFile("test.freq.22");
		// get a line to confirm the file is open
		String line = freq.readLine();
		assertEquals(6, line.split("\t").length);
	}
	
	@Test public void testOpenFreqFileCompressed() throws Exception {
		FreqFile freq = new FreqFile("test.freq.22.compressed.gz");
		String line = freq.readLine();
		assertEquals(6, line.split("\t").length);
	}
	
	FreqFile ff;
	
	@Before public void setUp() throws Exception {
		ff = new FreqFile("test.freq.22.compressed.gz");
	}
	
	@Test public void testGetReader() {
		assertEquals("java.io.BufferedReader", ff.getReader().getClass().getName());
	}
}

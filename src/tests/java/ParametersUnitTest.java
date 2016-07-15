package tests.java;
import static org.junit.Assert.*;

import org.junit.Test;

import main.java.Simulation.Parameters;


public class ParametersUnitTest {
	@Test
	public void testImportParameters() {
		Parameters.importParameters("test1.txt");
		assertEquals(new int []{2, 3, 5, 2, 6, 4}, Parameters.NFVreq);
		assertEquals(new int []{3, 5, 6, 7, 8, 5}, Parameters.NFVrate);
		assertEquals(new int []{2, 3, 5, 2, 6, 4}, Parameters.NFVOpCost);
		assertEquals(new int []{5, 6, 7, 4, 8, 5}, Parameters.NFVInitCost);
		assertEquals(5, Parameters.a);
		assertEquals(4, Parameters.b);
	}
}

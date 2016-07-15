import Simulation.Parameters;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ParametersUnitTest {
  @Test
  public void testImportParameters() {
    Parameters.importParameters("test1.txt");
    assertArrayEquals(new int[] {2, 3, 5, 2, 6, 4}, Parameters.NFVreq);
    assertArrayEquals(new int[] {3, 5, 6, 7, 8, 5}, Parameters.NFVrate);
    assertArrayEquals(new int[] {2, 3, 5, 2, 6, 4}, Parameters.NFVOpCost);
    assertArrayEquals(new int[] {5, 6, 7, 4, 8, 5}, Parameters.NFVInitCost);
    assertEquals(2, Parameters.a);
    assertEquals(2, Parameters.b);
  }
}

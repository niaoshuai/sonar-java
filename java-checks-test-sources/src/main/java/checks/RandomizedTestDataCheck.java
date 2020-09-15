package checks;

import java.util.Random;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RandomizedTestDataCheck {

  @Test
  public void randomizedTest() {
    int userAge = new Random().nextInt(42);  // Noncompliant[[sc=19;ec=31;secondary=11,22,24,26,28,30]]{{Replace randomly generated values with a fixed ones.}}
    UUID userID = UUID.randomUUID(); // Noncompliant[[sc=19;ec=36;secondary=12,23,25,27,29,31]]{{Replace randomly generated values with a fixed ones.}}
  }

  @Test
  public void notRandomizedTest() {
    int userAge = 31; //Compliant
    UUID userID = UUID.fromString("00000000-000-0000-0000-000000000001"); //Compliant
  }

  public void secondaryLocations() {
    int age1 = new Random().nextInt(42);
    UUID u1 = UUID.randomUUID();
    int age2 = new Random().nextInt(42);
    UUID u2 = UUID.randomUUID();
    int age3 = new Random().nextInt(42);
    UUID u3 = UUID.randomUUID();
    int age4 = new Random().nextInt(42);
    UUID u4 = UUID.randomUUID();
    int age5 = new Random().nextInt(42);
    UUID u5 = UUID.randomUUID();
  }

}

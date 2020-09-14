package checks.tests;

import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ParameterizedTestCheck {

  @Test
  void testSum11() {  // Noncompliant [[sc=8;ec=17;secondary=22,27]] {{Replace these tests with a single Parameterized test.}}
    assertEquals(Integer.sum(1, 1), 2);
  }

  @Test
  void testSum12() {  // Similar test
    assertEquals(Integer.sum(1, 2), 3);
  }

  @Test
  void testSum22() {  // Similar test
    assertEquals(Integer.sum(2, 2), 4);
  }

  // Could be:
  @ParameterizedTest
  @CsvSource({
    "1, 1, 2",
    "1, 2, 3",
    "2, 2, 4",
  })
  void testSum(int a, int b, int result) {
    assertEquals(Integer.sum(a, b), result);
  }

  // Only consider ints,shorts,bytes,longs,floats,doubles,chars,strings,boolean. Types does not need to be the same
  // ints
  @Test
  void testInt1() {  // Noncompliant [[secondary=49,54,59,64,69,74,79,84,89,93,98]]
    assertEquals(getObject(1), 1);
  }
  @Test
  void testInt2() {
    assertEquals(getObject(2), 2);
  }
  // shorts
  @Test
  void testShort1() {
    assertEquals(getObject((short) 1), 1);
  }
  // bytes
  @Test
  void testByte1() {
    assertEquals(getObject((byte) 1), 1);
  }
  // longs
  @Test
  void testLong1() {
    assertEquals(getObject(1L), 1);
  }
  // floats
  @Test
  void testFloat1() {
    assertEquals(getObject((float) 0.1), 0.1);
  }
  // doubles
  @Test
  void testDouble1() {
    assertEquals(getObject(0.1), 0.1);
  }
  // chars
  @Test
  void testChar1() {
    assertEquals(getObject('1'), '1');
  }
  // strings
  @Test
  void testString1() {
    assertEquals(getObject("1"), "1");
  }
  // booleans
  @Test
  void testClass1() {
    assertEquals(getObject(true), true);
  }
  @Test
  void testClass2() {
    assertEquals(getObject(false), false);
  }
  // null
  @Test
  void testNull() {
    assertEquals(getObject(null), null);
  }

  @Test
  void testCast() { // Not a secondary for any issue
    int i = 3;
    assertEquals(getObject((byte) i), 3);
  }

  @Test
  void testComplex1() { // Noncompliant [[secondary=118,127]]
    Object o = getObject(1);
    assertNotNull(o);
    String s = o.toString();
    assertEquals(s, "1");
    assertEquals(s.length(), 1);
  }

  @Test
  void testComplex2() {
    Object o = getObject(22);
    assertNotNull(o);
    String s = o.toString();
    assertEquals(s, "22");
    assertEquals(s.length(), 2);
  }

  @Test
  void testComplex3() {
    Object o = getObject(333);
    assertNotNull(o);
    String s = o.toString();
    assertEquals(s, "333");
    assertEquals(s.length(), 3);
  }

  @Test
  void testComplex4() { // Not related to any issue
    Object o = getObject(333);
    assertNotNull(o);
    String s = o.getClass().toString();
    assertEquals(s, "333");
    assertEquals(s.length(), 3);
  }

  // Should be:
  @ParameterizedTest
  @MethodSource("provideTestComplex")
  void testComplex123(int input, String toString, int size) {
    Object o = getObject(input);
    assertNotNull(o);
    String s = o.toString();
    Assert.assertEquals(s, toString);
    Assert.assertEquals(s.length(), size);
  }

  private static Stream<Arguments> provideTestComplex() {
    return Stream.of(
      Arguments.of(1, "1", 1),
      Arguments.of(22, "22", 2),
      Arguments.of(333, "333", 3)
    );
  }

  @Test
  void testMax1() {  // Compliant, 2 similar methods are not reported.
    assertEquals(Integer.max(1, 2), 2);
  }

  @Test
  void testMax2() {  // Similar test
    assertEquals(Integer.max(12, 13), 13);
  }

  // Creating a parameterized test for following method will results in too many parameters, not increasing the clarity of the test
  @Test
  void testTooManyParam1() {  // Compliant
    String o = getObject(1).toString();
    assertEquals(o, "1");
    assertNotEquals(o, "b");
    assertNotEquals(o, "c");
  }

  @Test
  void testTooManyParam2() {
    String o = getObject(2).toString();
    assertEquals(o, "2");
    assertNotEquals(o, "bb");
    assertNotEquals(o, "cc");
  }

  // Compliant, testTooManyParam 3,4 and 5 could be candidate for a parametrized tests, but it does not make much sense to
  // refactor only a subset of similar methods (1,2,3,4,5), so we don't report an issue.
  @Test
  void testTooManyParam3() {
    String o = getObject(3).toString();
    assertEquals(o, "3");
    assertNotEquals(o, "bbb");
    assertNotEquals(o, "ccc");
  }

  @Test
  void testTooManyParam4() {
    String o = getObject(4).toString();
    assertEquals(o, "4");
    assertNotEquals(o, "bbb");
    assertNotEquals(o, "ccc");
  }

  @Test
  void testTooManyParam5() {
    String o = getObject(5).toString();
    assertEquals(o, "5");
    assertNotEquals(o, "bbb");
    assertNotEquals(o, "ccc");
  }
  
  Object getObject(Object o) {
    return o;
  }

}

abstract class ParameterizedTestCheckOneCandidate {
  @Test
  void testSum11() {  // Compliant, only two methods are candidates, others are not.
    assertEquals(Integer.sum(1, 1), 2);
  }

  @Test
  void testSum12() {
    assertEquals(Integer.sum(1, 1), 2);
  }

  void notATest() {
    assertEquals(Integer.sum(1, 4), 5);
  }

  @ParameterizedTest
  @MethodSource("something")
  void alreadyParametrized() {
    assertEquals(Integer.sum(1, 5), 6);
  }

  @org.testng.annotations.Test
  @org.testng.annotations.Parameters({ "suite-param" })
  void alreadyParametrized2() {
    assertEquals(Integer.sum(1, 5), 6);
  }

  @Test
  void emptyTest() {
  }

  @Test
  abstract void abstractTest();
}

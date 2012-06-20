package infovis.routing.test;

import static org.junit.Assert.*;
import infovis.data.BusDataBuilder;
import infovis.data.BusEdge;
import infovis.data.BusLine;
import infovis.data.BusStation;
import infovis.data.BusStationManager;
import infovis.data.BusTime;
import infovis.routing.RouteFinder;

import java.awt.Color;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

/**
 * Tests for the {@link RouteFinder} class.
 * 
 * @author Leo Woerteler
 */
public class RouteFinderTests {

  /**
   * Checks if the line is changed when advantageous.
   * 
   * @throws InterruptedException exception
   */
  @Test
  public void shouldChange() throws InterruptedException {
    final BusDataBuilder builder = new BusDataBuilder(null);
    final BusLine s1 = BusDataBuilder.createLine("B1", Color.RED), s2 = BusDataBuilder.createLine(
        "B2", Color.BLUE);
    final BusStation a = builder.createStation("A", 0, 0, 0, 0, 0), b =
        builder.createStation("B", 1, 0, 0, 0, 0), c = builder.createStation("C", 2, 0, 0, 0, 0);

    final BusEdge ab = builder.addEdge(a, s1, 1, b, new BusTime(0, 0), new BusTime(0, 1));
    builder.addEdge(b, s1, 1, c, new BusTime(0, 1), new BusTime(0, 5));
    final BusEdge bc = builder.addEdge(b, s2, 1, c, new BusTime(0, 3), new BusTime(0, 4));

    final BusStationManager man = builder.finish();

    final List<BusEdge> route = RouteFinder.findRoute(a, c, new BusTime(0, 0), 2,
        man.getMaxTimeHours() * 60);

    assertEquals(Arrays.asList(ab, bc), route);
  }

  /**
   * Tests if routes without changes are taken if suitable.
   * 
   * <pre>
   *    , 00:00 ---1--- 00:01.
   *   /                     \
   * (A) 00:01 ---2---> 00:02 (B) 00:02 ---2---> 00:03 (C)
   *                            \                      /
   *                             ` 00:03 ---3--- 00:04´
   * </pre>
   * 
   * @throws InterruptedException exception
   */
  @Test
  public void continuous() throws InterruptedException {
    final BusDataBuilder builder = new BusDataBuilder(null);
    final BusLine s1 = BusDataBuilder.createLine("B1", Color.RED), s2 = BusDataBuilder.createLine(
        "B2", Color.BLUE), s3 = BusDataBuilder.createLine("B3", Color.YELLOW);
    final BusStation a = builder.createStation("A", 0, 0, 0, 0, 0), b =
        builder.createStation("B", 1, 0, 0, 0, 0), c = builder.createStation("C", 2, 0, 0, 0, 0);

    builder.addEdge(a, s1, 1, b, new BusTime(0, 0), new BusTime(0, 1));

    final BusEdge ab = builder.addEdge(a, s2, 1, b, new BusTime(0, 1), new BusTime(0, 2));
    final BusEdge bc = builder.addEdge(b, s2, 1, c, new BusTime(0, 2), new BusTime(0, 3));

    builder.addEdge(b, s3, 1, c, new BusTime(0, 3), new BusTime(0, 4));

    final BusStationManager man = builder.finish();

    final List<BusEdge> route = RouteFinder.findRoute(a, c, new BusTime(0, 0),
        5, man.getMaxTimeHours() * 60);

    assertEquals(Arrays.asList(ab, bc), route);

    final Map<BusStation, BusTime> times = new HashMap<BusStation, BusTime>();
    times.put(a, null);
    times.put(b, new BusTime(0, 1));
    times.put(c, new BusTime(0, 3));
    final Map<BusStation, List<BusEdge>> map = RouteFinder.findRoutesFrom(a, null,
        new BusTime(0, 0), 5, man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR);
    for(final Entry<BusStation, List<BusEdge>> r : map.entrySet()) {
      final BusStation s = r.getKey();
      assertEquals(times.get(s), getLastEnd(r.getValue()));
    }
  }

  /**
   * Tests the routing.
   * 
   * @throws InterruptedException exception.
   */
  @Test
  public void generalTest() throws InterruptedException {
    final BusDataBuilder builder = new BusDataBuilder(null);
    final BusLine line = BusDataBuilder.createLine("1", Color.RED);
    final BusStation a = builder.createStation("a", 0, 0, 0, 0, 0);
    final BusStation b = builder.createStation("b", 1, 0, 0, 0, 0);
    final BusStation c = builder.createStation("c", 2, 0, 0, 0, 0);
    final BusStation d = builder.createStation("d", 3, 0, 0, 0, 0);
    final BusStation e = builder.createStation("e", 4, 0, 0, 0, 0);
    builder.addEdge(a, line, 0, c, new BusTime(3, 10), new BusTime(3, 13));
    builder.addEdge(a, line, 1, b, new BusTime(3, 10), new BusTime(3, 12));
    builder.addEdge(a, line, 2, d, new BusTime(3, 10), new BusTime(3, 11));
    builder.addEdge(b, line, 0, a, new BusTime(3, 10), new BusTime(3, 20));
    builder.addEdge(b, line, 2, c, new BusTime(3, 9), new BusTime(3, 10));
    builder.addEdge(c, line, 0, a, new BusTime(2, 0), new BusTime(2, 1));
    builder.addEdge(d, line, 5, a, new BusTime(0, 1), new BusTime(0, 2));
    builder.addEdge(d, line, 6, b, new BusTime(0, 2), new BusTime(0, 3));
    builder.addEdge(d, line, 7, c, new BusTime(0, 3), new BusTime(0, 4));
    builder.addEdge(d, line, 8, e, new BusTime(0, 4), new BusTime(0, 5));

    final BusStationManager manager = builder.finish();
    final int mth = manager.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR;

    final List<BusEdge> routeTo = RouteFinder.findRoute(c, e, new BusTime(2, 0), 0,
        mth);
    final int[] ids = { 2, 0, 3, 4};
    int i = 0;
    assertEquals(ids[i++], routeTo.get(0).getFrom().getId());
    for(final BusEdge edge : routeTo) {
      assertEquals(ids[i++], edge.getTo().getId());
    }
    assertNull(RouteFinder.findRoute(e, c, new BusTime(2, 0), 0, mth));
  }

  /**
   * Tests with line changes and different max time.
   * 
   * @throws InterruptedException exception.
   */
  @Test
  public void lineChanging() throws InterruptedException {
    final BusDataBuilder builder = new BusDataBuilder(null);
    final BusLine line = BusDataBuilder.createLine("1", Color.RED);
    final BusLine other = BusDataBuilder.createLine("2", Color.BLUE);
    final BusStation e = builder.createStation("e", 4, 0, 0, 0, 0);
    final BusStation f = builder.createStation("f", 5, 0, 0, 0, 0);
    final BusStation g = builder.createStation("g", 6, 0, 0, 0, 0);
    final BusStation h = builder.createStation("h", 7, 0, 0, 0, 0);
    builder.addEdge(e, line, 0, h, new BusTime(23, 59), new BusTime(0, 1));
    builder.addEdge(e, line, 1, h, new BusTime(0, 7), new BusTime(0, 0));
    builder.addEdge(e, line, 2, h, new BusTime(0, 0), new BusTime(0, 6));
    builder.addEdge(e, line, 3, h, new BusTime(0, 6), new BusTime(0, 8));
    builder.addEdge(e, line, 4, h, new BusTime(0, 50), new BusTime(1, 0));
    builder.addEdge(e, line, 5, f, new BusTime(0, 0), new BusTime(0, 2));
    builder.addEdge(e, other, 0, f, new BusTime(0, 0), new BusTime(0, 1));
    builder.addEdge(e, line, 6, g, new BusTime(0, 1), new BusTime(0, 3));
    builder.addEdge(f, line, 7, h, new BusTime(1, 2), new BusTime(1, 3));
    builder.addEdge(f, line, 5, h, new BusTime(0, 2), new BusTime(0, 5));
    builder.addEdge(g, other, 0, h, new BusTime(0, 3), new BusTime(0, 4));
    builder.addEdge(g, line, 6, h, new BusTime(0, 4), new BusTime(0, 7));
    builder.addEdge(g, line, 8, h, new BusTime(0, 1), new BusTime(0, 2));
    final BusStationManager manager = builder.finish();
    final int mth = manager.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR;
    assertEquals(4,
        getLastEndMinute(RouteFinder.findRoute(e, h, new BusTime(0, 0), 0, mth)));
    assertEquals(5,
        getLastEndMinute(RouteFinder.findRoute(e, h, new BusTime(0, 0), 1, mth)));
    assertNull(RouteFinder.findRoute(e, h, new BusTime(0, 0), 0, 0));
    assertEquals(4, getLastEndMinute(RouteFinder.findRoute(e, h, new BusTime(0, 0), 0,
        BusTime.MINUTES_PER_HOUR)));
    assertEquals(5, getLastEndMinute(RouteFinder.findRoute(e, h, new BusTime(0, 0), 1,
        BusTime.HOURS_PER_DAY * BusTime.MINUTES_PER_HOUR)));
  }

  /**
   * The end point of the last edge.
   * 
   * @param route The route.
   * @return The end point.
   */
  private static BusTime getLastEnd(final List<BusEdge> route) {
    return !route.isEmpty() ? route.get(route.size() - 1).getEnd() : null;
  }

  /**
   * The minute of the end point of the last edge.
   * 
   * @param route The route.
   * @return The minute of the end point.
   */
  private static int getLastEndMinute(final List<BusEdge> route) {
    return getLastEnd(route).getMinute();
  }

  /**
   * Finds all shortest routes from all stations at 12:00 AM. This test checks
   * also the performance by letting each route take up to 100 milliseconds.
   * 
   * @throws Exception exception
   */
  @Test
  public void at12Am() throws Exception {
    final BusStationManager man = BusDataBuilder.load("src/main/resources");
    final AtomicBoolean fail = new AtomicBoolean(false);
    final BitSet set = new BitSet();
    int num = 0;
    for(final BusStation a : man.getStations()) {
      set.set(a.getId());
      ++num;
    }
    final int count = num;
    final Thread t = new Thread() {

      @Override
      public void run() {
        try {
          synchronized(this) {
            wait(count * 100);
            fail.getAndSet(true);
          }
        } catch(final InterruptedException e) {
          // interrupted
        }
      }

    };
    t.start();

    for(final BusStation a : man.getStations()) {
      if(fail.get()) {
        System.out.println("failed");
        break;
      }
      System.out.println(a
          + ", "
          + RouteFinder.findRoutesFrom(a, set, new BusTime(12, 0), 5,
              man.getMaxTimeHours() * BusTime.MINUTES_PER_HOUR).size());
    }

    t.interrupt();
    assertFalse("Test took longer than " + (count * 0.1) + "s", fail.get());
  }

}

package infovis;

import infovis.data.BusDataBuilder;
import infovis.data.BusStationManager;
import infovis.gui.MainWindow;

import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.WindowConstants;

/**
 * Starts the main application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class Main {
  /** Resource path. */
  public static final String RESOURCES = "src/main/resources";

  /** No constructor. */
  private Main() {
    // no constructor
  }

  /**
   * Starts the main application.
   * 
   * @param args If an argument is provided this path is used as resource path.
   *          Otherwise the default resources are used.
   */
  public static void main(final String[] args) {
    final BusStationManager m;
    try {
      m = loadData(args);
    } catch(final IOException e) {
      e.printStackTrace();
      return;
    }
    setLookAndFeel();
    // initialize window
    final MainWindow frame = new MainWindow(m, false);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /** Sets the look and feel of the application. */
  public static void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch(final Exception e) {
      // system look and feel already used -- proceed
    }
  }

  /**
   * Loads the data of the application.
   * 
   * @param args The arguments passed to the application.
   * @return The manager with the data.
   * @throws IOException I/O Exception.
   */
  public static BusStationManager loadData(final String[] args) throws IOException {
    final BusStationManager m;
    if(args.length > 0) {
      final String cs = args.length > 1 ? args[1] : null;
      m = BusDataBuilder.loadPath(args[0], cs);
    } else {
      m = BusDataBuilder.loadDefault("konstanz");
    }
    return m;
  }

}

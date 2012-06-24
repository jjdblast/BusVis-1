package infovis.gui;

import static infovis.data.BusTime.*;
import infovis.ctrl.BusVisualization;
import infovis.ctrl.Controller;
import infovis.data.BusStation;
import infovis.data.BusTime;
import infovis.routing.RoutingAlgorithm;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A control panel to access the controller via GUI.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class ControlPanel extends JPanel implements BusVisualization {

  /** SVUID. */
  private static final long serialVersionUID = 1644268841480928696L;

  /** The station box. */
  protected final JComboBox box;

  /** The bus time slider. */
  protected final JSlider bt;

  /** The bus time label. */
  private final JLabel btLabel;

  /** The check box to select now as start time. */
  protected final JCheckBox now;

  /** The change time slider. */
  protected final JSlider ct;

  /** The change time label. */
  private final JLabel ctLabel;

  /** The time window slider. */
  protected final JSlider tw;

  /** The walk time window label. */
  private final JLabel twwLabel;

  /** The walk time window slider. */
  protected final JSlider tww;

  /** The time window label. */
  private final JLabel twLabel;

  /** Maps bus station ids to indices in the combo box. */
  private final int[] indexMap;

  /** The algorithm box. */
  protected final JComboBox algoBox;

  /**
   * A thin wrapper for the bus station name. Also allows the <code>null</code>
   * bus station, representing no selection.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  private static final class BusStationName {

    /** The associated bus station. */
    public final BusStation station;

    /** The name of the station. */
    private final String name;

    /**
     * Creates a bus station name object.
     * 
     * @param station The station.
     */
    public BusStationName(final BusStation station) {
      this.station = station;
      name = station != null ? station.getName() : "(no selection)";
    }

    @Override
    public String toString() {
      return name;
    }
  }

  /**
   * Creates a list of all bus station names.
   * 
   * @param ctrl The controller.
   * @return All bus station names.
   */
  private static BusStationName[] getStations(final Controller ctrl) {
    final Collection<BusStation> s = ctrl.getStations();
    final BusStation[] arr = s.toArray(new BusStation[s.size()]);
    Arrays.sort(arr, new Comparator<BusStation>() {

      @Override
      public int compare(final BusStation a, final BusStation b) {
        return a.getName().compareTo(b.getName());
      }

    });
    final BusStationName[] res = new BusStationName[arr.length + 1];
    res[0] = new BusStationName(null);
    for(int i = 0; i < arr.length; ++i) {
      res[i + 1] = new BusStationName(arr[i]);
    }
    return res;
  }

  /**
   * Creates a control panel.
   * 
   * @param ctrl The corresponding controller.
   */
  public ControlPanel(final Controller ctrl) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    final Component space = Box.createRigidArea(new Dimension(5, 5));
    // routing selection
    final RoutingAlgorithm[] algos = Controller.getRoutingAlgorithms();
    if(algos.length != 1) {
      algoBox = new JComboBox(algos);
      algoBox.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(final ActionEvent e) {
          final RoutingAlgorithm routing = (RoutingAlgorithm)
              algoBox.getSelectedItem();
          if(routing != ctrl.getRoutingAlgorithm()) {
            ctrl.setRoutingAlgorithm(routing);
          }
        }

      });
      algoBox.setMaximumSize(algoBox.getPreferredSize());
      addHor(new JLabel("Routing:"), algoBox);
    } else {
      algoBox = null;
    }
    // station selection
    final BusStationName[] stations = getStations(ctrl);
    indexMap = new int[ctrl.maxId() + 1];
    for(int i = 0; i < stations.length; ++i) {
      if(stations[i].station == null) {
        continue;
      }
      indexMap[stations[i].station.getId()] = i;
    }
    box = new JComboBox(stations);
    box.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final BusStation station = ((BusStationName) box.getSelectedItem()).station;
        if(station != ctrl.getSelectedStation()) {
          ctrl.selectStation(station);
          ctrl.focusStation();
        }
      }

    });
    box.setMaximumSize(box.getPreferredSize());
    addHor(new JLabel("Stations:"), box);

    // start time
    bt = new JSlider(0, MIDNIGHT.minutesTo(MIDNIGHT.later(-1)));
    bt.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final int min = bt.getValue();
        final int to = MIDNIGHT.minutesTo(ctrl.getTime());
        if(min != to) {
          ctrl.setTime(MIDNIGHT.later(min));
        }
      }
    });

    btLabel = new JLabel();
    now = new JCheckBox("now");
    now.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final boolean b = ctrl.isStartTimeNow();
        if(now.isSelected()) {
          if(!b) {
            ctrl.setNow();
          }
        } else {
          if(b) {
            ctrl.setTime(MIDNIGHT.later(bt.getValue()));
          }
        }
      }
    });
    addHor(new JLabel("Start Time:"), bt, btLabel, now, space);
    // change time
    ct = new JSlider(-10, 60);
    ct.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
        final int min = ct.getValue();
        if(min != ctrl.getChangeTime()) {
          ctrl.setChangeTime(min);
        }
      }
    });
    ctLabel = new JLabel();
    addHor(new JLabel("Change Time:"), ct, ctLabel, space);

    // time window
    tw = new JSlider(0, 24);
    tw.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent arg0) {
        final int v = tw.getValue();
        if(ctrl.getMaxTimeHours() != v) {
          ctrl.setMaxTimeHours(v);
        }
      }

    });

    twLabel = new JLabel();
    addHor(new JLabel("Max Wait:"), tw, twLabel, space);

    // walk time window
    tww = new JSlider(0, 2 * BusTime.MINUTES_PER_HOUR);
    tww.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent arg0) {
        final int v = tww.getValue();
        if(ctrl.getWalkTime() != v) {
          ctrl.setWalkTime(v);
        }
      }

    });

    twwLabel = new JLabel();
    addHor(new JLabel("Max Walk:"), tww, twwLabel, space);

    // end of layout
    add(Box.createVerticalGlue());
    ctrl.addBusVisualization(this);
  }

  /**
   * Adds a number of components to the panel.
   * 
   * @param comps The components to add.
   */
  private void addHor(final Component... comps) {
    final JPanel hor = new JPanel();
    hor.setLayout(new BoxLayout(hor, BoxLayout.X_AXIS));
    for(final Component c : comps) {
      hor.add(Box.createRigidArea(new Dimension(5, 5)));
      if(c != null) {
        hor.add(c);
      }
    }
    hor.setAlignmentX(Component.LEFT_ALIGNMENT);
    add(hor);
  }

  @Override
  public void selectBusStation(final BusStation station) {
    box.setSelectedIndex(station != null ? indexMap[station.getId()] : 0);
  }

  @Override
  public void setStartTime(final BusTime time) {
    if(time == null) {
      bt.setEnabled(false);
      now.setSelected(true);
      final Calendar cal = Calendar.getInstance();
      btLabel.setText(BusTime.fromCalendar(cal).pretty(isBlinkSecond(cal)));
      return;
    }
    bt.setEnabled(true);
    now.setSelected(false);
    bt.setValue(MIDNIGHT.minutesTo(time));
    btLabel.setText(time.pretty());
  }

  @Override
  public void overwriteDisplayedTime(final BusTime time, final boolean blink) {
    btLabel.setText(time.pretty(blink));
  }

  @Override
  public void setChangeTime(final int minutes) {
    ct.setValue(minutes);
    ctLabel.setText(BusTime.minutesToString(minutes));
  }

  @Override
  public void focusStation() {
    // already covered by select bus station
  }

  @Override
  public void undefinedChange(final Controller ctrl) {
    final int mth = ctrl.getMaxTimeHours();
    tw.setValue(mth);
    twLabel.setText(BusTime.minutesToString(mth * BusTime.MINUTES_PER_HOUR));

    final int walkTime = ctrl.getWalkTime();
    tww.setValue(walkTime);
    twwLabel.setText(BusTime.minutesToString(walkTime));
    if(algoBox != null) {
      algoBox.setSelectedItem(ctrl.getRoutingAlgorithm());
    }
  }

}

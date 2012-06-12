package infovis.embed;

import static infovis.VecUtil.*;
import infovis.embed.pol.Interpolator;
import infovis.embed.pol.SinInterpolator;

import java.awt.geom.Point2D;

/**
 * A simple circular embedder.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class CircularEmbedder extends AbstractEmbedder {

  /**
   * The weighter.
   */
  private final Weighter weighter;

  /**
   * Creates a circular embedder.
   * 
   * @param weighter The weighter.
   * @param drawer The drawer.
   */
  public CircularEmbedder(final Weighter weighter, final NodeDrawer drawer) {
    super(drawer);
    this.weighter = weighter;
  }

  /**
   * The standard interpolator.
   */
  private static final Interpolator INTERPOLATOR = new SinInterpolator();

  /**
   * The standard animation duration.
   */
  private static final int DURATION = 1000;

  /**
   * The current reference node.
   */
  private SpringNode curRef;

  @Override
  protected void step() {
    final SpringNode ref = weighter.getReferenceNode();
    if(ref != curRef) {
      Point2D refP;
      if(ref != null) {
        refP = ref.getPos();
      } else {
        refP = null;
      }
      for(final SpringNode n : weighter.nodes()) {
        final Point2D pos = weighter.getDefaultPosition(n);
        if(n == ref) {
          continue;
        }
        Point2D dest;
        if(refP == null) {
          dest = pos;
        } else {
          if(!weighter.hasWeight(n, ref)) {
            dest = new Point2D.Double();
          } else {
            final double w = weighter.weight(n, ref);
            dest = addVec(setLength(subVec(pos, refP), w), refP);
          }
        }
        n.startAnimationTo(dest, INTERPOLATOR, DURATION);
      }
      curRef = ref;
    }
    for(final SpringNode n : weighter.nodes()) {
      n.animate();
    }
  }

}

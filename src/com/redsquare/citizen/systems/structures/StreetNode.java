package com.redsquare.citizen.systems.structures;

import com.redsquare.citizen.util.Sets;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class StreetNode {
  final SettlementLayout layout;

  final Point location;
  private final Type type;
  private final Set<Street> offshoots;
  private final Street from;

  private StreetNode(Point location, Type type, Street from,
                     SettlementLayout layout) {
    this.location = location;
    this.type = type;
    this.from = from;
    this.layout = layout;

    this.offshoots = new HashSet<>();

    layout.priority.add(this);
  }

  static StreetNode startNode(SettlementLayout layout) {
    return new StreetNode(new Point(999, 999), Type.OMNI, null, layout);
  }

  void generate() {
    while (!layout.priority.empty()) {
      StreetNode top = layout.priority.pop();
      top.generateOffshoots();

      // TODO - remove
      BufferedImage test = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = (Graphics2D) test.getGraphics();

      g.setColor(new Color(0, 180, 0));
      g.fillRect(0, 0, 400, 400);

      for (int x = 0; x < 100; x++) {
        for (int y = 0; y < 100; y++) {
          if (layout.streetMap[x][y]) {
            g.setColor(new Color(255, 0, 0));
            g.fillRect(x * 4, y * 4, 4, 4);
          }
        }
      }

      try {
        ImageIO.write(test, "png", new File("res/test_output/settlements/streetmap" + layout.iterCounter + ".png"));
        layout.iterCounter++;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  static StreetNode fromStreet(Street street) {
    Point location = street.getParent().location;

    SettlementLayout layout = street.getParent().layout;

    switch (street.getDirection()) {
      case EAST:
        location = new Point(location.x + street.getLength(),  location.y);
        break;
      case WEST:
        location = new Point(location.x - street.getLength(),  location.y);
        break;
      case SOUTH:
        location = new Point(location.x,  location.y + street.getLength());
        break;
      case NORTH:
        location = new Point(location.x,  location.y - street.getLength());
        break;
    }

    return new StreetNode(location, Type.random(street.getDirection(),
            street.getParent().depth() / (double) layout.maxDepth), street, layout);
  }

  public enum Type {
    OMNI, STUB,
    // from N
    W_N_E, N_E, W_E, W_N, W, N, E,
    // from E
    N_E_S, E_S, N_S, S,
    // from S
    E_S_W, S_W,
    // from W
    S_W_N;

    static Type random(Street.Direction from, double stubProb) {
      Set<Type> eligible = new HashSet<>();

      switch (from) {
        case EAST:
          eligible.addAll(Set.of(N_E_S, E_S, N_S, S, E, N, N_E));
          break;
        case WEST:
          eligible.addAll(Set.of(W_N, W, N, N_S, S, S_W, S_W_N));
          break;
        case NORTH:
          eligible.addAll(Set.of(W_N_E, N_E, W_E, W_N, W, N, E));
          break;
        case SOUTH:
          eligible.addAll(Set.of(W_E, W, E, E_S, S, E_S_W, S_W));
          break;
      }

      if (Math.random() < stubProb) {
        return STUB;
      }

      return Sets.randomEntry(eligible);
    }
  }

  int depth() {
    if (from == null) return 0;

    return 1 + from.getParent().depth();
  }

  private void generateOffshoots()  {
    Set<Street> offshoots = new HashSet<>();

    switch (type) {
      case OMNI:
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        break;
      case W_N_E:
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        break;
      case N_E_S:
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        break;
      case E_S_W:
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        break;
      case S_W_N:
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        break;
      case E_S:
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        break;
      case N_E:
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        break;
      case N_S:
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        break;
      case S_W:
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        break;
      case W_E:
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        break;
      case W_N:
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        break;
      case E:
        offshoots.add(Street.generate(Street.Direction.EAST, this));
        break;
      case S:
        offshoots.add(Street.generate(Street.Direction.SOUTH, this));
        break;
      case W:
        offshoots.add(Street.generate(Street.Direction.WEST, this));
        break;
      case N:
        offshoots.add(Street.generate(Street.Direction.NORTH, this));
        break;
      case STUB:
      default:
        break;
    }

    Set<Street> toRemove = new HashSet<>();
    for (Street offshoot : offshoots) {
      if (offshoot.getChild() == null)
        toRemove.add(offshoot);
    }

    offshoots.removeAll(toRemove);

    this.offshoots.addAll(offshoots);
  }

  void draw(Graphics2D g) {
    for (Street street : offshoots) {
      street.draw(g);
    }
  }
}

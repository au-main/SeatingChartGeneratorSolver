import processing.core.PApplet;
import processing.core.PVector;

import java.util.HashMap;

public class ControlWindow extends PApplet {
    private static final float LEFT_MARGIN = 10;
    private static final float MARGIN = 10;

    public static HashMap<String, ToggleSwitch> switches = new HashMap<>();

    public ControlWindow() {
        super();
        PApplet.runSketch(new String[] {this.getClass().getSimpleName()}, this);
    }

    public void settings() {
        size(300, 600);
    }

    public void setup() {
        switches.put("group numbers", new ToggleSwitch(new PVector(LEFT_MARGIN, 4*MARGIN)).setText("display group numbers"));
        switches.put("mirror", new ToggleSwitch(new PVector(LEFT_MARGIN, 5*MARGIN + ToggleSwitch.h*2)).setText("mirror for printing"));
    }

    public void draw() {
        textSize(14);

        for( ToggleSwitch s : switches.values() ) {
            s.draw(this);
            fill(0);
            text( s.getText(), s.getRight() + MARGIN, s.getCenterY());
        }
    }

    public void mousePressed() {
        for( ToggleSwitch s : switches.values() ) {
            s.handleClick(mouseX, mouseY);
        }
    }
}
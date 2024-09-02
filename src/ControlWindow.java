import processing.core.PApplet;
import processing.core.PVector;

public class ControlWindow extends PApplet {
    ToggleSwitch s1;

    public ControlWindow() {
        super();
        PApplet.runSketch(new String[] {this.getClass().getSimpleName()}, this);
    }

    public void settings() {
        size(300, 600);
    }

    public void setup() {
        s1 = new ToggleSwitch(new PVector(width/2, height/2));
    }

    public void draw() {
        s1.draw(this);
    }

    public void mousePressed() {
        s1.handleClick(mouseX, mouseY);
    }
}

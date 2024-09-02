import processing.core.PApplet;
import processing.core.PVector;

public class ToggleSwitch {
    private PVector pos, onPos, offPos;
    public static final float w = 50, h = 25;
    private int backgroundColor;
    private int offColor, onColor;
    private boolean on;
    private String text;

    public ToggleSwitch(PVector pos) {
        this.pos = pos;

        onPos = new PVector(pos.x + h/2, pos.y + h/2);
        offPos = new PVector(pos.x + w - h/2, pos.y + h/2);
    }

    public ToggleSwitch setText(String text) {
        this.text = text;
        return this;
    }

    public float getRight() {
        return this.pos.x + w;
    }

    public float getLeft() {
        return this.pos.x;
    }

    public float getCenterY() {
        return this.pos.y + h/2;
    }

    public void draw(PApplet window) {
        window.fill(backgroundColor);
        window.rect(onPos.x, pos.y, w-h, h);
        window.ellipse(onPos.x, onPos.y, h, h);
        window.ellipse(offPos.x, offPos.y, h, h);

        if (on) {
            window.fill(window.color(0, 255, 0));
            window.ellipse(onPos.x, onPos.y, h*0.8f, h*0.8f);
        } else {
            window.fill(window.color(255, 0, 0));
            window.ellipse(offPos.x, offPos.y, h*0.8f, h*0.8f);
        }
    }

    public void setState(boolean state) {
        on = state;
    }

    public boolean isOn() {
        return on;
    }

    public void toggle() {
        on = !on;
    }

    public void handleClick(int mouseX, int mouseY) {
        if (!mouseOver(mouseX, mouseY)) return;
        on = !on;
    }

    public boolean mouseOver(int mouseX, int mouseY) {
        return (pos.x <= mouseX && mouseX <= pos.x + w) &&
                (pos.y <= mouseY && mouseY <= pos.y + h);
    }

    public String getText() {
        return this.text;
    }
}

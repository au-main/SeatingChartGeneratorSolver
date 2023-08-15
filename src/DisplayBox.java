import processing.core.PApplet;

public class DisplayBox {
    private int x, y, w, h;
    private DeskPair desk;

    public DisplayBox(int x, int y,int w, int h, DeskPair desk) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
        this.desk = desk;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getText() {
        return getName1() + " and " + getName2();
    }

    public void draw(PApplet window) {
        window.fill(255);
        window.stroke(255);
        window.rect(x, y, w, h);
        window.fill(0);
        window.textAlign(window.LEFT, window.TOP);
        window.text(getText(), x, y);
    }

    public String getName1() {
        return (this.desk.getLeft() != null?this.desk.getLeft().getDisplayName():" no one");
    }

    public String getName2() {
        return (this.desk.getRight() != null?this.desk.getRight().getDisplayName():" no one");
    }

    public boolean isMouseOver(int mousex, int mousey) {
        return (x < mousex && mousex < x + w) && (y < mousey && mousey < y+h);
    }

    public void handleMouseClick(int mousex, int mousey, PApplet window) {
        if (!isMouseOver(mousex, mousey)) return;

        float n1Width = window.textWidth(this.getName1());
        if (mousex < x + n1Width) this.desk.deleteLeft();

        float n2Width = window.textWidth(this.getName2());
        float textWidth = window.textWidth(this.getText());
        if (mousex > x + textWidth - n2Width) this.desk.deleteRight();
    }

    private void removeStudent2() {
        System.out.println("removing: " + this.getName2());
        this.desk.removeRight();
    }

    private void removeStudent1() {
        System.out.println("removing" + this.getName1());
        this.desk.removeLeft();
    }

    public void highlight(PApplet window) {
        window.fill(0,0,0,0);
        window.stroke(0,255,0);
        window.rect(x, y, w, h);
    }

    public Student getStudent1() {
        return this.desk.getLeft();
    }

    public Student getStudent2() {
        return this.desk.getRight();
    }

    public void setWidthFromContents(PApplet window) {
        this.w = (int)(window.textWidth(this.getText())) + 1;
    }
}
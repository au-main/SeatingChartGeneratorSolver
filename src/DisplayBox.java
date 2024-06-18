import processing.core.PApplet;

public class DisplayBox {
    private static final boolean DRAW_BORDER = true;
    private static final float HORIZ_BUFFER = 20;
    private int x, y, w, h;
    private int rows, cols;     // how names are organized within the box
    private Group desk;

    public DisplayBox(int x, int y,int w, int h, int rows, int cols, Group desk) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.w = w;
        this.rows = rows;
        this.cols = cols;
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

/*
    public String getText() {
        return getName1() + " and " + getName2();
    }
*/

    public void draw(PApplet window) {
        window.fill(255);
        window.stroke(255);
        if (DRAW_BORDER)
            window.rect(x, y, w, h);
        window.fill(0);
        window.textAlign(window.LEFT, window.TOP);

        int position = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                window.text(getName(position), (int)(x + col*(w/(double)cols)), (int)(y + row*(h/(double)rows)));
                position++;
            }
        }
    }

    public String getName(int position) {
        return (this.desk.get(position) != null)?this.desk.get(position).getDisplayName():" no one";
    }

    public boolean isMouseOver(int mousex, int mousey) {
        return (x < mousex && mousex < x + w) && (y < mousey && mousey < y+h);
    }

    public int[] getNameBoxIndicies(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return null;

        int r = (int)((mouseY - y)/(h/(double)rows));
        int c = (int)((mouseX - x)/(w/(double)cols));

        return new int[] {r, c};
    }

    public void mouseOverHighlight(int mouseX, int mouseY, PApplet window) {
        int[] indicies = getNameBoxIndicies(mouseX, mouseY);
        if (indicies == null) return;

        int r = indicies[0];
        int c = indicies[1];

        window.fill(255);
        window.stroke(window.color(255, 255, 128)); // yellow highlight
        window.rect(x+(int)(c*((double)w/cols)), y+(int)(r*((double)h/rows)), (int)(w/cols), (int)(h/rows) );
    }

    public void handleMouseClick(int mouseX, int mouseY, PApplet window) {
        int[] indicies = getNameBoxIndicies(mouseX, mouseY);
        if (indicies == null) return;

        int r = indicies[0];
        int c = indicies[1];

        this.desk.delete(r*cols+c);
    }

    public void highlight(PApplet window) {
        window.fill(0,0,0,0);
        window.stroke(0,255,0);
        window.rect(x, y, w, h);
    }

    private int positionFor(int row, int col) {
        return row*cols+col;
    }

    private int getMaxRowWidth(PApplet window) {
        int maxWidth = 0;

        for (int row = 0; row < rows; row++) {
            int rowWidth = 0;
            for (int col = 0; col < cols; col++) {
                Student s = desk.get(positionFor(row, col));
                String name = (s == null)?" no one":s.getDisplayName();
                rowWidth += window.textWidth( name ) + HORIZ_BUFFER;
            }
            rowWidth -= HORIZ_BUFFER;       // because we don't want buffer on the right

            if (rowWidth > maxWidth) {
                maxWidth = rowWidth;
            }
        }

        return maxWidth;
    }

    public void setWidthFromContents(PApplet window) {
        int width = getMaxRowWidth(window);
        this.w = width;
    }
}
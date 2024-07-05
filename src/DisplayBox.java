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

    public static void swapLocations(DisplayBox b1, DisplayBox b2) {
        int x = b1.x;
        int y = b1.y;
        int w = b1.w;
        int h = b1.h;
        b1.x = b2.x;
        b1.y = b2.y;
        b1.w = b2.w;
        b1.h = b2.h;
        b2.x = x;
        b2.y = y;
        b2.w = w;
        b2.h = h;
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
                if (desk.isFrozen(position)) {
                    window.fill(255,0,0);
                    window.stroke(255,0,0);
                } else {
                    window.fill(0);
                    window.stroke(0);
                }
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

    public void highlight(PApplet window, int color) {
        window.fill(0,0,0,0);
        window.stroke(color);
        window.rect(x+1, y+1, w-2, h-2);
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

    public void toggleFreezeNameFor(int mouseX, int mouseY) {
        int position = 0;
        double boxWidth = w/(double)cols;
        double boxHeight = h/(double)rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double x1 = (x + col*(w/(double)cols));
                double y1 = (y + row*(h/(double)rows));
                double x2 = x1 + boxWidth;
                double y2 = y1 + boxHeight;
                if (x1 <= mouseX && mouseX <= x2) {
                    if (y1 <= mouseY && mouseY <= y2) {
                        desk.toggleFreeze(position);
                    }
                }
                position++;
            }
        }
    }
}
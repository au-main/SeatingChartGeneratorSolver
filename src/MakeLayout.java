import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MakeLayout extends PApplet {
    private static final float TEXT_SIZE = 20;

    ArrayList<DisplayBox> displayList = new ArrayList<DisplayBox>();
    DisplayBox ghost;
    int nextId = 1;

    float verticalBuffer = 10;
    float textHeight;

    private static final String DATA_DIR = "DataFiles/";
    private static final String CHARTS_DIR = "SavedCharts/";

    private int lastMouseButton;
    private boolean displayGrid = true;
    private int gridSpacing = 30;

    private int boxWidth = 150;
    private int boxHeight = 180;

    public void settings() {
        size(1600, 1000);
    }

    public void setup() {
        textSize(TEXT_SIZE);
        float strAscent = textAscent();
        float strDescent = textDescent();
        textHeight = strAscent + strDescent;
        ghost = new DisplayBox(-1000,-1000, (int)boxWidth, (int)boxHeight, null);
    }

    public void draw() {
        background(255);

        if (displayGrid) {
            for (int x = 0; x < width; x += gridSpacing) {
                stroke(200);
                fill(200);
                line(x, 0, x, height);
            }

            for (int y = 0; y < height; y += gridSpacing) {
                stroke(200);
                fill(200);
                line(0, y, width, y);
            }
        }

        // Draw all existing boxes
        for (DisplayBox box : displayList) {
            box.drawLayoutDisplay(this);
        }

        ghost.setX( displayGrid?snap(mouseX):mouseX );
        ghost.setY( displayGrid?snap(mouseY):mouseY );
        ghost.drawLayoutDisplay(this);
        text(""+nextId, ghost.getX() + ghost.getW()/2, ghost.getY() + ghost.getH());
    }

    private int snap(int val) {
        return (val/gridSpacing)*gridSpacing;
    }


    public void keyReleased() {
        if (key == 'd' || key == 'D') {
            displayGrid = !displayGrid;
        }
        if (key == 's' || key =='S') {
            String filename = getDateString("SeatingLayout-72-");
            this.saveLayoutToFile(DATA_DIR + filename);
            System.out.println("Saved to " + DATA_DIR + filename);
        }

        if (key == 'l' || key == 'L') {
            String chartFile = getFileWithDialog("Choose the file with the chart you want to load");
            this.displayList = loadLayoutFromFile(chartFile);
            System.out.println("Loaded chart!");
        }
    }

    public void saveLayoutToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            String headers = "desk id, pixel_x, pixel_y, width, height";
            writer.write(headers);
            writer.newLine();

            for (DisplayBox desk : this.displayList) {
                String row = desk.getId() + ", " + desk.getX() + ", " + desk.getY() + ", "+ desk.getW() + ", " + desk.getH();
                writer.write(row);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFileWithDialog(String prompt) {
        JFileChooser fileChooser = new JFileChooser();

        String currentWorkingDir = System.getProperty("user.dir");
        fileChooser.setCurrentDirectory(new File(currentWorkingDir));
        fileChooser.setDialogTitle(prompt);

        int returnVal = fileChooser.showOpenDialog(null); // Pass null to center the dialog

        String ret = "";
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            ret = selectedFile.getAbsolutePath();
            System.out.println("Chose: " + ret);
        } else {
            System.out.println("No file selected.");
        }

        return ret;
    }

    private String getDateString(String file) {
        String name = file;
        if (name.contains(".")) {
            name = name.substring(0, file.indexOf("."));
        }

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(dateFormatter);

        return name + "-" + formattedDate + ".csv";
    }

    public void mousePressed() {
        lastMouseButton = mouseButton;
    }

    public void mouseReleased() {
        DisplayBox selected = getSelectedDisplayBox(mouseX, mouseY, displayList);
        if (selected != null) {
            // TODO: remove it from the list
        } else {
            int boxx = displayGrid?snap(mouseX):mouseX;
            int boxy = displayGrid?snap(mouseY):mouseY;
            DisplayBox box = new DisplayBox(nextId, boxx, boxy, boxWidth, (int) boxHeight, null);
            displayList.add(box);
            nextId++;
        }
    }

    private DisplayBox getSelectedDisplayBox(int mouseX, int mouseY, ArrayList<DisplayBox> displayList) {
        for (DisplayBox box : displayList) {
            if (box.isMouseOver(mouseX, mouseY)) {
                return box;
            }
        }

        return null;
    }

    public static ArrayList<DisplayBox> loadLayoutFromFile(String layoutFilePath) {
        ArrayList<DisplayBox> displayList = new ArrayList<>();
         try {
            String raw = SeatingChart.readFile(layoutFilePath);
            String[] rows = raw.split("\n");
            for (int i = 1; i < rows.length; i++) {
                String row = rows[i];
                String[] vals = row.split(",");

                int deskId = Integer.parseInt( vals[0].trim() );
                int x = Integer.parseInt( vals[1].trim() );
                int y = Integer.parseInt( vals[2].trim() );
                int w = Integer.parseInt( vals[3].trim() );
                int h = Integer.parseInt( vals[4].trim() );

                displayList.add( new DisplayBox(deskId, x, y, w, h, null) );
            }
        } catch (IOException e) {
            System.out.println("Couldn't read file " + layoutFilePath);
        }

        return displayList;
    }

    public static void main(String[] args) {
        PApplet.main("MakeLayout");
    }
}

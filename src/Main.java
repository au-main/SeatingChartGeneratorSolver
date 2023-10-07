import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
TODO:   - make a separate program for generating chart
        - on hover or click display student info side by side so I can inspect / see constraint violations
        - toggle student display for skill levels red to green.

        - display list of unseated students and let me drag to reseat?
        - let me randomly generate possibilities after I hand-fix some pairs

        - make improved search --> greedy assignment w/ backtracking?
        - make improved search --> particle filter?
 */

public class Main extends PApplet {
    private static final float TEXT_SIZE = 32;
    private static final int TOP_BUFF = 80;
    private static final int LEFT_BUFF = 60;
    private static final int LIST_DISPLAY = 0;
    private static final int LAYOUT_DISPLAY = 1;
    private static final int NUM_TO_CHECK = 10;
    private static final int NUM_TO_KEEP = 20;

    SeatingChart chart = new SeatingChart();
    ArrayList<SeatingChart> charts = new ArrayList<>();
    ArrayList<DisplayBox> displayList = new ArrayList<DisplayBox>();
    boolean displayConflicts = false;
    float verticalBuffer = 10;
    float horizBuffer = 10;
    float textHeight, boxHeight;
    int numNamesPerCol, numColumns;
    int columnWidth;
    int indexToDisplay = 0;

    int displayMode = LIST_DISPLAY;
    private static final String DATA_DIR = "DataFiles/";
    private static final String CHARTS_DIR = "SavedCharts/";
    private String file = "block6.csv";
    private int lastMouseButton;

    public void settings() {
        size(1000, 1000);
    }

    public void setup() {
        textSize(TEXT_SIZE);
        float strAscent = textAscent();
        float strDescent = textDescent();
        textHeight = strAscent + strDescent;
        boxHeight = textHeight + verticalBuffer;
        numNamesPerCol = (int) ((height - TOP_BUFF) / boxHeight);

        try {
            ArrayList<Student> studentData = SeatingChart.loadStudents(DATA_DIR + file);
            System.out.println(studentData);

            chart.seatStudents(studentData);
        } catch (IOException e) {
            System.err.println("Couldn't read the file: " + DATA_DIR + file);
        }

        numColumns = (int) (chart.getAllStudents().size() / numNamesPerCol) + 1;
        columnWidth = width / numColumns;

        chart.forceReassignAllRandomly();

        System.out.println("Generating great chart!");
        for (int i = 0; i < NUM_TO_CHECK; i++) {
            SeatingChart next = new SeatingChart(chart);
            next.forceReassignAllRandomly();

            charts.add(next);
            Collections.sort(charts, Comparator.comparingDouble(SeatingChart::getScore));
            if (charts.size() > NUM_TO_KEEP) charts.remove(charts.size()-1);
        }

        this.chart = charts.get(indexToDisplay);
        displayList = makeDisplayListFor(chart);
    }

    private ArrayList<DisplayBox> makeDisplayListFor(SeatingChart chart) {
        ArrayList<DisplayBox> out = new ArrayList<>();

        if (displayMode == LIST_DISPLAY) {
            int row = 0;
            int col = 0;

            for (DeskPair desk : chart.getDesks()) {
                DisplayBox box = new DisplayBox(LEFT_BUFF + col * columnWidth, TOP_BUFF + (int) (row * boxHeight), this.columnWidth, (int) boxHeight, desk);
                box.setWidthFromContents(this);
                out.add(box);

                row++;
                if (row >= numNamesPerCol) {
                    row = 0;
                    col++;
                }
            }
            return out;
        } else if (displayMode == LAYOUT_DISPLAY){
            int row = 0;
            int col = 0;

            for (DeskPair desk : chart.getDesks()) {
                DisplayBox box = new DisplayBox(LEFT_BUFF + col * columnWidth, TOP_BUFF + (int) (row * boxHeight), this.columnWidth, (int) boxHeight, desk);
                box.setWidthFromContents(this);
                out.add(box);

                row++;
                if (row >= numNamesPerCol) {
                    row = 0;
                    col++;
                }
            }
            return out;
        }

        return out;
    }

    public void draw() {
        background(255);

        if (displayMode == LIST_DISPLAY) {
            listDisplay();
        } else if (displayMode == LAYOUT_DISPLAY) {
            layoutDisplay();
        }
    }

    private void layoutDisplay() {

    }

    private void listDisplay() {
        text("Left Seat   and   Right Seat", displayList.get(0).getX(), 30);
        //text("Left Seat   and   Right Seat", LEFT_BUFF + 500 + displayList.get(0).getX(), 30);

        int deskNum = 1;
        for (DisplayBox box : displayList) {
            fill(0);
            stroke(0);
            textSize(32);
            text(""+deskNum, 10, box.getY());
            deskNum++;
            box.drawListDisplay(this, displayConflicts);

            if (box.isMouseOver(mouseX, mouseY)) {
                box.highlight(this);
            }
        }

        String scoreDisplay = "Score: " + this.chart.getScore();
        text(scoreDisplay, 500, height - 45);
    }

    public void keyReleased() {
        if (key == 'r' || key == 'R') {
            reshuffle();
        }

        if (key == 's' || key =='S') {
            String filename = getDateString(file);
            this.chart.saveChartToFile(DATA_DIR + filename);
            System.out.println("Saved to " + DATA_DIR + filename);
        }

        if (key == 'd' || key == 'D') {
            displayConflicts = !displayConflicts;
        }

        if (key == 'l' || key == 'L') {
            String studentsFile = getFileWithDialog("Choose the file with the student records for this class");
            String chartFile = getFileWithDialog("Choose the file with the chart you want to load");

            this.chart = SeatingChart.createChartFromFile(chartFile, studentsFile);
            displayList = makeDisplayListFor(chart);
            System.out.println("Loaded chart!");
        }

        if (key == 'f' || key == 'F') {
            for (DisplayBox box : displayList) {
                if (box.isMouseOver(mouseX, mouseY)) {
                    DeskPair desk = box.getDeskPair();
                    if (box.isMouseOverLeftName(mouseX, mouseY, this)) {
                        desk.toggleFreezeLeft();
                    } else if (box.isMouseOverRightName(mouseX, mouseY, this)) {
                        desk.toggleFreezeRight();
                    }
                }
            }
        }

        if (key == CODED && keyCode == UP) {
            indexToDisplay--;
            if (indexToDisplay < 0) indexToDisplay = charts.size() - 1;

            this.chart = charts.get(indexToDisplay);

            this.chart.calculatePenaltyDistributions();
            this.chart.printStatsForMostAndLeast();

            displayList = makeDisplayListFor(chart);
        }

        if (key == CODED && keyCode == DOWN) {
            indexToDisplay++;
            if (indexToDisplay >= charts.size()) indexToDisplay = 0;

            this.chart = charts.get(indexToDisplay);

            this.chart.calculatePenaltyDistributions();
            this.chart.printStatsForMostAndLeast();

            displayList = makeDisplayListFor(chart);
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
        String name = file.substring(0, file.indexOf("."));

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(dateFormatter);

        return name + "-" + formattedDate + ".csv";
    }
    
    public void mousePressed() {
        lastMouseButton = mouseButton;
    }

    public void mouseReleased() {
        for (DisplayBox box : displayList) {
            box.handleMouseClick(mouseX, mouseY, lastMouseButton, this);
        }
        displayList = makeDisplayListFor(chart);
    }

    private void reshuffle() {
        chart.reAssignRandomly();
        displayList = makeDisplayListFor(chart);
    }

    public static String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public static void main(String[] args) {
        PApplet.main("Main");
    }
}

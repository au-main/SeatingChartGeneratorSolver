import processing.core.PApplet;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
TODO:   - make a separate program for generating chart
        - on hover or click display student info side by side so I can inspect / see constraint violations
        - let me unassign + manually re-assign students
        - let me randomly generate possibilities after I hand-fix some pairs

        - make improved search --> greedy assignment w/ backtracking?
        - make improved search --> particle filter?
 */

public class Main extends PApplet {
    private static final float TEXT_SIZE = 32;
    private static final int TOP_BUFF = 80;
    private static final int LIST_DISPLAY = 0;
    private static final int NUM_TO_CHECK = 500000;
    private static final int NUM_TO_KEEP = 10;

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
    private String file = "sampleData.csv";

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
            ArrayList<Student> studentData = SeatingChart.loadStudents(file);
            System.out.println(studentData);

            chart.addStudents(studentData);
        } catch (IOException e) {
            System.err.println("Couldn't read the file: " + file);
        }

        numColumns = (int) (chart.getStudents().size() / numNamesPerCol) + 1;
        columnWidth = width / numColumns;

        chart.assignRandomly();

        System.out.println("Generating great chart!");
        for (int i = 0; i < NUM_TO_CHECK; i++) {
            SeatingChart next = new SeatingChart(chart);
            next.assignRandomly();

            charts.add(next);
            Collections.sort(charts, Comparator.comparingDouble(SeatingChart::getScore));
            if (charts.size() > NUM_TO_KEEP) charts.remove(charts.size()-1);
        }

        this.chart = charts.get(indexToDisplay);
        displayList = makeDisplayListFor(chart);
    }

    private ArrayList<DisplayBox> makeDisplayListFor(SeatingChart chart) {
        ArrayList<DisplayBox> out = new ArrayList<>();
        int row = 0;
        int col = 0;

        for (DeskPair desk : chart.getDesks()) {
            DisplayBox box = new DisplayBox(col * columnWidth, TOP_BUFF + (int) (row * boxHeight), this.columnWidth, (int) boxHeight, desk);
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

    public void draw() {
        background(255);

        text("Left Seat   and   Right Seat", displayList.get(0).getX(), 30);
        text("Left Seat   and   Right Seat", 500 + displayList.get(0).getX(), 30);

        for (DisplayBox box : displayList) {
            box.draw(this, displayConflicts);

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
            this.chart.saveChartToFile(filename);
            System.out.println("Saved to " + filename);
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

    public void mouseReleased() {
        for (DisplayBox box : displayList) {
            box.handleMouseClick(mouseX, mouseY, this);
        }
        displayList = makeDisplayListFor(chart);
    }

    private void reshuffle() {
        chart.assignRandomly();
        displayList = makeDisplayListFor(chart);
    }

/*    private ArrayList<Student> getNamesFrom(ArrayList<DisplayBox> students) {
        ArrayList<Student> currentStudents = new ArrayList<>();
        for ( DisplayBox box : students) {
            Student s1 = box.getStudent1();
            Student s2 = box.getStudent2();
            if (s1 != null) currentStudents.add(s1);
            if (s2 != null) currentStudents.add(s2);
        }
        return currentStudents;
    }*/

    public static String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    public static void main(String[] args) {
        PApplet.main("Main");
    }
}

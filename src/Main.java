import processing.core.PApplet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/*
TODO: adjust font smaller automatically (or clip names) ?
 */

public class Main extends PApplet {
    private static final float TEXT_SIZE = 32;
    private static final int TOP_BUFF = 80;
    private static final int LEFT_BUFF = 80;
    private static final int LIST_DISPLAY = 0;

    private int studentsPerGroup = 3;
    private int currentSelectionIndex = -1;

    SeatingChart chart = new SeatingChart(studentsPerGroup);
    ArrayList<DisplayBox> displayList = new ArrayList<DisplayBox>();
    float verticalBuffer = 10;
    float textHeight, boxHeight;
    int numNamesPerCol, numColumns;
    int columnWidth;

    int displayMode = LIST_DISPLAY;
    private String BASE_PATH = "DataFiles/";
    private String file = "block2.csv";
    private boolean displayConflicts = false;

    public void settings() {
        size(1000, 1000);
    }

    public void setup() {
        textSize(TEXT_SIZE);
        float strAscent = textAscent();
        float strDescent = textDescent();
        textHeight = strAscent + strDescent;
        boxHeight = textHeight + verticalBuffer;
        numNamesPerCol = (int) (studentsPerGroup * (height - TOP_BUFF) / boxHeight);

        try {
            ArrayList<Student> studentData = loadStudents(BASE_PATH + file);
            System.out.println(studentData);
            chart.addStudents(studentData);
            String partnerHistoryFileName = file.substring(0, file.indexOf(".")) + "-partnerHistories.csv";
            chart.loadPartnerHistoryFromFile(BASE_PATH + partnerHistoryFileName);
        } catch (IOException e) {
            System.err.println("Couldn't read the file: " + file);
        }

        numColumns = (int) (chart.getStudents().size() / numNamesPerCol) + 1;
        columnWidth = (width - LEFT_BUFF) / numColumns;

        chart.assignRandomly();
        displayList = makeDisplayListFor(chart);
    }

    private ArrayList<DisplayBox> makeDisplayListFor(SeatingChart chart) {
        ArrayList<DisplayBox> out = new ArrayList<>();
        int row = 0;
        int col = 0;

        for (Group desk : chart.getGroups()) {
            DisplayBox box = new DisplayBox(LEFT_BUFF + col * columnWidth, TOP_BUFF + (int) (row * boxHeight), this.columnWidth, (int) boxHeight, 1, desk.size(), desk);
            //box.setWidthFromContents(this);
            out.add(box);

            row++;
            if (row >= numNamesPerCol) {
                row = 0;
                col++;
            }
        }

        return out;
    }

    // TODO: make one for saving partner records
    // TODO: on load, if no partner records make an empty one
    // TODO: if key 'u' for use, automatically update partner records
    // TODO: way to save/revisit history of charts.

    private HashMap<String, HashSet<String>> loadOldPartnerRecords(String filePath) throws IOException {
        HashMap<String, HashSet<String>> partnerRecords = new HashMap<String, HashSet<String>>();
        String file = readFile(filePath);
        String[] lines = file.split("\n");

        for (String line : lines) {
            line = line.trim();
            try {
                String[] vals = line.split(",");
                String id = vals[1];  // vals[0] is for name, not used
                HashSet<String> partners = new HashSet<>();
                for (int i = 2; i < vals.length; i++) {
                    partners.add(vals[i]);
                }
                partnerRecords.put(id, partners);
            } catch (Exception e) {
                System.err.println("Error making student from line: " + line);
            }
        }

        return partnerRecords;
    }

    private ArrayList<Student> loadStudents(String filePath) throws IOException {
        ArrayList<Student> students = new ArrayList<>();
        String file = readFile(filePath);
        String[] lines = file.split("\n");

        for (String line : lines) {
            line = line.trim();
            try {
                Student s = Student.makeStudentFromRow(line);
                students.add(s);
            } catch (Exception e) {
                System.err.println("Error making student from line: " + line);
            }
        }

        return students;
    }

    public void draw() {
        background(255);

        for (DisplayBox box : displayList) {
            box.draw(this, displayConflicts);

            if (box.isMouseOver(mouseX, mouseY)) {
                box.highlight(this, color(0, 255, 0));
            }
        }

        if (currentSelectionIndex >= 0) {
            displayList.get(currentSelectionIndex).highlight(this, color(0, 255, 255));
        }

        drawRowNumbers();
        drawColHeaders();
    }

    private void drawColHeaders() {
        fill(0);
        stroke(0);
        for (int i = 0; i < studentsPerGroup; i++) {
            text("seat " + (i + 1), LEFT_BUFF + i * (columnWidth / studentsPerGroup), 10);
        }
    }

    private void drawRowNumbers() {
        fill(0);
        stroke(0);
        for (int i = 1; i <= displayList.size(); i++) {
            text("" + i, 5, TOP_BUFF + (i - 1) * boxHeight);
        }
    }

    public void keyReleased() {
        if (key == 'f' || key == 'F') {
            for (DisplayBox box : displayList) {
                if (box.isMouseOver(mouseX, mouseY)) {
                    box.toggleFreezeNameFor(mouseX, mouseY);
                }
            }
        }

        if (key == 'd' || key == 'D') {
            displayConflicts = !displayConflicts;
        }

        if (key == 'r' || key == 'R') {
            reshuffle();
        }

        if (key == 'u' || key == 'U') {     // USE
            String baseFileName = file.substring(0, file.indexOf("."));
            chart.save(BASE_PATH, baseFileName);
        }
    }

    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            for (DisplayBox box : displayList) {
                box.handleMouseClick(mouseX, mouseY, this);
            }
            displayList = makeDisplayListFor(chart);
        }

        if (mouseButton == LEFT) {
            if (currentSelectionIndex >= 0) {
                int clickedIndex = getClickedBox(mouseX, mouseY);
                DisplayBox clickedBox = displayList.get(clickedIndex);
                if (clickedBox == null) return;
                DisplayBox selectedBox = displayList.get(currentSelectionIndex);
                DisplayBox.swapLocations(clickedBox, selectedBox);
                currentSelectionIndex = -1;
            } else {
                currentSelectionIndex = getClickedBox(mouseX, mouseY);
            }
        }
    }

    private int getClickedBox(int mouseX, int mouseY) {
        for (int i = 0; i < displayList.size(); i++) {
            DisplayBox box = displayList.get(i);
            if (box.isMouseOver(mouseX, mouseY)) return i;
        }
        return -1;
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

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeatingChart {
    private ArrayList<Group> groups;
    private ArrayList<Student> students;
    private int studentsPerGroup;

    public SeatingChart(SeatingChart toCopy) {
        this.groups = new ArrayList<>();
        for (Group g : toCopy.groups) {
            this.groups.add( new Group( g ) );
        }
        this.students = toCopy.students;
        this.studentsPerGroup = toCopy.studentsPerGroup;
    }

    public SeatingChart(int studentsPerGroup) {
        students = new ArrayList<>();
        groups = new ArrayList<>();
        this.studentsPerGroup = studentsPerGroup;
    }

    public SeatingChart(ArrayList<Student> students, int studentsPerGroup) {
        this.studentsPerGroup = studentsPerGroup;

        for (Student s : students) {
            this.students.add(s);
        }

        for (int i = 0; i <= students.size() / studentsPerGroup; i++) {
            groups.add(new Group(this, studentsPerGroup));
        }
    }

    public ArrayList<Student> getStudents() {
        return this.students;
    }

    public ArrayList<Group> getGroups() {
        return this.groups;
    }

    public void addStudent(Student s) {
        this.students.add(s);

        Group desk = findDeskWithSpace();
        if (desk != null) {
            desk.setEmptySeatTo(s);
        } else {
            Student[] list = new Student[studentsPerGroup];
            list[0] = s;
            groups.add(new Group(this, list));
        }

    }

    public void deleteStudent(Student s) {
        Group toRemove = null;
        students.remove(s);

        for (Group desk : groups) {
            if (desk.hasStudent(s)) {
                desk.removeStudent(s);

                if (desk.isEmpty()) {
                    toRemove = desk;
                }
            }
        }

        if (toRemove != null) this.groups.remove(toRemove);
    }

    private Group findDeskWithSpace() {
        for (Group desk : groups) {
            if (desk.hasSpace()) return desk;
        }
        return null;
    }

    public void assignRandomly() {
        ArrayList<Student> cleard = clearGroupAssignments();

        Collections.shuffle(cleard);
        int nextStudent = 0;
        for (Group group : groups) {
            while (group.hasSpace()) {
                group.add(cleard.get(nextStudent));
                nextStudent++;
                if (nextStudent >= cleard.size()) return;
            }
        }
    }

    private ArrayList<Student> clearGroupAssignments() {
        ArrayList<Student> cleared = new ArrayList<>();
        for (Group desk : this.groups) {
            ArrayList<Student> removed = desk.clearExceptFrozen();
            if (removed.size() > 0) cleared.addAll(removed);
        }
        return cleared;
    }

    public void addStudents(ArrayList<Student> studentData) {
        for (Student toAdd : studentData) {
            addStudent(toAdd);
        }
    }

    // If more than 1 desk with 1 person missing, we'll combine folks who don't have partners
    public void consolodate() {
        ArrayList<Group> desksWithEmpty = new ArrayList<>();
        for (Group desk : groups) {
            if (desk.hasSpace()) {
                desksWithEmpty.add(desk);
            }
        }
        if (desksWithEmpty.size() <= 1) return;

        while (desksWithEmpty.size() > 1) {
            Group group = desksWithEmpty.remove(0);
            while (!group.isEmpty() && !isFull(desksWithEmpty)) {
                Student s = group.removeStudent();
                assignStudent(s, desksWithEmpty);
            }
            if (group.isEmpty()) {
                groups.remove(group);
            }
        }
    }

    private void assignStudent(Student s, ArrayList<Group> desksWithEmpty) {
        for (Group group : desksWithEmpty) {
            if (group.hasSpace()) {
                group.add(s);
                if (!group.hasSpace()) desksWithEmpty.remove(group);
                return;
            }
        }
    }

    private boolean isFull(ArrayList<Group> desksWithEmpty) {
        for (Group g : desksWithEmpty) {
            if (g.hasSpace()) return false;
        }
        return true;
    }

    /***
     * Return the penalty for this seating chart.
     * @return
     */
    public double getScore() {
        double penalty = 0;
        for (Group desk : groups) {
            penalty += desk.getPenalty();
        }
        return penalty;
    }

    public void save(String pathToFile, String baseName) {
        for (Group group : groups) {
            group.updatePartnerHistories();
        }

        String partnerHistoryFileName = baseName + "-" + "partnerHistories.csv";
        savePartnerHistoryToFile(pathToFile + partnerHistoryFileName);

        // TODO: check if resulitng file includes full filepath
        String seatingChartName = getNextSequentialFilename(pathToFile, baseName);
        saveSeatingChartToFile(pathToFile + seatingChartName);
    }

    /***
     * Must be run after students have been loaded as we look up student objects based on their ids in the saved chart
     * @param filePath
     */
    public void loadSeatingChartFromFile(String filePath) {
        this.groups.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Group group = new Group(this, line, students);
                groups.add(group);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ;
    }

    private void saveSeatingChartToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Group group : this.groups) {
                String row = group.getCsvString();
                writer.write(row);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Couldn't save seating chart");
            System.err.println(e);
        }
    }

    public static String getNextSequentialFilename(String directoryPath, String baseName) {
        String nextNumber = getNumForNextSequentialFilename(directoryPath, baseName);
        return baseName + "-" + nextNumber + ".csv";
    }

    public static String getNumForNextSequentialFilename(String directoryPath, String baseName) {
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();

        if (files == null) {
            throw new IllegalArgumentException("The directory does not exist or is not a directory.");
        }

        int maxNumber = -1;
        Pattern pattern = Pattern.compile(Pattern.quote(baseName) + "-(\\d{2})\\.csv");

        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.matches()) {
                int number = Integer.parseInt(matcher.group(1));
                if (number > maxNumber) {
                    maxNumber = number;
                }
            }
        }

        int nextNumber = maxNumber + 1;
        return String.format("%02d", nextNumber);
    }


    // Method to save the contents of partnerHistory to a file
    public void savePartnerHistoryToFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Student student : this.students) {
                writer.write(student.getFn() + " " + student.getLn().substring(0, 1) + ", " + student.getId());

                for (Map.Entry<String, Integer> entry : student.getPartnerHistory().entrySet()) {
                    writer.write(", " + entry.getKey() + "," + entry.getValue());
                }

                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Couldn't save partner histories");
            System.err.println(e);
        }
    }

    /***
     * Loads partner history from file.  To be run only after student objects have been created since it looks up
     * existing student object to fill in partner history info.
     * @param filePath
     * @throws IOException
     */
    public void loadPartnerHistoryFromFile(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String id = parts[1].trim();
                Student student = getStudent(id);
                if (student == null) {
                    System.err.println("Could not find student object for " + parts[0] + ": " + parts[1]);
                    continue;
                }

                student.clearPartnerHistory();
                for (int i = 2; i < parts.length; i += 2) {
                    String idStr = parts[i].trim();
                    int num = Integer.parseInt(parts[i + 1].trim());
                    student.setPartnerHistoryFor(idStr, num);
                }
            }
        }
    }

    private Student getStudent(String id) {
        for (Student s : students) {
            if (s.getId().equals(id)) {
                return s;
            }
        }
        return null;
    }
}
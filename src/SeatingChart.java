import java.util.ArrayList;
import java.util.Collections;

public class SeatingChart {
    private ArrayList<Group> groups;
    private ArrayList<Student> students;

    public SeatingChart() {
        students = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public SeatingChart(ArrayList<Student> students) {
        for (Student s : students) {
            this.students.add(s);
        }

        for (int i = 0; i < this.students.size()/2 + 1; i++) {
            groups.add( new Group(null, null, this) );
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
        if (groups.size()*2 < students.size()) {
            groups.add( new Group(s, null, this) );
        } else {
            Group desk = findDeskWithSpace();
            if (desk != null) {
                desk.setEmptySeatTo(s);
            } else {
                groups.add( new Group(s, null, this) );
            }
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
        Collections.shuffle(this.students);
        for (int i = 0; i < students.size(); i += 2) {
            Student s1 = students.get(i);
            Student s2 =  (i+1 < students.size()) ? students.get(i+1) : null;

            Group desk = groups.get(i/2);
            desk.clear();
            desk.setLeft(s1);
            desk.setRight(s2);
        }
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
        for (Group group: desksWithEmpty) {
            if (group.hasSpace()) {
                group.add(s);
                if (!group.hasSpace()) desksWithEmpty.remove(group);
                return;
            }
        }
    }

    private boolean isFull(ArrayList<Group> desksWithEmpty) {
        for (Group g:desksWithEmpty) {
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
}
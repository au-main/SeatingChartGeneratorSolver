import java.util.ArrayList;
import java.util.Collections;

public class SeatingChart {
    private ArrayList<Group> groups;
    private ArrayList<Student> students;
    private int studentsPerGroup;

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

    public void save() {
        for (Group group : groups) {
            group.updatePartnerHistories();
        }

        // TODO: save this in an appropriately named file
    }
}
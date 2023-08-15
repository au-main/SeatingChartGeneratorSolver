import java.util.ArrayList;
import java.util.Collections;

public class SeatingChart {
    private ArrayList<DeskPair> desks;
    private ArrayList<Student> students;

    public SeatingChart() {
        students = new ArrayList<>();
        desks = new ArrayList<>();
    }

    public SeatingChart(ArrayList<Student> students) {
        for (Student s : students) {
            this.students.add(s);
        }

        for (int i = 0; i < this.students.size()/2 + 1; i++) {
            desks.add( new DeskPair(null, null, this) );
        }
    }

    public ArrayList<Student> getStudents() {
        return this.students;
    }

    public ArrayList<DeskPair> getDesks() {
        return this.desks;
    }

    public void addStudent(Student s) {
        this.students.add(s);
        if (desks.size()*2 < students.size()) {
            desks.add( new DeskPair(s, null, this) );
        } else {
            DeskPair desk = findDeskWithSpace();
            if (desk != null) {
                desk.setEmptySeatTo(s);
            } else {
                desks.add( new DeskPair(s, null, this) );
            }
        }
    }

    public void deleteStudent(Student s) {
        DeskPair toRemove = null;
        students.remove(s);

        for (DeskPair desk : desks) {
            if (desk.hasStudent(s)) {
                desk.removeStudent(s);

                if (desk.isEmpty()) {
                    toRemove = desk;
                }
            }
        }

        if (toRemove != null) this.desks.remove(toRemove);
    }

    private DeskPair findDeskWithSpace() {
        for (DeskPair desk : desks) {
            if (desk.hasSpace()) return desk;
        }
        return null;
    }

    public void assignRandomly() {
        Collections.shuffle(this.students);
        for (int i = 0; i < students.size(); i += 2) {
            Student s1 = students.get(i);
            Student s2 =  (i+1 < students.size()) ? students.get(i+1) : null;

            DeskPair desk = desks.get(i/2);
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
        ArrayList<DeskPair> desksWithEmpty = new ArrayList<>();
        for (DeskPair desk : desks) {
            if (desk.hasSpace()) {
                desksWithEmpty.add(desk);
            }
        }
        if (desksWithEmpty.size() <= 1) return;

        for (int i = 0; i < desksWithEmpty.size(); i += 2) {
            DeskPair d1 = desksWithEmpty.get(i);
            DeskPair d2 = desksWithEmpty.get(i+1);

            Student s = (d2.getRight() == null?d2.removeLeft():d2.removeRight());
            if (d1.getLeft() == null) {
                d1.setLeft(s);
            } else {
                d1.setRight(s);
            }
        }

        for (DeskPair desk : desksWithEmpty) {
            if (desk.isEmpty()) desks.remove(desk);
        }
    }

    /***
     * Return the penalty for this seating chart.
     * @return
     */
    public double getScore() {
        double penalty = 0;
        for (DeskPair desk : desks) {
            penalty += desk.getPenalty();
        }
        return penalty;
    }
}
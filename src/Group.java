import java.util.ArrayList;
import java.util.Arrays;

public class Group {
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static int nextId = 1;
    private int id;
    private Student[] seats;
    private int groupSize;
    private SeatingChart chart;

    public Group(SeatingChart chart, Student... students) {
        this.id = nextId++;
        this.chart = chart;
        this.groupSize = students.length;
        seats = Arrays.copyOf(students, students.length);
    }

    public Group(SeatingChart chart, int studentsPerGroup) {
        this(chart, new Student[studentsPerGroup]);
    }

    public Group(Student... students) {
        this(null, students);
    }

    public Group(Student left, Student right) {
        this(null, new Student[]{left, right});    // constructor for backward compatability
    }

    public Group(Student left, Student right, SeatingChart chart) {
        this(chart, new Student[]{left, right});        // constructor for backward compatability
    }

    public Student get(int position) {
        return seats[position];
    }

    public Student getLeft() {
        return get(LEFT);
    }

    public Student getRight() {
        return get(RIGHT);
    }

    public Student setLeft(Student left) {
        return set(LEFT, left);
    }

    public Student setRight(Student right) {
        return set(RIGHT, right);
    }

    public Student set(int position, Student student) {
        if (seats[position] != null) {
            System.err.println("Warning: overwriting an existing student: ");
            System.err.println(seats[position]);
        }
        Student removed = seats[position];
        seats[position] = student;
        return removed;
    }

    public Student remove(int position) {
        Student removed = seats[position];
        seats[position] = null;
        return removed;
    }

    public Student removeRight() {
        return remove(RIGHT);
    }

    public Student removeLeft() {
        return remove(LEFT);
    }

    public void setEmptySeatTo(Student s) {
        for (int position = 0; position < seats.length; position++) {
            if (isEmpty(position)) {
                set(position, s);
                return;
            }
        }
    }

    private boolean rightEmpty() {
        return isEmpty(RIGHT);
    }

    private boolean leftEmpty() {
        return isEmpty(LEFT);
    }

    public boolean hasSpace() {
        for (int pos = 0; pos < seats.length; pos++) {
            if (seats[pos] == null) return true;
        }
        return false;
    }

    public boolean hasStudent(Student s) {
        for (Student student : seats) {
            if (student != null && student.equals(s)) return true;
        }

        return false;
    }

    public void removeStudent(Student s) {
        for (int position = 0; position < seats.length; position++) {
            Student student = seats[position];
            if (student != null && student.equals(s)) {
                remove(position);
                return;
            }
        }
    }

    public Student removeStudent() {
        for (int position = 0; position < seats.length; position++) {
            if (seats[position] != null) {
                return remove(position);
            }
        }
        return null;
    }

    public void clear() {
        seats = new Student[seats.length];
    }

    public boolean isEmpty() {
        for (int position = 0; position < seats.length; position++) {
            if (!isEmpty(position)) return false;
        }
        return true;
    }

    public boolean isEmpty(int position) {
        return seats[position] == null;
    }

    public void delete(int position) {
        if (isEmpty(position)) return;
        if (chart == null) return;
        chart.deleteStudent(get(position));
        chart.consolodate();
    }

    public void deleteLeft() {
        delete(LEFT);
    }

    public void deleteRight() {
        delete(RIGHT);
    }

/*    // TODO: re-think this with 3 or more people =\
    public double getPenalty() {
        double penalty = 0;
        if (getLeft() != null) {
            penalty += getLeft().matchScoreWith(getRight());
        }
        if (getRight() != null) {
            penalty += getRight().matchScoreWith(getLeft());
        }

        return penalty;
    }*/

    // TODO: penalities
    // sitting with same partners

    public double getPenalty() {
        double penalty = 0;

        if (getLeft() != null) {
            penalty += getLeft().matchScoreWith(getRight());
        }
        if (getRight() != null) {
            penalty += getRight().matchScoreWith(getLeft());
        }

        return penalty;
    }

    public boolean add(Student s) {
        for (int position = 0; position < seats.length; position++) {
            if (seats[position] == null) {
                seats[position] = s;
                return true;
            }
        }
        return false;
    }

    public int size() {
        return seats.length;
    }
}
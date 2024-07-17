import javax.sql.rowset.FilteredRowSet;
import java.util.ArrayList;
import java.util.Arrays;

public class Group {
    private static final double[] PENALTY_LEVEL = {4, 16, 16*4, 16*16};
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static int nextId = 1;
    private int id;
    private Student[] seats;
    private boolean[] frozen;

    private int groupSize;
    private SeatingChart chart;

    public Group(Group toCopy) {
        this.groupSize = toCopy.groupSize;
        this.chart = toCopy.chart;
        this.id = toCopy.id;
        this.seats = new Student[toCopy.groupSize];
        for (int i = 0; i < seats.length; i++) {
            seats[i] = toCopy.seats[i];
        }
        this.frozen = new boolean[toCopy.groupSize];
        for (int i = 0; i < groupSize; i++) {
            frozen[i] = toCopy.frozen[i];
        }
    }

    public Group(SeatingChart chart, Student... students) {
        this.id = nextId++;
        this.chart = chart;
        this.groupSize = students.length;
        seats = Arrays.copyOf(students, students.length);
        frozen = new boolean[seats.length];
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

    public Group(SeatingChart chart, String csvRow, ArrayList<Student> students) {
        this.chart = chart;
        String[] vals = csvRow.split(",");
        this.groupSize = Integer.parseInt(vals[0]);
        this.id = nextId++;
        seats = new Student[groupSize];
        frozen = new boolean[groupSize];

        for (int i = 1; i < vals.length; i++) {
            Student s = getStudentWith(vals[i], students);
            seats[i-1] = s;
        }
    }

    private Student getStudentWith(String val, ArrayList<Student> students) {
        if (val.equals("null")) return null;
        for (Student student : students) {
            if (val.equals(student.getId())) return student;
        }
        return null;
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

    public ArrayList<Student> clearExceptFrozen() {
        ArrayList<Student> cleared = new ArrayList<>();
        for (int i = 0; i < seats.length; i++) {
            if (!frozen[i] && seats[i] != null) {
                cleared.add(seats[i]);
                seats[i] = null;
            }
        }
        return cleared;
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

    public double getPenalty() {
        double penalty = 0;

        penalty += getRepeatedPartnerPentalty();
        penalty += getAffinityGroupPenalty();
        return penalty;
    }

    /***
     * if a group member wants female partners but doesn't get them, that's an affinity penalty
     * @return
     */
    private double getAffinityGroupPenalty() {
        double penalty = 0;
        for (int i = 0; i < this.seats.length; i++) {
            if (seats[i] == null) continue;

            for (int j = i+1; j < seats.length; j++) {
                if (seats[j] == null) continue;

                penalty += PENALTY_LEVEL[1] * seats[i].isAffinityViolation(seats[j]);
                penalty += PENALTY_LEVEL[1] * seats[j].isAffinityViolation(seats[i]);
            }
        }

        return penalty;
    }

    private double getRepeatedPartnerPentalty() {
        double penalty = 0;
        for (int i = 0; i < this.seats.length; i++) {
            if (seats[i] == null) continue;

            for (int j = i+1; j < seats.length; j++) {
                if (seats[j] == null) continue;

                penalty += PENALTY_LEVEL[0] * seats[i].timesSatWith(""+seats[j].getId());
            }
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

    public void updatePartnerHistories() {
        for (int i = 0; i < seats.length; i++) {
            if (seats[i] == null) continue;

            for (int j = i+1; j < seats.length; j++) {
                if (seats[j] == null) continue;

                seats[i].recordSittingWith(seats[j]);
                seats[j].recordSittingWith(seats[i]);
            }
        }
    }

    public void toggleFreeze(int position) {
        frozen[position] = !frozen[position];
    }

    public boolean isFrozen(int position) {
        return frozen[position];
    }

    public int getGroupSize() {
        return this.groupSize;
    }

    public String getCsvString() {
        String row = "" + this.getGroupSize() + ",";
        for (Student student : seats) {
            row += (student == null?"null":student.getId()) + ",";
        }
        row = row.substring(0, row.length()-1); // remove last comma
        return row;
    }
}
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SeatingChart {
    private ArrayList<DeskPair> desks;
    private ArrayList<Student> students;

    public SeatingChart() {
        students = new ArrayList<>();
        desks = new ArrayList<>();
    }

    public SeatingChart(ArrayList<Student> students) {
        this.students.addAll(students);

        for (int i = 0; i < this.students.size() / 2 + 1; i++) {
            desks.add(new DeskPair(null, null, this));
        }
    }

    public SeatingChart(SeatingChart toCopy) {
        students = new ArrayList<>();
        desks = new ArrayList<>();
        students.addAll(toCopy.students);
        for (DeskPair desk : toCopy.desks) {
            DeskPair copy = new DeskPair(desk);
            copy.setChart(this);
            this.desks.add(copy);
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
        if (desks.size() * 2 < students.size()) {
            desks.add(new DeskPair(s, null, this));
        } else {
            DeskPair desk = findDeskWithSpace();
            if (desk != null) {
                desk.setEmptySeatTo(s);
            } else {
                desks.add(new DeskPair(s, null, this));
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
            Student s2 = (i + 1 < students.size()) ? students.get(i + 1) : null;

            DeskPair desk = desks.get(i / 2);
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
            DeskPair d2 = desksWithEmpty.get(i + 1);

            Student s = (d2.getRight() == null ? d2.removeLeft() : d2.removeRight());
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

    /***
     * Genereate mean, median, min and max penalties for
     * @param student
     * @return array containing [ min, max, mean, median, standard devation ].
     */
    public void assignPenaltyStatsTo(Student student) {
        List<Double> penalties = getPenaltyListFor(student);

        double minimum = Collections.min(penalties);
        double maximum = Collections.max(penalties);

        double sum = 0;
        for (double value : penalties) {
            sum += value;
        }
        double mean = sum / penalties.size();

        Collections.sort(penalties);
        int size = penalties.size();
        double median;
        if (size % 2 == 0) {
            int middle = size / 2;
            double median1 = penalties.get(middle - 1);
            double median2 = penalties.get(middle);
            median = (median1 + median2) / 2;
        } else {
            median = penalties.get(size / 2);
        }

        double sumOfSquaredDifferences = 0;
        for (double value : penalties) {
            double diff = value - mean;
            sumOfSquaredDifferences += diff * diff;
        }
        double variance = sumOfSquaredDifferences / penalties.size();
        double standardDeviation = Math.sqrt(variance);

        student.setMax(maximum);
        student.setMin(minimum);
        student.setMean(mean);
        student.setMedian(median);
        student.setStdev(standardDeviation);
    }

    private List<Double> getPenaltyListFor(Student student) {
        List<Double> data = new ArrayList<>();
        for (Student s : this.getStudents()) {
            if (!s.equals(student)) {
                data.add( student.getMatchScoreFor(s) );
            }
        }
        return data;
    }

    public void calculatePenaltyDistributions() {
        System.out.println("Running calculatePenaltyDistriutions()");
        for (Student s : getStudents()) {
            assignPenaltyStatsTo(s);
        }
    }

    public void printStatsForMostAndLeast() {
        System.out.println("Must run calculatePenaltyDistributions() first!");
        Collections.sort(this.students, Comparator.comparingDouble(Student::getMin));
        Student least = students.get(0);
        Student most = students.get(students.size()-1);

        System.out.println(least.getDisplayName() + ": min" + least.getMin() + " median: " + least.getMedian() + " max: " + least.getMax());
        System.out.println(most.getDisplayName() + ": min" + most.getMin() + " median: " + most.getMedian() + " max: " + most.getMax());
    }

    public String toString() {
        return "Score: " + this.getScore() + " : " + this.desks;
    }
}
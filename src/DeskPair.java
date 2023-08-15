public class DeskPair {
    private static int nextId = 1;
    private int id;
    private Student left, right;
    private SeatingChart chart;

    public DeskPair(Student left, Student right) {
        this.id = nextId++;
        this.left = left;
        this.right = right;
    }

    public DeskPair(Student left, Student right, SeatingChart chart) {
        this.id = nextId++;
        this.left = left;
        this.right = right;
        this.chart = chart;
    }

    public Student getLeft() {
        return left;
    }

    public void setLeft(Student left) {
        this.left = left;
    }

    public Student getRight() {
        return right;
    }

    public void setRight(Student right) {
        this.right = right;
    }

    public Student removeRight() {
        Student toRemove = this.right;
        this.right = null;
        return toRemove;
    }

    public Student removeLeft() {
        Student toRemove = this.left;
        this.left = null;
        return toRemove;
    }

    public void setEmptySeatTo(Student s) {
        if (leftEmpty()) {
            setLeft(s);
            return;
        }
        if (rightEmpty()) {
            setRight(s);
            return;
        }
    }

    private boolean rightEmpty() {
        return right == null;
    }

    private boolean leftEmpty() {
        return left == null;
    }

    public boolean hasSpace() {
        return leftEmpty() || rightEmpty();
    }

    public boolean hasStudent(Student s) {
        Student right = getRight();
        if (right != null && right.equals(s)) return true;

        Student left = getLeft();
        if (left != null && left.equals(s)) return true;

        return false;
    }

    public void removeStudent(Student s) {
        Student right = getRight();
        if (right != null && right.equals(s)) {
            removeRight();
            return;
        }

        Student left = getLeft();
        if (left != null && left.equals(s)) {
            removeLeft();
            return;
        }
    }

    public void clear() {
        removeLeft();
        removeRight();
    }

    public boolean isEmpty() {
        return leftEmpty() && rightEmpty();
    }

    public void deleteLeft() {
        if (getLeft() == null) return;
        if (chart != null) {
            chart.deleteStudent(getLeft());
            chart.consolodate();
        }
    }

    public void deleteRight() {
        if (getRight() == null) return;
        if (chart != null) {
            chart.deleteStudent(getRight());
            chart.consolodate();
        }
    }

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
}
package com.lab6;

/**
 * Schema / Model class for a Student document.
 *
 * Each field maps to a MongoDB document field:
 *   _id        -> auto-generated ObjectId (handled by the driver)
 *   name       -> student full name
 *   rollNumber -> unique roll number
 *   branch     -> e.g. "CSE", "ECE"
 *   marks      -> percentage marks (double)
 */
public class Student {

    private String name;
    private String rollNumber;
    private String branch;
    private double marks;

    // ─────────────────────────── Constructors ───────────────────────────

    public Student() {}

    public Student(String name, String rollNumber, String branch, double marks) {
        this.name       = name;
        this.rollNumber = rollNumber;
        this.branch     = branch;
        this.marks      = marks;
    }

    // ─────────────────────────── Getters / Setters ──────────────────────

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public String getRollNumber()                    { return rollNumber; }
    public void   setRollNumber(String rollNumber)   { this.rollNumber = rollNumber; }

    public String getBranch()                { return branch; }
    public void   setBranch(String branch)   { this.branch = branch; }

    public double getMarks()                 { return marks; }
    public void   setMarks(double marks)     { this.marks = marks; }

    // ─────────────────────────── toString ───────────────────────────────

    @Override
    public String toString() {
        return String.format(
            "Student { name='%s', rollNumber='%s', branch='%s', marks=%.1f }",
            name, rollNumber, branch, marks
        );
    }
}

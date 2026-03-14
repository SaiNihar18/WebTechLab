package com.lab6;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.List;

/**
 * LAB 6 – MongoDB CRUD Operations using Java
 *
 * Database  : studentDB
 * Collection: students
 * Schema    : Student (name, rollNumber, branch, marks)
 */
public class Main {

    // ── Connection string – points to our local MongoDB instance ─────────
    private static final String CONNECTION_URI = "mongodb://localhost:27017";
    private static final String DB_NAME        = "studentDB";
    private static final String COLLECTION     = "students";

    public static void main(String[] args) {

        // Open a connection (auto-closed by try-with-resources)
        try (MongoClient client = MongoClients.create(CONNECTION_URI)) {

            MongoDatabase   db         = client.getDatabase(DB_NAME);
            MongoCollection<Document> students = db.getCollection(COLLECTION);

            // ── DROP any previous data so each run is clean ───────────────
            students.drop();
            System.out.println("=== LAB 6 : MongoDB CRUD with Java ===\n");

            // ─────────────────────────────────────────────────────────────
            // 1. CREATE  –  insertMany()
            // ─────────────────────────────────────────────────────────────
            System.out.println("─── CREATE (Insert Documents) ───");

            List<Student> newStudents = Arrays.asList(
                new Student("Nihar Sai",    "CSE001", "CSE", 88.5),
                new Student("Ananya Reddy", "CSE002", "CSE", 76.0),
                new Student("Rahul Verma",  "ECE001", "ECE", 91.2),
                new Student("Priya Sharma", "ECE002", "ECE", 65.8),
                new Student("Arjun Singh",  "MECH001","MECH",79.3)
            );

            // Convert Student objects → BSON Documents for MongoDB
            List<Document> docs = newStudents.stream()
                .map(Main::toDocument)
                .toList();

            InsertManyResult insertResult = students.insertMany(docs);
            System.out.println("Inserted " + insertResult.getInsertedIds().size()
                               + " students into '" + COLLECTION + "'.");
            System.out.println("Inserted IDs: " + insertResult.getInsertedIds().values() + "\n");

            // ─────────────────────────────────────────────────────────────
            // 2. READ  –  find() / find(filter)
            // ─────────────────────────────────────────────────────────────
            System.out.println("─── READ (Find All Students) ───");
            for (Document d : students.find()) {
                System.out.println("  " + fromDocument(d));
            }

            System.out.println("\n─── READ (Find CSE branch only) ───");
            Bson branchFilter = Filters.eq("branch", "CSE");
            for (Document d : students.find(branchFilter)) {
                System.out.println("  " + fromDocument(d));
            }

            System.out.println("\n─── READ (Find students with marks > 80) ───");
            Bson marksFilter = Filters.gt("marks", 80.0);
            for (Document d : students.find(marksFilter)) {
                System.out.println("  " + fromDocument(d));
            }

            // ─────────────────────────────────────────────────────────────
            // 3. UPDATE  –  updateOne() and updateMany()
            // ─────────────────────────────────────────────────────────────
            System.out.println("\n─── UPDATE (Update marks for CSE001) ───");
            Bson findOne    = Filters.eq("rollNumber", "CSE001");
            Bson setMarks   = Updates.set("marks", 95.0);
            UpdateResult ur = students.updateOne(findOne, setMarks);
            System.out.println("Matched: " + ur.getMatchedCount()
                               + " | Modified: " + ur.getModifiedCount());

            // Add a new field 'grade' to ALL students whose marks >= 80
            System.out.println("\n─── UPDATE (Add 'grade=A' to all students with marks >= 80) ───");
            Bson highMarks    = Filters.gte("marks", 80.0);
            Bson addGradeA    = Updates.set("grade", "A");
            UpdateResult umr  = students.updateMany(highMarks, addGradeA);
            System.out.println("Matched: " + umr.getMatchedCount()
                               + " | Modified: " + umr.getModifiedCount());

            // ─────────────────────────────────────────────────────────────
            // 4. READ again – verify updates
            // ─────────────────────────────────────────────────────────────
            System.out.println("\n─── READ (All students after update) ───");
            for (Document d : students.find()) {
                System.out.printf("  name=%-15s roll=%-8s branch=%-5s marks=%-6.1f grade=%s%n",
                    d.getString("name"),
                    d.getString("rollNumber"),
                    d.getString("branch"),
                    d.getDouble("marks"),
                    d.getString("grade") != null ? d.getString("grade") : "B"
                );
            }

            // ─────────────────────────────────────────────────────────────
            // 5. DELETE  –  deleteOne() and deleteMany()
            // ─────────────────────────────────────────────────────────────
            System.out.println("\n─── DELETE (Remove student with rollNumber ECE002) ───");
            Bson           deleteFilter = Filters.eq("rollNumber", "ECE002");
            DeleteResult   dr           = students.deleteOne(deleteFilter);
            System.out.println("Deleted count: " + dr.getDeletedCount());

            System.out.println("\n─── DELETE (Remove all MECH branch students) ───");
            Bson         mechFilter = Filters.eq("branch", "MECH");
            DeleteResult dmr        = students.deleteMany(mechFilter);
            System.out.println("Deleted count: " + dmr.getDeletedCount());

            // ─────────────────────────────────────────────────────────────
            // 6. READ – final state
            // ─────────────────────────────────────────────────────────────
            System.out.println("\n─── READ (Final – remaining students) ───");
            long total = students.countDocuments();
            System.out.println("Total documents remaining: " + total);
            for (Document d : students.find()) {
                System.out.println("  " + fromDocument(d));
            }

            System.out.println("\n=== CRUD operations completed successfully ===");
        }
    }

    // ─────────────────────────── Helpers ────────────────────────────────

    /** Convert a Student POJO into a MongoDB BSON Document */
    private static Document toDocument(Student s) {
        return new Document("name",       s.getName())
                   .append("rollNumber",  s.getRollNumber())
                   .append("branch",      s.getBranch())
                   .append("marks",       s.getMarks());
    }

    /** Convert a MongoDB BSON Document back into a Student POJO */
    private static Student fromDocument(Document d) {
        return new Student(
            d.getString("name"),
            d.getString("rollNumber"),
            d.getString("branch"),
            d.getDouble("marks")
        );
    }
}

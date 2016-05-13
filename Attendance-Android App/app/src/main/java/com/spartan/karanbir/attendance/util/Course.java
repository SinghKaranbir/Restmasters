package com.spartan.karanbir.attendance.util;

/**
 * Created by karanbir on 5/10/16.
 */
public class Course {

    private String Id;
    private String courseName;
    private String courseId;

    public Course(String id, String courseName, String courseId) {
        Id = id;
        this.courseName = courseName;
        this.courseId = courseId;
    }

    public String getId() {
        return Id;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getCourseId() {
        return courseId;
    }
}

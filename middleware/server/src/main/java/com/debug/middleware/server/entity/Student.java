package com.debug.middleware.server.entity;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class Student implements Serializable {
    private String id;
    private String userName;
    private String name;

    public Student() {
    }

    public Student(String id, String userName, String name) {
        this.id = id;
        this.userName = userName;
        this.name = name;
    }
}

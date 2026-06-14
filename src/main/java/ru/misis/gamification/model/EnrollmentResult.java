package ru.misis.gamification.model;

import ru.misis.gamification.entity.Course;
import ru.misis.gamification.entity.Group;

public record EnrollmentResult(Course course, Group group) {}

//*********************************************************
// Project: BCS430W - Senior Project
//
// Project Name: OnyxRidge
//
// File: WorkDay.java
//
// Written by: Raymond O'Neill
//
// Date written: 11/1/20
//
// Details: Holds information regarding a day of work
//          (the day itself and the total hours worked)
//*********************************************************
package com.icecrown.onyxridgecm.workseries;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class WorkDay {
    private GregorianCalendar day;
    private double hours;

    public WorkDay() {
        day = new GregorianCalendar();
        hours = 00;
    }
    public WorkDay(GregorianCalendar day, double hours)
    {
        this.day = new GregorianCalendar(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));
        this.hours = hours;
    }
    public WorkDay(WorkDay day) {
        this.equate(day);
    }

    public double getHours() {
        return hours;
    }

    public GregorianCalendar getDay() {
        return day;
    }

    public void setDay(GregorianCalendar day) {
        this.day = day;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }
    public WorkDay equate(WorkDay day) {
        this.day = new GregorianCalendar(day.getDay().get(Calendar.YEAR), day.getDay().get(Calendar.MONTH), day.getDay().get(Calendar.DAY_OF_MONTH));
        this.hours = day.hours;
        return this;
    }

    public String GenerateHyphenDateString() {
        String s = (day.get(Calendar.MONTH) + 1) + "-" + day.get(Calendar.DAY_OF_MONTH) + "-" + day.get(Calendar.YEAR);
        return s;
    }

}
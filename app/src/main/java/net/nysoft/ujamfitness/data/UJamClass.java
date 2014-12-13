/*
 * Copyright (c) 2014 PayPal, Inc.
 *
 * All rights reserved.
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 */

package net.nysoft.ujamfitness.data;

/**
 * TODO: Write Javadoc for UJamClass.
 *
 * @author pngai
 */
public class UJamClass {

    protected String instructor;
//    protected String _dateTime;
    protected String day;
    protected String time;

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

//    public String get_dateTime() {
//        return _dateTime;
//    }
//
//    public void set_dateTime(String _dateTime) {
//        this._dateTime = _dateTime;
//    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

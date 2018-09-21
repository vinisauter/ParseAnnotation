package com.vas.annotation.model;

import com.parse.annotation.BindParseObject;

import java.io.Serializable;
import java.util.Date;

@BindParseObject
public class Person implements Serializable {
    String name;
    String surname;
    Date birthDate;

    Gender aEnum;
    byte aByte;
    short aShort;
    int anInt;
    long aLong;
    float aFloat;
    double aDouble;
    char aChar;
    boolean aBoolean;
}
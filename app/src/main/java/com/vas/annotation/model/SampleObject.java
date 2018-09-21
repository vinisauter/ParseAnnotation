package com.vas.annotation.model;

import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParsePolygon;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.annotation.BindParseObject;
import com.parse.annotation.CaseFormat;
import com.parse.annotation.Ignore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 19/09/2018.
 */
@BindParseObject(value = "SAMPLE_OBJECT_NAME", columnCaseFormat = CaseFormat.LOWER_UNDERSCORE)
public class SampleObject extends ParseObject {
    Object field;
    String fieldString;
    byte[] fieldBytes;
    Number fieldNumber;
    JSONArray fieldJsonArray;
    List<UserObject_> fieldList;
    Map<String, UserObject_> fieldMap;
    JSONObject fieldJsonObject;
    Integer fieldInt;
    Double fieldDouble;
    Long fieldLong;
    Boolean fieldBoolean;
    Date fieldDate;

    ParseObject fieldParseObject;
    ParseUser fieldParseUser;
    ParseFile fieldParseFile;
    ParseGeoPoint fieldParseGeoPoint;
    ParsePolygon fieldParsePolygon;
    ParseRelation<UserObject_> fieldRelation;

    @Ignore
    public Object fieldIgnore;

    public void methodVoid() {

    }

    public String methodString() {
        return "String";
    }

    @Override
    public String toString() {
        return "SampleObject{" +
                "field=" + field +
                ", fieldString='" + fieldString + '\'' +
                ", fieldBytes=" + Arrays.toString(fieldBytes) +
                ", fieldNumber=" + fieldNumber +
                ", fieldJsonArray=" + fieldJsonArray +
                ", fieldList=" + fieldList +
                ", fieldMap=" + fieldMap +
                ", fieldJsonObject=" + fieldJsonObject +
                ", fieldInt=" + fieldInt +
                ", fieldDouble=" + fieldDouble +
                ", fieldLong=" + fieldLong +
                ", fieldBoolean=" + fieldBoolean +
                ", fieldDate=" + fieldDate +
                ", fieldParseObject=" + fieldParseObject +
                ", fieldParseUser=" + fieldParseUser +
                ", fieldParseFile=" + fieldParseFile +
                ", fieldParseGeoPoint=" + fieldParseGeoPoint +
                ", fieldParsePolygon=" + fieldParsePolygon +
                ", fieldRelation=" + fieldRelation +
                ", fieldIgnore=" + fieldIgnore +
                '}';
    }
}

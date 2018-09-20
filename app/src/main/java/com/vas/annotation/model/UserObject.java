package com.vas.annotation.model;

import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.annotation.BindParseObject;

/**
 * Created by user on 19/09/2018.
 */
@BindParseObject("User")
public class UserObject extends ParseUser {
    ParseFile photo;
}

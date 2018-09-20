package com.vas.annotation;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseUser;
import com.parse.annotation.ParseInit;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        ParseInit.registerSubclasses();
//        Parse.initialize(new Parse.Configuration.Builder(this)
//                .applicationId("YOUR_APP_ID")
//                .clientKey("YOUR_CLIENT_KEY")
//                .server("http://localhost:1337/parse/")
//                .build()
//        );

        ParseUser.enableAutomaticUser();
        ParseInit.initialize(new Parse.Configuration.Builder(this)
                .applicationId("DrPJsSLa3elKiSheX2Sjl67wqUb5ynytesl83smK")
                .clientKey("Mvz5E8Kt6prVDkuxf34dqGJP04zx422FDuZuy7No")
                .server("https://parseapi.back4app.com/")
                .build());
    }
}
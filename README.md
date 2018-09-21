# ParseAnnotation
[![License](https://img.shields.io/github/license/blipinsk/RecyclerViewHeader.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
---

Library usage
=============

If you're using [Parse.com Android SDK](https://docs.parseplatform.org/android/guide/) and you're tired of constantly creating setters and getters for every single field, this library might be for you.

With a simple annotation `@BindParseObject` you can take this:

Person.java/

    @BindParseObject
    public class Person extends ParseObject {
        String name;
        String surname;
        Date birthDate;
        ParseFile photo;
    }

App.java/

    import com.parse.Parse;
    import com.parse.annotation.ParseInit;
    public class App extends Application {
        @Override
        public void onCreate() {
            super.onCreate();
            ParseInit.initialize(new Parse.Configuration.Builder(this)
                    .applicationId("YOUR_APP_ID")
                    .clientKey("YOUR_CLIENT_KEY")
                    .server("http://localhost:1337/parse/")
                    .build()
            );
        }
    }

and the library generates this:
Person_.java/

    @ParseClassName("Person")
    public class Person_ extends Person {
        public static final String CLASS_NAME = "Person";
        public static final String NAME = "name";
        public static final String SURNAME = "surname";
        public static final String BIRTH_DATE = "birthDate";
        public static final String PHOTO = "photo";
    
        public static Person_ create() {
            return (Person_) create(CLASS_NAME);
        }
    
        public static Person_ createWithoutData(String objectId) {
            return (Person_) createWithoutData(CLASS_NAME, objectId);
        }
    
        public static Person_ fetch(String objectId, GetCallback<Person_> callback) {
            Person_ object = createWithoutData(objectId);
            object.fetchIfNeededInBackground(callback);
            return object;
        }
    
        public static ParseQuery<Person_> query() {
            return ParseQuery.getQuery(CLASS_NAME);
        }
    
        public String getName() {
            super.name = (java.lang.String) get(NAME);
            return super.name;
        }
    
        public Person_ setName(String name) {
            super.name = name;
            put(NAME, name);
            return this;
        }
    
        public String getSurname() {
            super.surname = (java.lang.String) get(SURNAME);
            return super.surname;
        }
    
        public Person_ setSurname(String surname) {
            super.surname = surname;
            put(SURNAME, surname);
            return this;
        }
    
        public Date getBirthDate() {
            super.birthDate = (java.util.Date) get(BIRTH_DATE);
            return super.birthDate;
        }
    
        public Person_ setBirthDate(Date birthDate) {
            super.birthDate = birthDate;
            put(BIRTH_DATE, birthDate);
            return this;
        }
    
        public ParseFile getPhoto() {
            super.photo = (com.parse.ParseFile) get(PHOTO);
            return super.photo;
        }
    
        public Person_ setPhoto(ParseFile photo) {
            super.photo = photo;
            put(PHOTO, photo);
            return this;
        }
    
        @Override
        public String toString() {
            String oId = getObjectId();
            return super.getClassName() + "{ objectId=" + oId + " }";
        }
    }

ParseInit.java/

    public class ParseInit {
        public static final void registerSubclasses() {
            com.parse.ParseObject.registerSubclass(Person_.class);
            com.parse.ParseObject.registerSubclass(UserObject_.class);
            com.parse.ParseObject.registerSubclass(SampleObject_.class);
        }
    
        public static void initialize(Configuration configuration) {
            registerSubclasses();
            com.parse.Parse.initialize(configuration);
        }
    }


Usage notes
-----------

The library assumes two things considering naming conventions:
 
  1. *Classes* should be named using `UpperCamelCase` ("ThisIsAnExample")
  2. *Fields* should be named using `LowerCamelCase` ("thisIsAnExample")
  
**It doesn't mean the library won't work if you name your classes/fields differently!**
It can just case some unexpected method names (it depends on the `quava` methods behaviour).

Including In Your Project
-------------------------

Add it in your root build.gradle at the end of repositories:

```groovy
allprojects {
	repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

Add the dependency
[![](https://jitpack.io/v/parse-community/Parse-SDK-Android.svg)](https://jitpack.io/#parse-community/Parse-SDK-Android)
```groovy
dependencies {
    implementation "com.github.parse-community.Parse-SDK-Android:parse:${parseVersion}"

    implementation 'com.github.vinisauter.ParseAnnotation:parse-annotation:master-SNAPSHOT'
    annotationProcessor 'com.github.vinisauter.ParseAnnotation:parse-processor:master-SNAPSHOT'
}
```

Developed by
============
 * Vinicius Sauter

License
=======

    Copyright 2018 Vinicius Sauter
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

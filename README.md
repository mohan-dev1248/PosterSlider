# PosterSlider
Easy to use Image/Video Slider for Android

## How to download
### Gradle
add this line to your module build.gradle dependecies block:

    compile 'com.github.mohan-dev1248:PosterSlider:1.0.2'
    
Also in your root gradle file
    
    allprojects {
		  repositories {
			...
			maven { url 'https://jitpack.io' }
		  }
	  }

## How use this library
### XML

```xml
<com.asura.library.views.PosterSlider
        android:id="@+id/poster_slider"
        android:layout_width="match_parent"
        android:layout_height="300dp"
        app:defaultIndicator="circle"
        app:imageSlideInterval="5000"
        app:layout_constraintTop_toTopOf="parent"
        app:hideIndicators="true"
        app:loopSlides="true" />
```

### Java
```java
  
    PosterSlider posterSlider = (posterSlider) findViewById(R.id.poster_slider);
    List<Poster> posters=new ArrayList<>();
    //add poster using remote url
    posters.add(new RemoteImage("Put poster image url here ..."));
    //add poster using resource drawable
    posters.add(new DrawableImager(R.drawable.yourDrawable));
    //add raw video using raw resource file
    posters.add(new RawVideo(R.raw.yourRawFile));
    //add remote video using uri
    posters.add(new RemoteVideo(Put your Uri here);
    posterSlider.setPosters(posters);
```
## Licence
Copyright 2016 Saeed Shahini

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and

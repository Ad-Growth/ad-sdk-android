# ad-sdk-android

# Ad SDK Android

[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://jitpack.io/v/Ad-Growth/ad-sdk-android.svg)](https://jitpack.io/#ad-Growth/ad-sdk-android)

This project is an SDK for consuming ads on the android platform.

## Download

Gradle:

Add Jitpack repository on your project `build.gradle` or `settings.gradle`

```gradle
repositories {
   maven { url 'https://jitpack.io' }
}
```

Add adserver dependency on your `app/build.gradle`

```gradle
dependencies {
  implementation 'com.github.Ad-Growth:ad-sdk-android:1.0.0'
}
```

Or Maven:

```xml

<repositories>
   <repository>
      <id>jitpack.io</id>
      <url>https://jitpack.io</url>
   </repository>
</repositories>

<dependency>
  <groupId>com.github.Ad-Growth</groupId>
  <artifactId>ad-sdk-android</artifactId>
  <version>1.0.0</version>
</dependency>
```

## How do I use Ad SDK?

1. Initializing the SDK

```java
import com.adgrowth.adserver.AdServer;
import com.adgrowth.adserver.entities.ClientProfile;
import com.adgrowth.adserver.exceptions.SDKInitException;
public class MainActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // get the client_key registering the app on Adserver Panel
        String client_key = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX";
        ClientProfile profile = new ClientProfile();
        // you can add some client info and interests
        profile.setAge(16);
        profile.setGender(ClientProfile.Gender.ALL);
        profile.addInterest("games");
        profile.addInterest("adventure_games");

        // if your app have acess to user location, you can provide it for better advertisement experience
        ClientAddress address = profile.getClientAddress();
        address.setLatitude(40.68905007092866);
        address.setLongitude(-74.04438969510598);

        // or you can provide a country, state and/or city
        address.setCountry("US");
        address.setState("NW");
        address.setState("New york");


        AdServer.initialize(client_key, profile, new AdServer.Listener() {
            @Override
            public void onInit() {
                // lib was initialized
            }
            @Override
            public void onFailed(SDKInitException e) {
               // failed to initialize, handle it
            }
        });
    }
}
```

2.  InterstitialAd

Register the app display place with alias on [AdServer Panel](https://publisher.ad.adgrowth.com)

```java
...
import com.adgrowth.adserver.AdRequest;
import com.adgrowth.adserver.InterstitialAd;
import com.adgrowth.adserver.exceptions.AdRequestException;
public class MainActivity extends AppCompatActivity implements AdServer.Listener {
           @Override
    public void onInit() {
        // when the lib was initialized
       loadButton.setVisibility(VISIBLE);
        // each display place on your app need to be registered on AdServer Panel.
        String unit_id = "main_screen_1";
        // instantiate a AdRequest class
        AdRequest adRequest = new AdRequest(this);
        interstitialAd = new InterstitialAd(unit_id, adRequest);
        interstitialAd.setListener(new InterstitialAd.Listener() {
            @Override
            public void onClicked() {
            }
            @Override
            public void onDismissed() {
                interstitialAd = null; // destroy the ad after consumed

            }
            @Override
            public void onImpression() {
            }
            @Override
            public void onFailedToLoad(AdRequestException exception) {
            }
            @Override
            public void onFailedToShow(String code) {
            }
            @Override
            public void onLoad() {
                // ad is ready to be showed
                interstitialAd.show(MainActivity.this)
            }
        });
        // load a interstitial
        interstitialAd.load(MainActivity.this);
    }
}
```

## Compatibility

- **Minimum Android SDK**: Ad SDK requires a minimum API level of 24.
- **Compile Android SDK**: Ad SDK requires you to compile against API 28 or later.

## Dependencies

This SDK use [Glide](https://github.com/bumptech/glide) to load images and gifs.

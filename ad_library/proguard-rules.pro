-obfuscationdictionary "../dic.txt"
-classobfuscationdictionary "../dic.txt"
-packageobfuscationdictionary "../dic.txt"

-mergeinterfacesaggressively
-overloadaggressively
-repackageclasses 'com.adgrowth.adserver'

-keep public class com.adgrowth.adserver.* {public *;}
-keep public class com.adgrowth.adserver.enums.* {public *;}
-keep public class com.adgrowth.adserver.entities.* {public *;}
-keep public class com.adgrowth.adserver.helpers.* {public *;}
-keep public interface com.adgrowth.adserver.interfaces.* {public *;}
-keep public class com.adgrowth.adserver.exceptions.* {public *;}
-keep public class com.adgrowth.adserver.views.* {public *;}

-keepclassmembers class com.adgrowth.adserver.helpers.LayoutHelpers {
    public static ** Companion;
}
-keep,allowobfuscation public class com.adgrowth.internal.** {public *; }
-keep,allowobfuscation public interface com.adgrowth.internal.** {public *; }

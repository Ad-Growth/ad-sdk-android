-obfuscationdictionary "/Users/dhoncrisley/Documents/Github/ad-sdk-android/dic.txt"
-classobfuscationdictionary "/Users/dhoncrisley/Documents/Github/ad-sdk-android/dic.txt"
-packageobfuscationdictionary "/Users/dhoncrisley/Documents/Github/ad-sdk-android/dic.txt"

-mergeinterfacesaggressively
-overloadaggressively
-repackageclasses 'com.adgrowth.adserver'

-keep public class com.adgrowth.adserver.* {public *;}
-keep public class com.adgrowth.adserver.entities.* {public *;}
-keep public class com.adgrowth.adserver.exceptions.* {public *;}
-keep public class com.adgrowth.adserver.views.* {public *;}
#
#-injars       in.jar
#-outjars      out.jar
#-libraryjars  <java.home>/jmods/java.base.jmod(!**.jar;!module-info.class)
#-printmapping out.map
#
#-keep public class * {
#    public protected *;
#}
#
#-keepparameternames
#-renamesourcefileattribute SourceFile
#-keepattributes Signature,Exceptions,*Annotation*,
#                InnerClasses,PermittedSubclasses,EnclosingMethod,
#                Deprecated,SourceFile,LineNumberTable
#
#-keepclasseswithmembernames,includedescriptorclasses class * {
#    native <methods>;
#}
#
#-keepclassmembers,allowoptimization enum * {
#    public static **[] values();
#    public static ** valueOf(java.lang.String);
#}
#
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}
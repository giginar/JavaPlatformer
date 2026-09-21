# Deep Drift application rules for the standalone R8 release pipeline.

# Preserve useful crash-deobfuscation metadata and reflection annotations used by SDKs.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault

# Android instantiates the launcher from the merged manifest. AAPT also emits a generated
# manifest/resource rule, but this explicit application entry-point rule documents the contract.
-keep,allowoptimization class com.game.diver.android.AndroidLauncher {
    public <init>();
}

# libGDX and FreeType contain Java native declarations whose class names, member names, and
# descriptors are part of their JNI lookup contract. Keep only classes that declare native
# methods. Member optimization is intentionally disallowed because argument removal changes
# the JNI descriptor even when the Java/native method name is retained.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# gdx-controllers-core selects this Android implementation by a literal Class.forName name and
# constructs it reflectively with the public no-argument constructor.
-keep,allowoptimization class com.badlogic.gdx.controllers.android.AndroidControllers {
    public <init>();
}

# These are the providers named by the two packaged kotlinx-coroutines service descriptors.
-keep,allowoptimization class kotlinx.coroutines.android.AndroidDispatcherFactory {
    public <init>();
}
-keep,allowoptimization class kotlinx.coroutines.android.AndroidExceptionPreHandler {
    public <init>();
}

# The following enum constant names are persisted in libGDX Preferences. Their classes may be
# renamed, but their public enum fields must keep their historical identifiers across upgrades.
-keepclassmembers,allowoptimization enum com.game.model.Achievement {
    public static final ** *;
}
-keepclassmembers,allowoptimization enum com.game.model.ChallengeModifier {
    public static final ** *;
}
-keepclassmembers,allowoptimization enum com.game.model.DiverSuit {
    public static final ** *;
}
-keepclassmembers,allowoptimization enum com.game.model.PermanentUpgrade {
    public static final ** *;
}
-keepclassmembers,allowoptimization enum com.game.model.RunDifficulty {
    public static final ** *;
}
-keepclassmembers,allowoptimization enum com.game.settings.DisplaySettingsStore$WindowMode {
    public static final ** *;
}
-keepclassmembers,allowoptimization enum com.game.settings.DisplaySettingsStore$TextScale {
    public static final ** *;
}

# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line operand information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line operand information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#-dontobfuscate

#-keep class android.support.graphics.** { *; }
#-keep class android.graphics.** { *; }
#-keep class android.animation.** { *; }
#-keep class * extends com.derekbearded.android.swipecalc.anim.LineAnimator$LineHolder
#-keep class * extends com.derekbearded.android.swipecalc.anim.CircleAnimator$CircleHolder
#-keep class * extends com.derekbearded.android.swipecalc.anim.Animator
#-keep class * extends com.derekbearded.android.swipecalc.anim.CircleAnimator
#-keep class * extends com.derekbearded.android.swipecalc.anim.LineAnimator

#-keepattributes InnerClasses
#-keep class com.derekbearded.android.swipecalc.**
#-keep class com.derekbearded.android.swipecalc.*$*
#-keepclassmembers class com.derekbearded.android.swipecalc.** {
#    *;
#}
#-keepclassmembers class com.derekbearded.android.swipecalc.*$* {
#    *;
#}

#-keepclassmembers class android.support.** { public float getWidth(); }
#-keepclassmembers class android.support.** { public int getOpacity(); }
#-keepclassmembers class android.support.** { public void setWidth(float); }
#-keepclassmembers class android.support.** { public void setOpacity(int); }

#-keepclassmembers class android.** { public float getWidth(); }
#-keepclassmembers class android.** { public int getOpacity(); }
#-keepclassmembers class android.** { public void setWidth(float); }
#-keepclassmembers class android.** { public void setOpacity(int); }


#-keepclassmembers class * extends com.derekbearded.android.swipecalc.anim.Animator{
# public int getOpacity();
#}
#-keepclassmembers class * extends com.derekbearded.android.swipecalc.anim.Animator{
# public void setOpacity(int);
#}
#-keepclassmembers class * {
#   public void *(android.view.View);
#}

#-keepclassmembers class * extends com.derekbearded.android.swipecalc.anim.CircleAnimator$CircleHolder{
# *;
# float getDiameter();
# void setDiameter(float);
# getDiameter();
# setDiameter(float);
#}
#-keepclassmembers class * extends com.derekbearded.android.swipecalc.anim.LineAnimator$LineHolder{
# *;
# public float getWidth();
# public void setWidth(float);
# public int getAlpha();
# public void setAlpha(int);
# float getWidth();
# void setWidth(float);
# int getAlpha();
# void setAlpha(int);
#}

#-keepclassmembernames

#-keep class com.derekbearded.android.swipecalc.anim.** {
#   public protected *;
#   *;
#}

#-keepclassmembers class com.derekbearded.android.swipecalc.anim.CirleAnimator$CircleHolder {
#*;
#}
#-keep class com.derekbearded.android.swipecalc.anim.CirleAnimator$CircleHolder

#-keepclassmembers class com.derekbearded.android.swipecalc.anim.**
#-keep class com.derekbearded.android.swipecalc.anim.**
# private void set*(...);
# private *** get*(...);
#}

#-keepclassmembers class com.derekbearded.android.swipecalc.anim.** {
 #!public !protected private *;
 #*;
#}
#-keepclassmembers class com.derekbearded.android.swipecalc.anim.CircleAnimator$CircleHolder { void setDiameter(float); }
#-keepclassmembers class com.derekbearded.android.swipecalc.anim.CircleAnimator$CircleHolder { float getDiameter(); }
#-keepclassmembers class com.derekbearded.android.swipecalc.anim.LineAnimator$LineHolder { int getAlpha(); }
#-keepclassmembers class com.derekbearded.android.swipecalc.anim.LineAnimator$LineHolder { void setAlpha(int); }
#-keepclassmembers class com.derekbearded.android.swipecalc.anim.LineAnimator$LineHolder { void setWidth(float); }
#-keepclassmembers class com.derekbearded.android.swipecalc.anim.LineAnimator$LineHolder { float getWidth(); }
#-keep class android.support.graphics.** {
# *;
# }
#-keep class android.animation.** {
# *;
#}
#-keep class android.graphics.** {
# *;
#}

#-keep class android.animation.**
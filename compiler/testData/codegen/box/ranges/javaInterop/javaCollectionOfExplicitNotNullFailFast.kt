// LANGUAGE: +StrictJavaNullabilityAssertions
// TARGET_BACKEND: JVM
// IGNORE_BACKEND_K1: JVM_IR
// WITH_STDLIB
// JVM_TARGET: 1.8

// Note: this fails on JVM_IR because of KT-36343.
// It requires potentially breaking changes in FE, so please, don't touch it until the language design decision.

// FILE: box.kt
import kotlin.test.*

fun box(): String {
    // Sanity check to make sure there IS an exception even when not in a for-loop
    try {
        val i = J.listOfNotNull()[0]
        return "Fail: should throw on get()"
    } catch (e: NullPointerException) {}

    try {
        for (i in J.listOfNotNull()) {
            return "Fail: should throw on get() in loop header"
        }
    }
    catch (e: NullPointerException) {}
    return "OK"
}

// FILE: J.java
import java.util.*;
import org.jetbrains.annotations.*;

public class J {
    public static List<@NotNull Integer> listOfNotNull() {
        return Collections.singletonList(null);
    }
}

// FILE: NotNull.java
package org.jetbrains.annotations;

import java.lang.annotation.*;

// org.jetbrains.annotations used in the compiler is version 13, whose @NotNull does not support the TYPE_USE target (version 15 does).
// We're using our own @org.jetbrains.annotations.NotNull for testing purposes.
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE})
public @interface NotNull {
}

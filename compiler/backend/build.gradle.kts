plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":kotlin-annotations-jvm"))
    api(project(":compiler:util"))
    api(project(":compiler:backend-common"))
    api(project(":compiler:frontend"))
    api(project(":core:descriptors.jvm"))
    api(project(":compiler:frontend.common.jvm"))
    api(project(":compiler:serialization"))
    api(project(":compiler:backend.common.jvm"))
    compileOnly(intellijCore())
    compileOnly(libs.intellij.fastutil)
    compileOnly(libs.intellij.asm)
    compileOnly(libs.guava)
}

sourceSets {
    "main" {
        projectDefault()
    }
    "test" {}
}

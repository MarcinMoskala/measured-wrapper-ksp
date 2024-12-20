
plugins {
    kotlin("jvm")
}

// Versions are declared in 'gradle.properties' file
val kspVersion: String by project

dependencies {
    implementation("com.squareup:kotlinpoet:2.0.0")
    implementation("com.squareup:kotlinpoet-ksp:2.0.0")
    implementation(project(":annotations"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    testImplementation(kotlin("test"))
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.9")
}

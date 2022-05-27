plugins {
    id("com.android.application") version "7.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
    id("com.github.ben-manes.versions") version "0.42.0" apply false
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}

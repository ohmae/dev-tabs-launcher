plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("com.github.ben-manes.versions") version "0.46.0" apply false
}

tasks.create("clean", Delete::class) {
    delete(rootProject.buildDir)
}

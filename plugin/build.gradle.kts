plugins {
    id("io.freefair.lombok") version "9.0.0"
    `maven-publish`
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
    options.encoding = "UTF-8"
}

// 编译桩：citizensapi 2.0.24 与 PvPDiffTimer 1.4.4 源均不可获取（官方库经代理 404 / 上游仓库消失）。
// 桩仅满足编译期符号解析，不打入产物（本服无 Citizens/PvPDiffTimer，hook 永不启用）。
sourceSets {
    main {
        java {
            srcDir("../stub")
        }
    }
}

tasks.jar {
    exclude("net/citizensnpcs/**")
    exclude("br/net/fabiozumbi12/**")
}

dependencies {
    compileOnly(project(":api"))
    compileOnly(project(":skills"))

    compileOnly("com.zaxxer:HikariCP:3.4.2")
    compileOnly("de.keyle:knbt:0.0.5")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") { exclude(group = "org.bukkit", module = "bukkit") }
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("org.mozilla:rhino:1.7.15")

    compileOnly("org.slf4j:slf4j-api:1.7.30")
    compileOnly("org.slf4j:slf4j-nop:1.7.30")

    compileOnly("org.apache.logging.log4j:log4j-core:2.17.1")

    compileOnly("at.blvckbytes:RawMessage:0.2")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.5")
    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.5")
    compileOnly("com.github.retrooper:packetevents-spigot:2.14.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("org.bstats:bstats-bukkit:1.7")
    compileOnly("org.mongodb:mongodb-driver:3.12.11")
    compileOnly("de.keyle:knbt:0.0.5")

    // Prefer Lombok as compileOnly + annotationProcessor in Gradle:
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    compileOnly("io.sentry:sentry:1.7.30")

}

fun getVersionFromName(filename: String): String {
    return """\d+(\.\d+)+(-SNAPSHOT)?""".toRegex().find(filename)?.value
        ?: throw GradleException("Failed to get PE version from: '$filename'")
}

publishing {
    repositories {
        if (System.getenv("MVN_USER") != null) {
            maven {
                val repoType = if (project.version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
                // Eg: https://mvn.lib.co.nz/repositories/maven-%branch%/
                url = uri(System.getenv("MVN_PATH").replace("%branch%", repoType))

                credentials {
                    username = System.getenv("MVN_USER")
                    password = System.getenv("MVN_PASS")
                }
            }
        } else {
            mavenLocal()
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            artifactId = "mypet"
        }
    }
}

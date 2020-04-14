# WebCraft

![](https://i.loli.net/2020/04/11/j5YimS2nNXUbQpJ.png)

WebCraft is a open-source GUI library based on Minecraft Forge for createing GUIs with HTML, CSS and JavaScript. 

(It is still under development and only supports for Minecraft 1.15.2) 

WebCraft accomplishes the GUI-rendering system by [Ultralight](https://ultralig.ht) as Web engine.

Please obey WebCraft Open-Source License (LGPL) and the licenses of Ultralight.

The operating system we support: Windows & Linux (We only have tested on Ubuntu, Manjaro and ArchLinux so we don't know whether it can work on other distros or not. If you test on others, please let us know)

About the support for Mac OS: We have tried our best to, but we don't have any Mac OSX devices, so we gave up. If you can make it support MAC, please send a pull request.

API docs are editing......

## TODO List

Full version(v1.0):

* Use with Inventories/Slots/TileEntities.
* API doc with details.

Plans in the future:

* Provide some APIs for bukkit plugins.
* Use Fabric instead of Forge
* Develop a verison in Minecraft 1.12.2.
* Separate our mod from Mod Loaders and let it be a special mod.

## How to install WebCraft?

You can download it from this [link](https://github.com/Hookan/hookan.github.io/tree/master/cafe/qwq/webcraft).

Then put it into the `mods` folder.(needs to install Forge)

And you should install [VC redist](https://aka.ms/vs/16/release/vc_redist.x64.exe) if you are using Windows.

When the first time you run this mod, it will download natives jar. It may be VERY slow because we only use one thread to download. So, you can download it by yourself and unzip it in the folder `natives-<webcraft_version>` .

## Get Started with WebCraft

Tips: All of the `<webcraft_version>` need to be insteaded of the version of WebCraft you want. (For example, you put the version `0.3.3` into the `webcraft:<webcraft_version>` and the result is `webcraft:0.3.3`)

Firstly, download Forge MDK and set `mappings` to `mappings channel: 'snapshot', version: '20200306-1.15.1'` in `build.gradle` (you can skip this step if you still want to apply your appropriate `mappings`)

Secondly, put these codes in `build.gradle` (NOT IN `buildscript`!!).

```groovy
repositories {
    maven { url 'https://maven.qwq.cafe' } // or https://ci.qwq.cafe/maven
}
```

Thirdly, add `implementation 'cafe.qwq:webcraft:<webcraft_version>:dev'` or `implementation fg.deobf('cafe.qwq:webcraft:runtime')`(which can help you change mapping freely) to `dependencies`.

This is an example for a completly available`build.gradle`. (Modified by mdk's default build.gradle)

```groovy
buildscript {
    repositories {
        maven { url = 'https://files.minecraftforge.net/maven' }
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath group: 'net.minecraftforge.gradle', name: 'ForgeGradle', version: '3.+', changing: true
    }
}
apply plugin: 'net.minecraftforge.gradle'
// Only edit below this line, the above code adds and enables the necessary things for Forge to be setup.
apply plugin: 'eclipse'
apply plugin: 'maven-publish'

version = '1.0'
group = 'com.yourname.modid' // http://maven.apache.org/guides/mini/guide-naming-conventions.html
archivesBaseName = 'modid'

sourceCompatibility = targetCompatibility = compileJava.sourceCompatibility = compileJava.targetCompatibility = '1.8' // Need this here so eclipse task generates correctly.

minecraft {
    mappings channel: 'snapshot', version: '20200306-1.15.1'
    runs {
        client {
            workingDirectory project.file('run')

            // Recommended logging data for a userdev environment
            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
            property 'forge.logging.console.level', 'debug'

            mods {
                examplemod {
                    source sourceSets.main
                }
            }
        }

        server {
            workingDirectory project.file('run')

            // Recommended logging data for a userdev environment
            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
            property 'forge.logging.console.level', 'debug'

            mods {
                examplemod {
                    source sourceSets.main
                }
            }
        }

        data {
            workingDirectory project.file('run')

            // Recommended logging data for a userdev environment
            property 'forge.logging.markers', 'SCAN,REGISTRIES,REGISTRYDUMP'

            // Recommended logging level for the console
            property 'forge.logging.console.level', 'debug'

            args '--mod', 'examplemod', '--all', '--output', file('src/generated/resources/')

            mods {
                examplemod {
                    source sourceSets.main
                }
            }
        }
    }
}

repositories {
    maven { url 'https://maven.qwq.cafe' }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.15.2-31.1.0'
    implementation 'cafe.qwq:webcraft:<webcraft_version>:dev'
    //or implementation fg.deobf('cafe.qwq:webcraft:<webcraft_version>:runtime')
}

jar {
    manifest {
        attributes([
            "Specification-Title": "examplemod",
            "Specification-Vendor": "examplemodsareus",
            "Specification-Version": "1", // We are version 1 of ourselves
            "Implementation-Title": project.name,
            "Implementation-Version": "${version}",
            "Implementation-Vendor" :"examplemodsareus",
            "Implementation-Timestamp": new Date().format("yyyy-MM-dd'T'HH:mm:ssZ")
        ])
    }
}

def reobfFile = file("$buildDir/reobfJar/output.jar")
def reobfArtifact = artifacts.add('default', reobfFile) {
    type 'jar'
    builtBy 'reobfJar'
}
publishing {
    publications {
        mavenJava(MavenPublication) {
            artifact reobfArtifact
        }
    }
    repositories {
        maven {
            url "file:///${project.projectDir}/mcmodsrepo"
        }
    }
}

```

Then add these codes to `mods.toml`:

```toml
[[dependencies.modid]] # Please change it to your modid.
    modId="webcraft"
    mandatory=true
    versionRange="[<webcraft_version>]"
    ordering="NONE"
    side="BOTH" # We will provide some APIs for server in the future.
```

Then you can use WebCraft API in your mod.

```java
WebScreen screen = new WebScreen(new StringTextComponent("MyGui"));
Minecraft.getInstance()
    .displayScreen(screen.addView(new View().loadHTML("<p>Hello,World!</p>")));
```

## How to build development environment for WebCraft

* Linux

Firstly, you should install GCC and make sure you can use the command `g++`

For Debian/Ubuntu:

```sh
sudo apt update
sudo apt install gcc
sudo apt install g++
```

For ArchLinux/Manjaro:

```sh
sudo pacman -Syy
sudo pacman -S gcc
```

For CentOS/Fedora:

```sh
sudo dnf update
sudo dnf install gcc
sudo dnf install gcc-c++
```

Then import this project into your IDE.

* Windows

Install the BuildTool of VisualStudio at first.(Only need to install VC++)

Then you need to find the path you install in and configure the environment variables. If you configure it successfully, you should  able to use the command `cl` in the command line.

At last you should import this project into your IDE.
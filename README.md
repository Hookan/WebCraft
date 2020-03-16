# WebCraft

这是一个基于Minecraft Forge的GUI API（本mod还在开发中）

主要功能是可以使用HTML/CSS/JS来进行GUI的编写

使用了[Ultralight](https://ultralig.ht)作为Web引擎

如果你需要使用本mod，请不要忘了遵守本mod的开源协议（LGPL）以及Ultralight的协议

支持系统：Windows和Linux（仅在Ubuntu/Manjaro两个发行版上测试过，其它发行版未知，如果你在别的发行版测试通过/不通过可以告诉我）的64位版本

关于MAC的兼容问题：我已经尝试过兼容，无奈没有MAC设备，故放弃，如果你可以兼容MAC，欢迎发PullRequest

API文档正在编写中


## TODO List:

* JavaScript与Java的交互
* 事件系统
* 和背包/格子/TileEntity的配套使用
* API详细使用文档

## 如何安装本mod

可以到[这里](https://github.com/Hookan/hookan.github.io/tree/master/cafe/qwq/webcraft)下载最新版本（也可以是历史版本）的WebCraft（jar文件名中需要包含`runtime`）

然后放入mods文件夹即可（需要安装Forge）

另外，对于Windows用户，还需要安装[VC++运行库](https://aka.ms/vs/16/release/vc_redist.x64.exe)

由于本mod在首次运行时会下载natives，又因为本mod采用单线程下载，速度较慢，你可以顺便下载对应版本对应系统的natives（jar文件名中包含了`natives`和`win`或者`linux`），然后再将其解压到`mods/webcraft/natives-<verion>`（需要将`<version>`替换成你所安装的WebCraft版本（不是MC/Forge版本））


## 如何构建附属mod的开发环境

* 方案1（如果你懒得折腾，对mcp的方法名无所谓的话，你可以参照这个方法）

下载forge mdk后，把mappings修改为`mappings channel: 'snapshot', version: '20200306-1.15.1'`

然后添加以下代码

```groovy
repositories {
    maven { url 'https://maven.qwq.cafe' }
}
```

再在`dependencies`添加`implementation 'cafe.qwq:webcraft:0.3.2'`

完整的build.gradle示范
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
    //修改的地方
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

//添加的地方（1）
repositories {
    maven { url 'https://maven.qwq.cafe' }
}

dependencies {
    minecraft 'net.minecraftforge:forge:1.15.2-31.1.0'
    //添加的地方（2）
    implementation 'cafe.qwq:webcraft:0.3.2'
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

然后再在`mods.toml`中添加：
```toml
[[dependencies.modid]] # 请修改成你的modid
    modId="webcraft"
    mandatory=true
    versionRange="[0.3.2]"
    ordering="NONE"
    side="BOTH" # 未来WebCraft会有一些服务端的API
```

* 方案2（你可以自由的选择你mod的mcp mappings）

首先到[https://maven.qwq.cafe/cafe/qwq/webcraft/0.3.2/webcraft-0.3.2-runtime.jar](https://maven.qwq.cafe/cafe/qwq/webcraft/0.3.2/webcraft-0.3.2-runtime.jar)下载运行环境的WebCraft

然后参考[ustc-zzzz的教程](https://fmltutor.ustc-zzzz.net/3.5.1-%E4%BD%BF%E7%94%A8%E5%85%B6%E4%BB%96Mod%E7%9A%84API.html)来生成一份你想要的mappings的WebCraft并引用

## 如何构建本mod的开发环境

* Linux

首先要确定你安装了GCC并且能使用g++命令

然后用IDE导入本项目的build.gradle即可

* Windows

首先安装VisualStudio的BuildTool（只用安装VC++）

其次你需要找到你安装的路径并配置环境变量，如果配置成功，你应该可以在命令行中使用`cl`命令

然后用IDE导入本项目的build.gradle即可

## 感谢名单(以下排名不分先后)：

drenal为mod兼容MAC做出的努力

协助测试人员：

* Bogos外群群员（南小笙 dragon_龙鑫 等）
* 蓝鲸
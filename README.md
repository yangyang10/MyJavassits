# MyJavassits
javassist dome

#使用javassist自定义Gradle插件,在编译时插入代码，实现一个low版本的eventbus

1、新建一个Module，main文件夹下创建groovy文件夹
2、修改 build.gradle文件

    apply plugin: 'groovy'
    apply plugin: 'maven'

    dependencies {
        compile gradleApi() //gradle sdk
        compile localGroovy() //groovy sdk

        compile 'com.android.tools.build:gradle:3.5.1'
        compile 'org.javassist:javassist:3.22.0-GA'
    }

    repositories {
        jcenter()
    }

    //发布到本地
    uploadArchives {
        repositories.mavenDeployer {
            repository(url: uri('../test')) //仓库的路径，此处是项目根目录下的 repo 的文件夹
            pom.groupId = 'com.timel.bus'  //groupId ，自行定义，一般是包名
            pom.artifactId = 'timelBus' //artifactId ，自行定义
            pom.version = '1.0.0' //version 版本号
        }
    }

3、新建JavassistPlugin.groovy文件

    public class JavassistPlugin implements Plugin<Project> {

        void  apply(Project project){
            def log = project.logger
            log.error "======================"
            log.error "Javassist 修改class 测试"
            log.error "======================="
            project.android.registerTransform(new JavassistTransform(project))
        }
    }

4、resources文件下创建文件夹 META-INF 再创建文件夹 gradle-plugins
    创建properties文件

    implementation-class=com.timel.bus.JavassistPlugin  （对应你implements Plugin<Project>的文件）


5、使用
    项目目录下的build.gradle
        buildscript {
            repositories {
                google()
                jcenter()
                maven {
                    url("test")//本地依赖
                }

            }
            dependencies {
                classpath 'com.android.tools.build:gradle:3.5.1'


                //本地依赖
                classpath 'com.timel.bus:timelBus:1.0.0'
            }
        }

    在使用的Module添加
        apply plugin: 'com.timel.bus'


6.发布到本地
    点击右边Gradle 找到你的module -> clean  upload -> uploadArchives



#遗留问题

1、第一个无法读取另一个module中的注解文件；

2、继承AppCompatActivity使用javassist创建onCreateMethod和onDestory方法super方法无法找到父类；
    2.1 androidx库无法加入到ClassPool








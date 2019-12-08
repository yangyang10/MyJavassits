package com.timel.bus

import org.gradle.api.Plugin
import org.gradle.api.Project


public class JavassistPlugin implements Plugin<Project> {

    void  apply(Project project){
        def log = project.logger
        log.error "======================"
        log.error "Javassist 修改class 测试"
        log.error "======================="
        project.android.registerTransform(new JavassistTransform(project))
    }
}
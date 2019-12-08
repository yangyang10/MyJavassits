package com.timel.bus

import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project


public class LogTimeHelper{
    final static String prefix = "\n long startTime = System.currentTimeMillis();\n"
    final static String postfix = "\n long endTime = System.currentTimeMillis();\n"
    final static String LogTimeAnnotation = "com.timel.bus.annotation.LogTime"

    public static void initLogTime(Project project, String methodName, String className, CtMethod ctmethod, CtClass c,String path){
        //开始javassist修改class文件
        project.logger.error("开始修改class文件" + className+"."+methodName)
        String outputStr = "\n System.out.println(\" this method "+ className +"."+methodName
        +" cost:\" + (endTime - startTime) +\"ms\")"

        String newMethodName = methodName + '$' + "impl"
        //对方法进行重新命名
        ctmethod.setName(newMethodName)
        project.logger.error("替换老方法："+className +"."+methodName + "改名为："+newMethodName)

        //创建新的方法，复制原来的方法，名字为原来的名字
        CtMethod newMethod = CtMethod.copy(ctmethod,methodName,c,null)

        StringBuilder builder = new StringBuilder()
        builder.append("{")
        builder.append(prefix)
        //$$ 表示方法中的所有参数
        builder.append(newMethodName+"("+'$$'+");\n")
        builder.append(postfix)
        builder.append(outputStr)
        builder.append("}")
        //方法编写
        newMethod.setBody(builder.toString())
        //方法绑定到类中
        c.addMethod(newMethod)
        //写到磁盘中
        c.writeFile(path)
        //把CtClass从ClassPool中移除，防止内存泄漏
        c.detach()

    }
}
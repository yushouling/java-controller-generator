package com.daoshu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * 启动类，由于本工程上传的文件是放在工程里的，发布时请使用tomcat部署
 * <pre>如需使用jar包部署，需要修改上传文件时的保存路径</pre>
 *
 * @author YUSL
 */
@SpringBootApplication
@ServletComponentScan
public class JavaControllerGeneratorApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(JavaControllerGeneratorApplication.class, args);
    }

}

package com.daoshu.yapi.codegen.controller;

import com.daoshu.yapi.codegen.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * 上传文件
 */
@WebServlet("/upload")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServlet.class);

    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        InputStream input = null;
        OutputStream output = null;
        // 上传后的文件名
        String uploadedFileName = "";
        // 原文件名
        String originalFileName = "";
        try {
            request.setCharacterEncoding("UTF-8");
            Part part = request.getPart("uploadFile");
            originalFileName = part.getSubmittedFileName();
            input = part.getInputStream();
            // 要保存的目标文件的目录
            String tagDir = getServletContext().getRealPath("upload");
            // 避免文件名重复使用uuid来避免,产生一个随机的uuid字符
            String uuid = UUID.randomUUID().toString().replaceAll("-", "");
            // 后缀名
            int fileSuffixIndex = originalFileName.lastIndexOf(".");
            String suffixName;
            if(fileSuffixIndex <= 0) {
                printResult(response, "上传失败：不是json文件");
                return;
            } else {
                suffixName = originalFileName.substring(fileSuffixIndex);
            }
            uploadedFileName = uuid + suffixName;
            File uploadFolder = new File(tagDir);
            if(! uploadFolder.exists()) {
                uploadFolder.mkdirs();
            }
            output = new FileOutputStream(new File(tagDir, uploadedFileName));
            int len = 0;
            byte[] buff = new byte[1024 * 8];
            while ((len = input.read(buff)) > -1) {
                output.write(buff, 0, len);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }
        try {
            // 请求转发到生成代码页
            request.setAttribute(Constant.UPLOADED_FILE_NAME, uploadedFileName);
            request.setAttribute(Constant.ORIGINAL_FILE_NAME, originalFileName);
            request.getRequestDispatcher("/").forward(request, response);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printResult(response, "上传成功");
    }

    private void printResult(HttpServletResponse response, String msg) {
        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().print(msg);
        } catch (IOException e) {
            LOGGER.error("", e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

}
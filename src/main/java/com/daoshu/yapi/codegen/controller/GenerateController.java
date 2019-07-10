package com.daoshu.yapi.codegen.controller;

import com.daoshu.yapi.codegen.common.Constant;
import com.daoshu.yapi.codegen.gen.CodeGen;
import com.daoshu.yapi.codegen.parse.ParseYApiJson;
import com.daoshu.yapi.codegen.util.ZipUtils;
import com.daoshu.yapi.codegen.vo.ClassVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * 生成代码
 *
 * @author YUSL
 */
@RestController
public class GenerateController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateController.class);

    /**
     * 跳转到生成页面
     *
     * @return
     */
    @RequestMapping("/")
    public ModelAndView start(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("start");

        String uploadedFileName = (String) request.getAttribute(Constant.UPLOADED_FILE_NAME);
        String originalFileName = (String) request.getAttribute(Constant.ORIGINAL_FILE_NAME);
        List<String> classNameList = (List<String>) request.getAttribute(Constant.CLASS_NAME_LIST);
        if (!StringUtils.isEmpty(uploadedFileName)) {
            mv.addObject(Constant.UPLOADED_FILE_NAME, uploadedFileName);
            mv.addObject(Constant.ORIGINAL_FILE_NAME, originalFileName);
            mv.addObject(Constant.CLASS_NAME_LIST, classNameList);
        }
        return mv;
    }

    /**
     * 生成代码
     *
     * @param packageName
     * @return
     */
    @PostMapping("/generate")
    public String generate(@RequestParam(name = "package", required = false) String packageName,
                           @RequestParam(name = "file", required = false) String jsonFileName,
                           @RequestParam(name = "classNameSelected", required = false) String[] classNameSelected,
                           HttpServletResponse response, HttpServletRequest request) {
        if (StringUtils.isEmpty(jsonFileName)) {
            return "解析文件不存在！";
        }
        if (StringUtils.isEmpty(packageName)) {
            packageName = "com.daoshu.demo.controller";
        } else {
            packageName = packageName.trim();
        }
        if(classNameSelected == null || classNameSelected.length <= 0) {
            return "没有需要生成的接口！";
        }

        LOGGER.info("   >>>classNameSelected:" + classNameSelected);

        // 上传的json文件目录
        String uploadPath = request.getSession().getServletContext().getRealPath("/upload");
        String jsonContent = ParseYApiJson.readFile(uploadPath + "\\" + jsonFileName);
        if (StringUtils.isEmpty(jsonContent)) {
            return "解析失败！文件不存在";
        }
        // 代码目录
        String codePath = request.getSession().getServletContext().getRealPath("/code");
        LOGGER.error("开始生成代码，存放目录：" + codePath);
        try {
            String timeFolderName = timeFolderName();
            // 上传目录
            String uploadFolderPath = codePath + "\\" + timeFolderName;
            List<ClassVO> classVOList = ParseYApiJson.jsonDataMapper(jsonContent, packageName, uploadFolderPath, classNameSelected);
            new CodeGen().createFile(packageName, classVOList, uploadFolderPath);

            // 压缩包里第一个文件夹名称
            String firstFolderName = "";
            if (!StringUtils.isEmpty(packageName)) {
                String[] pck = packageName.split("\\.");
                if (pck != null && pck.length > 0) {
                    firstFolderName = pck[0] + "\\";
                }
            }
            downLoadZipFile(response, firstFolderName, uploadFolderPath);
        } catch (IOException e) {
            LOGGER.error("", e);
            return "代码生成失败：" + e.getMessage();
        }
        return "代码生成成功！";
    }

    /**
     * 打包压缩下载文件
     */
    private void downLoadZipFile(HttpServletResponse response, String firstFolderName, String uploadFolderPath) throws IOException {
        String zipName = "yapi-java-controller.zip";
        response.setContentType("APPLICATION/OCTET-STREAM");
        response.setHeader("Content-Disposition", "attachment; filename=" + zipName);
        ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
        try {
            ZipUtils.doCompress(uploadFolderPath + firstFolderName, out);

            // 下载完成后异步删除时间文件夹
            new Thread(new Runnable() {
                @Override
                public void run() {
                    deleteDir(uploadFolderPath);
                }
            }).start();

            response.flushBuffer();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            out.close();
        }
    }

    /**
     * 删除文件夹
     *
     * @param path
     * @return
     */
    private static boolean deleteDir(String path) {
        File file = new File(path);
        if (!file.exists()) {//判断是否待删除目录是否存在
            System.err.println("文件夹不存在!");
            return false;
        }

        String[] content = file.list();//取得当前目录下所有文件和文件夹
        for (String name : content) {
            File temp = new File(path, name);
            if (temp.isDirectory()) {//判断是否是目录
                deleteDir(temp.getAbsolutePath());//递归调用，删除目录里的内容
                temp.delete();//删除空目录
            } else {
                if (!temp.delete()) {//直接删除文件
                    System.err.println("Failed to delete " + name);
                }
            }
        }
        return true;
    }

    /**
     * 用当前时间做为文件夹名，避免重复
     *
     * @return
     */
    public static String timeFolderName() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyyMMddHHmmss");
        Date date = new Date();
        return sdf.format(date) + "\\";
    }

}

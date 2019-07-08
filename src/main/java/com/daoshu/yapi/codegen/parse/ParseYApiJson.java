package com.daoshu.yapi.codegen.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daoshu.yapi.codegen.common.Constant;
import com.daoshu.yapi.codegen.vo.ClassVO;
import com.daoshu.yapi.codegen.vo.MethodVO;
import com.daoshu.yapi.codegen.vo.RequestParamsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 解析YApi导出的json数据
 *
 * @author YUSL
 */
public class ParseYApiJson {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParseYApiJson.class);


    /**
     * json数据映射到类代码和方法代码
     *
     * @param jsonContent
     * @return
     */
    public static List<ClassVO> jsonDataMapper(String jsonContent) {
        if (StringUtils.isEmpty(jsonContent)) return null;
        List<JSONObject> objects = JSONObject.parseArray(jsonContent, JSONObject.class);
        List<ClassVO> classVOList = new ArrayList<>();
        ClassVO classVO;
        Set<MethodVO> methodList;
        MethodVO methodVO;
        Set<RequestParamsVO> requestParamsVOS;
        RequestParamsVO requestParamsVO;
        String splitSymbol = Constant.MIDDLE_LINE;
        for (JSONObject jsonObject : objects) {
            classVO = new ClassVO();
            methodList = new HashSet<>();
            // 类名
            String originalClassName = jsonObject.getString("name");
            // 格式良好的类名
            String wellFormedClassName = "";
            if (StringUtils.isEmpty(originalClassName)) continue;
            if (originalClassName.contains(Constant.UNDER_LINE)) splitSymbol = Constant.UNDER_LINE;
            String[] classNameSplit = originalClassName.split(splitSymbol);
            for (String splitStr : classNameSplit) {
                // 首字母大写
                wellFormedClassName += (splitStr.substring(0, 1).toUpperCase() + splitStr.substring(1));
            }
            String classDesc = jsonObject.getString("desc");
            classVO.setClassName(wellFormedClassName);
            classVO.setClassDesc(classDesc);

            // 方法
            String jsonMethodString = jsonObject.getString("list");
            List<JSONObject> methodJsonObjectList = JSONArray.parseArray(jsonMethodString, JSONObject.class);
            // 接口列表
            for (JSONObject methodJsonObject : methodJsonObjectList) {
                String requestType = methodJsonObject.getString("method");
                String methodDesc = methodJsonObject.getString("title");
                String requestPath = methodJsonObject.getString("path");
                String requestParams = methodJsonObject.getString("req_query");
                String requestBody = methodJsonObject.getString("req_body_other");

                methodVO = new MethodVO();
                requestParamsVOS = new HashSet<>();
                methodVO.setMethodName(requestPath != null ? requestPath.substring(requestPath.lastIndexOf("/") + 1) : "_TODO");
                methodVO.setMethodDesc(methodDesc);
                methodVO.setRequestPath(requestPath);
                methodVO.setRequestType(requestType);
                methodVO.setRequestParams(requestParamsVOS);
                methodVO.setRequestBody(requestBody);
                methodList.add(methodVO);

                // 方法参数
                List<JSONObject> requestParamsJsonObjects = JSONArray.parseArray(requestParams, JSONObject.class);
                for (JSONObject requestParamJsonObject : requestParamsJsonObjects) {
                    requestParamsVO = new RequestParamsVO();
                    requestParamsVO.setName(requestParamJsonObject.getString("name"));
                    requestParamsVO.setDesc(requestParamJsonObject.getString("desc"));
                    requestParamsVO.setRequired("1".equalsIgnoreCase(requestParamJsonObject.getString("required")) ? "true" : "false");
                    requestParamsVOS.add(requestParamsVO);
                }

            }
            classVO.setMethodList(methodList);
            classVOList.add(classVO);
        }
        return classVOList;
    }

    /**
     * 读文件
     *
     * @return
     */
    public static String readFile(String jsonFilePath) {
        InputStream inputStream = null;
        try {
            File file = new File(jsonFilePath);
            if (file == null || !file.exists()) {
                LOGGER.error("解析失败！文件不存在：" + jsonFilePath);
                return null;
            }
            inputStream = new FileInputStream(file);
            Long length = file.length();
            byte[] content = new byte[length.intValue()];
            inputStream.read(content);
            String result = new String(content, "UTF-8");
            return result;
        } catch (FileNotFoundException e) {
            LOGGER.error("", e);
        } catch (IOException e) {
            LOGGER.error("", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }
        return null;
    }
}

package com.daoshu.yapi.codegen.parse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daoshu.yapi.codegen.common.Constant;
import com.daoshu.yapi.codegen.gen.CodeGen;
import com.daoshu.yapi.codegen.vo.ClassVO;
import com.daoshu.yapi.codegen.vo.MethodVO;
import com.daoshu.yapi.codegen.vo.RequestParamsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

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
    public static List<ClassVO> jsonDataMapper(String jsonContent, String packageName, String uploadFolderPath) {
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

                String reqBodyType = methodJsonObject.getString("req_body_type");
                String resBodyType = methodJsonObject.getString("res_body_type");

                methodVO = new MethodVO();
                requestParamsVOS = new HashSet<>();
                methodVO.setMethodName(requestPath != null ? requestPath.substring(requestPath.lastIndexOf("/") + 1) : "_TODO");
                methodVO.setMethodDesc(methodDesc);
                methodVO.setRequestPath(requestPath);
                methodVO.setRequestType(requestType);
                if (!"json".equals(reqBodyType)) {
                    // 方法参数
                    List<JSONObject> requestParamsJsonObjects = JSONArray.parseArray(requestParams, JSONObject.class);
                    for (JSONObject requestParamJsonObject : requestParamsJsonObjects) {
                        requestParamsVO = new RequestParamsVO();
                        requestParamsVO.setName(requestParamJsonObject.getString("name"));
                        requestParamsVO.setDesc(requestParamJsonObject.getString("desc"));
                        requestParamsVO.setRequired("1".equalsIgnoreCase(requestParamJsonObject.getString("required")) ? "true" : "false");
                        requestParamsVOS.add(requestParamsVO);
                    }
                    methodVO.setRequestParams(requestParamsVOS);
                }

                if ("POST".equalsIgnoreCase(requestType)) {
                    // 生成参数实体
                    String reqObjName = generateReqObject(packageName, requestType, requestPath, methodJsonObject, uploadFolderPath);
                    methodVO.setRequestBody(reqObjName);
                }
                if ("json".equals(resBodyType)) {
                    // 生成返回对象
                    String resObjName = generateResObject(packageName, requestType, requestPath, methodJsonObject, uploadFolderPath);
                    methodVO.setResponseType(StringUtils.isEmpty(resObjName) ? "String" : resObjName);
                } else {
                    methodVO.setResponseType("String");
                }

                methodList.add(methodVO);
            }
            classVO.setMethodList(methodList);
            classVOList.add(classVO);
        }
        return classVOList;
    }

    /**
     * 转换成java类型
     *
     * @param type
     * @return
     */
    private static String switchJavaType(String type) {
        switch (type) {
            case "string":
                return "String";
            case "integer":
                return "Integer";
            case "boolean":
                return "Boolean";
            case "number":
                return "Double";
            default:
                return "Object";
        }
    }

    /**
     * 生成请求参数对象
     * <pre>每个controller只生成一个返回值对象</pre>
     *
     * @param methodJsonObject
     * @return
     */
    private static String generateReqObject(String packageName, String requestType, String requestPath, JSONObject methodJsonObject, String uploadFolderPath) {
        String dtoFileName = null;
        // 上一层的包名
        String parentPackage = packageName.substring(0, packageName.lastIndexOf("."));
        // 当前包名
        String currentPackage = parentPackage + ".dto.req";
        if ("GET".equalsIgnoreCase(requestType)) {
            return null;
        }
        String req_body_type = methodJsonObject.getString("req_body_type");
        String req_body_other = methodJsonObject.getString("req_body_other");
        String methodDesc = methodJsonObject.getString("desc");
        if (StringUtils.isEmpty(req_body_other) || "raw".equals(req_body_type)) {
            return null;
        }
        // "req_body_type": "json",
        req_body_other = req_body_other.replaceAll("\\\\n", "").replaceAll("\\\\", "");
        JSONObject jsonObject = JSONObject.parseObject(req_body_other);
        JSONObject properties = jsonObject.getJSONObject("properties");
        if (properties == null || properties.size() <= 0) {
            return null;
        }
        Iterator<Map.Entry<String, Object>> iterator = properties.entrySet().iterator();

        StringBuilder dtoBuilder = new StringBuilder(100 * properties.size());
        dtoFileName = requestPath.replaceAll("save", "")
                .replaceAll("Save", "")
                .replaceAll("Insert", "")
                .replaceAll("update", "")
                .replaceAll("delete", "")
                .replaceAll("Delete", "")
                .replaceAll("Del", "")
                .replaceAll("get", "")
                .replaceAll("query", "")
                .replaceAll("Search", "")
                .replaceAll("List", "")
                .replaceAll("find", "");
        if (StringUtils.isEmpty(dtoFileName)) {
            return null;
        }
        dtoFileName = dtoFileName.substring(dtoFileName.lastIndexOf("/") + 1) + "ReqDTO";
        // 首字母大写
        dtoFileName = dtoFileName.substring(0, 1).toUpperCase() + dtoFileName.substring(1);

        CodeGen codeGen = new CodeGen();
        dtoBuilder.append("package ").append(currentPackage).append(";");
        codeGen.addBlankLine(dtoBuilder);
        dtoBuilder.append("import io.swagger.annotations.ApiModel;");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("import io.swagger.annotations.ApiModelProperty;");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("import lombok.Data;");
        codeGen.addBlankLine(dtoBuilder);

        codeGen.addClassNote(dtoBuilder, methodDesc);
        dtoBuilder.append("@Data");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("@ApiModel(\"").append(methodDesc).append("\")");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("public class ").append(dtoFileName).append(" {");
        codeGen.addLine(dtoBuilder);
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String name = entry.getKey();
            JSONObject value = (JSONObject) entry.getValue();
            String type = value.getString("type");
            String description = value.getString("description");
            type = switchJavaType(type);

            if (!StringUtils.isEmpty(description)) {
                codeGen.addFieldNote(dtoBuilder, description);
            }
            dtoBuilder.append("    private ").append(type).append(" ").append(name).append(";");
            codeGen.addLine(dtoBuilder);
        }
        dtoBuilder.append("}");
        new CodeGen().createDTOFile(currentPackage, uploadFolderPath, dtoFileName, dtoBuilder);

        if ("raw".equals(methodJsonObject.getString("res_body_type"))) {
            return null;
        }
        return dtoFileName;
    }

    /**
     * 生成返回参数对象
     * <pre>每个controller只生成一个返回值对象</pre>
     *
     * @param methodJsonObject
     * @return
     */
    private static String generateResObject(String packageName, String requestType, String requestPath, JSONObject methodJsonObject, String uploadFolderPath) {
        String dtoFileName;
        // 上一层的包名
        String parentPackage = packageName.substring(0, packageName.lastIndexOf("."));
        // 当前包名
        String currentPackage = parentPackage + ".dto.res";
        String res_body = methodJsonObject.getString("res_body");
        String methodDesc = methodJsonObject.getString("desc");
        if (StringUtils.isEmpty(res_body)) {
            return null;
        }
        // "req_body_type": "json",
        res_body = res_body.replaceAll("\\\\n", "").replaceAll("\\\\", "");
        JSONObject jsonObject = JSONObject.parseObject(res_body);
        JSONObject properties = jsonObject.getJSONObject("properties");
        if (properties == null || properties.size() <= 0) {
            return null;
        }
        Iterator<Map.Entry<String, Object>> iterator = properties.entrySet().iterator();

        StringBuilder dtoBuilder = new StringBuilder(100 * properties.size());
        dtoFileName = requestPath.replaceAll("save", "")
                .replaceAll("Save", "")
                .replaceAll("Insert", "")
                .replaceAll("update", "")
                .replaceAll("delete", "")
                .replaceAll("Delete", "")
                .replaceAll("Del", "")
                .replaceAll("get", "")
                .replaceAll("query", "")
                .replaceAll("Search", "")
                .replaceAll("List", "")
                .replaceAll("find", "");
        if (StringUtils.isEmpty(dtoFileName)) {
            return null;
        }
        dtoFileName = dtoFileName.substring(dtoFileName.lastIndexOf("/") + 1) + "ResDTO";
        // 首字母大写
        dtoFileName = dtoFileName.substring(0, 1).toUpperCase() + dtoFileName.substring(1);

        CodeGen codeGen = new CodeGen();
        dtoBuilder.append("package ").append(currentPackage).append(";");
        codeGen.addBlankLine(dtoBuilder);
        dtoBuilder.append("import io.swagger.annotations.ApiModel;");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("import io.swagger.annotations.ApiModelProperty;");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("import lombok.Data;");
        codeGen.addBlankLine(dtoBuilder);

        codeGen.addClassNote(dtoBuilder, methodDesc);
        dtoBuilder.append("@Data");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("@ApiModel(\"").append(methodDesc).append("\")");
        codeGen.addLine(dtoBuilder);
        dtoBuilder.append("public class ").append(dtoFileName).append(" {");
        codeGen.addLine(dtoBuilder);
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entry = iterator.next();
            String name = entry.getKey();
            JSONObject value = (JSONObject) entry.getValue();
            String type = value.getString("type");
            String description = value.getString("description");
            type = switchJavaType(type);

            if (!StringUtils.isEmpty(description)) {
                codeGen.addFieldNote(dtoBuilder, description);
            }
            dtoBuilder.append("    private ").append(type).append(" ").append(name).append(";");
            codeGen.addLine(dtoBuilder);
        }
        dtoBuilder.append("}");
        new CodeGen().createDTOFile(currentPackage, uploadFolderPath, dtoFileName, dtoBuilder);
        return dtoFileName;
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

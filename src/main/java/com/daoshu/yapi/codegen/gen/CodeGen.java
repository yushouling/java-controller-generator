package com.daoshu.yapi.codegen.gen;

import com.daoshu.yapi.codegen.vo.ClassVO;
import com.daoshu.yapi.codegen.vo.MethodVO;
import com.daoshu.yapi.codegen.vo.RequestParamsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 代码生成
 *
 * @author YUSL
 */
public class CodeGen {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeGen.class);

    /**
     * 生成代码文件
     *
     * @param packageName
     * @param classVOList
     */
    public void createFile(String packageName, List<ClassVO> classVOList, String codePath) {
        File file;
        FileWriter fileWriter = null;
        String path;
        String dirName;

        try {
            for (ClassVO classVO : classVOList) {
                StringBuilder sbd = appendClassCode(packageName, classVO);
                // 创建文件夹
                dirName = packageName.replaceAll("\\.", "\\\\");
                path = codePath + dirName + "\\";

                file = new File(path);
                file.mkdirs();

                // 创建文件
                path = path + classVO.getClassName() + ".java";
                LOGGER.info("创建文件: " + path);
                file = new File(path);
                file.createNewFile();

                fileWriter = new FileWriter(path);
                fileWriter.write(sbd.toString());
                fileWriter.close();
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    /**
     * 生成DTO代码文件
     * @param packageName
     * @param codePath
     * @param dtoFileName
     * @param dtoBuilder
     */
    public void createDTOFile(String packageName, String codePath, String dtoFileName, StringBuilder dtoBuilder) {
        File file;
        FileWriter fileWriter = null;
        String path;
        String dirName;
        try {
                // 创建文件夹
                dirName = packageName.replaceAll("\\.", "\\\\");
                path = codePath + dirName + "\\";
                file = new File(path);
                file.mkdirs();

                // 创建文件
                path = path + dtoFileName + ".java";
                LOGGER.info("创建文件: " + path);
                file = new File(path);
                file.createNewFile();

                fileWriter = new FileWriter(path);
                fileWriter.write(dtoBuilder.toString());
                fileWriter.close();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    LOGGER.error("", e);
                }
            }
        }
    }

    /**
     * 组装类代码
     *
     * @param packageName 包名
     * @param jsonInfoVO  类信息
     * @return
     */
    private StringBuilder appendClassCode(String packageName, ClassVO jsonInfoVO) {
        StringBuilder sbd = new StringBuilder(jsonInfoVO.getMethodList() != null ? jsonInfoVO.getMethodList().size() * 2000 : 1000);

        sbd.append("package ").append(packageName).append(";");
        addBlankLine(sbd);
        sbd.append("import org.springframework.web.bind.annotation.*;");
        addLine(sbd);
        sbd.append("import io.swagger.annotations.*;");
        addLine(sbd);
        // 上一层的包名
        String parentPackage = packageName.substring(0 ,packageName.lastIndexOf("."));
        sbd.append("import ").append(parentPackage).append(".dto.req.*;");
        addLine(sbd);
        sbd.append("import ").append(parentPackage).append(".dto.res.*;");
        addBlankLine(sbd);
        addClassNote(sbd, jsonInfoVO.getClassDesc());
        sbd.append("@RestController");
        addLine(sbd);
        sbd.append("@RequestMapping(\"/\")");
        addLine(sbd);
        // 类start
        sbd.append("public class ").append(jsonInfoVO.getClassName()).append(" {");
        addLine(sbd);
        for (MethodVO methodVO : jsonInfoVO.getMethodList()) {
            // 方法start
            addMethodNote(sbd, methodVO.getMethodDesc());
            // 参数注解
            Set<RequestParamsVO> requestParams = methodVO.getRequestParams();
            if ("GET".equalsIgnoreCase(methodVO.getRequestType())) {
                addMethodSwaggerAnnotation(sbd, methodVO.getMethodDesc(), requestParams);
                add1Space(sbd);
                sbd.append("@GetMapping(\"").append(methodVO.getRequestPath()).append("\")");
            } else {
                // 对象参数
                add1Space(sbd);
                sbd.append("@PostMapping(\"").append(methodVO.getRequestPath()).append("\")");
            }
            addLine(sbd);
            add1Space(sbd);
            sbd.append("public ").append(methodVO.getResponseType()).append(" ").append(methodVO.getMethodName()).append("(");
            // 参数start
            if (!CollectionUtils.isEmpty(requestParams) && "GET".equalsIgnoreCase(methodVO.getRequestType())) {
                for (RequestParamsVO requestParamsVO : requestParams) {
                    sbd.append("String ").append(requestParamsVO.getName()).append(",");
                }
                deleteLastChar(sbd);
            } else {
                // POST/PUT
                if(! StringUtils.isEmpty(methodVO.getRequestBody())) {
                    // 小写参数名
                    String reqLowerName = methodVO.getRequestBody().substring(0,1).toLowerCase() + methodVO.getRequestBody().substring(1);
                    sbd.append("@RequestBody ").append(methodVO.getRequestBody()).append(" ").append(reqLowerName);
                }
            }
            // 参数end
            sbd.append(") {");
            // 方法体
            addBlankLine(sbd);
            add2Space(sbd);
            sbd.append("return null;");
            addLine(sbd);
            add1Space(sbd);
            sbd.append("}");
            addLine(sbd);
            // 方法end
        }
        addLine(sbd);
        sbd.append("}");
        // 类end
        return sbd;
    }

    /**
     * 增加方法注释
     *
     * @param sbd
     * @param methodDesc
     */
    public void addMethodNote(StringBuilder sbd, String methodDesc) {
        addLine(sbd);
        add1Space(sbd);
        sbd.append("/**");
        addLine(sbd);
        add1Space(sbd);
        sbd.append(" * ").append(methodDesc);
        addLine(sbd);
        add1Space(sbd);
        sbd.append(" */");
        addLine(sbd);
    }

    /**
     * 增加属性注释
     * @param sbd
     * @param desc
     */
    public void addFieldNote(StringBuilder sbd, String desc) {
        addLine(sbd);
        add1Space(sbd);
        sbd.append("/**");
        addLine(sbd);
        add1Space(sbd);
        sbd.append(" * ").append(desc);
        addLine(sbd);
        add1Space(sbd);
        sbd.append(" */");
        addLine(sbd);
        add1Space(sbd);
        sbd.append("@ApiModelProperty(\"").append(desc).append("\")");
        addLine(sbd);
    }

    /**
     * 增加类注释
     *
     * @param sbd
     * @param classDesc
     */
    public void addClassNote(StringBuilder sbd, String classDesc) {
        sbd.append("/**");
        addLine(sbd);
        sbd.append(" * ").append(classDesc);
        addLine(sbd);
        sbd.append(" *");
        addLine(sbd);
        sbd.append(" * @author TODO");
        addLine(sbd);
        sbd.append(" */");
        addLine(sbd);
    }

    /**
     * 删除最后一个逗号
     *
     * @param sbd
     */
    public void deleteLastChar(StringBuilder sbd) {
        sbd.deleteCharAt(sbd.length() - 1);
    }

    /**
     * 方法加swagger注解
     *
     * @param sbd
     * @param requestParams
     */
    private void addMethodSwaggerAnnotation(StringBuilder sbd, String methodDesc, Set<RequestParamsVO> requestParams) {
        if (CollectionUtils.isEmpty(requestParams)) return;

        int index = 0;
        add1Space(sbd);
        sbd.append("@ApiOperation(value = \"").append(methodDesc).append("\", notes = \"").append(methodDesc).append("\")");
        addLine(sbd);
        add1Space(sbd);
        sbd.append("@ApiImplicitParams({");
        addLine(sbd);
        for (RequestParamsVO requestParamsVO : requestParams) {
            index++;
            add2Space(sbd);
            sbd.append("@ApiImplicitParam(name = \"")
                    .append(requestParamsVO.getName())
                    .append("\", value=\"")
                    .append(requestParamsVO.getDesc())
                    .append("\"");
            if ("true".equalsIgnoreCase(requestParamsVO.getRequired())) {
                sbd.append(", required = ").append(requestParamsVO.getRequired());
            }
            sbd.append(", dataType = \"String\"),");
            if (requestParams.size() != index) {
                // 最后一个参数不换行
                addLine(sbd);
            }
        }
        deleteLastChar(sbd);
        addLine(sbd);
        add1Space(sbd);
        sbd.append("})");
        addLine(sbd);
    }

    /**
     * 增加一行空行
     *
     * @param sbd
     */
    public void addBlankLine(StringBuilder sbd) {
        sbd.append(System.lineSeparator()).append(System.lineSeparator());
    }

    /**
     * 换行
     *
     * @param sbd
     */
    public void addLine(StringBuilder sbd) {
        sbd.append(System.lineSeparator());
    }

    /**
     * 增加1个缩进
     *
     * @param sbd
     */
    public void add1Space(StringBuilder sbd) {
        sbd.append("    ");
    }

    /**
     * 增加2个缩进
     *
     * @param sbd
     */
    public void add2Space(StringBuilder sbd) {
        sbd.append("        ");
    }

    /**
     * 增加3个缩进
     *
     * @param sbd
     */
    public void add3Space(StringBuilder sbd) {
        sbd.append("            ");
    }
}

package com.daoshu.yapi.codegen.vo;

import lombok.Data;

import java.util.Set;

/**
 * 类
 */
@Data
public class ClassVO {
    /**
     * 类名，对应name属性
     * <code>"name":"alarm-controller",</code>
     */
    private String className;

    /**
     * 类注释
     * <code>"desc":"警情接口",</code>
     */
    private String classDesc;

    /**
     * 接口集合
     * <code>"list":</code>
     */
    private Set<MethodVO> methodList;
}

package com.daoshu.yapi.codegen.vo;

import lombok.Data;

/**
 * GET请求参数
 * {
 *      *             "required": "1",//必须
 *      *             "name": "pageIndex",
 *      *             "desc": "当前页"
 *      *           },
 */
@Data
public class RequestParamsVO {
    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数描述
     */
    private String desc;

    /**
     * 是否必须
     */
    private String required;
}

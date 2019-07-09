package com.daoshu.yapi.codegen.vo;

import lombok.Data;

import java.util.Set;

/**
 * 方法信息
 */
@Data
public class MethodVO {

    /**
     * 接口路径
     * <code>
     * "query_path": {
     *           "path": "/alarm/getAlarmDetails",
     *           "params": []
     *         },
     * </code>
     *  <code>或"path":"/alarm/getAlarmDetails",</code>
     */
    private String requestPath;

    /**
     * 方法注释
     * <code>"desc":"获取警情处理信息",</code>
     */
    private String methodDesc;


    /**
     * 方法名
     * 读取<code>path":"/alarm/getAlarmDetails",</code>
     */
    private String methodName;

    /**
     * 请求类型：GET/POST/PUT
     */
    private String requestType;

    /**
     * POST请求的参数名称和类型
     * <code>"req_body_other": {
     * "properties": {
     * "alarmStatus": {
     * "type": "string"
     * },
     * "areaNum": {
     * "type": "string"
     * }
     * },
     * </code>
     */
    private String requestBody;

    /**
     * GET请求参数
     * <code>"req_query": [
     *           {
     *             "required": "1",//必须
     *             "name": "pageIndex",
     *             "desc": "当前页"
     *           },
     *           {
     *             "required": "0",
     *             "name": "pageSize",
     *             "desc": "页大小"
     *           }
     *         ],
     * </code>
     */
    private Set<RequestParamsVO> requestParams;

    /**
     * 当"res_body_type": "json"时返回ResultVO
     * 返回类型
     */
    private String responseType;

}

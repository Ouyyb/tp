package com.tp.entity.log;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Date;

/**
 * @ClassName: LogEntity
 * @Description: 支付日志对象
 * @Auther: ouyang
 * @Date: 2018/6/22 11:08
 */
@Table(name = "pay_log")
public class PayLogEntity {
    /**
     * 应用id  必须
     */
    @Column(name = "api_code")
    String apiCode;

    /**
     * 服务id  必须
     */
    @Column(name = "service_desc")
    String serviceDesc;


    /**
     * 请求时间  必须
     */
    @Column(name = "req_time")
    Date reqTime;

    /**
     * 返回时间  必须
     */
    @Column(name = "res_time")
    Date resTime;

    /**
     * 服务器耗时 必须
     */
    @Column(name = "duration")
    String duration;

    /**
     * 服务端ip 必须
     */
    @Column(name = "host")
    String host;

    /**
     * 请求ip
     */
    @Column(name = "req_ip")
    String reqIp;

    /**
     * 请求参数  必须
     */
    @Column(name = "req_data")
    String reqData;

    /**
     * 返回参数  必须
     */
    @Column(name = "res_data")
    String resData;

    /**
     * 状态(1-成功；2-失败)  必须
     */
    @Column(name = "status")
    String status;


    /**
     * 错误信息
     */
    @Column(name = "error_info")
    String errorInfo;

    /**
     * 请求类型 1用户请求服务器 2服务器请求三方
     */
    @Column(name = "req_type")
    String reqType;

    public String getReqIp() {
        return reqIp;
    }

    public void setReqIp(String reqIp) {
        this.reqIp = reqIp;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getServiceDesc() {
        return serviceDesc;
    }

    public void setServiceDesc(String serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    public Date getReqTime() {
        return reqTime;
    }

    public void setReqTime(Date reqTime) {
        this.reqTime = reqTime;
    }

    public Date getResTime() {
        return resTime;
    }

    public void setResTime(Date resTime) {
        this.resTime = resTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getReqData() {
        return reqData;
    }

    public void setReqData(String reqData) {
        this.reqData = reqData;
    }

    public String getResData() {
        return resData;
    }

    public void setResData(String resData) {
        this.resData = resData;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorInfo() {
        return errorInfo;
    }

    public void setErrorInfo(String errorInfo) {
        this.errorInfo = errorInfo;
    }
}

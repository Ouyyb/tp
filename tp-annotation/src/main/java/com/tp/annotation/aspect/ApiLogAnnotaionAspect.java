package com.tp.annotation.aspect;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.tp.annotation.ApiLogAnnotaion;
import com.tp.constants.CommonConstant;
import com.tp.entity.log.PayLogEntity;
import com.tp.mapper.PayLogMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @ClassName: ApiLogAnnotaionAspect
 * @Description: 访问日志记录注解切面
 * @Auther: ouyang
 * @Date: 2018/6/22 10:37
 */
@Aspect
@Component
public class ApiLogAnnotaionAspect {

    @Resource
    private PayLogMapper payLogMapper;


    /**
     * ThreadLocal用于保存某个线程共享变量：对于同一个static ThreadLocal，不同线程只能从中get，set，remove自己的变量，而不会影响其他线程的变量。
     * ThreadLocal.get: 获取ThreadLocal中当前线程共享变量的值。
     * ThreadLocal.set: 设置ThreadLocal中当前线程共享变量的值。
     * ThreadLocal.remove: 移除ThreadLocal中当前线程共享变量的值。
     * ThreadLocal.initialValue: ThreadLocal没有被当前线程赋值时或当前线程刚调用remove方法后调用get方法，返回此方法值。
     */
    private ThreadLocal<PayLogEntity> logLocal = new ThreadLocal<PayLogEntity>();

    private static final Logger logger = LoggerFactory.getLogger("API_REQ_LOG");

    @After("controllerAspect()")
    public void doAfter() {
    }

    /**
     * Controller层切点
     */
    @Pointcut("@annotation(com.tp.annotation.ApiLogAnnotaion)")
    public void controllerAspect() {

    }


    /**
     * 前置通知 用于拦截Controller层记录用户的操作 参数请求前
     *
     * @param joinPoint 切点
     */
    @Before("controllerAspect()")
    public void doBefore(JoinPoint joinPoint) throws Exception {
        PayLogEntity log = new PayLogEntity();
        log.setReqTime(getNowTime());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Map<String, String> map = getControllerMethodDescriptionAndOperationType(joinPoint);
        HttpServletRequest request = attributes.getRequest();
        JSONObject reqData = getReqData(request);
        String data = reqData.getString(CommonConstant.DATA);
        String appId = reqData.getString(CommonConstant.APPID);

        log.setApiCode(appId == null ? CommonConstant.STRING_NULL : appId);
        log.setReqData(data == null ? CommonConstant.STRING_NULL : data);
        log.setServiceDesc(map.get("serviceDesc"));
        log.setApiCode(map.get("apiCode"));
        log.setReqType(map.get("reqType"));
        log.setHost(getHost());
        log.setReqIp(getReqId(request));
        logLocal.set(log);
    }

    /**
     * 请求返回时
     *
     * @param object 请求返回参数
     */
    @AfterReturning(pointcut = "controllerAspect()", returning = "object")
    public void doAfterReturn(Object object) {
        PayLogEntity log = logLocal.get();
        log.setResData(getOutput(object));
        log.setDuration(getDuration(log.getReqTime()));
        log.setStatus(CommonConstant.STRING_ONE);
        log.setResTime(getNowTime());
        logger.info(JSONObject.toJSONString(log, SerializerFeature.WriteMapNullValue));
        payLogMapper.insert(log);
        logLocal.remove();

    }

    /**
     * @param joinPoint
     */
    @AfterThrowing(pointcut = "controllerAspect()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, RuntimeException e) {
        PayLogEntity log = logLocal.get();
        log.setStatus(CommonConstant.STRING_TWO);
        log.setResData(CommonConstant.STRING_NULL);
        log.setDuration(getDuration(log.getReqTime()));
        log.setStatus(CommonConstant.STRING_TWO);
        log.setResTime(getNowTime());
        log.setErrorInfo(e.fillInStackTrace().toString());
        logger.error(JSONObject.toJSONString(log, SerializerFeature.WriteMapNullValue));
        payLogMapper.insert(log);
        logLocal.remove();
    }

    /**
     * 获取到返回数据  转换成 json字符串
     *
     * @param object 返回的数据
     * @return json字符串
     */
    private String getOutput(Object object) {
        if (object instanceof Map) {
            Map map = (Map) object;
            Map result = new HashMap();
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                String value = map.get(obj).toString();
                if (value.length() > 100) {
                    String vString = value.substring(0, 50) + value.substring(value.length() - 50, value.length());
                    result.put(obj, vString);
                } else {
                    result.put(obj, value);
                }
            }
            return JSONObject.toJSONString(result);
        } else if (object != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            // 得到类对象
            Class userCla = (Class) object.getClass();
            /* 得到类中的所有属性集合 */
            Field[] fs = userCla.getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                Field f = fs[i];
                // 设置些属性是可以访问的
                f.setAccessible(true);
                Object val = new Object();
                try {
                    val = f.get(object);
                    // 得到此属性的值
                    // 设置键值
                    map.put(f.getName(), val);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return JSONObject.toJSONString(map);
        } else {
            return CommonConstant.STRING_NULL;
        }
    }

    /**
     * 获取请求参数 转换成 json字符串
     *
     * @param request HttpServletRequest
     * @return json字符串
     */
    private JSONObject getReqData(HttpServletRequest request) {
        @SuppressWarnings("unchecked")
        Enumeration<String> names = request.getParameterNames();
        Map<String, String> inputs = new HashMap<>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getParameter(name);
            if (value.length() > 100) {
                String vString = value.substring(0, 50) + value.substring(value.length() - 50, value.length());
                inputs.put(name, vString);
            } else {
                inputs.put(name, value);
            }
        }
        return JSONObject.parseObject(JSONObject.toJSONString(inputs));
    }

    /**
     * 获取当前时间
     *
     * @return 返回当前时间字符串
     */
    private Date getNowTime() {
        return new Date();
    }

    /**
     * 获取 注解参数
     *
     * @param joinPoint JoinPoint
     * @return map集合
     * @throws Exception 抛出异常
     */
    private static Map<String, String> getControllerMethodDescriptionAndOperationType(JoinPoint joinPoint) throws Exception {
        String targetName = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();
        Object[] arguments = joinPoint.getArgs();
        Class targetClass = Class.forName(targetName);
        Method[] methods = targetClass.getMethods();
        Map<String, String> map = new HashMap<>(3);
        String apiCode = "";
        String serviceDesc = "";
        String reqType = "";
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class[] clazzs = method.getParameterTypes();
                if (clazzs.length == arguments.length) {
                    apiCode = method.getAnnotation(ApiLogAnnotaion.class).apiCode();
                    serviceDesc = method.getAnnotation(ApiLogAnnotaion.class).serviceDesc();
                    reqType = method.getAnnotation(ApiLogAnnotaion.class).reqType();
                    break;
                }
            }
        }
        map.put("apiCode", apiCode);
        map.put("serviceDesc", serviceDesc);
        map.put("reqType", reqType);
        return map;
    }

    private String getDuration(Date reqDate) {
        Date endtime = new Date();
        long duration = endtime.getTime() - reqDate.getTime();
        return String.valueOf(duration);
    }

    /**
     * 获取服务器IP地址
     *
     * @return 服务器IP地址
     * @throws UnknownHostException 抛出异常
     */
    private String getHost() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        //获取本机ip;
        return addr.getHostAddress();
    }

    /**
     * 获取请求Ip
     *
     * @param request HttpServletRequest
     * @return 请求Ip
     */
    private String getReqId(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

//    /**
//     * 获取设备信息
//     *
//     * @param request HttpServletRequest
//     * @return 设备信息
//     */
//    private String getDev(HttpServletRequest request) {
//        String agent = request.getHeader("user-agent");
//        String[] keywords = {"Windows NT", "compatible; MSIE 9.0;", "Android", "iPhone", "iPod", "iPad",
//                "Windows Phone", "MQQBrowser"};
//        for (String key : keywords) {
//            if (agent.contains(key)) {
//                return key;
//            }
//        }
//        return CommonConstant.STRING_NULL;
//    }

}

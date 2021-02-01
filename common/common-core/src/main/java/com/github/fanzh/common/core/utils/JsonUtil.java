package com.github.fanzh.common.core.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.fanzh.common.core.exceptions.CommonException;

import java.util.Collections;
import java.util.List;

/**
 * JSON 工具类
 *
 * @author fanzh
 */
public class JsonUtil {

    private JsonUtil() {
    }

    /**
     * 对象转jsonObject
     *
     * @param obj
     * @return
     */
    public static JSONObject objToJsonObject(Object obj) {
        if (ParamsUtil.isEmpty(obj)) {
            obj = new JSONObject();
        }
        if (obj instanceof byte[]) {
            obj = new String((byte[]) obj);
        }
        try {
            return JSONObject.parseObject(JSONObject.toJSON(obj).toString());
        } catch (Exception e) {
            throw new CommonException(String.format("objToJsonObject error: %s", e.getMessage()));
        }
    }

    /**
     * 对象转对象
     *
     * @param obj         需要转换的对象
     * @param targetClass 需要返回的对象类型
     * @param <T>
     * @return
     */
    public static <T> T objToObj(Object obj, Class<T> targetClass) {
        return JSON.toJavaObject(objToJsonObject(obj), targetClass);
    }

    /**
     * 集合对象转jsonArray
     *
     * @param listObj
     * @return
     */
    public static JSONArray listToJsonArray(Object listObj) {
        if (ParamsUtil.isEmpty(listObj)) {
            listObj = Collections.emptyList();
        }
        try {
            if (listObj instanceof byte[]) {
                return JSONArray.parseArray(new String((byte[]) listObj));
            } else {
                return JSONArray.parseArray(JSON.toJSONString(listObj));
            }
        } catch (Exception e) {
            throw new CommonException(String.format("objToJsonArray error: %s", e.getMessage()));
        }
    }

    /**
     * 集合转集合
     *
     * @param listObj
     * @param targetClass
     * @param <T>
     * @return
     */
    public static <T> List<T> listToList(Object listObj, Class<T> targetClass) {
        return JSONObject.parseArray(listToJsonArray(listObj).toJSONString(), targetClass);
    }
}
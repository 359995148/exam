package com.github.fanzh.exam.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author fanzh
 * @date 2019/10/22 21:44
 */
@Getter
@AllArgsConstructor
public enum SubmitStatusEnum {

    NOT_SUBMITTED("未提交", 0),
    SUBMITTED("已提交", 1),
    CALCULATE("正在统计", 2),
    CALCULATED("统计完成", 3);

    private String name;

    private Integer value;

    public static SubmitStatusEnum match(Integer value, SubmitStatusEnum defaultValue) {
        if (value != null) {
            for (SubmitStatusEnum item : SubmitStatusEnum.values()) {
                if (item.value.equals(value)) {
                    return item;
                }
            }
        }
        return defaultValue;
    }

    public static SubmitStatusEnum matchByValue(Integer value) {
        for (SubmitStatusEnum item : SubmitStatusEnum.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return NOT_SUBMITTED;
    }

    public static SubmitStatusEnum matchByName(String name) {
        for (SubmitStatusEnum item : SubmitStatusEnum.values()) {
            if (item.name.equals(name)) {
                return item;
            }
        }
        return NOT_SUBMITTED;
    }
}

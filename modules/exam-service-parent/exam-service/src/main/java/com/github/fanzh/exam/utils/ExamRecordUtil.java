package com.github.fanzh.exam.utils;

import com.github.fanzh.common.core.utils.DateUtils;

import java.util.Date;

/**
 * 考试记录工具类
 *
 * @author fanzh
 * @date 2018/12/31 22:35
 */
public class ExamRecordUtil {

	private ExamRecordUtil() {
	}

	/**
	 * 计算持续时间
	 * @param startTime startTime
	 * @param endTime endTime
	 * @return String
	 */
	public static String getExamDuration(Date startTime, Date endTime) {
		// 持续时间
		String suffix = "分钟";
		Integer duration = DateUtils.getBetweenMinutes(startTime, endTime);
		if (duration <= 0) {
			duration = DateUtils.getBetweenSecond(startTime, endTime);
			suffix = "秒";
		}
		return duration + suffix;
	}
}

package com.example.projectmanagement.util;

import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import java.time.LocalDate;
import java.util.List;

/**
 * 时间冲突检测工具类
 */
public class TimeConflictChecker {

    /**
     * 检查两个任务是否有时间重叠
     */
    public static boolean hasTimeConflict(TaskModel task1, TaskModel task2) {
        return isOverlap(
                task1.getStartDate(), task1.getEndDate(),
                task2.getStartDate(), task2.getEndDate()
        );
    }

    /**
     * 检查指定时间段是否与现有任务冲突
     */
    public static boolean isOverlap(LocalDate start1, LocalDate end1,
                                    LocalDate start2, LocalDate end2) {
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }


}
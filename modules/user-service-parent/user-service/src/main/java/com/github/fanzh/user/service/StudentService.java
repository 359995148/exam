package com.github.fanzh.user.service;

import com.github.fanzh.common.basic.service.BaseService;
import com.github.fanzh.common.basic.vo.UserVo;
import com.github.fanzh.common.core.exceptions.CommonException;
import com.github.fanzh.common.core.utils.ParamsUtil;
import com.github.fanzh.common.security.utils.SysUtil;
import com.github.fanzh.user.api.constant.UserStudentConstant;
import com.github.fanzh.user.api.dto.StudentDto;
import com.github.fanzh.user.api.module.Student;
import com.github.fanzh.user.api.module.UserStudent;
import com.github.fanzh.user.mapper.StudentMapper;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 学生Service
 *
 * @author fanzh
 * @date 2019/07/09 15:28
 */
@AllArgsConstructor
@Service
public class StudentService extends BaseService<StudentMapper, Student> {

    private final UserService userService;

    private final UserStudentService userStudentService;


    public Long addStudent(StudentDto studentDto) {
        String currentUser = SysUtil.getUser();
        String tenantCode = SysUtil.getTenantCode();
        Long userId = studentDto.getUserId();
        if (ParamsUtil.isNotEmpty(userId)) {
            // 查询当前用户
            UserVo userVo = userService.findUserByIdentifier(null, currentUser, tenantCode);
            if (userVo == null) {
                throw new CommonException("Get user info failed");
            }
            userId = userVo.getId();
        }
        Student student = new Student();
        BeanUtils.copyProperties(studentDto, student);
        student.setCommonValue(currentUser, SysUtil.getSysCode(), tenantCode);
        // 新增用户学生关系
        UserStudent userStudent = new UserStudent();
        userStudent.setCommonValue(currentUser, SysUtil.getSysCode(), tenantCode);
        userStudent.setUserId(userId);
        userStudent.setStudentId(student.getId());
        // 默认关系类型是爸爸
        if (studentDto.getRelationshipType() == null) {
            userStudent.setRelationshipType(UserStudentConstant.RELATIONSHIP_TYPE_FATHER);
        }
        userStudentService.baseSave(userStudent);
        // 保存学生
        this.baseSave(student);
        return student.getId();
    }
}

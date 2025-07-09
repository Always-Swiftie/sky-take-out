package com.sky.service;


import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserReportVO;

import java.time.LocalDate;

public interface UserService {

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 用户统计
     * @param begin
     * @param end
     * @return
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
}

package com.sky.controller.admin;

import com.github.pagehelper.PageHelper;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
      *新增员工
      *@param employeeDTO
     * @return
     */

    @PostMapping
    public Result save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工: {}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     *员工分页查询
     * @param employeePageQueryDTO
     * @return PageResult

     */
    @GetMapping("/page")
    public Result page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("分页查询: {}", employeePageQueryDTO);
        PageResult page = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(page);
    }

    /**
     * 启用,禁用员工账号
     * @Param status,id
     *
     */
    @PostMapping("/status/{status}")
    Result startOrStop(@PathVariable Integer status,Long id) {
        log.info("启用或禁用员工:{}", id);
        employeeService.startOrStop(id,status);
        return Result.success();
    }

    /**
     * 根据id查询员工信息
     * @Param id
     * @Return employee
     */
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("查询员工的id: {}", id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @Param employeeDTO
     *
     */
    @PutMapping()
    public Result update(@RequestBody EmployeeDTO employeeDTO) {
        log.info("修改员工信息: {}", employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }


    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }


}

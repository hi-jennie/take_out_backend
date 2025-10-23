package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // using md5 or BCryptPasswordEncoder
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * save new employee
     *
     * @param employeeDTO
     */
    public void save(EmployeeDTO employeeDTO) {
        Employee newEmployee = new Employee();
        BeanUtils.copyProperties(employeeDTO, newEmployee);

        newEmployee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // set the Default status
        newEmployee.setStatus(StatusConstant.ENABLE);

        // set the timestamps
        // newEmployee.setCreateTime(LocalDateTime.now());
        // newEmployee.setUpdateTime(LocalDateTime.now());

        // set the operator
        // newEmployee.setCreateUser(BaseContext.getCurrentId());
        // newEmployee.setUpdateUser(BaseContext.getCurrentId());

        // if the employee.name is already exists, database will throw duplicate key exception then we can handle it in GlobalExceptionHandler
        employeeMapper.insert(newEmployee);

    }

    /**
     * employee paging query
     *
     * @param pageQueryDTO
     * @return
     */
    public PageResult getPageQuery(EmployeePageQueryDTO pageQueryDTO) {
        // This line tells PageHelper: “The next SQL query should be paginated.”
        // PageHelper intercepts the SQL and returns a Page instead of a plain List.
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // pageHelper will add limit to the sql automatically
        // PageHelper wraps the result List<Employee> into a Page<Employee> which contains more pagination info
        Page<Employee> page = employeeMapper.getPagedEmployees(pageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total, records);

    }

    /**
     * modify employee status
     *
     * @param status
     * @param id
     */
    public void modifyStatus(Integer status, Long id) {
        // right now we can just update the status field
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();

        employeeMapper.update(employee);
    }

    /**
     * get employee by id
     *
     * @param id
     * @return
     */
    public Employee getEmployeeById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // set a masked password before return
        employee.setPassword("****");
        return employee;
    }

    /**
     * update employee info
     *
     * @param employeeDTO
     */
    public void updateEmpInfo(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

//        employee.setUpdateTime(LocalDateTime.now());
        // interceptor has already set the current user id into BaseContext
//        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }
}

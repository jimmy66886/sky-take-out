package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContextByMe;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
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

        // 1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        // 2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            // 账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 密码比对
        // 后期需要进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            // 密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            // 账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        System.out.println("当前线程的id" + Thread.currentThread().getId());

        // 虽然前端传来的是DTO,但是还是建议跟数据交互时使用实体类,所以这里要进行对象转换
        Employee employee = new Employee();

        // 对象属性拷贝--如果使用set一个一个写，是很繁琐的
        // 前提是属性名一致
        BeanUtils.copyProperties(employeeDTO, employee);

        // 设置账号的状态 1 表示正常，这里使用常量类，防止硬编码
        employee.setStatus(StatusConstant.ENABLE);
        // 密码默认为123456-md5加密后再存
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 设置当前记录的创建和修改时间
        // employee.setCreateTime(LocalDateTime.now());
        // employee.setUpdateTime(LocalDateTime.now());

        // 设置记录的创建人id和修改人id
        // 从ThreadLocal中获取到登录用户的id
        // Long empId = BaseContextByMe.getCurrentId();
        // employee.setCreateUser(empId);
        // employee.setUpdateUser(empId);

        employeeMapper.save(employee);
    }

    /**
     * 分页查询
     *
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {

        // 开发分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        // 这乍一看感觉Page和PageHelper没有传递参数的关系，但是其实startPage里面是用到了ThreadLocal来取值的

        long total = page.getTotal();
        List<Employee> records = page.getResult();
        return new PageResult(total, records);
    }

    /**
     * 启用/禁用员工账号
     *
     * @param status
     * @param id
     */
    @Override
    @AutoFill(value = OperationType.UPDATE)
    public void startOrStop(Integer status, Long id) {

        Employee employee = Employee.builder().id(id).status(status).build();

        employeeMapper.update(employee);
    }

    /**
     * 根据id查询员工信息
     *
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // 密码不会发送到前端
        employee.setPassword("****");
        return employee;
    }

    /**
     * 修改员工信息
     *
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);

        /*employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContextByMe.getCurrentId());*/

        employeeMapper.update(employee);
    }

}

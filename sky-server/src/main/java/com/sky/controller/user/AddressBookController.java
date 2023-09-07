package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-07 20:51
 */
@Api(tags = "地址簿接口")
@Slf4j
@RestController
@RequestMapping("/user/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @ApiOperation("新增地址")
    @PostMapping
    public Result add(@RequestBody AddressBook addressBook) {
        log.info("新增地址：{}", addressBook);
        addressBookService.add(addressBook);
        return Result.success();
    }

    @ApiOperation("查询登录用户的所有地址")
    @GetMapping("/list")
    public Result<List<AddressBook>> getByUserId() {
        List<AddressBook> list = addressBookService.list();
        return Result.success(list);
    }

    @ApiOperation("查询默认地址")
    @GetMapping("/default")
    public Result<AddressBook> getDefault() {
        AddressBook addressBook = addressBookService.getDefault();
        return Result.success(addressBook);
    }

    @ApiOperation("修改地址")
    @PutMapping
    public Result update(@RequestBody AddressBook addressBook) {
        log.info("修改地址:{}", addressBook);
        addressBookService.update(addressBook);
        return Result.success();
    }

    @ApiOperation("根据id查询地址")
    @GetMapping("/{id}")
    public Result<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    @ApiOperation("根据id删除地址")
    @DeleteMapping
    public Result deleteById(Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }

    @ApiOperation("设置默认地址")
    @PutMapping("/default")
    public Result setDefault(@RequestBody AddressBook addressBook){
        // 这个倒是用了请求体的id了
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

}

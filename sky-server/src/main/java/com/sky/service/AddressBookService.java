package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-07 20:55
 */
public interface AddressBookService {
    /**
     * 添加地址簿数据
     *
     * @param addressBook
     */
    void add(AddressBook addressBook);

    /**
     * 查询用户的所有地址
     *
     * @return
     */
    List<AddressBook> list();

    /**
     * 查询默认地址
     *
     * @return
     */
    AddressBook getDefault();

    /**
     * 修改地址
     *
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据id查询一条地址信息
     *
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 根据id删除地址
     *
     * @param id
     */
    void deleteById(Long id);

    /**
     * 设置默认地址
     *
     */
    void setDefault(AddressBook addressBook);
}

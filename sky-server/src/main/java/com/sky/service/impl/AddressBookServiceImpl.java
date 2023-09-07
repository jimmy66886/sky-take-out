package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.context.BaseContextByMe;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-07 20:55
 */
@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 添加地址簿数据
     *
     * @param addressBook
     */
    @Override
    public void add(AddressBook addressBook) {
        // 不出所料,userId前端并没有传过来,还是要自己从ThreadLocal中获取
        Long userId = BaseContextByMe.getCurrentId();
        // 忘了设置这个状态了
        addressBook.setIsDefault(0);
        addressBook.setUserId(userId);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 查询用户的所有地址
     *
     * @return
     */
    @Override
    public List<AddressBook> list() {
        Long userId = BaseContextByMe.getCurrentId();
        List<AddressBook> list = addressBookMapper.list(userId);
        return list;
    }

    /**
     * 查询默认地址
     * 不知道这个逻辑对不对
     *
     * @return
     */
    @Override
    public AddressBook getDefault() {
        Long userId = BaseContextByMe.getCurrentId();

        // 获取该用户的默认地址
        // select * from address_book where is_default = 1
        AddressBook addressBook = addressBookMapper.getDefault(userId);
        if (addressBook == null) {
            // 如果没有默认地址怎么办?
            List<AddressBook> list = addressBookMapper.list(userId);
            // 查询所有的地址,然后选择第一个?
            addressBook = list.get(0);
        }
        // 不管是否存在默认地址,都可以直接return
        // 如果存在,则不会进入if,不存在,进入if后该addressBook也已经被赋值
        return addressBook;
    }

    /**
     * 修改地址
     *
     * @param addressBook
     */
    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据id查询一条地址信息
     *
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return addressBook;
    }

    /**
     * 根据id删除地址
     *
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

    /**
     * 设置默认地址
     */
    @Override
    public void setDefault(AddressBook addressBook) {
/*        Long id = addressBook.getId();
        addressBookMapper.setDefault(id);
        // 只设置这个默认的可不行,还要把其余的地址全都设置成不是默认的
        // 根据用户id查出该用户的所有地址
        List<AddressBook> list = addressBookMapper.list(BaseContextByMe.getCurrentId());
        for (AddressBook book : list) {
            if (book.getId() == id) {
                continue;
            }
            // 不是要设置默认的地址,就要将该地址的默认设为0
            book.setIsDefault(0);
            // 然后将该条数据更新数据库
            addressBookMapper.update(book);
        }*/

        // =====================

        // 或者说,老师的逻辑是更清楚地,先将该用户的所有地址都设置成不是默认,然后再设置指定地址为默认
        addressBookMapper.updateAllAddressByUserId(BaseContextByMe.getCurrentId());

        // 然后设置默认
        addressBookMapper.setDefault(addressBook.getId());
    }


}

package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author zzmr
 * @create 2023-09-07 20:57
 */
@Mapper
public interface AddressBookMapper {

    @Insert("insert into address_book(user_id, consignee, sex, phone, province_code, province_name, city_code, " +
            "city_name, district_code, district_name, detail, label,is_default) VALUES (#{userId},#{consignee}," +
            "#{sex},#{phone}," +
            "#{provinceCode},#{provinceName},#{cityCode},#{cityName},#{districtCode},#{districtName},#{detail}," +
            "#{label},#{isDefault})")
    void insert(AddressBook addressBook);

    @Select("select * from address_book where user_id = #{userId}")
    List<AddressBook> list(Long userId);

    @Select("select * from address_book where is_default = 1 and user_id = #{userId}")
    AddressBook getDefault(Long userId);

    void update(AddressBook addressBook);

    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    @Delete("delete from address_book where id = #{id}")
    void deleteById(Long id);

    @Update("update address_book set is_default = 1 where id = #{id}")
    void setDefault(Long id);

    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void updateAllAddressByUserId(Long userId);
}

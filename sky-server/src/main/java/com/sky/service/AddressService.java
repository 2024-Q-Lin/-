package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressService {
    /**
     * 新增地址
     * @param addressBook
     */
    void save(AddressBook addressBook);

    /**
     * 查询当前用户所有地址
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 获取默认地址
     * @return
     */
    AddressBook defaultAddress();

    /**
     * 修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getAddressBookById(Long id);

    /**
     * 删除地址
     * @param id
     */
    void deleteById(Long id);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefaultAddress(AddressBook addressBook);
}

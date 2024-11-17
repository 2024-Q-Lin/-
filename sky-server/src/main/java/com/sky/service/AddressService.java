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
}

package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressMapper;
import com.sky.result.Result;
import com.sky.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressMapper.save(addressBook);
    }

    /**
     * 查询当前用户所有地址
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressMapper.list(addressBook);
    }

    /**
     * 查询当前用户默认地址
     * @return
     */
    @Override
    public AddressBook defaultAddress() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(1);
        List<AddressBook> list = addressMapper.list(addressBook);
        return list.get(0);
    }

    /**
     * 修改地址
     * @param addressBook
     */
    @Override
    public void update(AddressBook addressBook) {
        addressMapper.update(addressBook);
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Override
    public AddressBook getAddressBookById(Long id) {
        AddressBook addressBook = new AddressBook();
        addressBook.setId(id);
        List<AddressBook> list = addressMapper.list(addressBook);
        return list.get(0);
    }

    /**
     * 删除地址
     * @param id
     */
    @Override
    public void deleteById(Long id) {
        addressMapper.deleteById(id);
    }

    /**
     * 设置默认地址
     * @param addressBook
     */
    @Override
    public void setDefaultAddress(AddressBook addressBook) {
        //将当前用户所有地址都修改为默认地址
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressMapper.updateIsDefaultByUserId(addressBook);

        //将当前地址改为默认地址
        addressBook.setIsDefault(1);
        addressMapper.update(addressBook);
    }
}

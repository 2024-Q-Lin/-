package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "C端-地址管理接口")
public class AddressController {

    @Autowired
    private AddressService addressService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result<String> saveAddressBook(@RequestBody AddressBook addressBook) {
        log.info("新增地址信息：{}", addressBook);
        addressService.save(addressBook);
        return Result.success();
    }

    /**
     * 查询登录用户的所有地址
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询登录用户的所有地址")
    public Result<List<AddressBook>> list() {
        log.info("查询登录用户的所有地址");
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> addressBookList = addressService.list(addressBook);
        return Result.success(addressBookList);
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> defaultAddress() {
        log.info("查询默认地址");
        AddressBook defaultAddressBook = addressService.defaultAddress();
        if (defaultAddressBook != null) {
            return Result.success(defaultAddressBook);
        }
        return Result.error("没有默认地址");
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("修改地址")
    public Result<String> updateAddress(@RequestBody AddressBook addressBook) {
        log.info("修改地址信息：{}", addressBook);
        addressService.update(addressBook);
        return Result.success();
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getAddressBookById(@PathVariable Long id) {
        log.info("根据id查询地址：{}", id);
        AddressBook addressBook = addressService.getAddressBookById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id删除地址
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result<String> deleteAddressBookById(Long id) {
        log.info("根据id删除地址：{}", id);
        addressService.deleteById(id);
        return Result.success();
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result<String> setDefaultAddress(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址：{}", addressBook);
        addressService.setDefaultAddress(addressBook);
        return Result.success();
    }
}

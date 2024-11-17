package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.Address;
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
}

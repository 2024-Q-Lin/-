package com.sky.controller.user;

import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("user/addressBook")
@Api(tags = "address book related api")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @PostMapping("")
    @ApiOperation("save a new address")
    public Result save(@RequestBody AddressBook addressBook) {
        log.info("save a new address: {}", addressBook);
        addressBookService.save(addressBook);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("get all the address")
    public Result<List<AddressBook>> list() {
        log.info("get all the address of user ");
        // we still use the AddressBook as query condition
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> addresses = addressBookService.list(addressBook);
        return Result.success(addresses);

    }

    /**
     * get default address
     */
    @GetMapping("/default")
    @ApiOperation("get default address")
    public Result<AddressBook> getDefault() {
        AddressBook addressBook = new AddressBook();
        addressBook.setIsDefault(1);
        List<AddressBook> addresses = addressBookService.getDefault(addressBook);
        if (addresses != null && !addresses.isEmpty()) {
            return Result.success(addresses.get(0));
        }
        return Result.error("can't find default address");
    }

    /**
     * get single address by id (before update we need retrieve the original one)
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("get address by id")
    public Result<AddressBook> getById(@PathVariable Long id) {
        AddressBook address = addressBookService.getById(id);
        return Result.success(address);
    }

    @PutMapping("")
    @ApiOperation("update address")
    public Result update(@RequestBody AddressBook addressBook) {
        addressBookService.update(addressBook);
        return Result.success();
    }

    @DeleteMapping("")
    @ApiOperation("delete address by id")
    public Result deleteById(Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/default")
    @ApiOperation("set default address")
    public Result setDefaultAddress(@RequestBody HashMap<String, Long> idObject) {
        Long id = idObject.get("id");
        log.info("set the address to default", id);
        AddressBook addressBook = new AddressBook();
        addressBook.setId(id);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(1);
        addressBookService.setDefault(addressBook);
        return Result.success();
    }
}

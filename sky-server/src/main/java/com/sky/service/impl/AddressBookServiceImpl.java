package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * save a new address
     *
     * @param addressBook
     */
    public void save(AddressBook addressBook) {
        // get to the useId
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);

        // insert a new address
        addressBookMapper.insert(addressBook);
    }

    /**
     * get all the address of the current user
     *
     * @return
     */
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    /**
     * get address by id
     *
     * @param id
     * @return
     */
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    /**
     * get default address
     *
     * @param addressBook
     * @return
     */
    public List<AddressBook> getDefault(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    /**
     * update address
     *
     * @param addressBook
     */
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * delete address by id
     *
     * @param id
     */
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

    public void setDefault(AddressBook addressBook) {
        // update the original default address to 0
        AddressBook originalDefaultAddress = addressBookMapper.getDefaultAddress();
        if (originalDefaultAddress != null) {
            originalDefaultAddress.setIsDefault(0);
            addressBookMapper.update(originalDefaultAddress);
        }
        addressBookMapper.update(addressBook);
    }
}

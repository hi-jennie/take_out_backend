package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

public interface AddressBookService {
    void save(AddressBook addressBook);

    List<AddressBook> list(AddressBook addressBook);

    List<AddressBook> getDefault(AddressBook addressBook);

    void update(AddressBook addressBook);

    AddressBook getById(Long id);

    void deleteById(Long id);

    void setDefault(AddressBook addressBook);
}

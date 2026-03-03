package com.quietlogic.allisok.data.repository

import com.quietlogic.allisok.data.local.dao.ContactSlotDao
import com.quietlogic.allisok.data.local.entity.ContactSlotEntity
import kotlinx.coroutines.flow.Flow

class ContactsRepository(
    private val contactSlotDao: ContactSlotDao
) {

    fun getAllContacts(): Flow<List<ContactSlotEntity>> {
        return contactSlotDao.getAll()
    }

    suspend fun saveContact(contact: ContactSlotEntity) {
        contactSlotDao.insert(contact)
    }

    suspend fun updateContact(contact: ContactSlotEntity) {
        contactSlotDao.update(contact)
    }

    suspend fun clearAll() {
        contactSlotDao.clearAll()
    }
}
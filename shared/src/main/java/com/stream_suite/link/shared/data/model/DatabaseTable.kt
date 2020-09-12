package com.stream_suite.link.shared.data.model

import android.database.Cursor


interface DatabaseTable {

    fun getCreateStatement(): String

    fun getTableName(): String

    fun getIndexStatements(): Array<String>

    fun fillFromCursor(cursor: Cursor)

    fun encrypt(utils: com.stream_suite.link.encryption.EncryptionUtils)

    fun decrypt(utils: com.stream_suite.link.encryption.EncryptionUtils)

}
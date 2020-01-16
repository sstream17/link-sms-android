package xyz.stream.messenger.shared.data.model

import android.database.Cursor
import xyz.stream.messenger.encryption.EncryptionUtils


interface DatabaseTable {

    fun getCreateStatement(): String

    fun getTableName(): String

    fun getIndexStatements(): Array<String>

    fun fillFromCursor(cursor: Cursor)

    fun encrypt(utils: xyz.stream.messenger.encryption.EncryptionUtils)

    fun decrypt(utils: xyz.stream.messenger.encryption.EncryptionUtils)

}
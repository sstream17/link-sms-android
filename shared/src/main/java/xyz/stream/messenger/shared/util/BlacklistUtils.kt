/*
 * Copyright (C) 2020 Luke Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.stream.messenger.shared.util

import android.content.Context
import android.util.Log

import xyz.stream.messenger.shared.data.DataSource
import xyz.stream.messenger.shared.data.Settings
import xyz.stream.messenger.shared.data.model.Blacklist
import xyz.stream.messenger.shared.data.pojo.UnknownNumbersReception
import java.util.regex.PatternSyntaxException

/**
 * Helper for checking whether or not a contact is blacklisted.
 */
object BlacklistUtils {

    fun isBlacklisted(context: Context, incomingNumber: String, incomingText: String?): Boolean {
        val source = DataSource
        val cursor = source.getBlacklists(context)

        val numberIndex = cursor.getColumnIndex(Blacklist.COLUMN_PHONE_NUMBER)
        val phraseIndex = cursor.getColumnIndex(Blacklist.COLUMN_PHRASE)

        if (cursor.moveToFirst()) {
            do {
                val blacklistedNumber = cursor.getString(numberIndex)
                if (!blacklistedNumber.isNullOrBlank() && numbersMatch(incomingNumber, blacklistedNumber)) {
                    Log.v("Blacklist", "$incomingNumber matched phone number blacklist: $blacklistedNumber")
                    CursorUtil.closeSilent(cursor)
                    return true
                }

                val blacklistedPhrase = cursor.getString(phraseIndex)
                val isBlacklisted = !blacklistedPhrase.isNullOrBlank() &&
                        if (Settings.blacklistPhraseRegex) textMatchesBlacklistRegex(blacklistedPhrase, incomingText)
                        else textMatchesBlacklistPhrase(blacklistedPhrase, incomingText)

                if (isBlacklisted) {
                    Log.v("Blacklist", "$incomingText matched phrase blacklist: $incomingText")
                    CursorUtil.closeSilent(cursor)
                    return true
                }
            } while (cursor.moveToNext())
        }

        cursor.closeSilent()
        return isBlockedAsUnknownNumber(context, incomingNumber)
    }

    private fun textMatchesBlacklistPhrase(blacklistedPhrase: String, incomingText: String?): Boolean {
        return incomingText?.toLowerCase()?.contains(blacklistedPhrase.toLowerCase()) == true;
    }

    private fun textMatchesBlacklistRegex(blacklistedPhrase: String, incomingText: String?): Boolean {
        if (incomingText == null) {
            return false;
        }

        return try {
            Regex(blacklistedPhrase, setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE)).containsMatchIn(incomingText);
        } catch (e: PatternSyntaxException) {
            // Return false if they put in an invalid regex
            false;
        }

    }

    fun isMutedAsUnknownNumber(context: Context, number: String): Boolean {
        if (Settings.unknownNumbersReception != UnknownNumbersReception.MUTE) {
            return false
        }

        return !contactExists(context, number)
    }

    private fun isBlockedAsUnknownNumber(context: Context, number: String): Boolean {
        if (Settings.unknownNumbersReception != UnknownNumbersReception.BLOCK) {
            return false
        }

        return !contactExists(context, number)
    }

    private fun contactExists(context: Context, number: String) = try {
        val id = ContactUtils.findContactId(number, context)
        id != -1
    } catch (e: NoSuchElementException) {
        false
    }

    fun numbersMatch(number: String, blacklisted: String): Boolean {
        if (number == blacklisted) {
            // some countries get spam from lettered number (HP-BHKPOS)
            // those would not get matched when it goes into the id matchers,
            // since the letters all get stripped out.
            return true
        }

        val number = PhoneNumberUtils.clearFormattingAndStripStandardReplacements(number)
        val blacklisted = PhoneNumberUtils.clearFormattingAndStripStandardReplacements(blacklisted)

        val numberMatchers = SmsMmsUtils.createIdMatcher(number)
        val blacklistMatchers = SmsMmsUtils.createIdMatcher(blacklisted)

        val shorterLength = if (number.length < blacklisted.length) number.length else blacklisted.length

        return when {
            shorterLength >= 10 -> numberMatchers.tenLetter == blacklistMatchers.tenLetter
            shorterLength in 8..9 -> numberMatchers.eightLetter == blacklistMatchers.eightLetter
            shorterLength == 7 -> numberMatchers.sevenLetter == blacklistMatchers.sevenLetter
            number.length == blacklisted.length -> numberMatchers.fiveLetter == blacklistMatchers.fiveLetter
            else -> false
        }
    }

}

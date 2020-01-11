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

package xyz.klinker.messenger.utils.swipe_to_dismiss

import xyz.klinker.messenger.shared.data.model.Conversation

/**
 * Interface for listening for swipe to delete actions.
 */
interface SwipeToDeleteListener {

    fun onSwipeToDelete(conversation: Conversation)
    fun onSwipeToArchive(conversation: Conversation)
    fun onMarkSectionAsRead(sectionText: String, sectionType: Int)
    fun onShowMarkAsRead(sectionText: String)

}

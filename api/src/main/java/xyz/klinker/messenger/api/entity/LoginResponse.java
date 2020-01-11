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

package xyz.klinker.messenger.api.entity;

public class LoginResponse {

    public String accountId;
    public String salt1;
    public String salt2;
    public String phoneNumber;
    public String name;
    public String baseTheme;
    public String passcode;
    public boolean rounderBubbles;
    public boolean useGlobalTheme;
    public boolean applyPrimaryColorToToolbar;
    public boolean conversationCategories;
    public boolean messageTimestamp;
    public int color;
    public int colorDark;
    public int colorLight;
    public int colorAccent;

    @Override
    public String toString() {
        return accountId + ", " + salt1 + ", " + salt2 + ", " + phoneNumber + ", " + name;
    }

}

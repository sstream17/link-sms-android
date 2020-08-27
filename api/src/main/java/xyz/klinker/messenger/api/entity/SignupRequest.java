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

/**
 * Request to sign up for the service.
 */
public class SignupRequest {

    public String name;
    public String realName;
    public String passwordHash;
    public String phoneNumber;
    public String salt1;
    public String salt2;
    public int iterations;

    public SignupRequest(
            String username,
            String realName,
            String passwordHash,
            String phoneNumber,
            String salt1,
            String salt2,
            int iterations) {
        this.name = username;
        this.realName = realName;
        this.passwordHash = passwordHash;
        this.phoneNumber = phoneNumber;
        this.salt1 = salt1;
        this.salt2 = salt2;
        this.iterations = iterations;
    }

}

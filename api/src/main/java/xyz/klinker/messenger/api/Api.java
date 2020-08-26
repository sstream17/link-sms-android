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

package xyz.klinker.messenger.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Locale;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import xyz.klinker.messenger.api.service.AccountService;
import xyz.klinker.messenger.api.service.ActivateService;
import xyz.klinker.messenger.api.service.AutoReplyService;
import xyz.klinker.messenger.api.service.BetaService;
import xyz.klinker.messenger.api.service.BlacklistService;
import xyz.klinker.messenger.api.service.ContactService;
import xyz.klinker.messenger.api.service.ConversationService;
import xyz.klinker.messenger.api.service.DeviceService;
import xyz.klinker.messenger.api.service.DraftService;
import xyz.klinker.messenger.api.service.FolderService;
import xyz.klinker.messenger.api.service.MessageService;
import xyz.klinker.messenger.api.service.PurchaseService;
import xyz.klinker.messenger.api.service.ScheduledMessageService;
import xyz.klinker.messenger.api.service.TemplateService;

/**
 * Direct access to the messenger APIs using retrofit.
 */
public class Api {

    private static final String API_DEBUG_URL = "https://192.168.0.142:45455/api/";
    private static final String API_STAGING_URL = "https://192.168.0.142:45455/api/";
    private static final String API_RELEASE_URL = "https://192.168.0.142:45455/api/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static CallAdapter.Factory callAdapterFactory = new CallAdapter.Factory() {
        @Override
        public CallAdapter<Object, Object> get(final Type returnType, Annotation[] annotations,
                                       Retrofit retrofit) {
            // if returnType is retrofit2.Call, do nothing
            if (returnType.getClass().getPackage().getName().contains("retrofit2.Call")) {
                return null;
            }

            return new CallAdapter<Object, Object>() {
                @Override
                public Type responseType() {
                    return returnType;
                }

                @Override
                public Object adapt(Call call) {
                    Response response;
                    Call retry = call.clone();

                    try {
                        response = call.execute();
                    } catch (Exception e) {
                        response = null;
                    }

                    if (response == null || !response.isSuccessful()) {
                        try {
                            response = retry.execute();
                        } catch (Exception e) {
                            response = null;
                        }
                    }

                    if (response == null || !response.isSuccessful()) {
                        return null;
                    } else {
                        return response.body();
                    }
                }
            };
        }
    };

    private static Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .create();

    private Retrofit retrofit;
    private String baseUrl;

    public enum Environment {
        DEBUG, STAGING, RELEASE
    }

    /**
     * Creates a new API access object that will connect to the correct environment.
     *
     * @param environment the Environment to use to connect to the APIs.
     */
    public Api(Environment environment) {
        this(environment == Environment.DEBUG ? API_DEBUG_URL :
                (environment == Environment.STAGING ? API_STAGING_URL : API_RELEASE_URL));
    }

    /**
     * Creates a new API access object that will automatically attach your API key to all
     * requests.
     */
    private Api(String baseUrl) {
//        httpClient.addInterceptor(new Interceptor() {
//            @Override
//            public okhttp3.Response intercept(Chain chain) throws IOException {
//                Request request = chain.request();
//                HttpUrl url = request.url().newBuilder().build();
//                request = request.newBuilder().url(url).build();
//                return chain.proceed(request);
//            }
//        });

        // gzip all bodies, the server should automatically unzip them
//        httpClient.addInterceptor(new GzipRequestInterceptor());

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        //httpClient.addInterceptor(logging);

        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .addConverterFactory(GsonConverterFactory.create(gson));
                        //.addCallAdapterFactory(callAdapterFactory);

        this.retrofit = builder.client(httpClient.build()).build();
        this.baseUrl = baseUrl;
    }

    /**
     * Gets a service that can be used for account requests such as signup and login.
     */
    public AccountService account() {
        return retrofit.create(AccountService.class);
    }

    /**
     * Gets a service that can be used for device requests.
     */
    public DeviceService device() {
        return retrofit.create(DeviceService.class);
    }

    /**
     * Gets a service that can be used for message requests.
     */
    public MessageService message() {
        return retrofit.create(MessageService.class);
    }

    /**
     * Gets a service that can be used for contact requests.
     */
    public ContactService contact() {
        return retrofit.create(ContactService.class);
    }

    /**
     * Gets a service that can be used for conversation requests.
     */
    public ConversationService conversation() {
        return retrofit.create(ConversationService.class);
    }

    /**
     * Gets a service that can be used for draft requests.
     */
    public DraftService draft() {
        return retrofit.create(DraftService.class);
    }

    /**
     * Gets a service that can be used for scheduled message requests.
     */
    public ScheduledMessageService scheduled() {
        return retrofit.create(ScheduledMessageService.class);
    }

    /**
     * Gets a service that can be used for blacklist requests.
     */
    public BlacklistService blacklist() {
        return retrofit.create(BlacklistService.class);
    }

    /**
     * Gets a service that can be used for template requests.
     */
    public TemplateService template() {
        return retrofit.create(TemplateService.class);
    }
    /**
     * Gets a service that can be used for template requests.
     */
    public PurchaseService purchases() {
        return retrofit.create(PurchaseService.class);
    }

    /**
     * Gets a service that can be used for auto reply requests.
     */
    public AutoReplyService autoReply() {
        return retrofit.create(AutoReplyService.class);
    }

    /**
     * Gets a service that can be used for folder requests.
     */
    public FolderService folder() {
        return retrofit.create(FolderService.class);
    }

    /**
     * Gets a service that can be used for beta requests.
     */
    public BetaService beta() {
        return retrofit.create(BetaService.class);
    }

    /**
     * Gets a service that can be used to activate your account on the web instead of on the device.
     */
    public ActivateService activate() {
        return retrofit.create(ActivateService.class);
    }

    public String baseUrl() {
        return baseUrl;
    }

    final class GzipRequestInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (originalRequest.body() == null ||
                    originalRequest.header("Content-Encoding") != null) {
                return chain.proceed(originalRequest);
            }

            Request compressedRequest = originalRequest.newBuilder()
                    .header("Content-Encoding", "gzip")
                    .method(originalRequest.method(), gzip(originalRequest.body()))
                    .build();
            return chain.proceed(compressedRequest);
        }

        private RequestBody gzip(final RequestBody body) {
            return new RequestBody() {
                @Override
                public MediaType contentType() {
                    return body.contentType();
                }

                @Override
                public long contentLength() {
                    return -1; // We don't know the compressed length in advance!
                }

                @Override
                public void writeTo(BufferedSink sink) throws IOException {
                    BufferedSink gzipSink = Okio.buffer(new GzipSink(sink));
                    body.writeTo(gzipSink);
                    gzipSink.close();
                }
            };
        }
    }

}

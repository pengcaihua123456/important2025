package com.evenbus.view;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;


public class OkhttpActivity extends Activity {

    // 1. 定义Repo数据类
    public static class Repo {
        private String name;
        private String full_name;

        // 必须有无参构造函数
        public Repo() {}

        // Getter和Setter
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullName() {
            return full_name;
        }

        public void setFullName(String full_name) {
            this.full_name = full_name;
        }
    }



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 测试调用
        okhttp();
        retrofit();
    }

    private void okhttp() {
        // 1. 创建客户端
        OkHttpClient client = new OkHttpClient();

        // 2. 手动构建请求
        Request request = new Request.Builder()
                .url("https://api.github.com/users/octocat/repos")
                .build();

        // 3. 发送请求并处理原始响应

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                }

                // 4. 手动解析 JSON
                String json = response.body().string();
                List<Repo> repos = new Gson().fromJson(json, new TypeToken<List<Repo>>(){}.getType());

                // 5. 手动线程切换
                runOnUiThread(() -> updateUI(repos));
            }
        });
    }

    // 2. 定义接口（声明式API）
    interface GitHubService {
        @GET("users/{user}/repos")
        Call<List<Repo>> listRepos(@Path("user") String user);
    }

    private void retrofit() {
        // 2. 创建服务实例
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GitHubService service = retrofit.create(GitHubService.class);

        // 3. 直接调用接口方法
        service.listRepos("").enqueue(new retrofit2.Callback<List<Repo>>() {
            @Override
            public void onResponse(Call<List<Repo>> call, Response<List<Repo>> response) {
                if (response.isSuccessful()) {
                    // 4. 自动获得解析后的对象（主线程回调）
                    updateUI(response.body());
                } else {
                    System.out.println("请求失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Repo>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void updateUI(List<Repo> repos) {
        // 更新UI的代码
        if (repos != null) {
            for (Repo repo : repos) {
                System.out.println("Repo: " + repo.getName());
            }
        }
    }
}
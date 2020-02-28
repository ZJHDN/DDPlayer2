package top.bilibililike.player.widget.live.subtitle.roominfo;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import top.bilibililike.player.widget.live.subtitle.utils.Api;
import top.bilibililike.player.widget.live.subtitle.utils.MD5Util;


/**
 * @author Xbs
 * @date 2020年1月19日15:26:11
 */
public class RoomInterceptor implements Interceptor {
    private HashMap<String, String> paramMap;

    public RoomInterceptor(HashMap<String, String> paramMap) {
        if (paramMap != null) {
            this.paramMap = paramMap;
        }
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        Request request = chain.request();
        HashMap<String, String> paramMap = new HashMap<>(Api.getParams());
        if (this.paramMap != null) {
            paramMap.putAll(this.paramMap);
        }

        HttpUrl httpUrl = request.url();

        httpUrl.queryParameterNames();
        List<String> queryNames = new ArrayList<>(httpUrl.queryParameterNames());
        for (String param : queryNames) {
            Log.d("GetInterceptor", param);
            paramMap.put(param, httpUrl.queryParameterValues(param).get(0));
        }
        List<String> paramNames = new ArrayList<>(paramMap.keySet());
        Collections.sort(paramNames);

        HttpUrl.Builder builder = httpUrl.newBuilder();
        StringBuilder signBuilder = new StringBuilder();
        for (int i = 0; i < paramNames.size(); i++) {
            builder.addQueryParameter(paramNames.get(i), paramMap.get(paramNames.get(i)));
            signBuilder.append(paramNames.get(i)).append("=").append(URLEncoder.encode(paramMap.get(paramNames.get(i))));
            if ((i + 1) < paramNames.size()) {
                signBuilder.append("&");
            }
        }
        signBuilder.append(Api.APP_SECRET);
        //Log.d("GetInterceptor sign", signBuilder.toString());
        builder.addQueryParameter("sign", MD5Util.getMD5(signBuilder.toString()));
        httpUrl = builder.build();
        Log.d("GetInterceptor", httpUrl.toString());
        request = request.newBuilder().url(httpUrl).build();


        return chain.proceed(request);

    }

    public void replaceParam(HashMap<String, String> param) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        } else {
            paramMap.clear();
            paramMap.putAll(Api.getParams());
        }
        if (param != null){
            paramMap.putAll(param);
        }

    }

    public void replaceRoom(String roomId) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
            paramMap.putAll(Api.getParams());
        }
        if (paramMap.containsKey("room_id")) {
            paramMap.replace("room_id", roomId);
        } else {
            paramMap.put("room_id", roomId);
        }
    }
}



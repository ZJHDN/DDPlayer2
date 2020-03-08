package top.bilibililike.player.widget.live.subtitle.roominfo;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import top.bilibililike.player.widget.live.subtitle.bean.RepoBean;
import top.bilibililike.player.widget.live.subtitle.utils.LiverRepo;


/**
 * @author Xbs
 * @date 2020年1月19日14:59:27
 */
public class RoomRepo {
    /**
     * 面包狗 21421141  aqua 14917277  星街 190577 coco 21752686  peko 21560356 狗妈 21304638 心心 14275133
     */
    private static final String[] Liver_ROOMS = LiverRepo.getLiverRooms();

    public static void getLivers(LiverCallback callback) {
        List<RepoBean.DataBean> resultList = new ArrayList<>();
        RoomInterceptor roomInterceptor = new RoomInterceptor(null);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(roomInterceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.live.bilibili.com/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        RepoService service = retrofit.create(RepoService.class);

        Observable.fromArray(Liver_ROOMS)
                .subscribeOn(Schedulers.io())
                .flatMap(roomId -> {
                    roomInterceptor.replaceRoom(roomId);
                    return service.getRoomData();
                })
                .map(repoBean -> {
                    if (repoBean.getCode() == 0) {
                        return repoBean.getData();
                    } else {
                        Observable.error(new Throwable("sign Error"));
                        return repoBean.getData();
                    }
                })
                .retryWhen(throwableObservable -> throwableObservable
                        .flatMap((Function<Throwable, ObservableSource<?>>)
                                throwable -> {
                                    Log.d("RoomRepo", throwable.toString());
                                    roomInterceptor.replaceParam(null);
                                    resultList.clear();
                                    return Observable.timer(100, TimeUnit.MILLISECONDS);
                                })
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RepoBean.DataBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(RepoBean.DataBean dataBean) {
                        if (dataBean.getRoom_info().getLive_status() == 1) {
                            resultList.add(dataBean);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        callback.onSuccess(resultList);
                    }
                });


    }

    public interface LiverCallback {
        /**
         * callBack，用于获取Liver房间的回调
         *
         * @param liverList 正在开播的Liver房间列表
         */
        void onSuccess(List<RepoBean.DataBean> liverList);

        void onStartLoading();
        //todo show dialog


        void onError(String reason);
    }

}

package com.cniao5.cniao5shop.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;

import com.cniao5.cniao5shop.MyApplication;
import com.cniao5.cniao5shop.R;
import com.cniao5.cniao5shop.adapter.BaseAdapter;
import com.cniao5.cniao5shop.adapter.FavoriteAdapter;
import com.cniao5.cniao5shop.adapter.decoration.CardViewtemDecortion;
import com.cniao5.cniao5shop.bean.Favorite;
import com.cniao5.cniao5shop.http.OkHttpHelper;
import com.cniao5.cniao5shop.http.SpotsCallBack;
import com.cniao5.cniao5shop.msg.BaseResMsg;
import com.cniao5.cniao5shop.widget.Constants;
import com.cniao5.cniao5shop.widget.CustomDialog;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.okhttp.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 我的收藏
 */
public class MyFavoriteActivity extends BaseActivity{

    @ViewInject(R.id.recycle_view)
    private RecyclerView mRecyclerview;

    private FavoriteAdapter mAdapter;

    private OkHttpHelper okHttpHelper = OkHttpHelper.getInstance();
    private CustomDialog mDialog;
    private OkHttpHelper mHttpHelper = OkHttpHelper.getInstance();

    private void initFavorite() {

        String userId = MyApplication.getInstance().getUser().getId() + "";

        if (!TextUtils.isEmpty(userId)) {
            Map<String, String> params = new HashMap<>();

            params.put("user_id", userId);

            okHttpHelper.doGet(Constants.API.FAVORITE_LIST, params, new SpotsCallBack<List<Favorite>>(this) {
                @Override
                public void onSuccess(Response response, List<Favorite> favorites) {
                    showFavorite(favorites);
                }

                @Override
                public void onError(Response response, int code, Exception e) {

                }
            });
        }
    }

    /**
     * 显示删除提示对话框
     *
     * @param favorite
     */
    private void showDialog(final Favorite favorite) {
        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage("您确定删除该商品吗？");
        builder.setTitle("友情提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteFavorite(favorite);
                initFavorite();

                if (mDialog.isShowing())
                    mDialog.dismiss();
            }
        });

        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (mDialog.isShowing())
                            mDialog.dismiss();
                    }
                });

        mDialog = builder.create();
        mDialog.show();
    }

    private void deleteFavorite(Favorite favorite) {
        Map<String, String> params = new HashMap<>(1);
        params.put("id", favorite.getId() + "");

        mHttpHelper.doPost(Constants.API.FAVORITE_DEL, params, new SpotsCallBack<BaseResMsg>(MyFavoriteActivity.this) {

            @Override
            public void onSuccess(Response response, BaseResMsg resMsg) {
                if (resMsg.getStatus() == resMsg.STATUS_SUCCESS) {
                    setResult(RESULT_OK);
                    System.out.println("----------------" + resMsg.getStatus());
                    if (mDialog.isShowing())
                        mDialog.dismiss();
                }
            }

            @Override
            public void onError(Response response, int code, Exception e) {

            }
        });
    }

    private void showFavorite(final List<Favorite> favorites) {

        if (mAdapter == null) {
            mAdapter = new FavoriteAdapter(this, favorites, new FavoriteAdapter.FavoriteLisneter() {
                @Override
                public void onClickDelete(Favorite favorite) {
                    showDialog(favorite);
                }
            });
            mRecyclerview.setAdapter(mAdapter);
            mRecyclerview.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerview.addItemDecoration(new CardViewtemDecortion());

            mAdapter.setOnItemClickListenner(new BaseAdapter.OnItemClickListenner() {
                @Override
                public void onItemClick(View view, int position) {
                    mAdapter.showDetail(favorites.get(position).getWares());
                }
            });
        } else {
            mAdapter.refreshData(favorites);
            mRecyclerview.setAdapter(mAdapter);
        }
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_my_favorite;
    }

    @Override
    public void init() {
        initFavorite();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initFavorite();
    }

    @Override
    public void setToolbar() {
        getToolbar().setTitle("我的收藏");
        getToolbar().setleftButtonIcon(R.drawable.icon_back_32px);
    }

}

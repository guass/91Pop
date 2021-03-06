package com.dante.ui;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.danikula.videocache.HttpProxyCacheServer;
import com.orhanobut.logger.Logger;
import com.trello.lifecycle2.android.lifecycle.AndroidLifecycle;
import com.trello.rxlifecycle2.LifecycleProvider;
import com.dante.MyApplication;
import com.dante.R;
import com.dante.custom.TastyToast;
import com.dante.data.DataManager;
import com.dante.data.model.Category;
import com.dante.data.model.UnLimit91PornItem;
import com.dante.data.model.User;
import com.dante.di.component.ActivityComponent;
import com.dante.di.component.DaggerActivityComponent;
import com.dante.di.module.ActivityModule;
import com.dante.utils.PlaybackEngine;
import com.dante.utils.constants.Keys;

import javax.inject.Inject;

/**
 * @author flymegoc
 * @date 2017/11/20
 * @describe
 */

public abstract class BaseFragment extends Fragment {
    protected final LifecycleProvider<Lifecycle.Event> provider = AndroidLifecycle.createLifecycleProvider(this);
    private final String TAG = getClass().getSimpleName();
    private final String KEY_SAVE_DIN_STANCE_STATE_CATEGORY = "key_save_din_stance_state_category";
    protected Context context;
    protected Activity activity;
    protected Category category;
    protected boolean mIsLoadedData;
    protected RecyclerView recyclerView;
    @Inject
    protected HttpProxyCacheServer httpProxyCacheServer;
    @Inject
    protected User user;
    @Inject
    protected DataManager dataManager;
    private ActivityComponent mActivityComponent;

    public void scrollToTop() {
        if (getView() != null) {
            recyclerView = getView().findViewById(R.id.recyclerView);
            Log.d(TAG, "onCreate: "+recyclerView+ getClass().getSimpleName());
        }
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = getContext();
        activity = getActivity();
        mActivityComponent = DaggerActivityComponent.builder()
                .activityModule(new ActivityModule((AppCompatActivity) activity))
                .applicationComponent(((MyApplication) activity.getApplication()).getApplicationComponent())
                .build();
    }

    public ActivityComponent getActivityComponent() {
        return mActivityComponent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            category = (Category) savedInstanceState.getSerializable(KEY_SAVE_DIN_STANCE_STATE_CATEGORY);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_SAVE_DIN_STANCE_STATE_CATEGORY, category);
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed()) {
            handleOnVisibilityChangedToUser(isVisibleToUser);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            handleOnVisibilityChangedToUser(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            handleOnVisibilityChangedToUser(false);
        }
    }

    /**
     * 处理对用户是否可见
     *
     * @param isVisibleToUser 可见
     */
    private void handleOnVisibilityChangedToUser(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            // 对用户可见
            if (!mIsLoadedData) {
                mIsLoadedData = true;
                onLazyLoadOnce();
            }
            onVisibleToUser();
        } else {
            // 对用户不可见
            onInvisibleToUser();
        }
    }

    /**
     * 懒加载一次。如果只想在对用户可见时才加载数据，并且只加载一次数据，在子类中重写该方法
     */
    protected void onLazyLoadOnce() {
    }

    /**
     * 对用户可见时触发该方法。如果只想在对用户可见时才加载数据，在子类中重写该方法
     */
    protected void onVisibleToUser() {
    }

    /**
     * 对用户不可见时触发该方法
     */
    protected void onInvisibleToUser() {
    }

    public String getTitle() {
        return "";
    }

    /**
     * 带动画的启动activity
     */
    public void startActivityWithAnimotion(Intent intent) {
        startActivity(intent);
        playAnimation();
    }

    /**
     * 带动画的启动activity
     */
    public void startActivityForResultWithAnimotion(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
        playAnimation();
    }

    private void playAnimation() {
        if (activity != null) {
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.side_out_left);
        }
    }

    protected void goToPlayVideo(UnLimit91PornItem unLimit91PornItem) {
        Intent intent = PlaybackEngine.getPlaybackEngineIntent(getContext(), dataManager.getPlaybackEngine());
        intent.putExtra(Keys.KEY_INTENT_UNLIMIT91PORNITEM, unLimit91PornItem);
        if (activity != null) {
            startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.side_out_left);
        } else {
            showMessage("无法获取宿主Activity", TastyToast.WARNING);
        }
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logger.t(TAG).d("------------------onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.t(TAG).d("------------------onDestroy()");
    }

    protected void showMessage(String msg, int type) {
        if (getView() == null) return;
        TastyToast.makeText(context.getApplicationContext(), msg, TastyToast.LENGTH_SHORT, type);
    }
}

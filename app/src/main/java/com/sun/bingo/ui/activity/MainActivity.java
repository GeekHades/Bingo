package com.sun.bingo.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.siyamed.shapeimageview.CircularImageView;
import com.shamanland.fab.FloatingActionButton;
import com.sun.bingo.R;
import com.sun.bingo.control.NavigateManager;
import com.sun.bingo.entity.UserEntity;
import com.sun.bingo.framework.update.DownloadApk;
import com.sun.bingo.ui.fragment.FavoriteFragment;
import com.sun.bingo.ui.fragment.MyBingoFragment;
import com.sun.bingo.ui.fragment.SquareBingoFragment;
import com.sun.bingo.util.ShareUtil;
import com.sun.bingo.util.UserEntityUtil;
import com.sun.bingo.util.theme.ColorChooserDialog;
import com.sun.bingo.util.theme.Selector;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.bmob.v3.BmobUser;


public class MainActivity extends BaseActivity implements ColorChooserDialog.Callback {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.fl_container)
    FrameLayout flContainer;
    @InjectView(R.id.floating_action_button)
    FloatingActionButton floatingActionButton;
    @InjectView(R.id.main_navigation_layout)
    NavigationView mainNavigationLayout;
    @InjectView(R.id.main_drawer_layout)
    DrawerLayout mainDrawerLayout;

    private String[] titles;
    private int mCurrentPosition = 0;

    private CircularImageView civUserAvatar;
    private TextView tvNickName;
    private TextView tvUserSign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        initVersion();
        checkToken();
        initData();
        initView();
        initListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkToken();
    }

    private void initVersion() {
        new DownloadApk(this).checkVersion();
    }

    private void checkToken() {
        userEntity = BmobUser.getCurrentUser(this, UserEntity.class);
        if (userEntity == null) {
            NavigateManager.gotoLoginActivity(this);
            finish();
        }
    }

    private void initData() {
        titles = new String[3];
        titles[0] = getString(R.string.menu_square_bingo);
        titles[1] = getString(R.string.menu_my_bingo);
        titles[2] = getString(R.string.menu_my_favorite);
    }

    @SuppressLint("NewApi")
    private void initView() {
        initToolBar(toolbar, false, R.string.app_name);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mainDrawerLayout, toolbar, 0, 0);
        drawerToggle.syncState();
        if (userEntity != null) {
            controlShowFragment(0);
        }

        floatingActionButton.setBackground(Selector.createOvalShapeSelector(getColorPrimary()));
        civUserAvatar = (CircularImageView) mainNavigationLayout.findViewById(R.id.civ_user_avatar);
        tvNickName = (TextView) mainNavigationLayout.findViewById(R.id.tv_nick_name);
        tvUserSign = (TextView) mainNavigationLayout.findViewById(R.id.tv_user_sign);

        if (userEntity != null) {
            UserEntityUtil.setUserAvatarView(civUserAvatar, userEntity.getUserAvatar());
            UserEntityUtil.setTextViewData(tvNickName, userEntity.getNickName());
            UserEntityUtil.setTextViewData(tvUserSign, userEntity.getUserSign());
        }
    }

    private void initListener() {
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigateManager.gotoEditNewBingoActivity(MainActivity.this);
            }
        });

        mainNavigationLayout.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_square_bingo:
                        controlShowFragment(0);
                        break;
                    case R.id.nav_my_bingo:
                        controlShowFragment(1);
                        break;
                    case R.id.nav_favorite_bingo:
                        controlShowFragment(2);
                        break;
                    case R.id.nav_switch_theme:
                        changeTheme();
                        break;
                    case R.id.nav_feedback:
                        ShareUtil.feedback(MainActivity.this);
                        break;
                    case R.id.nav_send_to_friend:
                        ShareUtil.sendToFriend(MainActivity.this);
                        break;
                    case R.id.nav_share:
                        ShareUtil.share(MainActivity.this);
                        break;
                }
                menuItem.setChecked(true);
                return true;
            }
        });

        civUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigateManager.gotoProfileActivity(MainActivity.this, false);
            }
        });
    }

    private void controlShowFragment(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Fragment oldFragment = fragmentManager.findFragmentByTag(makeTag(mCurrentPosition));
        if (oldFragment != null) {
            fragmentTransaction.hide(oldFragment);
        }
        mCurrentPosition = position;

        Fragment currentFragment = fragmentManager.findFragmentByTag(makeTag(position));
        if (currentFragment != null) {
            fragmentTransaction.show(currentFragment);
        } else {
            fragmentTransaction.add(R.id.fl_container, getFragment(position), makeTag(position));
        }
        fragmentTransaction.commitAllowingStateLoss();

        if (mainDrawerLayout.isShown()) {
            mainDrawerLayout.closeDrawers();
        }
        toolbar.setTitle(titles[position]);
    }

    private String makeTag(int position) {
        return R.id.fl_container + "" + position;
    }

    private Fragment getFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new SquareBingoFragment();
                break;
            case 1:
                fragment = new MyBingoFragment();
                break;
            case 2:
                fragment = new FavoriteFragment();
                break;
            default:
                fragment = new SquareBingoFragment();
                break;
        }
        return fragment;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //修复Fragment重叠问题
    }

    //设置主题
    private void changeTheme() {
        new ColorChooserDialog().show(this, getSettingsSharedPreferences().themeValue());
    }

    @Override
    public void onColorSelection(int index, int color, int darker) {
        getSettingsSharedPreferences().themeValue(index);
        recreate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_menu_app:
                NavigateManager.gotoAboutAppActivity(this);
                return true;
            case R.id.item_menu_author:
                NavigateManager.gotoAboutAuthorActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case NavigateManager.PROFILE_REQUEST_CODE:
                if (userEntity != null) {
                    userEntity = BmobUser.getCurrentUser(MainActivity.this, UserEntity.class);
                    UserEntityUtil.setUserAvatarView(civUserAvatar, userEntity.getUserAvatar());
                    UserEntityUtil.setTextViewData(tvNickName, userEntity.getNickName());
                    UserEntityUtil.setTextViewData(tvUserSign, userEntity.getUserSign());
                }
                break;
        }
    }
}

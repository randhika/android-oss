package com.kickstarter.ui.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.CurrentUser;
import com.kickstarter.libs.KSColorUtils;
import com.kickstarter.libs.Logout;
import com.kickstarter.models.User;
import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.activities.ActivityFeedActivity;
import com.kickstarter.ui.activities.DiscoveryActivity;
import com.kickstarter.ui.activities.LoginToutActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class DiscoveryToolbar extends Toolbar {
  @BindColor(R.color.discovery_toolbar) int discoveryToolbarColor;
  @Bind(R.id.activity_feed_button) TextView activityFeedButton;
  @Bind(R.id.color_button) TextView colorButton;
  @Bind(R.id.current_user_button) TextView currentUserButton;
  @Bind(R.id.filter_expand_more_button) TextView filterExpandMoreButton;
  @Bind(R.id.filter_text_view) TextView filterTextView;
  @Bind(R.id.login_button) TextView loginButton;
  @Inject CurrentUser currentUser;
  @Inject Logout logout;

  Subscription loginSubscription;

  public DiscoveryToolbar(final Context context) {
    super(context);
  }

  public DiscoveryToolbar(final Context context, final AttributeSet attrs) {
    super(context, attrs);
  }

  public DiscoveryToolbar(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    if (isInEditMode()) {
      return;
    }

    ButterKnife.bind(this);
    ((KSApplication) getContext().getApplicationContext()).component().inject(this);

    activityFeedButton.setOnClickListener(v -> {
      final Context context = getContext();
      context.startActivity(new Intent(context, ActivityFeedActivity.class));
    });
  }

  @OnClick(R.id.filter_button)
  public void filterButtonClick(@NonNull final View view) {
    final DiscoveryActivity activity = (DiscoveryActivity) getContext();
    activity.presenter().filterButtonClick();
  }

  public void style(@NonNull final DiscoveryParams params) {
    filterTextView.setText(params.filterString(getContext()));

    final int backgroundColor = params.category() != null ?
      params.category().secondaryColor(getContext()) :
      discoveryToolbarColor;
    setBackgroundColor(backgroundColor);

    setForegroundColor(KSColorUtils.foregroundColor(backgroundColor, getContext()));
  }

  protected void showLoggedInMenu(final User user) {
    loginButton.setVisibility(GONE);
    currentUserButton.setVisibility(VISIBLE);
    currentUserButton.setOnClickListener(v -> {
      final PopupMenu popup = new PopupMenu(v.getContext(), currentUserButton);
      popup.getMenuInflater().inflate(R.menu.current_user_menu, popup.getMenu());

      popup.setOnMenuItemClickListener(item -> {
        switch (item.getItemId()) {
          case R.id.logout:
            final Context context = v.getContext();
            logout.execute();
            final Intent intent = new Intent(context, DiscoveryActivity.class)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
            break;
        }

        return true;
      });

      popup.show();
    });
  }

  protected void showLoggedOutMenu() {
    currentUserButton.setVisibility(GONE);
    loginButton.setVisibility(VISIBLE);
    loginButton.setOnClickListener(v -> {
      Intent intent = new Intent(getContext(), LoginToutActivity.class);
      getContext().startActivity(intent);
    });

  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (isInEditMode()) {
      return;
    }

    if (currentUser.getUser() == null) {
      showLoggedOutMenu();
    }

    loginSubscription = currentUser.loggedInUser()
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(this::showLoggedInMenu);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (isInEditMode()) {
      return;
    }

    loginSubscription.unsubscribe();
  }

  protected void setForegroundColor(final int color) {
    final Observable<TextView> views = Observable.just(activityFeedButton,
      colorButton,
      currentUserButton,
      filterExpandMoreButton,
      filterTextView,
      loginButton);

    final float alpha = KSColorUtils.isLight(color) ? 1.0f : 0.8f;

    final Subscription subscription = views.subscribe(view -> {
      view.setAlpha(alpha);
      view.setTextColor(color);
    });

  }

  // TODO: Remove
  public void setColorFromEditor(final int color) {
    setBackgroundColor(color);
    setForegroundColor(KSColorUtils.foregroundColor(color, getContext()));
  }

  // TODO: Remove
  @OnClick({R.id.color_button})
  public void onColorButtonClicked() {
    final DiscoveryActivity activity = (DiscoveryActivity) getContext();
    activity.showColorDialog();
  }
}

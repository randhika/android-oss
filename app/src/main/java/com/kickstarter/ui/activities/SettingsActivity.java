package com.kickstarter.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.SwitchCompat;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxCompoundButton;
import com.kickstarter.KSApplication;
import com.kickstarter.R;
import com.kickstarter.libs.BaseActivity;
import com.kickstarter.libs.Logout;
import com.kickstarter.libs.qualifiers.RequiresViewModel;
import com.kickstarter.viewmodels.SettingsViewModel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;

@RequiresViewModel(SettingsViewModel.class)
public final class SettingsActivity extends BaseActivity<SettingsViewModel> {
  protected @Bind(R.id.happening_now_switch) SwitchCompat happeningNewsletterSwitch;
  protected @Bind(R.id.kickstarter_news_and_events_switch) SwitchCompat promoNewsletterSwitch;
  protected @Bind(R.id.projects_we_love_switch) SwitchCompat weeklyNewsletterSwitch;

  protected @BindColor(R.color.green) int green;
  protected @BindColor(R.color.gray) int gray;

  @Inject Logout logout;

  @Override
  protected void onCreate(final @Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_layout);
    ButterKnife.bind(this);
    ((KSApplication) getApplication()).component().inject(this);

    RxCompoundButton.checkedChanges(happeningNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(viewModel::sendHappeningNewsletter);

    RxCompoundButton.checkedChanges(promoNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(viewModel::sendPromoNewsletter);

    RxCompoundButton.checkedChanges(weeklyNewsletterSwitch)
      .compose(bindToLifecycle())
      .subscribe(viewModel::sendWeeklyNewsletter);
  }

  @OnClick(R.id.log_out_button)
  public void logout() {
    logout.execute();
    final Intent intent = new Intent(this, DiscoveryActivity.class)
      .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
  }

  @OnClick(R.id.manage_project_notifications)
  public void onClick() {
    // todo
  }

  public void setDisabled(final @NonNull TextView iconTextView) {
    iconTextView.setTextColor(gray);
  }

  public void setEnabled(final @NonNull TextView iconTextView) {
    iconTextView.setTextColor(green);
  }
}

package com.bluelinelabs.conductor.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.bluelinelabs.conductor.archnavigation.NavHostLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class ArchNavActivity extends AppCompatActivity implements ActionBarProvider {

  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.controller_container)
  NavHostLayout container;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_navmain);
    ButterKnife.bind(this);

    setSupportActionBar(toolbar);
  }

  @Override
  public void onBackPressed() {
    if (!container.onBackPressed()) {
      super.onBackPressed();
    }
  }
}

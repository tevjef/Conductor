/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bluelinelabs.conductor.archnavigation;

import android.app.Activity;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.lifecycle.ViewModelStore;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NavigationRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;

import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavHost;
import androidx.navigation.Navigation;
import androidx.navigation.Navigator;

/**
 * NavHostController provides an area within your layout for self-contained navigation to occur.
 *
 * <p>NavHostController is intended to be used as the content area within a layout resource
 * defining your app's chrome around it, e.g.:</p>
 *
 * <pre class="prettyprint">
 *     <android.support.v4.widget.DrawerLayout
 *             xmlns:android="http://schemas.android.com/apk/res/android"
 *             xmlns:app="http://schemas.android.com/apk/res-auto"
 *             android:layout_width="match_parent"
 *             android:layout_height="match_parent">
 *         <com.bluelinelabs.conductor.archnavigation.NavHostLayout
 *             android:layout_width="match_parent"
 *             android:layout_height="match_parent"
 *             app:defaultNavHost="true"
 *             app:navGraph="@navigation/nav_graph"
 *             />
 *         <android.support.design.widget.NavigationView
 *                 android:layout_width="wrap_content"
 *                 android:layout_height="match_parent"
 *                 android:layout_gravity="start"/>
 *     </android.support.v4.widget.DrawerLayout>
 * </pre>
 *
 * <p>Each NavHostController has a {@link NavController} that defines valid navigation within
 * the navigation host. This includes the {@link NavGraph navigation graph} as well as navigation
 * state such as current location and back stack that will be saved and restored along with the
 * NavHostController itself.</p>
 *
 * <p>NavHostControllers register their navigation controller at the root of their view subtree
 * such that any descendant can obtain the controller instance through the {@link Navigation}
 * helper class's methods such as {@link Navigation#findNavController(View)}. View event listener
 * implementations such as {@link View.OnClickListener} within navigation destination
 * controllers can use these helpers to navigate based on user interaction without creating a tight
 * coupling to the navigation host.</p>
 */
public class NavHostLayout extends FrameLayout implements NavHost, LifecycleObserver {
  private static final String KEY_NAV_CONTROLLER_STATE =
      "android-support-nav:controller:navControllerState";
  private int graphId;
  private StateViewModel viewModel;
  private NavController navController;
  private Router router;

  public NavHostLayout(@NonNull Context context) {
    super(context);
  }

  public NavHostLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    final TypedArray a = context.obtainStyledAttributes(attrs, com.bluelinelabs.conductor.archnavigation.R.styleable.NavHostLayout);
    graphId = a.getResourceId(com.bluelinelabs.conductor.archnavigation.R.styleable.NavHostLayout_navGraph, 0);
    a.recycle();

    init(context);
  }

  public void init(Context context) {
    viewModel = ViewModelProviders.of((AppCompatActivity) context).get(StateViewModel.class);
    Bundle savedInstanceState = viewModel.getState();

    router = Conductor.attachRouter((Activity) this.getContext(), this, savedInstanceState);
    navController = new NavController(context);
    Navigation.setViewNavController(this, navController);
    navController.getNavigatorProvider().addNavigator(createControllerNavigator());

    Bundle navState = null;
    if (savedInstanceState != null) {
      navState = savedInstanceState.getBundle(KEY_NAV_CONTROLLER_STATE);
    }

    if (navState != null) {
      navController.restoreState(navState);
    } else {
      if (graphId != 0) {
        navController.setGraph(graphId);
      } else {
        navController.setMetadataGraph();
      }
    }
  }

  @NonNull
  @Override
  public NavController getNavController() {
    if (navController == null) {
      throw new IllegalStateException("NavController is not available");
    }
    return navController;
  }

  @Nullable
  @Override
  protected Parcelable onSaveInstanceState() {
    Bundle navState = navController.saveState();
    if (navState != null) {
      viewModel.getState().putBundle(KEY_NAV_CONTROLLER_STATE, navState);
    }
    return super.onSaveInstanceState();
  }
  @NonNull
  protected Navigator<? extends ControllerNavigator.Destination> createControllerNavigator() {
    return new ControllerNavigator(router);
  }

  /**
   * Set a {@link NavGraph} for this navigation host's {@link NavController} by resource id.
   * The existing graph will be replaced.
   *
   * @param graphResId resource id of the navigation graph to inflate
   */
  public void setGraph(@NavigationRes int graphResId) {
    navController.setGraph(graphResId);
  }

  public boolean onBackPressed() {
    return router.handleBack();
  }

  public static class StateViewModel extends ViewModel {
    private final Bundle state;

    public StateViewModel() {
      this.state = new Bundle();
    }

    Bundle getState() {
      return state;
    }
  }
}
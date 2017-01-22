package com.bluelinelabs.conductor.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;
import com.bluelinelabs.conductor.changehandler.SimpleSwapChangeHandler;
import com.bluelinelabs.conductor.demo.controllers.DragDismissController;
import com.bluelinelabs.conductor.demo.controllers.HomeController;
import com.bluelinelabs.conductor.demo.controllers.MasterDetailListController;
import com.bluelinelabs.conductor.demo.controllers.MultipleChildRouterController;
import com.bluelinelabs.conductor.demo.controllers.NavigationDemoController;
import com.bluelinelabs.conductor.demo.controllers.NavigationDemoController.DisplayUpMode;
import com.bluelinelabs.conductor.demo.controllers.PagerController;
import com.bluelinelabs.conductor.demo.controllers.ParentController;
import com.bluelinelabs.conductor.demo.controllers.RxLifecycle2Controller;
import com.bluelinelabs.conductor.demo.controllers.RxLifecycleController;
import com.bluelinelabs.conductor.demo.controllers.TargetDisplayController;
import com.bluelinelabs.conductor.demo.util.DeepLinkHandler;
import com.bluelinelabs.conductor.demo.util.DeepLinkHandler.DeepLinkCall;
import com.bluelinelabs.conductor.demo.util.DeepLinkHandler.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class MainActivity extends AppCompatActivity implements ActionBarProvider {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.controller_container) ViewGroup container;

    private Router router;
    private DeepLinkHandler deepLinkHandler = new DeepLinkHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        registerDeepLinks();

        router = Conductor.attachRouter(this, container, savedInstanceState);
        if (!deepLinkHandler.handleIntent(getIntent()) && !router.hasRootController()) {
            router.setRoot(RouterTransaction.with(new HomeController()));
        }
    }

    @Override
    public void onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        deepLinkHandler.handleIntent(intent);
    }

    private void registerDeepLinks() {
        deepLinkHandler.registerDeepLink("navigation", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new NavigationDemoController(0, DisplayUpMode.SHOW_FOR_CHILDREN_ONLY));
            }
        });

        deepLinkHandler.registerDeepLink("transitions", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                HomeController controller = new HomeController();
                List<RouterTransaction> backstack = Collections.singletonList(RouterTransaction.with(controller));
                router.setBackstack(backstack, new SimpleSwapChangeHandler());
            }
        });

        deepLinkHandler.registerDeepLink("target", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new TargetDisplayController());
            }
        });

        deepLinkHandler.registerDeepLink("viewpager", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new PagerController());
            }
        });

        deepLinkHandler.registerDeepLink("child", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new ParentController());
            }
        });

        deepLinkHandler.registerDeepLink("overlay", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                //TODO
                HomeController controller = new HomeController();
                setBackstackWith(controller);
            }
        });

        deepLinkHandler.registerDeepLink("drag-dismiss", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new DragDismissController());
            }
        });

        deepLinkHandler.registerDeepLink("rxlifecycle", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new RxLifecycleController());
            }
        });

        deepLinkHandler.registerDeepLink("rxlifecycle2", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new RxLifecycle2Controller());
            }
        });

        deepLinkHandler.registerDeepLink("multiple-child", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new MultipleChildRouterController());
            }
        });

        deepLinkHandler.registerDeepLink("master-detail", new DeepLinkCall() {
            @Override
            public void call(List<Parameter> parameters) {
                setBackstackWith(new MasterDetailListController());
            }
        });
    }

    private void setBackstackWith(Controller controller) {
        RouterTransaction transaction = RouterTransaction.with(controller)
                .pushChangeHandler(new FadeChangeHandler())
                .popChangeHandler(new FadeChangeHandler());

        setBackstackWith(transaction);
    }

    private void setBackstackWith(RouterTransaction... transactions) {
        List<RouterTransaction> backstack = new ArrayList<>();
        backstack.add(RouterTransaction.with(new HomeController()));
        backstack.addAll(Arrays.asList(transactions));

        router.setBackstack(backstack, new SimpleSwapChangeHandler());
    }

}

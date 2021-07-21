package com.example.tablayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AdapterStatics<DATA> {

  static class Refresher implements Runnable {
    private WeakReference<AdapterStatics> reference;

    public Refresher(AdapterStatics statics) {
      reference = new WeakReference<>(statics);
    }

    @Override
    public void run() {
      AdapterStatics statics = reference.get();
      if (statics != null) {
        statics.exeRefresher();
      }
    }
  }

  static class Recovery implements Runnable {
    private WeakReference<AdapterStatics> reference;

    public Recovery(AdapterStatics statics) {
      reference = new WeakReference<>(statics);
    }

    @Override
    public void run() {
      AdapterStatics statics = reference.get();
      if (statics != null) {
        statics.exeRecovery();
      }
    }
  }

  static final int STATE_BINDED = 0;
  static final int STATE_ATTACH = 1;
  static final int STATE_REMOVE = 2;

  private class DataInfo {
    WeakReference<View> weak;
    long start;
    int state;
    int index;
    DATA data;
  }

  private class ListNode<TYPE> {
    ListNode<TYPE> next;
    TYPE type;

    public ListNode(TYPE type) {
      this.type = type;
    }
  }

  private ListNode<DataInfo> dataNode;
  private ListNode<DataInfo> rubbish;

  private ArrayList<DATA> arrayList;

  private Handler handler;
  private boolean refreshing;

  private ViewGroup rootView;
  private int[] location;

  public AdapterStatics() {
    arrayList = new ArrayList<>();
    location = new int[2];
  }

  //  /**
  //   * 新增需求规则，选择性调用。调用该方法，actvity stop后会记录当前mv点，resume后再重新diff上报一次。
  //   *
  //   * @param act
  //   */
  //  public void observeAct(AppCompatActivity act) {
  //    if (act == null) {
  //      return;
  //    }
  //
  //    act.lifecycle()
  //        .subscribe(
  //            event -> {
  //              if (event == LifecycleEvent.Resume) {
  //                runRecovery();
  //              }
  //
  //              if (event == LifecycleEvent.Stop) {
  //                setRecovery();
  //              }
  //            });
  //  }

  /**
   * 检测到页面UI添加到Root后回调，可以在该方法中做补充流程
   *
   * @param rootView
   * @param parent
   */
  public void onAttachRoot(ViewGroup rootView, ViewGroup parent) {}

  // 活动状态上报
  public void reportActived(DATA data, int index) {}

  // 确认移除上报
  public void reportRemoved(DATA data, int index, long time) {}

  public void onBindData(View view, DATA data, int index) {
    if (view == null || data == null) {
      return;
    }

    Log.e("AdapterStatics", "绑定时间：" + System.currentTimeMillis());
    ListNode<DataInfo> find = null;
    ListNode<DataInfo> node = dataNode;

    while (node != null && find == null) {
      if (node.type.weak != null && node.type.weak.get() == view) {
        find = node;
      } else {
        node = node.next;
      }
    }

    if (find != null) {
      if (isEquals(find.type.data, data)) {
        return;
      } else {
        find.type.weak = null;
      }
    }

    find = null;
    node = dataNode;
    while (node != null && find == null) {
      if (isEquals(node.type.data, data)) {
        find = node;
      } else {
        node = node.next;
      }
    }

    if (find == null) {
      ListNode<DataInfo> add = reuseBubbish();
      add.type.weak = new WeakReference<>(view);
      add.type.state = STATE_BINDED;
      add.type.index = index;
      add.type.data = data;
      add.type.start = 0;
      sortAppend(add);
    } else if (find.type.weak == null || find.type.weak.get() != view) {
      find.type.weak = new WeakReference<>(view);
    }

    runRefresher();
  }

  public void runRefresher() {
    if (refreshing) {
      return;
    }
    long start = System.currentTimeMillis();

    refreshing = true;
    if (handler == null) {
      handler = new Handler(Looper.getMainLooper());
    }

    Refresher runnable = new Refresher(this);
    handler.post(runnable);
    Log.e("AdapterStatics", "runRefresher开始：" + (System.currentTimeMillis() - start));
  }

  private void exeRefresher() {
    long start = System.currentTimeMillis();
    refreshing = false;
    ListNode<DataInfo> node = null;
    ListNode<DataInfo> next = null;
    ListNode<DataInfo> pre = null;

    node = dataNode;
    while (node != null) {
      next = node.next;

      if (!isVisible(node.type)) {
        if (node.type.state == STATE_ATTACH) {
          node.type.state = STATE_REMOVE;
          long time = AnimationUtils.currentAnimationTimeMillis() - node.type.start;
          reportRemoved(node.type.data, node.type.index, time);
        }
      }

      if (node.type.weak == null || node.type.weak.get() == null) {
        if (pre == null) {
          dataNode = next;
        } else {
          pre.next = next;
        }
        node.next = null;
        storeRubbish(node);
        node = next;
      } else {
        pre = node;
        node = next;
      }
    }

    node = dataNode;
    while (node != null) {
      if (isVisible(node.type)) {

        if (node.type.start <= 0) {
          node.type.start = AnimationUtils.currentAnimationTimeMillis();
        }

        if (node.type.state != STATE_ATTACH) {
          node.type.state = STATE_ATTACH;
          reportActived(node.type.data, node.type.index);
        }
      }
      node = node.next;
    }
    Log.e("AdapterStatics", "exeRefresher完成：" + (System.currentTimeMillis() - start));
  }

  public void runRecovery() {
    if (handler == null) {
      handler = new Handler(Looper.getMainLooper());
    }

    Recovery runnable = new Recovery(this);
    handler.postDelayed(runnable, 200);
  }

  private void exeRecovery() {
    ListNode<DataInfo> node = dataNode;
    while (node != null) {
      if (node.type.state == STATE_ATTACH && contains(node.type.data, arrayList)) {
        reportActived(node.type.data, node.type.index);
      }
      node = node.next;
    }
    arrayList.clear();
  }

  private void setRecovery() {
    ListNode<DataInfo> node = dataNode;
    arrayList.clear();
    while (node != null) {
      if (node.type.state == STATE_ATTACH) {
        arrayList.add(node.type.data);
      }
      node = node.next;
    }
  }

  /** use runRefresher insted */
  @Deprecated
  public void onAttached(View view) {
    runRefresher();
  }

  /** use runRefresher insted */
  @Deprecated
  public void onDetached(View view) {
    runRefresher();
  }

  private void sortAppend(ListNode<DataInfo> add) {
    ListNode<DataInfo> pre = null;
    ListNode<DataInfo> node = dataNode;

    while (node != null && node.type.index < add.type.index) {
      pre = node;
      node = node.next;
    }

    if (pre == null) {
      add.next = dataNode;
      dataNode = add;
    } else {
      ListNode<DataInfo> next = pre.next;
      add.next = next;
      pre.next = add;
    }
  }

  private ListNode<DataInfo> storeRubbish(ListNode<DataInfo> node) {
    node.type.state = STATE_BINDED;
    node.type.start = 0;
    node.type.index = -1;
    node.type.weak = null;
    node.type.data = null;
    node.next = null;

    if (rubbish == null) {
      rubbish = node;
    } else {
      node.next = rubbish;
      rubbish = node;
    }

    return rubbish;
  }

  private ListNode<DataInfo> reuseBubbish() {
    ListNode<DataInfo> node = null;

    if (rubbish == null) {
      DataInfo data = new DataInfo();
      node = new ListNode<>(data);
    } else {
      node = rubbish;
      rubbish = rubbish.next;
      node.next = null;
    }

    return node;
  }

  private boolean isVisible(View view) {
    if (rootView == null) {
      ViewParent toFind = null;
      ViewParent parent = view.getParent();
      while (parent != null && parent instanceof View) {
        toFind = parent;
        parent = parent.getParent();
      }

      rootView = ((ViewGroup) toFind).findViewById(android.R.id.content);

      if (rootView == null) {
        rootView = (ViewGroup) toFind;
      }

      if (rootView != null) {
        ViewGroup myParent = (ViewGroup) view.getParent();
        onAttachRoot(rootView, myParent);
      }
    }

    if (rootView == null) {
      return false;
    }

    rootView.getLocationInWindow(location);
    int w1 = rootView.getMeasuredWidth();
    int h1 = rootView.getMeasuredHeight();
    int l1 = location[0];
    int t1 = location[1];

    view.getLocationInWindow(location);
    int w2 = view.getMeasuredWidth();
    int h2 = view.getMeasuredHeight();
    int l2 = location[0];
    int t2 = location[1];
    return (t2 < t1 + h1 && t2 + h2 > t1) && (l2 < l1 + w1 && l2 + w2 > l1);
  }

  private boolean isVisible(DataInfo data) {
    WeakReference<View> weak = data.weak;
    View view = weak != null ? weak.get() : null;

    if (view == null || view.getParent() == null) {
      return false;
    }

    return isVisible(view);
  }

  private boolean contains(Object o1, List mList) {
    boolean result = false;
    for (Object o2 : mList) {
      if (isEquals(o1, o2)) {
        result = true;
        break;
      }
    }
    return result;
  }

  private boolean isEquals(Object o1, Object o2) {
    if (o1 == null) {
      return o2 == null;
    }
    return o1 == o2 || o1.equals(o2);
  }
}

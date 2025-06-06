package com.tencent.liteav.demo.superplayer.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import expo.modules.txplayer.R;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseAdapter;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseListView;
import com.tencent.liteav.demo.superplayer.ui.view.base.BaseViewHolder;
import com.tencent.rtmp.TXTrackInfo;

import java.util.List;

public class VodSoundTrackView extends BaseListView<VodSoundTrackView.VodSoundTrackAdapter, TXTrackInfo> {

  private OnClickSoundTrackItemListener mListener;

  public VodSoundTrackView(Context context) {
    super(context);
  }

  public VodSoundTrackView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public VodSoundTrackView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void init(Context context) {
    super.init(context);
  }

  @Override
  protected String getTitle() {
    return mContext.getString(R.string.superplayer_audio_tracks);
  }

  @Override
  protected VodSoundTrackAdapter getAdapter() {
    return new VodSoundTrackAdapter();
  }

  public void doInitAudioTrackSelect(TXTrackInfo lastTrackInfo) {
    if (mData != null && mData.size() != 0) {
      TXTrackInfo firstTrackInfo = mData.get(0);
      // Can only be added once , prevent multiple calls
      if (firstTrackInfo != null && firstTrackInfo.trackIndex != -1) {
        TXTrackInfo info = new TXTrackInfo();
        info.name = mContext.getString(R.string.superplayer_off);
        info.trackIndex = -1;
        mAdapter.mItems.add(0, info);
        mData.add(0, info);
      }
      int initSelectIndex = 1;
      TXTrackInfo initSelectTrackInfo = mData.get(1);
      // restore last selected track
      if (lastTrackInfo != null) {
        int lastSelectedIndex = -1;
        for (int i = 0; i < mData.size(); i++) {
          TXTrackInfo data = mData.get(i);
          if (data.name.equals(lastTrackInfo.name) && data.trackType == lastTrackInfo.trackType
              && data.trackIndex == lastTrackInfo.trackIndex) {
            lastSelectedIndex = i;
            break;
          }
        }
        if (lastSelectedIndex >= 0) {
          initSelectIndex = lastSelectedIndex;
          initSelectTrackInfo = lastTrackInfo;
        }
      }
      mListener.onClickSoundTrackItem(initSelectTrackInfo);
      setCurrentPosition(initSelectIndex);
    }
  }

  class VodSoundTrackAdapter extends BaseAdapter<VodSoundTrackItemView, TXTrackInfo> {

    public VodSoundTrackAdapter() {
      super();
    }

    @Override
    public BaseViewHolder createViewHolder() {
      return new VodSoundTrackItemHolder(new VodSoundTrackItemView(mContext));
    }

    class VodSoundTrackItemHolder extends BaseViewHolder {

      public VodSoundTrackItemHolder(@NonNull View itemView) {
        super(itemView);
      }

      @Override
      public void onItemClick(View view) {
        int position = (int) (view.getTag());
        setCurrentPosition(position);
        mListener.onClickSoundTrackItem(mData.get(position));
        notifyDataSetChanged();
      }
    }
  }

  public interface OnClickSoundTrackItemListener {
    void onClickSoundTrackItem(TXTrackInfo clickInfo);
  }

  public void setOnClickSoundTrackItemListener(OnClickSoundTrackItemListener listener) {
    mListener = listener;
  }

}

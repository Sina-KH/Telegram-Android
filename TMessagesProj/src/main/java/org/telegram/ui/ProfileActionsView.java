package org.telegram.ui;

import static org.telegram.messenger.Utilities.clamp;

import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ViewConstructor")
public class ProfileActionsView extends FrameLayout {

    public static final int liveStreamItem = 1;
    public static final int messageItem = 2;
    public static final int joinItem = 3;
    public static final int muteItem = 4;
    public static final int unmuteItem = 5;
    public static final int callItem = 6;
    public static final int videoCallItem = 7;
    public static final int voiceChatItem = 8;
    public static final int addStoryItem = 9;
    public static final int discussItem = 10;
    public static final int giftItem = 11;
    public static final int shareItem = 12;
    public static final int stopItem = 13;
    public static final int reportItem = 14;
    public static final int leaveItem = 15;

    float spacing = AndroidUtilities.dpf2(6.6f);

    private final Map<Integer, ActionItemView> itemViews = new HashMap<>();
    private final List<Integer> visibleItemIds = new ArrayList<>();

    private final BaseFragment fragment;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, int itemId);
    }

    public ProfileActionsView(BaseFragment fragment) {
        super(fragment.getContext());
        this.fragment = fragment;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
    private void onItemClicked(View view, int itemId) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(view, itemId);
        }
    }

    float heightScale = 1f;
    float actionsScale = 1f;
    public void setActionsScale(float scale) {
        if (heightScale == scale)
            return;
        heightScale = scale;
        actionsScale = clamp((scale - 0.4f) * 5f / 3f, 1f, 0f);
        setAlpha(actionsScale);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = (int) (AndroidUtilities.dpf2(54) * heightScale);

        int count = min(4, visibleItemIds.size());
        int buttonWidth = count > 0 ? (int) ((width - spacing * (count - 1)) / count) : 0;

        int childHeightSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        int childWidthSpec = MeasureSpec.makeMeasureSpec(buttonWidth, MeasureSpec.EXACTLY);

        for (Integer id : visibleItemIds) {
            ActionItemView view = itemViews.get(id);
            if (view != null && view.getVisibility() == VISIBLE) {
                view.measure(childWidthSpec, childHeightSpec);
            }
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = 0;

        for (Integer id : visibleItemIds) {
            ActionItemView view = itemViews.get(id);
            if (view != null && view.getVisibility() == VISIBLE) {
                int width = view.getMeasuredWidth();
                view.layout(left, 0, left + width, getMeasuredHeight());
                view.setActionsScale(actionsScale);
                left += (int) (width + spacing);
            }
        }
    }

    public void setItemVisibility(int identifier, boolean isVisible) {
        ActionItemView view = itemViews.get(identifier);

        if (isVisible) {
            if (view == null) {
                ActionItem item = createItemFromIdentifier(identifier);
                if (item == null) return;

                view = new ActionItemView(fragment, item, this::onItemClicked);
                itemViews.put(identifier, view);
                addView(view);
            }

            view.setVisibility(VISIBLE);
            if (!visibleItemIds.contains(identifier)) {
                visibleItemIds.add(identifier);
                Collections.sort(visibleItemIds);
            }

        } else {
            if (view != null) {
                view.setVisibility(GONE);
            }
            visibleItemIds.remove((Integer) identifier);
        }

        invalidate();
    }

    @Nullable
    private ActionItem createItemFromIdentifier(int id) {
        switch (id) {
            case liveStreamItem:
                return new ActionItem(id, R.drawable.ic_profile_call, "Live");
            case messageItem:
                return new ActionItem(id, R.drawable.ic_profile_message, "Message");
            case joinItem:
                return new ActionItem(id, R.drawable.ic_profile_join, "Join");
            case muteItem:
                return new ActionItem(id, R.drawable.ic_profile_mute, "Mute");
            case unmuteItem:
                return new ActionItem(id, R.drawable.ic_profile_unmute, "Unmute");
            case callItem:
                return new ActionItem(id, R.drawable.ic_profile_call, "Call");
            case videoCallItem:
                return new ActionItem(id, R.drawable.ic_profile_video, "Video");
            case voiceChatItem:
                return new ActionItem(id, R.drawable.ic_profile_voice_chat, "Voice Chat");
            case addStoryItem:
                return new ActionItem(id, R.drawable.ic_profile_story, "Add Story");
            case discussItem:
                return new ActionItem(id, R.drawable.ic_profile_message, "Discuss");
            case giftItem:
                return new ActionItem(id, R.drawable.ic_profile_gift, "Gift");
            case shareItem:
                return new ActionItem(id, R.drawable.ic_profile_share, "Share");
            case stopItem:
                return new ActionItem(id, R.drawable.ic_profile_stop, "Stop");
            case reportItem:
                return new ActionItem(id, R.drawable.ic_profile_report, "Report");
            case leaveItem:
                return new ActionItem(id, R.drawable.ic_profile_leave, "Leave");
            default:
                return null;
        }
    }

    private static class ActionItem {
        public final int id;
        public final int iconResId;
        public final String text;

        public ActionItem(int id, int iconResId, String text) {
            this.id = id;
            this.iconResId = iconResId;
            this.text = text;
        }
    }

    private static class ActionItemView extends FrameLayout {

        private ImageView icon;
        private TextView label;

        public ActionItemView(BaseFragment fragment, ActionItem item, OnItemClickListener clickListener) {
            super(fragment.getContext());
            Context context = fragment.getContext();

            int cornerRadius = AndroidUtilities.dp(10);
            int defaultColor = ColorUtils.setAlphaComponent(Color.BLACK, 25);
            int baseColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
            int pressedColor = ColorUtils.setAlphaComponent(baseColor, 0x33);
            int maskColor = ColorUtils.setAlphaComponent(baseColor, 0x50);
            setBackground(Theme.createSimpleSelectorRoundRectDrawable(
                    cornerRadius,
                    defaultColor,
                    pressedColor,
                    maskColor
            ));

            setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(this, item.id);
                }
            });

            icon = new ImageView(context);
            icon.setImageResource(item.iconResId);
            icon.setColorFilter(Theme.getColor(Theme.key_actionBarDefaultTitle));
            icon.setPivotX(AndroidUtilities.dp(12));
            icon.setPivotY(0);
            LayoutParams iconParams = new LayoutParams(AndroidUtilities.dp(24), AndroidUtilities.dp(24), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            iconParams.topMargin = AndroidUtilities.dp2(6.66f);
            iconParams.leftMargin = AndroidUtilities.dp2(0.66f);
            addView(icon, iconParams);

            label = new TextView(context);
            label.setText(item.text);
            label.setTextColor(Theme.getColor(Theme.key_actionBarDefaultTitle));
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            label.setTypeface(AndroidUtilities.bold());
            label.setGravity(Gravity.CENTER);
            LayoutParams labelParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            labelParams.bottomMargin = AndroidUtilities.dp2(6.66f);
            labelParams.leftMargin = AndroidUtilities.dp2(0.66f);
            addView(label, labelParams);
        }

        void setActionsScale(float scale) {
            icon.setScaleX(scale);
            icon.setScaleY(scale);
            label.setScaleX(scale);
            label.setScaleY(scale);
        }
    }

}
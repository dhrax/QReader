package com.daisa.qreader;

import android.app.Activity;

import androidx.drawerlayout.widget.DrawerLayout;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.ShowcaseTooltip;

//fixme Showcase is a very chaotic
public class AppShowcase {

    public static int presentShowcaseViewsShown = 0;
    public static int drawerShowcaseViewsShown = 0;
    private Util util;
    Activity activity;
    ShowcaseConfig config;

    public AppShowcase(Util util, Activity activity) {
        this.util = util;
        this.activity = activity;
        config = new ShowcaseConfig();
        config.setRenderOverNavigationBar(util.hasNavBar(activity));
    }

    public void presentShowcaseView(final DrawerLayout drawerLayout) {

        final MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, Constants.APP_SHOWCASE_ID);

        sequence.setConfig(config);

        ShowcaseTooltip tooltipPresentation = ShowcaseTooltip.build(activity)
                .textColor(activity.getColor(R.color.tooltip_text_color))
                .arrowHeight(0)
                //.animation()
                .text(activity.getString(R.string.tooltip_presentation_content_text));

        sequence.addSequenceItem(addItem(R.id.spacerTop, R.string.none, R.string.none, tooltipPresentation, true, false, 0, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.switchCamera, R.string.showcase_switch_camera_title, R.string.showcase_switch_camera_desc, null, false, false, 0, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.toggleFlash, R.string.showcase_flash_title, R.string.showcase_flash_desc, null, false, false, 0, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.zoomLayout, R.string.showcase_zoom_layout_title, R.string.showcase_zoom_layout_desc, null, true, false, 0, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.zoomOut, R.string.showcase_zoom_out_title, R.string.showcase_zoom_out_desc, null, false, false, 0, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.zoomBar, R.string.showcase_zoom_bar_title, R.string.showcase_zoom_bar_desc, null, true, false, 0, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.zoomIn, R.string.showcase_zoom_in_title, R.string.showcase_zoom_in_desc,  null, false, false, 0, 30, 0, false));

        ShowcaseTooltip tooltipPresentationEnd = ShowcaseTooltip.build(activity)
                .textColor(activity.getColor(R.color.tooltip_text_color))
                .arrowHeight(0)
                .corner(30)
                .text(activity.getString(R.string.tooltip_presentation_end_content_text));

        sequence.addSequenceItem(addItem(R.id.initSpacer, R.string.none, R.string.none, tooltipPresentationEnd, true, false, 0, 30, 0, true));

        sequence.start();
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView itemView, int position) {
                presentShowcaseViewsShown++;
                if (presentShowcaseViewsShown == 8) {
                    util.openDrawer(drawerLayout);
                    presentShowcaseViewsShown = 0;
                }
            }
        });
    }

    public void playDrawerShowcase(final DrawerLayout drawerLayout) {
        final MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, Constants.DRAWER_SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(addItem(R.id.nav_gallery, R.string.gallery, R.string.showcase_gallery_desc, null, true, true, 100, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.nav_history, R.string.history, R.string.showcase_history_desc, null, true, true, 100, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.nav_favorites, R.string.favorites, R.string.showcase_favorites_desc, null, true, true, 100, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.nav_share, R.string.share, R.string.showcase_share_desc, null, true, true, 100, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.nav_email, R.string.contact, R.string.showcase_contact_desc, null, true, true, 100, 30, 0, false));

        sequence.addSequenceItem(addItem(R.id.nav_preferences, R.string.preferences, R.string.showcase_preferences_desc, null, true, true, 100, 30, 0, false));

        //todo traducir
        ShowcaseTooltip tooltipDrawer = ShowcaseTooltip.build(activity)
                .textColor(activity.getColor(R.color.tooltip_text_color))
                .arrowHeight(0)
                .corner(30)
                .text(activity.getString(R.string.tooltip_drawer_content_text));

        sequence.addSequenceItem(addItem(R.id.initSpacer, R.string.none, R.string.none, tooltipDrawer, true, false, 0, 30, 0, true));

        sequence.start();

        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView itemView, int position) {
                drawerShowcaseViewsShown++;
                //fixme change to be hasfired doesnt work
                if (drawerShowcaseViewsShown == 7) {
                    util.closeDrawer(drawerLayout);
                    drawerShowcaseViewsShown = 0;
                }
            }
        });

    }

    public MaterialShowcaseView addItem(int target, int titleText, int contentText, ShowcaseTooltip tooltip,
                                        boolean rectangleShape, boolean fullWidth, int tooltipMargin, int shapePadding, int delay, boolean last) {
        MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(activity);

        builder.setTarget(activity.findViewById(target))
                .setTitleText(activity.getString(titleText)) //title on the outside area
                .setContentText(activity.getString(contentText)); //text on the outside area
        if (last) {
            builder.setDismissText(activity.getString(R.string.none)); //"button" text to dismiss
        } else {
            builder.setSkipText(activity.getString(R.string.skip))
                    .setDismissText(activity.getString(R.string.next));//"button" text to skip the rest of the sequence
        }
        builder.setToolTip(tooltip);
        if (rectangleShape) {
            builder.withRectangleShape(fullWidth);
        }
        builder.setTooltipMargin(tooltipMargin) //margin top
                .setShapePadding(shapePadding) //size of the marker circle
                .setDismissOnTouch(true) //close when touching the screen
                .setMaskColour(activity.getColor(R.color.presentation_background)) //color of the outside area
                .setContentTextColor(activity.getColor(R.color.presentation_text)) //color of the content text
                .setTitleTextColor(activity.getColor(R.color.presentation_text))
                .setDelay(delay); //delay at the start

        return builder.build();
    }
}

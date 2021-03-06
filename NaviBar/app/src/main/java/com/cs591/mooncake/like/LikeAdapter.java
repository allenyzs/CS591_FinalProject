package com.cs591.mooncake.like;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cs591.mooncake.FirebaseUtils.FirebaseProfile;
import com.cs591.mooncake.R;
import com.cs591.mooncake.SQLite.MySQLiteHelper;
import com.cs591.mooncake.SQLite.SingleEvent;
import com.cs591.mooncake.SQLite.SingleUser;

import java.util.List;

/**
 * Created by LinLi on 4/8/18, modified by MingyangYan, QifanHe.
 */

public class LikeAdapter extends RecyclerView.Adapter<LikeAdapter.ViewHolder> {

    //  field for initialization
    private Context mContext;
    private int lastPosition = -1;
    private List<Object> mList;
    LikeFragment.OnLikedEventClickedListener olcl;

    //  initialization of LikeAdaptor
    LikeAdapter(Context context, List<Object> list, LikeFragment.OnLikedEventClickedListener OLCL){
        this.olcl = OLCL;
        mContext = context;
        mList = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // fields for initialization on rv_like_items.xml
        ImageView item_image;
        TextView item_name;
        Button likebtn;

        public ViewHolder(View itemView) {
            super(itemView);

            //  give the references for TextView, Button and ImageView
            likebtn = itemView.findViewById(R.id.likebtn);
            item_image = itemView.findViewById(R.id.item_image);
            item_name = itemView.findViewById(R.id.item_name);

        }
    }

    //  Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
    //  Set up viewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View view = layoutInflater.inflate(R.layout.rv_like_items,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        //  Called to display the data at the specified position in RecyclerView.
        //  This method should update the contents of the itemView to reflect the item at the given position.

        //  Connection to SingleEvent database
        final SingleEvent singleEvent = (SingleEvent) mList.get(position);

        //  Set text and image from database to cardView holder
        holder.item_name.setText(singleEvent.getName());
        holder.item_image.setImageBitmap(singleEvent.getPic());

        // go to the specific event page when click on the liked item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                olcl.openLikeEvent(singleEvent.getID());
            }
        });

        //
        holder.likebtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                // get SQLite DB
                final MySQLiteHelper mydb = new MySQLiteHelper(view.getContext());
                //final SingleUser singleUser = mydb.getProfile();

                // set the message with the event name
                String normalText1 = mContext.getString(R.string.unlike_check);
                String boldText = singleEvent.getName();
                String normalText2 = mContext.getString(R.string.question_mark);

                SpannableString str = new SpannableString(normalText1 + boldText + normalText2);
                str.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), normalText1.length(), normalText1.length() + boldText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Create a dialog box for user to decide whether to unlike an event or not.
                AlertDialog.Builder altUnlike = new AlertDialog.Builder(mContext);
                // The dialog box cannot be canceled by clicking the back button.
                // Set the "Yes" button and then remove the item from the Like page
                altUnlike.setMessage(str).setCancelable(false)
                        .setPositiveButton(mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // remove the event from the user database
                                SingleUser singleUser = mydb.getProfile();
                                singleUser.removeLiked(singleEvent.getID());
                                // update the local database
                                mydb.addProfile(singleUser);

                                // update Firebase real time data
                                new FirebaseProfile().updateLikedScheduled(singleUser);
                                mList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position,mList.size());

                            }
                        })
                        // close the dialog box when click on "No"
                        .setNegativeButton(mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = altUnlike.create();
                alert.show();

            }
        });
        // call the function to give animation when go to the like fragment from other fragments
        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
            animation.setDuration(200 + position * 80);
        }

    }

}

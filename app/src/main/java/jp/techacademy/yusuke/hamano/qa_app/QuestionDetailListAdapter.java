package jp.techacademy.yusuke.hamano.qa_app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hamanoyuusuke on 2019/02/07.
 */

public class QuestionDetailListAdapter extends BaseAdapter {

    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQuestion;

    private ArrayList<Question> mQuestionArrayList;

    private DatabaseReference dataBaseReference;
    private DatabaseReference qDatabaseReference;
    private DatabaseReference contents;
    private DatabaseReference mDatabaseReference;

    public boolean mFavoriteFlag;
    private QuestionsListAdapter mAdapter;
    private ListView mListView;

    public QuestionDetailListAdapter(ArrayList<Question> QuestionArrayList, Context context, Question question) {
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQuestion = question;
        mQuestionArrayList = QuestionArrayList;
    }


    @Override
    public int getCount() {
        return 1 + mQuestion.getAnswers().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQuestion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (getItemViewType(position) == TYPE_QUESTION) {

            if (convertView == null) {

                if (user == null) {
                    //ログインしていない場合、表示されていない画面を表示
                    convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
                } else {
                    //ログインされていれば、お気に入りボタンが追加された画面を表示
                    convertView = mLayoutInflater.inflate(R.layout.list_question_detail2, parent, false);

                    dataBaseReference = FirebaseDatabase.getInstance().getReference();

                    mDatabaseReference = dataBaseReference.child(Const.FavPATH).child(user.getUid());

                    contents = dataBaseReference
                            .child(Const.ContentsPATH)
                            .child(String.valueOf(mQuestion.getGenre()))
                            .child(mQuestion.getQuestionUid());


                    final DatabaseReference question = dataBaseReference.child(Const.FavPATH).child(user.getUid()).child(mQuestion.getQuestionUid());

                    final ImageView imageView = (android.widget.ImageView) convertView.findViewById(R.id.button_favorite);

                    mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String quid = "";
                            String quid2 = "";
                            String quid3 = "";

                            for (DataSnapshot datachild : dataSnapshot.getChildren()) {

                                quid = datachild.getKey();

                                Log.d("quid_data", quid);

                                Log.d("test_test", contents.getKey());

                                quid2 = contents.getKey();

                                if (quid.equals(quid2)) {

                                    mFavoriteFlag = true;

                                    quid3 = quid;

                                    Log.d("true", quid3);

                                    break;

                                } else {

                                    mFavoriteFlag = false;

                                    Log.d("false", quid);
                                }
                            }

                            if (mFavoriteFlag) {
                                imageView.setImageResource(R.drawable.ic_favorite_black_24dp);
                                Log.d("boolean_2", String.valueOf(mFavoriteFlag));

                            } else {
                                imageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                Log.d("boolean_1", String.valueOf(mFavoriteFlag));
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    final View finalConvertView = convertView;
                    final View finalConvertView1 = convertView;
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String quid = "";
                                    String quid2 = "";
                                    String quid3 = "";

                                    for (DataSnapshot datachild : dataSnapshot.getChildren()) {

                                        quid = datachild.getKey();
                                        quid2 = contents.getKey();

                                        if (quid.equals(quid2)) {

                                            mFavoriteFlag = true;
                                            quid3 = quid;
                                            Log.d("true", quid3);
                                            break;

                                        } else {

                                            mFavoriteFlag = false;
                                            Log.d("false", quid);
                                        }
                                        Log.d("quid2", quid2);
                                        Log.d("quid", quid);
                                    }


                                    if (mFavoriteFlag) {
                                        qDatabaseReference = dataBaseReference.child(Const.FavPATH).child(user.getUid()).child(quid3);
                                        imageView.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                                        qDatabaseReference.removeValue();

                                    } else {
                                        Map<String, String> data = new HashMap<>();
                                        data.put("genre", String.valueOf(mQuestion.getGenre()));
                                        question.setValue(data);
                                        imageView.setImageResource(R.drawable.ic_favorite_black_24dp);
                                    }
                                }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }

                            });
                        }
                    });

                }
                Log.d("qqqqq", String.valueOf(mFavoriteFlag));
            }

            String body = mQuestion.getBody();
            String name = mQuestion.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQuestion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }

        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQuestion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }
        return convertView;
    }

}




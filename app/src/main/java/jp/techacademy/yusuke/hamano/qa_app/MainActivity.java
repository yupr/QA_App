package jp.techacademy.yusuke.hamano.qa_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private int mGenre = 1;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference dataBaseReference;
    private DatabaseReference favorite_list;
    private DatabaseReference contents;

    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    private ChildEventListener mEventListener = new ChildEventListener() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");

            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            mQuestionArrayList.add(question);
            mAdapter.notifyDataSetChanged();

            Log.d("athat", String.valueOf(dataSnapshot.getKey()));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }


        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ログイン済みのユーザーを取得する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // ナビゲーションドロワーの設定
        if (user == null) {
            setContentView(R.layout.activity_main);

            //ログインしてなければ、お気に入りが表示されない。
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

        } else {
            setContentView(R.layout.activity_main2);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
            navigationView.setNavigationItemSelectedListener(this);

        }

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });


        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {

            // 1:趣味を既定の選択とする
            if (mGenre == 0) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
            }
        } else {
            if (mGenre == 1) {
                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
                onNavigationItemSelected(navigationView.getMenu().getItem(1));
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            if (id == R.id.nav_hobby) {
                mToolbar.setTitle("趣味");
                mGenre = 1;
            } else if (id == R.id.nav_life) {
                mToolbar.setTitle("生活");
                mGenre = 2;
            } else if (id == R.id.nav_health) {
                mToolbar.setTitle("健康");
                mGenre = 3;
            } else if (id == R.id.nav_compter) {
                mToolbar.setTitle("コンピュータ");
                mGenre = 4;
            }
        } else {
            if (id == R.id.nav_fav) {

                mToolbar.setTitle("お気に入り");
                dataBaseReference = FirebaseDatabase.getInstance().getReference();

                favorite_list = dataBaseReference
                        .child(Const.FavPATH)
                        .child(user.getUid());

                ChildEventListener fav_listener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String quid = "";
                        String genre = "";

                        for (DataSnapshot datachild : dataSnapshot.getChildren()) {

                            quid = dataSnapshot.getKey();
                            genre = (String) dataSnapshot.child("genre").getValue();

                            Log.d("genre2", genre);
                            Log.d("genre3", quid);

                            contents = dataBaseReference
                                    .child(Const.ContentsPATH)
                                    .child(String.valueOf(genre))
                                    .child(quid);

                            contents.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    HashMap map = (HashMap) dataSnapshot.getValue();

                                    String title = (String) map.get("title");
                                    String body = (String) map.get("body");
                                    String name = (String) map.get("name");
                                    String uid = (String) map.get("uid");
                                    String imageString = (String) map.get("image");

                                    byte[] bytes;
                                    if (imageString != null) {
                                        bytes = Base64.decode(imageString, Base64.DEFAULT);
                                    } else {
                                        bytes = new byte[0];
                                    }

                                    ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
                                    HashMap answerMap = (HashMap) map.get("answers");
                                    if (answerMap != null) {
                                        for (Object key : answerMap.keySet()) {
                                            HashMap temp = (HashMap) answerMap.get((String) key);
                                            String answerBody = (String) temp.get("body");
                                            String answerName = (String) temp.get("name");
                                            String answerUid = (String) temp.get("uid");
                                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                                            answerArrayList.add(answer);
                                        }
                                    }

                                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
                                    mQuestionArrayList.add(question);
                                    mAdapter.notifyDataSetChanged();
                                }


                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                            });
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };

                favorite_list.addChildEventListener(fav_listener);


                mGenre = 0;

            } else if (id == R.id.nav_hobby) {
                mToolbar.setTitle("趣味");
                mGenre = 1;
            } else if (id == R.id.nav_life) {
                mToolbar.setTitle("生活");
                mGenre = 2;
            } else if (id == R.id.nav_health) {
                mToolbar.setTitle("健康");
                mGenre = 3;
            } else if (id == R.id.nav_compter) {
                mToolbar.setTitle("コンピュータ");
                mGenre = 4;
            }
        }

        if (user == null) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        } else {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
            drawer.closeDrawer(GravityCompat.START);
        }

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        // 選択したジャンルにリスナーを登録する
        if (mGenreRef != null) {
            mGenreRef.removeEventListener(mEventListener);
        }

        mGenreRef = mDatabaseReference
                .child(Const.ContentsPATH)
                .child(String.valueOf(mGenre));

        mGenreRef.addChildEventListener(mEventListener);

        return true;
    }
}

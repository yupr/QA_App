package jp.techacademy.yusuke.hamano.qa_app;

import java.io.Serializable;

/**
 * Created by hamanoyuusuke on 2019/02/07.
 */

public class Answer implements Serializable {
    private String mBody;
    private String mName;
    private String mUid;
    private String mAnswerUid;

    public Answer(String body, String name, String uid, String answerUid) {
        mBody = body;
        mName = name;
        mUid = uid;
        mAnswerUid = answerUid;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getAnswerUid() {
        return mAnswerUid;
    }
}

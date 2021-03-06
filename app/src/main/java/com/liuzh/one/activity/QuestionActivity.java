package com.liuzh.one.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.liuzh.one.R;
import com.liuzh.one.adapter.CommentRvAdapter;
import com.liuzh.one.application.App;
import com.liuzh.one.bean.Tag;
import com.liuzh.one.bean.comment.Comment;
import com.liuzh.one.bean.comment.CommentData;
import com.liuzh.one.bean.question.Question;
import com.liuzh.one.bean.question.QuestionData;
import com.liuzh.one.utils.Constant;
import com.liuzh.one.utils.HtmlFmtUtil;
import com.liuzh.one.utils.RetrofitUtil;
import com.liuzh.one.view.AppToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by 刘晓彬 on 2017/3/28.
 */

public class QuestionActivity extends BaseActivity {

    private AppToolbar mToolbar;
    private TextView mTvQuestionTitle;
    private TextView mTvQuestionContent;
    private TextView mTvQuestionAsker;
    private TextView mTvAnswerer;
    private TextView mTvEditorInfo;
    private TextView mTvCopyright;
    private WebView mWvContent;
    private Call<Question> mQuestionCall;
    private Call<Comment> mCommentCall;
    private RecyclerView mRvComments;

    public static void start(Context context, int id) {
        Intent intent = new Intent(context, QuestionActivity.class);
        intent.putExtra(Constant.INTENT_KEY_ID, id);
        context.startActivity(intent);
    }

    @Override
    protected int getContentId() {
        return R.layout.activity_question;
    }


    @Override
    protected void findViews() {
        mToolbar = (AppToolbar) findViewById(R.id.toolbar);
        mTvQuestionTitle = (TextView) findViewById(R.id.tv_question_title);
        mTvQuestionContent = (TextView) findViewById(R.id.tv_question_content);
        mTvQuestionAsker = (TextView) findViewById(R.id.tv_question_asker);
        mTvAnswerer = (TextView) findViewById(R.id.tv_answerer);
        mWvContent = (WebView) findViewById(R.id.webView);
        mTvEditorInfo = (TextView) findViewById(R.id.tv_info);
        mTvCopyright = (TextView) findViewById(R.id.tv_copyright);
        mRvComments = (RecyclerView) findViewById(R.id.rv_comments);
    }

    @Override
    protected void initViewData() {
        showLoadingView();
        // init toolbar
        mToolbar.setLBtnDrawable(R.drawable.back);
        mToolbar.setLBtnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mToolbar.setRRDrawable(R.drawable.share);
        mToolbar.initShareListener(findViewById(R.id.main));
        setSupportActionBar(mToolbar);
        mWvContent.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hiddenLoadingView();
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRvComments.setLayoutManager(layoutManager);
        mRvComments.addItemDecoration(new DividerItemDecoration(
                mContext, DividerItemDecoration.VERTICAL));
    }



    private void setData(QuestionData data) {
        List<Tag> tags = data.tag_list;
        String toolbarTitle = getString(R.string.one_question);
        if (tags.size() != 0) {
            toolbarTitle = tags.get(0).title;
        }
        mToolbar.setToolbarTitle(toolbarTitle);
        mTvQuestionTitle.setText(data.question_title);
        mTvQuestionContent.setText(data.question_content);
        mTvQuestionAsker.setText("————" + data.asker.user_name + "问道");
        mTvAnswerer.setText(data.answerer.user_name + "答：");
        mWvContent.loadDataWithBaseURL("about:blank",
                HtmlFmtUtil.fmt(data.answer_content), "text/html", "utf-8", null);
        mTvEditorInfo.setText(data.charge_edt + "  " + data.charge_email);
        if (TextUtils.isEmpty(data.copyright)) {
            mTvCopyright.setVisibility(View.GONE);
        } else {
            mTvCopyright.setText(data.copyright + "");
        }
    }

    @Override
    protected void fetchData() {
        int id = getIntent().getIntExtra(Constant.INTENT_KEY_ID, -1);
        if (id == -1) {
            App.showToast("ID = -1");
            return;
        }
        fetchQuestion(id);
        fetchComment(id);

    }

    private void fetchComment(final int id) {
        mCommentCall = RetrofitUtil.getQuestionCommentCall(id);
        mCommentCall.enqueue(new Callback<Comment>() {
            @Override
            public void onResponse(Call<Comment> call, Response<Comment> response) {
                setComment(response.body().data);
            }

            @Override
            public void onFailure(Call<Comment> call, Throwable t) {
                fetchComment(id);
            }
        });
    }


    private void setComment(CommentData comment) {
        mRvComments.setAdapter(new CommentRvAdapter(mContext, comment));
    }

    private void fetchQuestion(final int id) {
        mQuestionCall = RetrofitUtil.getQuestionCall(id);
        mQuestionCall.enqueue(new Callback<Question>() {
            @Override
            public void onResponse(Call<Question> call, Response<Question> response) {
                setData(response.body().data);
                hiddenLoadingView();
            }

            @Override
            public void onFailure(Call<Question> call, Throwable t) {
                App.showToast("失败，再次尝试");
                fetchQuestion(id);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQuestionCall.cancel();
    }

    @Override
    public void onBackPressed() {
        if (mToolbar.popIsShowing()) {
            mToolbar.dismissPop();
        } else {
            finish();
        }
    }
}

package com.nightlynexus.hockey.fragment;

import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nightlynexus.hockey.R;
import com.nightlynexus.hockey.model.App;
import com.nightlynexus.hockey.network.HockeyNetwork;
import com.nightlynexus.hockey.util.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginFragment extends AbsTitleFragment {

    private static final int ACTION_ID_LOGIN = 100;

    private View mLoadingLayout;
    private View mLoginLayout;
    private EditText mApiKeyEt;
    private Button mLoginBtn;
    private Button mBrowserBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        mLoadingLayout = rootView.findViewById(R.id.loading_layout);
        mLoginLayout = rootView.findViewById(R.id.login_layout);
        mApiKeyEt = (EditText) mLoginLayout.findViewById(R.id.api_key_entry);
        mLoginBtn = (Button) mLoginLayout.findViewById(R.id.login);
        mBrowserBtn = (Button) mLoginLayout.findViewById(R.id.go_to_auth_tokens);
        mApiKeyEt.setImeActionLabel(mApiKeyEt.getContext().getText(R.string.edit_text_login),
                ACTION_ID_LOGIN);
        mApiKeyEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == ACTION_ID_LOGIN) {
                    login();
                }
                return false;
            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        mBrowserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.button_go_to_auth_tokens_clicked,
                        Toast.LENGTH_LONG).show();
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(HockeyNetwork.URL_AUTH_TOKENS)));
            }
        });
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final String apiKey = SharedPreferencesUtils.getApiKey(getActivity());
        if (apiKey != null) {
            showLoading(true);
            login(apiKey, false);
        } else {
            showLoading(false);
        }
    }

    private void showLoading(boolean showLoading) {
        final CharSequence title;
        if (showLoading) {
            mLoginLayout.setVisibility(View.GONE);
            mLoadingLayout.setVisibility(View.VISIBLE);
            title = getText(R.string.title_logging_in);
        } else {
            mLoginLayout.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.GONE);
            title = null;
        }
        getActivity().setTitle(setTitleAlt(title));
    }

    private void login() {
        final String apiKey = mApiKeyEt.getText().toString();
        login(apiKey, true);
        // close soft keyboard
        final InputMethodManager inputManager = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mApiKeyEt.getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void login(final String apiKey, final boolean save) {
        showLoading(true);
        HockeyNetwork.getAppList(apiKey, new Callback<List<App>>() {
            @Override
            public void success(List<App> appsList, Response response) {
                if (!isAdded()) {
                    return;
                }
                if (save) {
                    SharedPreferencesUtils.setApiKey(getActivity(), apiKey);
                }
                getLoginActivity().completeLogin(new ArrayList<App>(appsList));
            }

            @Override
            public void failure(RetrofitError error) {
                if (!isAdded()) {
                    return;
                }
                showLoading(false);
                switch (error.getKind()) {
                    case HTTP:
                        // clear API key preference, since it is probably invalid
                        SharedPreferencesUtils.clearApiKey(getActivity());
                        Toast.makeText(getActivity(), R.string.login_error_http,
                                Toast.LENGTH_LONG).show();
                        break;
                    case NETWORK:
                        Toast.makeText(getActivity(), R.string.login_error_network,
                                Toast.LENGTH_LONG).show();
                        break;
                    default:
                        throw error;
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        paste();
    }

    private void paste() {
        if (mApiKeyEt.getText().length() == 0) {
            final ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(
                    Context.CLIPBOARD_SERVICE);
            if (clipboard.hasPrimaryClip()) {
                final ClipDescription clipDescription = clipboard.getPrimaryClipDescription();
                if (clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        || clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                    final CharSequence cs = clipboard.getPrimaryClip().getItemAt(0).getText();
                    if (cs != null) {
                        final String text = cs.toString();
                        if (isLikelyAuthToken(text)) {
                            mApiKeyEt.setText(text);
                            mApiKeyEt.setSelection(mApiKeyEt.getText().length());
                        }
                    }
                }
            }
        }
    }

    private static boolean isLikelyAuthToken(String text) {
        return text.length() > 25 && text.length() < 100 && text.matches("[A-Za-z0-9]+");
    }
}

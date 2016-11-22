package ip.partyplaylist.controllers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import ip.partyplaylist.screen_actions.LoginScreenActions;
import ip.partyplaylist.util.SharedPreferenceHelper;
import ip.partyplaylist.util.SpotifyScope;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by az on 22/05/16.
 */
public class LoginActivityController {

    private static final String TAG = LoginActivityController.class.getSimpleName();

    private static final String CLIENT_ID = "ea09225ef2974242a1549f3812a15496";
    private static final String REDIRECT_URI = "ip-partyplaylist://callback";
    public static final int REQUEST_CODE = 1;

    private final SharedPreferenceHelper mSharedPreferenceHelper;
    private Context mContext;

    public LoginActivityController(Context context) {
        mContext = context;
        mSharedPreferenceHelper = new SharedPreferenceHelper(context);
    }

    public void onLoginUserToSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);

        builder.setScopes(new String[]{
                SpotifyScope.PLAYLIST_MODIFY_PRIVATE,
                SpotifyScope.PLAYLIST_MODIFY_PUBLIC});

        AuthenticationRequest request = builder.build();
        AuthenticationClient.openLoginActivity((Activity) mContext, REQUEST_CODE, request);
    }

    public void onUserLoggedInSuccessfully(String accessToken) {
        mSharedPreferenceHelper.saveSpotifyToken(accessToken);

        SpotifyApi mSpotifyApi = new SpotifyApi();
        mSpotifyApi.setAccessToken(accessToken);
        SpotifyService spotifyService = mSpotifyApi.getService();

        spotifyService.getMe(new Callback<UserPrivate>() {
            @Override
            public void success(UserPrivate userPrivate, Response response) {
                Log.d(TAG, "Obtained User Information.");

                mSharedPreferenceHelper.saveCurrentUserId(userPrivate.id);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, "FAILED to Obtain User Information.");
            }
        });

        ((LoginScreenActions) mContext).showMainScreen();
    }

    public void onCheckIfUserIsLoggedIn() {
        if(mSharedPreferenceHelper.getCurrentSpotifyToken().length() != 0) {
            ((LoginScreenActions) mContext).showMainScreen();
        }
    }
}
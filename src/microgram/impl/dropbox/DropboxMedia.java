package microgram.impl.dropbox;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import microgram.api.java.Media;
import microgram.api.java.Result;
import org.pac4j.scribe.builder.api.DropboxApi20;
import utils.Hash;
import utils.JSON;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class DropboxMedia implements Media {
    private static final String apiKey = "d9oz4yov19fb7id";
    private static final String apiSecret = "ex37bz4mbfemj86";
    private static final String accessTokenStr = "PImj5hw9xNAAAAAAAAAEELeYa0uTOhRZFQkkQDWUlryTZu7Dw-D-3Fr_nqBMC1m0";


    protected static final String JSON_CONTENT_TYPE = "application/json; charset=utf-8";
    protected static final String OCTETSTREAM_CONTENT_TYPE = "application/octet-stream";

    private static final String CREATE_FILE_V2_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DELETE_FILE_V2_URL = "https://api.dropboxapi.com/2/files/delete";
    private static final String DOWNLOAD_FILE_V2_URL = "https://content.dropboxapi.com/2/files/download";

    private static final String DROPBOX_API_ARG = "Dropbox-API-Arg";

    protected OAuth20Service service;
    protected OAuth2AccessToken accessToken;


    public DropboxMedia() {
    }

    public DropboxMedia(OAuth20Service service, OAuth2AccessToken accessToken) {
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
        accessToken = new OAuth2AccessToken(accessTokenStr);
    }


    public static DropboxMedia createClientWithAccessToken() throws Exception {
        try {
            OAuth20Service service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
            OAuth2AccessToken accessToken = new OAuth2AccessToken(accessTokenStr);

            System.err.println(accessToken.getAccessToken());
            System.err.println(accessToken.toString());
            return new DropboxMedia(service, accessToken);

        } catch (Exception x) {
            x.printStackTrace();
            throw new Exception(x);
        }
    }


    @Override
    public Result<String> upload(byte[] bytes) throws InterruptedException, ExecutionException, IOException {

        String id = Hash.digest(bytes).toString();
        OAuthRequest upload = new OAuthRequest(Verb.POST, CREATE_FILE_V2_URL);
        upload.addHeader("Content-Type", OCTETSTREAM_CONTENT_TYPE);
        upload.addHeader(DROPBOX_API_ARG, JSON.encode(new UploadArgs("/media/" + id)));
        upload.setPayload(bytes);


        try {
            service.signRequest(accessToken, upload);
        } catch (Exception e){
            e.printStackTrace();
        }
        Response r = service.execute(upload);


        if (r.getCode() == 404) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else if (r.getCode() == 200) {
            return Result.ok(id);
        } else {
            return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
        }
    }

    @Override
    public Result<byte[]> download(String id) throws InterruptedException, ExecutionException, IOException {
        OAuthRequest getFile = new OAuthRequest(Verb.GET, DOWNLOAD_FILE_V2_URL);
        getFile.addHeader("Content-Type", JSON_CONTENT_TYPE);
        getFile.addHeader(DROPBOX_API_ARG, JSON.encode(new DownloadArgs("/media/" + id)));

        service.signRequest(new OAuth2AccessToken(accessTokenStr), getFile);
        Response r = service.execute(getFile);

        if (r.getCode() == 404) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else if (r.getCode() == 200) {
            return Result.ok(JSON.decode(r.getBody()));
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }

    }

    @Override
    public Result<Void> delete(String id) throws InterruptedException, ExecutionException, IOException {
        OAuthRequest delFile = new OAuthRequest(Verb.DELETE, DELETE_FILE_V2_URL);
        delFile.addHeader("Content-Type", JSON_CONTENT_TYPE);
        delFile.addHeader(DROPBOX_API_ARG, JSON.encode(new DownloadArgs("/media/" + id)));

        service.signRequest(new OAuth2AccessToken(accessTokenStr), delFile);
        Response r = service.execute(delFile);

        if (r.getCode() == 404) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else if (r.getCode() == 200) {
            return Result.ok();
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }
}

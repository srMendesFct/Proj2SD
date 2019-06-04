package microgram.impl.dropbox;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import microgram.api.java.Media;
import microgram.api.java.Result;
import org.pac4j.scribe.builder.api.DropboxApi20;
import utils.Hash;
import utils.JSON;

import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

public class DropboxMedia implements Media {
    private static final String apiKey = "d9oz4yov19fb7id";
    private static final String apiSecret = "ex37bz4mbfemj86";
    private static final String accessTokenStr = "PImj5hw9xNAAAAAAAAAEELeYa0uTOhRZFQkkQDWUlryTZu7Dw-D-3Fr_nqBMC1m0";

   static OAuth20Service service;


    public DropboxMedia() {
        service = new ServiceBuilder(apiKey).
                apiSecret(apiSecret).
                build(DropboxApi20.INSTANCE);

    }

    @Override
    public Result<String> upload(byte[] bytes) throws InterruptedException, ExecutionException, IOException {
        String id = Hash.digest(bytes).toString();              //ou /upload
        OAuthRequest upload = new OAuthRequest(Verb.POST, "https://content.dropboxapi.com/2/files/upload");
        upload.addHeader("path", "/" + id);
        upload.addHeader("Content-Type", MediaType.APPLICATION_OCTET_STREAM);

        upload.setPayload(bytes);
        service.signRequest( new OAuth2AccessToken(accessTokenStr), upload);
        Response r = service.execute(upload);


        if (r.getCode() == 404) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        } else if (r.getCode() == 200) {
            return Result.ok(id);
        } else {
            return Result.error(Result.ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<byte[]> download(String id) throws InterruptedException, ExecutionException, IOException {
        OAuthRequest getFile = new OAuthRequest(Verb.GET, "https://api.dropboxapi.com/2/" + id + "/get");
        getFile.addHeader("path", "/" + id);
        service.signRequest( new OAuth2AccessToken(accessTokenStr), getFile);
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
        OAuthRequest delFile = new OAuthRequest(Verb.DELETE, "https://api.dropboxapi.com/2/files/delete_v2");
        delFile.addHeader("path", "/" + id);

        service.signRequest( new OAuth2AccessToken(accessTokenStr), delFile);
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

package microgram.impl.rest.media;
import microgram.api.rest.RestMedia;
import microgram.impl.dropbox.DropboxMedia;
import microgram.impl.rest.RestResource;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class RestMediaResources extends RestResource implements RestMedia {

	final DropboxMedia impl;
	final String baseUri;

	public RestMediaResources(String baseUri) throws Exception {
		this.baseUri = baseUri;
		this.impl = DropboxMedia.createClientWithAccessToken();
	}

	@Override
	public String upload(byte[] bytes) throws InterruptedException, ExecutionException, IOException {
		return baseUri + "/" + super.resultOrThrow(impl.upload(bytes));
	}

	@Override
	public byte[] download(String id) throws InterruptedException, ExecutionException, IOException {
		return super.resultOrThrow(impl.download(id));
	}

	@Override
	public void delete(String id) throws InterruptedException, ExecutionException, IOException {
		super.resultOrThrow(impl.delete(id));
	}
}

package microgram.impl.dropbox;

import java.util.HashMap;
import java.util.List;

public class DownloadReturnArgs {

    private DownloadEntry entries;

    public static class DownloadEntry extends HashMap<String, Object> {
        private static final long serialVersionUID = 1L;
        public DownloadEntry() {}

        @Override
        public String toString() {
            return super.get("size").toString();
        }
    }

    public DownloadReturnArgs(){ }

    public DownloadEntry getSize() {
        return entries;
    }

}

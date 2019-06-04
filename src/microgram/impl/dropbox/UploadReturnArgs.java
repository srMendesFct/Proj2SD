package microgram.impl.dropbox;

import java.util.HashMap;
import java.util.List;

public class UploadReturnArgs {

    private UploadEntry entries;

    public static class UploadEntry extends HashMap<String, Object> {

        private static final long serialVersionUID = 1L;
        public UploadEntry() {}

        @Override
        public String toString() {
            return super.get("path_display").toString();
        }
    }

    public UploadReturnArgs(){ }

    public UploadEntry getEntries() {
        return entries;
    }


}

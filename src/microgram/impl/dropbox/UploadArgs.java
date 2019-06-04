package microgram.impl.dropbox;

public class UploadArgs {
    final String path;
    final String mode;
    final boolean autorename, mute, strict_conflict;

    public UploadArgs(String path) {
        this.path = path;
        this.mode = "add";
        this.autorename = false;
        this.mute = false;
        this.strict_conflict = false;
    }

}
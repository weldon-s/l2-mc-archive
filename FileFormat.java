import java.io.File;

public final class FileFormat {
    private final String directory;
    private final String extension;

    public FileFormat(String dir, String ext) {
        directory = dir;
        extension = ext;

        new File(directory).mkdirs();
    }

    public File getFile(String name) {
        return new File(directory + "/" + name + "." + extension);
    }

    public FileFormat getFileFormatWithSubFolder(String folderName, String ext) {
        return new FileFormat(directory + "/" + folderName, ext);
    }

    public FileFormat getFileFormatWithSubFolder(String folderName) {
        return getFileFormatWithSubFolder(folderName, extension);
    }
}